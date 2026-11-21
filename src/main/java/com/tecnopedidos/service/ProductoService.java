package com.tecnopedidos.service;

import com.tecnopedidos.domain.Categoria;
import com.tecnopedidos.domain.Producto;
import com.tecnopedidos.repository.ProductoRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Reglas de negocio del catalogo (HU-04, HU-10, HU-15, HU-23)
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final EmpresaActualService empresaActual;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaService categoriaService,
                           EmpresaActualService empresaActual) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.empresaActual = empresaActual;
    }

    // Estados validos del filtro del inventario
    private static final Set<String> ESTADOS = Set.of("DISPONIBLE", "BAJO_STOCK", "INACTIVO");

    // Busqueda por codigo/nombre, categoria y estado (HU-15)
    @Transactional(readOnly = true)
    public List<Producto> buscar(String texto, Long idCategoria, String estado) {

        String filtro = (texto == null || texto.isBlank()) ? null : texto.trim();
        String estadoValido = (estado != null && ESTADOS.contains(estado)) ? estado : null;

        return productoRepository.buscar(
                empresaActual.getIdEmpresa(), filtro, idCategoria, estadoValido);
    }

    // Indicador del menu lateral y de la pantalla de inicio
    @Transactional(readOnly = true)
    public long contarBajoStock() {
        return productoRepository.contarBajoStock(empresaActual.getIdEmpresa());
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository
                .findByEmpresa_IdEmpresaAndActivoTrueOrderByNombreAsc(empresaActual.getIdEmpresa());
    }

    // Reporte de bajo stock (HU-10)
    @Transactional(readOnly = true)
    public List<Producto> buscarBajoStock() {
        return productoRepository.buscarBajoStock(empresaActual.getIdEmpresa());
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository
                .findByIdProductoAndEmpresa_IdEmpresa(id, empresaActual.getIdEmpresa())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto no existe"));
    }

    @Transactional
    public void actualizarImagenPath(Long id, String imagenPath) {
        Producto producto = buscarPorId(id);
        producto.setImagenPath(imagenPath);
        productoRepository.save(producto);
    }

    // Registro (HU-04) y actualizacion (HU-23)
    @Transactional
    public Producto guardar(Producto datos) {

        if (datos == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }

        if (datos.getCodigo() == null || datos.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El codigo del producto es obligatorio");
        }

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (datos.getPrecio() == null || datos.getPrecio().signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }

        if (datos.getStockMinimo() == null || datos.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        }

        if (datos.getCategoria() == null
                || datos.getCategoria().getIdCategoria() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoria");
        }

        String codigo = datos.getCodigo().trim().toUpperCase();

        // Codigo unico dentro de la empresa (HU-04 esc. 2)
        var existente = productoRepository.findByEmpresa_IdEmpresaAndCodigoIgnoreCase(
                empresaActual.getIdEmpresa(), codigo);

        if (existente.isPresent()
                && !existente.get().getIdProducto().equals(datos.getIdProducto())) {
            throw new IllegalArgumentException("Ya existe un producto con ese codigo");
        }

        // La categoria debe pertenecer a la empresa (HU-04 esc. 2)
        Categoria categoria = categoriaService.buscarPorId(
                datos.getCategoria().getIdCategoria());

        Producto producto;

        if (datos.getIdProducto() == null) {
            // Nuevo: stock inicial en cero (HU-04 esc. 3)
            producto = new Producto();
            producto.setEmpresa(empresaActual.getEmpresa());
            producto.setStock(0);
            producto.setActivo(true);
        } else {
            // Edicion: el stock NO se modifica aqui (HU-23 esc. 2)
            producto = buscarPorId(datos.getIdProducto());
        }

        // Solo se puede asignar una categoria activa
        boolean cambiaCategoria = producto.getCategoria() == null
                || !producto.getCategoria().getIdCategoria().equals(categoria.getIdCategoria());

        if (cambiaCategoria && !categoria.isActivo()) {
            throw new IllegalArgumentException("La categoria seleccionada esta inactiva");
        }

        producto.setCodigo(codigo);
        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStockMinimo(datos.getStockMinimo());
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    // Los productos no se eliminan: se desactivan y conservan su historial (HU-23 esc. 4)
    @Transactional
    public void desactivar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional
    public void activar(Long id) {
        Producto producto = buscarPorId(id);

        if (!producto.getCategoria().isActivo()) {
            throw new IllegalStateException(
                    "Active primero la categoria " + producto.getCategoria().getNombre());
        }

        producto.setActivo(true);
        productoRepository.save(producto);
    }
}
