package com.tecnopedidos.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Tabla transaccional principal (HU-06, HU-07, HU-24)
// Servicio, controlador y vistas: rama feature/pedidos
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Usuario que registro el pedido.
    // Queda opcional hasta integrar el inicio de sesion (rama base-seguridad)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    // Tecnico responsable actual (HU-08); nulo si no requiere asistencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico")
    private Usuario tecnico;

    @Column(name = "requiere_asistencia", nullable = false)
    private boolean requiereAsistencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    // Calculado en el servidor a partir de los detalles (HU-07 esc. 2)
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Relacion uno a muchos: un pedido tiene varias lineas de detalle
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    // Historial de estados del pedido (HU-09)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("fecha ASC")
    private List<SeguimientoPedido> seguimientos = new ArrayList<>();

    public Pedido() {
    }

    // Referencia legible para pantallas y correos: PED-0001
    public String getCodigo() {
        return idPedido == null ? "" : String.format("PED-%04d", idPedido);
    }

    // Un pedido esta abierto mientras no se entregue ni se cancele
    public boolean isAbierto() {
        return estado != EstadoPedido.ENTREGADO && estado != EstadoPedido.CANCELADO;
    }

    // Se puede asignar o reasignar tecnico (HU-08 esc. 4)
    public boolean isAsignable() {
        return requiereAsistencia
                && (estado == EstadoPedido.PENDIENTE
                    || estado == EstadoPedido.ASIGNADO
                    || estado == EstadoPedido.EN_PROCESO);
    }

    // Siguiente estado que puede registrar el tecnico (HU-09 esc. 2)
    public EstadoPedido getSiguienteEstadoTecnico() {
        return switch (estado) {
            case ASIGNADO -> EstadoPedido.EN_PROCESO;
            case EN_PROCESO -> EstadoPedido.LISTO;
            default -> null;
        };
    }

    // Mantiene sincronizados ambos lados de la relacion
    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }

    public void agregarSeguimiento(SeguimientoPedido seguimiento) {
        seguimiento.setPedido(this);
        seguimientos.add(seguimiento);
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    public void setTecnico(Usuario tecnico) {
        this.tecnico = tecnico;
    }

    public boolean isRequiereAsistencia() {
        return requiereAsistencia;
    }

    public void setRequiereAsistencia(boolean requiereAsistencia) {
        this.requiereAsistencia = requiereAsistencia;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    public List<SeguimientoPedido> getSeguimientos() {
        return seguimientos;
    }

    public void setSeguimientos(List<SeguimientoPedido> seguimientos) {
        this.seguimientos = seguimientos;
    }
}
