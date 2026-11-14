package com.tecnopedidos.repository;

import com.tecnopedidos.domain.EstadoPedido;
import com.tecnopedidos.domain.Pedido;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Listado de pedidos de la empresa con filtro opcional de estado (P06)
    @Query("""
            SELECT p FROM Pedido p
            JOIN FETCH p.cliente c
            LEFT JOIN FETCH p.tecnico t
            WHERE p.empresa.idEmpresa = :idEmpresa
              AND (:estado IS NULL OR p.estado = :estado)
            ORDER BY p.fechaCreacion DESC, p.idPedido DESC
            """)
    List<Pedido> buscar(@Param("idEmpresa") Long idEmpresa, @Param("estado") EstadoPedido estado);

    // Busca por id solo dentro de la empresa (HU-02 esc. 2)
    Optional<Pedido> findByIdPedidoAndEmpresa_IdEmpresa(Long idPedido, Long idEmpresa);

    // Bandeja del tecnico: solo pedidos asignados actualmente a el (HU-25 esc. 1)
    @Query("""
            SELECT p FROM Pedido p
            JOIN FETCH p.cliente c
            WHERE p.empresa.idEmpresa = :idEmpresa
              AND p.tecnico.idUsuario = :idTecnico
              AND p.estado IN :estados
            ORDER BY p.fechaCreacion DESC, p.idPedido DESC
            """)
    List<Pedido> buscarAsignados(@Param("idEmpresa") Long idEmpresa,
                                 @Param("idTecnico") Long idTecnico,
                                 @Param("estados") Collection<EstadoPedido> estados);

    // Indicadores del panel de inicio (HU-18)
    long countByEmpresa_IdEmpresaAndEstado(Long idEmpresa, EstadoPedido estado);

    long countByEmpresa_IdEmpresaAndEstadoNotIn(Long idEmpresa, Collection<EstadoPedido> estados);

    List<Pedido> findTop5ByEmpresa_IdEmpresaOrderByFechaCreacionDescIdPedidoDesc(Long idEmpresa);
}
