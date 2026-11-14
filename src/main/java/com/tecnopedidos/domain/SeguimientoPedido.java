package com.tecnopedidos.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Historial de estados y avances de un pedido (HU-08, HU-09, HU-26)
// Servicio, controlador y vistas: rama feature/seguimiento
@Entity
@Table(name = "seguimiento_pedido")
public class SeguimientoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Long idSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    // Usuario que realizo el cambio.
    // Queda opcional hasta integrar el inicio de sesion (rama base-seguridad)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Nulo en el primer registro (creacion del pedido)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoPedido estadoNuevo;

    // Visible para el cliente en el seguimiento publico (HU-09 esc. 4)
    @Column(name = "comentario_publico", length = 500)
    private String comentarioPublico;

    // Solo visible para usuarios internos
    @Column(name = "observacion_interna", length = 500)
    private String observacionInterna;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public SeguimientoPedido() {
    }

    public Long getIdSeguimiento() {
        return idSeguimiento;
    }

    public void setIdSeguimiento(Long idSeguimiento) {
        this.idSeguimiento = idSeguimiento;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EstadoPedido getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoPedido estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoPedido getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoPedido estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getComentarioPublico() {
        return comentarioPublico;
    }

    public void setComentarioPublico(String comentarioPublico) {
        this.comentarioPublico = comentarioPublico;
    }

    public String getObservacionInterna() {
        return observacionInterna;
    }

    public void setObservacionInterna(String observacionInterna) {
        this.observacionInterna = observacionInterna;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
