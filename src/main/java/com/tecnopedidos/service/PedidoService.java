package com.tecnopedidos.service;

import com.tecnopedidos.domain.CarritoPedido;
import com.tecnopedidos.domain.Cliente;
import com.tecnopedidos.domain.DetallePedido;
import com.tecnopedidos.domain.EstadoPedido;
import com.tecnopedidos.domain.Pedido;
import com.tecnopedidos.domain.Producto;
import com.tecnopedidos.domain.SeguimientoPedido;
import com.tecnopedidos.domain.TipoMovimiento;
import com.tecnopedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Registro, consulta y cancelacion de pedidos (HU-06, HU-07, HU-24)
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final MovimientoInventarioService movimientoService;
    private final EmpresaActualService empresaActual;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoService productoService,
                         ClienteService clienteService,
                         MovimientoInventarioService movimientoService,
                         EmpresaActualService empresaActual) {
        this.pedidoRepository = pedidoRepository;
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.movimientoService = movimientoService;
        this.empresaActual = empresaActual;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Pedido> buscar(EstadoPedido estado) {
        return pedidoRepository.buscar(empresaActual.getIdEmpresa(), estado);
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findByIdPedidoAndEmpresa_IdEmpresa(id, empresaActual.getIdEmpresa())
                .orElseThrow(() -> new IllegalArgumentException("El pedido no existe"));
    }

    @Transactional(readOnly = true)
    public List<Pedido> recientes() {
        return pedidoRepository
                .findTop5ByEmpresa_IdEmpresaOrderByFechaCreacionDescIdPedidoDesc(empresaActual.getIdEmpresa());
    }

    // Pendientes de entrega: todos los abiertos (HU-18 esc. 2)
    @Transactional(readOnly = true)
    public long contarPendientesEntrega() {
        return pedidoRepository.countByEmpresa_IdEmpresaAndEstadoNotIn(
                empresaActual.getIdEmpresa(), List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO));
    }

    @Transactional(readOnly = true)
    public long contarListos() {
        return pedidoRepository.countByEmpresa_IdEmpresaAndEstado(
                empresaActual.getIdEmpresa(), EstadoPedido.LISTO);
    }

    // ------------------------------------------------------------------
    // Carrito del pedido en preparacion (HU-07)
    // ------------------------------------------------------------------

    // Agrega un producto al carrito; si ya estaba, consolida la cantidad (HU-07 esc. 1)
    @Transactional(readOnly = true)
    public void agregarLinea(CarritoPedido carrito, Long idProducto, Integer cantidad) {

        if (idProducto == null) {
            throw new IllegalArgumentException("Seleccione un producto");
        }

        validarCantidad(cantidad);

        Producto producto = productoService.buscarPorId(idProducto);

        if (!producto.isActivo()) {
            throw new IllegalArgumentException("El producto " + producto.getNombre() + " esta inactivo");
        }

        carrito.agregar(idProducto, cantidad);
    }

    // Cambia la cantidad de una linea antes de confirmar (HU-07 esc. 3)
    public void actualizarLinea(CarritoPedido carrito, Long idProducto, Integer cantidad) {
        validarCantidad(cantidad);
        carrito.actualizar(idProducto, cantidad);
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un numero entero mayor que cero");
        }
    }

    // Lineas con el precio vigente del catalogo, calculadas en el servidor (HU-07 esc. 2).
    // Son objetos de vista: no se guardan.
    @Transactional(readOnly = true)
    public List<DetallePedido> calcularLineas(CarritoPedido carrito) {

        List<DetallePedido> lineas = new ArrayList<>();

        for (Map.Entry<Long, Integer> entrada : carrito.getLineas().entrySet()) {
            Producto producto = productoService.buscarPorId(entrada.getKey());
            lineas.add(crearDetalle(producto, entrada.getValue()));
        }

        return lineas;
    }

    public BigDecimal calcularTotal(List<DetallePedido> lineas) {
        return lineas.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private DetallePedido crearDetalle(Producto producto, int cantidad) {
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setSubtotal(producto.getPrecio()
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP));
        return detalle;
    }

    // ------------------------------------------------------------------
    // Registro transaccional (HU-06)
    // ------------------------------------------------------------------

    // En una sola transaccion: crea el pedido y sus detalles, descuenta el stock,
    // registra las salidas de inventario y crea el historial PENDIENTE (HU-06 esc. 2).
    // Si algo falla, @Transactional revierte todo (HU-06 esc. 3).
    @Transactional
    public Pedido registrar(CarritoPedido carrito) {

        if (carrito.getIdCliente() == null) {
            throw new IllegalArgumentException("Seleccione el cliente del pedido");
        }

        if (carrito.isVacio()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        Cliente cliente = clienteService.buscarPorId(carrito.getIdCliente());

        if (!cliente.isActivo()) {
            throw new IllegalArgumentException("El cliente seleccionado esta inactivo");
        }

        Pedido pedido = new Pedido();
        pedido.setEmpresa(empresaActual.getEmpresa());
        pedido.setCliente(cliente);
        pedido.setVendedor(empresaActual.getUsuario());
        pedido.setRequiereAsistencia(carrito.isRequiereAsistencia());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFechaCreacion(LocalDateTime.now());

        // Detalles con el precio vigente; se valida el stock antes de escribir
        for (Map.Entry<Long, Integer> entrada : carrito.getLineas().entrySet()) {

            Producto producto = productoService.buscarPorId(entrada.getKey());
            int cantidad = entrada.getValue();

            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " esta inactivo");
            }

            if (producto.getStock() < cantidad) {
                throw new IllegalStateException("Existencias insuficientes de " + producto.getNombre()
                        + ": disponible " + producto.getStock() + ", solicitado " + cantidad);
            }

            pedido.agregarDetalle(crearDetalle(producto, cantidad));
        }

        pedido.setTotal(calcularTotal(pedido.getDetalles()));
        pedidoRepository.save(pedido);

        // Salidas de inventario ligadas al pedido (HU-05 esc. 4)
        for (DetallePedido detalle : pedido.getDetalles()) {
            movimientoService.aplicar(detalle.getProducto(), TipoMovimiento.SALIDA_PEDIDO,
                    detalle.getCantidad(), "Pedido " + pedido.getCodigo(), pedido);
        }

        registrarSeguimiento(pedido, null, EstadoPedido.PENDIENTE, "Pedido registrado", null);

        return pedidoRepository.save(pedido);
    }

    // ------------------------------------------------------------------
    // Cancelacion (HU-24)
    // ------------------------------------------------------------------

    // Cambia a CANCELADO y devuelve las existencias en una sola transaccion (HU-24 esc. 2)
    @Transactional
    public void cancelar(Long idPedido, String motivo) {

        Pedido pedido = buscarPorId(idPedido);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            // No se devuelve stock dos veces (HU-24 esc. 4)
            throw new IllegalStateException("El pedido ya fue cancelado");
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("Un pedido entregado no se puede cancelar");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo de la cancelacion");
        }

        EstadoPedido anterior = pedido.getEstado();

        for (DetallePedido detalle : pedido.getDetalles()) {
            movimientoService.aplicar(detalle.getProducto(), TipoMovimiento.DEVOLUCION_CANCELACION,
                    detalle.getCantidad(), "Cancelacion " + pedido.getCodigo(), pedido);
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        registrarSeguimiento(pedido, anterior, EstadoPedido.CANCELADO,
                "Pedido cancelado", "Motivo: " + motivo.trim());

        pedidoRepository.save(pedido);
    }

    // ------------------------------------------------------------------
    // Historial
    // ------------------------------------------------------------------

    // Agrega un registro al historial del pedido con el usuario y la fecha.
    // Tambien lo usa SeguimientoService para asignaciones y avances.
    void registrarSeguimiento(Pedido pedido, EstadoPedido anterior, EstadoPedido nuevo,
                              String comentarioPublico, String observacionInterna) {

        SeguimientoPedido seguimiento = new SeguimientoPedido();
        seguimiento.setUsuario(empresaActual.getUsuario());
        seguimiento.setEstadoAnterior(anterior);
        seguimiento.setEstadoNuevo(nuevo);
        seguimiento.setComentarioPublico(comentarioPublico);
        seguimiento.setObservacionInterna(
                (observacionInterna == null || observacionInterna.isBlank()) ? null : observacionInterna.trim());
        seguimiento.setFecha(LocalDateTime.now());

        pedido.agregarSeguimiento(seguimiento);
    }
}
