package com.tecnopedidos.service;

import com.tecnopedidos.domain.MovimientoInventario;
import com.tecnopedidos.domain.Pedido;
import com.tecnopedidos.domain.Producto;
import com.tecnopedidos.domain.TipoMovimiento;
import com.tecnopedidos.repository.MovimientoInventarioRepository;
import com.tecnopedidos.repository.ProductoRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Unica via para modificar existencias (HU-05)
@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final EmpresaActualService empresaActual;

    public MovimientoInventarioService(MovimientoInventarioRepository movimientoRepository,
                                       ProductoRepository productoRepository,
                                       ProductoService productoService,
                                       EmpresaActualService empresaActual) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.productoService = productoService;
        this.empresaActual = empresaActual;
    }

    // Historial, opcionalmente filtrado por producto
    @Transactional(readOnly = true)
    public List<MovimientoInventario> buscar(Long idProducto) {
        return movimientoRepository.buscar(empresaActual.getIdEmpresa(), idProducto);
    }

    // Tipos que el administrador puede registrar desde el formulario
    public List<TipoMovimiento> tiposManuales() {
        return Arrays.stream(TipoMovimiento.values())
                .filter(TipoMovimiento::isManual)
                .toList();
    }

    // Entrada o ajuste manual desde el formulario (HU-05 esc. 1, 2 y 4)
    @Transactional
    public MovimientoInventario registrarManual(Long idProducto, TipoMovimiento tipo,
                                                Integer cantidad, String motivo) {

        if (idProducto == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de movimiento");
        }

        // SALIDA_PEDIDO y DEVOLUCION_CANCELACION solo los generan los pedidos
        if (!tipo.isManual()) {
            throw new IllegalArgumentException(
                    "Ese tipo de movimiento solo lo generan las operaciones de pedidos");
        }

        Producto producto = productoService.buscarPorId(idProducto);

        return aplicar(producto, tipo, cantidad, motivo, null);
    }

    // Aplica un movimiento y actualiza el stock en la misma transaccion.
    // La rama feature/pedidos usa este metodo con SALIDA_PEDIDO y
    // DEVOLUCION_CANCELACION (HU-06 y HU-24).
    // Si cualquier escritura falla, @Transactional revierte ambos cambios (HU-05 esc. 3).
    @Transactional
    public MovimientoInventario aplicar(Producto producto, TipoMovimiento tipo,
                                        Integer cantidad, String motivo, Pedido pedido) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser un numero entero mayor que cero");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }

        int stockAnterior = producto.getStock();
        int stockResultante = stockAnterior + (tipo.getSigno() * cantidad);

        // Un ajuste o salida nunca puede dejar stock negativo (HU-05 esc. 2)
        if (stockResultante < 0) {
            throw new IllegalStateException(
                    "Existencias insuficientes de " + producto.getNombre()
                    + ": disponible " + stockAnterior + ", solicitado " + cantidad);
        }

        producto.setStock(stockResultante);
        productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setEmpresa(producto.getEmpresa());
        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockResultante(stockResultante);
        movimiento.setMotivo(motivo.trim());
        movimiento.setPedido(pedido);
        movimiento.setFecha(LocalDateTime.now());
        // Usuario que registra el movimiento (trazabilidad, HU-05 esc. 1)
        movimiento.setUsuario(empresaActual.getUsuario());

        return movimientoRepository.save(movimiento);
    }
}
