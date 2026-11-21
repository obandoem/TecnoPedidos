package com.tecnopedidos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// Producto del catalogo e inventario (HU-04, HU-10, HU-23)
@Entity
@Table(name = "producto",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_producto_empresa_codigo",
                columnNames = {"id_empresa", "codigo"}))
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    // La empresa la asigna el servicio, nunca el formulario (HU-02)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // Codigo unico dentro de la empresa (HU-04 esc. 2)
    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 30)
    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // El stock NO se edita desde el formulario: inicia en 0 y cambia
    // solo mediante movimientos de inventario (HU-04 esc. 3, HU-23 esc. 2)
    @Column(name = "stock", nullable = false)
    private int stock = 0;

    @NotNull(message = "El stock minimo es obligatorio")
    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    // Relacion muchos a uno: varios productos pertenecen a una categoria
    @NotNull(message = "La categoria es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    // Los productos se desactivan, no se eliminan (HU-23 esc. 4)
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Size(max = 500)
    @Column(name = "imagen_path", length = 500)
    private String imagenPath;

    // Bloqueo optimista: si dos usuarios modifican el mismo producto a la vez,
    // el segundo cambio se rechaza en lugar de sobrescribir el stock (HU-06 esc. 4)
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Producto() {
    }

    // Reglas de bajo stock (HU-10)
    public boolean isStockBajo() {
        return stockMinimo != null && stock <= stockMinimo;
    }

    public int getFaltante() {
        if (stockMinimo == null) {
            return 0;
        }
        return Math.max(0, stockMinimo - stock);
    }

    // Prioridad del reporte de bajo stock (P08):
    // CRITICO si esta agotado o tiene la mitad del minimo o menos;
    // ADVERTENCIA si esta en el minimo o por debajo, pero sobre la mitad.
    public String getPrioridad() {
        if (!isStockBajo()) {
            return null;
        }
        if (stock == 0 || stock * 2 <= stockMinimo) {
            return "CRITICO";
        }
        return "ADVERTENCIA";
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getImagenPath() {
        return imagenPath;
    }

    public void setImagenPath(String imagenPath) {
        this.imagenPath = imagenPath;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
