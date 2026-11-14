package com.tecnopedidos.service;

import com.tecnopedidos.domain.EstadoPedido;
import com.tecnopedidos.domain.Pedido;
import com.tecnopedidos.domain.Usuario;
import com.tecnopedidos.repository.PedidoRepository;
import com.tecnopedidos.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Atencion tecnica y cierre de pedidos (HU-08, HU-09, HU-25, HU-26)
@Service
public class SeguimientoService {

    private static final List<EstadoPedido> ESTADOS_BANDEJA =
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_PROCESO, EstadoPedido.LISTO);

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoService pedidoService;
    private final EmpresaActualService empresaActual;

    public SeguimientoService(PedidoRepository pedidoRepository,
                              UsuarioRepository usuarioRepository,
                              PedidoService pedidoService,
                              EmpresaActualService empresaActual) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoService = pedidoService;
        this.empresaActual = empresaActual;
    }

    // ------------------------------------------------------------------
    // Asignacion de tecnico (HU-08) - administrador
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Usuario> tecnicosDisponibles() {
        return usuarioRepository.findByEmpresa_IdEmpresaAndRol_NombreAndActivoTrueOrderByNombreAsc(
                empresaActual.getIdEmpresa(), "TECNICO");
    }

    @Transactional
    public void asignarTecnico(Long idPedido, Long idTecnico, String motivo) {

        Pedido pedido = pedidoService.buscarPorId(idPedido);

        // Solo pedidos con asistencia y no listos, entregados o cancelados (HU-08 esc. 4)
        if (!pedido.isAsignable()) {
            throw new IllegalStateException("Este pedido no admite asignacion de tecnico");
        }

        if (idTecnico == null) {
            throw new IllegalArgumentException("Seleccione un tecnico");
        }

        // Solo tecnicos activos de la misma empresa (HU-08 esc. 1)
        Usuario tecnico = usuarioRepository
                .findByIdUsuarioAndEmpresa_IdEmpresa(idTecnico, empresaActual.getIdEmpresa())
                .filter(u -> u.isActivo() && "TECNICO".equals(u.getRol().getNombre()))
                .orElseThrow(() -> new IllegalArgumentException("El tecnico seleccionado no es valido"));

        EstadoPedido estado = pedido.getEstado();

        if (pedido.getTecnico() == null) {
            // Primera asignacion: PENDIENTE -> ASIGNADO (HU-08 esc. 2)
            pedido.setTecnico(tecnico);
            pedido.setEstado(EstadoPedido.ASIGNADO);
            pedidoService.registrarSeguimiento(pedido, estado, EstadoPedido.ASIGNADO,
                    "Se asignó un técnico a su pedido", "Asignado a " + tecnico.getNombre());

        } else {
            // Reasignacion: conserva el estado y exige motivo (HU-08 esc. 3)
            Usuario anterior = pedido.getTecnico();

            if (anterior.getIdUsuario().equals(tecnico.getIdUsuario())) {
                throw new IllegalArgumentException("El pedido ya esta asignado a ese tecnico");
            }

            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException("Indique el motivo de la reasignacion");
            }

            pedido.setTecnico(tecnico);
            pedidoService.registrarSeguimiento(pedido, estado, estado, null,
                    "Reasignado de " + anterior.getNombre() + " a " + tecnico.getNombre()
                    + ". Motivo: " + motivo.trim());
        }

        pedidoRepository.save(pedido);
    }

    // ------------------------------------------------------------------
    // Bandeja y avances del tecnico (HU-25, HU-09)
    // ------------------------------------------------------------------

    // Solo los pedidos asignados actualmente al tecnico autenticado (HU-25 esc. 1 y 3)
    @Transactional(readOnly = true)
    public List<Pedido> bandeja(EstadoPedido estado) {
        List<EstadoPedido> estados = (estado != null && ESTADOS_BANDEJA.contains(estado))
                ? List.of(estado) : ESTADOS_BANDEJA;

        return pedidoRepository.buscarAsignados(
                empresaActual.getIdEmpresa(), empresaActual.getIdUsuario(), estados);
    }

    // El tecnico solo abre pedidos que tiene asignados en este momento (HU-09 esc. 1)
    @Transactional(readOnly = true)
    public Pedido buscarAsignado(Long idPedido) {

        Pedido pedido = pedidoService.buscarPorId(idPedido);

        if (pedido.getTecnico() == null
                || !pedido.getTecnico().getIdUsuario().equals(empresaActual.getIdUsuario())) {
            throw new IllegalStateException("No tiene asignado este pedido");
        }

        return pedido;
    }

    // Avance tecnico: ASIGNADO -> EN_PROCESO -> LISTO, con comentario publico (HU-09 esc. 2 y 3)
    @Transactional
    public void registrarAvance(Long idPedido, EstadoPedido nuevoEstado,
                                String comentarioPublico, String observacionInterna) {

        Pedido pedido = buscarAsignado(idPedido);

        EstadoPedido permitido = pedido.getSiguienteEstadoTecnico();

        if (nuevoEstado == null || nuevoEstado != permitido) {
            throw new IllegalStateException("Cambio de estado no permitido para este pedido");
        }

        if (comentarioPublico == null || comentarioPublico.isBlank()) {
            throw new IllegalArgumentException("El comentario publico es obligatorio");
        }

        EstadoPedido anterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);

        // Comentario publico y observaciones internas se guardan por separado (HU-09 esc. 4)
        pedidoService.registrarSeguimiento(pedido, anterior, nuevoEstado,
                comentarioPublico.trim(), observacionInterna);

        pedidoRepository.save(pedido);
    }

    // ------------------------------------------------------------------
    // Preparacion y entrega (HU-26) - vendedor o administrador
    // ------------------------------------------------------------------

    // Pedido sin asistencia: PENDIENTE -> LISTO (HU-26 esc. 1 y 2)
    @Transactional
    public void marcarListo(Long idPedido) {

        Pedido pedido = pedidoService.buscarPorId(idPedido);

        if (pedido.isRequiereAsistencia()) {
            throw new IllegalStateException(
                    "Un pedido con asistencia técnica solo pasa a LISTO mediante el avance del técnico");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo un pedido PENDIENTE se puede marcar como listo");
        }

        pedido.setEstado(EstadoPedido.LISTO);
        pedidoService.registrarSeguimiento(pedido, EstadoPedido.PENDIENTE, EstadoPedido.LISTO,
                "Su pedido está listo para entregar", null);

        pedidoRepository.save(pedido);
    }

    // Entrega: solo desde LISTO y con comentario publico.
    // No vuelve a descontar stock: ya se desconto al registrar el pedido (HU-26 esc. 3 y 4)
    @Transactional
    public void entregar(Long idPedido, String comentarioPublico) {

        Pedido pedido = pedidoService.buscarPorId(idPedido);

        if (pedido.getEstado() != EstadoPedido.LISTO) {
            throw new IllegalStateException("Solo un pedido LISTO se puede entregar");
        }

        if (comentarioPublico == null || comentarioPublico.isBlank()) {
            throw new IllegalArgumentException("El comentario de entrega es obligatorio");
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedidoService.registrarSeguimiento(pedido, EstadoPedido.LISTO, EstadoPedido.ENTREGADO,
                comentarioPublico.trim(), null);

        pedidoRepository.save(pedido);
    }
}
