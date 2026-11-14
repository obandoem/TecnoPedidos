package com.tecnopedidos.repository;

import com.tecnopedidos.domain.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Todas las categorias de la empresa (activas e inactivas)
    List<Categoria> findByEmpresa_IdEmpresaOrderByNombreAsc(Long idEmpresa);

    // Solo activas: para asignarlas a productos
    List<Categoria> findByEmpresa_IdEmpresaAndActivoTrueOrderByNombreAsc(Long idEmpresa);

    // Busca por id solo dentro de la empresa (HU-02 esc. 2)
    Optional<Categoria> findByIdCategoriaAndEmpresa_IdEmpresa(Long idCategoria, Long idEmpresa);

    // Para evitar nombres duplicados dentro de la empresa (HU-21 esc. 1)
    Optional<Categoria> findByEmpresa_IdEmpresaAndNombreIgnoreCase(Long idEmpresa, String nombre);
}
