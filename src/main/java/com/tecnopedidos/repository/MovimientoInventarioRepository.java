package com.tecnopedidos.repository;

import com.tecnopedidos.domain.MovimientoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    // Historial de movimientos de la empresa, opcionalmente de un producto
    @Query("""
            SELECT m FROM MovimientoInventario m
            JOIN FETCH m.producto p
            WHERE m.empresa.idEmpresa = :idEmpresa
              AND (:idProducto IS NULL OR p.idProducto = :idProducto)
            ORDER BY m.fecha DESC, m.idMovimiento DESC
            """)
    List<MovimientoInventario> buscar(@Param("idEmpresa") Long idEmpresa,
                                      @Param("idProducto") Long idProducto);
}
