package com.tecnopedidos.repository;

import com.tecnopedidos.domain.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Busqueda por codigo o nombre (parcial, sin distinguir mayusculas)
    // con filtros opcionales de categoria y estado (HU-15).
    // estado: null = activos, DISPONIBLE, BAJO_STOCK o INACTIVO
    @Query("""
            SELECT p FROM Producto p
            JOIN FETCH p.categoria c
            WHERE p.empresa.idEmpresa = :idEmpresa
              AND (:idCategoria IS NULL OR c.idCategoria = :idCategoria)
              AND (:texto IS NULL
                   OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND ((:estado IS NULL AND p.activo = true)
                   OR (:estado = 'DISPONIBLE' AND p.activo = true AND p.stock > p.stockMinimo)
                   OR (:estado = 'BAJO_STOCK' AND p.activo = true AND p.stock <= p.stockMinimo)
                   OR (:estado = 'INACTIVO' AND p.activo = false))
            ORDER BY p.nombre
            """)
    List<Producto> buscar(@Param("idEmpresa") Long idEmpresa,
                          @Param("texto") String texto,
                          @Param("idCategoria") Long idCategoria,
                          @Param("estado") String estado);

    // Cantidad de productos con bajo stock, para el indicador del menu
    @Query("""
            SELECT COUNT(p) FROM Producto p
            WHERE p.empresa.idEmpresa = :idEmpresa
              AND p.activo = true
              AND p.stock <= p.stockMinimo
            """)
    long contarBajoStock(@Param("idEmpresa") Long idEmpresa);

    // Productos activos con stock actual menor o igual al minimo (HU-10)
    @Query("""
            SELECT p FROM Producto p
            JOIN FETCH p.categoria c
            WHERE p.empresa.idEmpresa = :idEmpresa
              AND p.activo = true
              AND p.stock <= p.stockMinimo
            ORDER BY p.nombre
            """)
    List<Producto> buscarBajoStock(@Param("idEmpresa") Long idEmpresa);

    // Busca por id solo dentro de la empresa (HU-02 esc. 2)
    Optional<Producto> findByIdProductoAndEmpresa_IdEmpresa(Long idProducto, Long idEmpresa);

    // Para evitar codigos duplicados dentro de la empresa (HU-04 esc. 2)
    Optional<Producto> findByEmpresa_IdEmpresaAndCodigoIgnoreCase(Long idEmpresa, String codigo);

    // Productos activos de la empresa, para formularios
    List<Producto> findByEmpresa_IdEmpresaAndActivoTrueOrderByNombreAsc(Long idEmpresa);

    // Una categoria con productos activos no se puede desactivar (HU-21 esc. 3)
    boolean existsByCategoria_IdCategoriaAndActivoTrue(Long idCategoria);
}
