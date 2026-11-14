package com.tecnopedidos.service;

import com.tecnopedidos.domain.Categoria;
import com.tecnopedidos.repository.CategoriaRepository;
import com.tecnopedidos.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Reglas de negocio de categorias (HU-21)
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final EmpresaActualService empresaActual;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            ProductoRepository productoRepository,
                            EmpresaActualService empresaActual) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.empresaActual = empresaActual;
    }

    // Consultas de categorias de la empresa actual
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository
                .findByEmpresa_IdEmpresaOrderByNombreAsc(empresaActual.getIdEmpresa());
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository
                .findByEmpresa_IdEmpresaAndActivoTrueOrderByNombreAsc(empresaActual.getIdEmpresa());
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository
                .findByIdCategoriaAndEmpresa_IdEmpresa(id, empresaActual.getIdEmpresa())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La categoria no existe"));
    }

    // Registro y actualizacion
    @Transactional
    public Categoria guardar(Categoria datos) {

        String nombre = datos.getNombre();

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoria es obligatorio");
        }

        nombre = nombre.trim();

        // Nombre unico dentro de la empresa (HU-21 esc. 1)
        var existente = categoriaRepository.findByEmpresa_IdEmpresaAndNombreIgnoreCase(
                empresaActual.getIdEmpresa(), nombre);

        if (existente.isPresent()
                && !existente.get().getIdCategoria().equals(datos.getIdCategoria())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoria con ese nombre");
        }

        Categoria categoria;

        if (datos.getIdCategoria() == null) {
            // Nueva: la empresa se toma de la sesion, no del formulario
            categoria = new Categoria();
            categoria.setEmpresa(empresaActual.getEmpresa());
            categoria.setActivo(true);
        } else {
            // Edicion: solo se copian los campos editables
            categoria = buscarPorId(datos.getIdCategoria());
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());

        return categoriaRepository.save(categoria);
    }

    // Las categorias no se eliminan: se desactivan (HU-21 esc. 3 y 4)
    @Transactional
    public void desactivar(Long id) {

        Categoria categoria = buscarPorId(id);

        if (productoRepository.existsByCategoria_IdCategoriaAndActivoTrue(id)) {
            throw new IllegalStateException(
                    "No se puede desactivar una categoria con productos activos. "
                    + "Reasigne o desactive primero sus productos.");
        }

        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void activar(Long id) {
        Categoria categoria = buscarPorId(id);
        categoria.setActivo(true);
        categoriaRepository.save(categoria);
    }
}
