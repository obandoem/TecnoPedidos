package com.tecnopedidos.repository;

import com.tecnopedidos.domain.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busqueda por nombre o identificacion dentro de la empresa (HU-22 esc. 1)
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.empresa.idEmpresa = :idEmpresa
              AND (:texto IS NULL
                   OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(c.identificacion) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY c.nombre
            """)
    List<Cliente> buscar(@Param("idEmpresa") Long idEmpresa, @Param("texto") String texto);

    // Busca por id solo dentro de la empresa (HU-02 esc. 2)
    Optional<Cliente> findByIdClienteAndEmpresa_IdEmpresa(Long idCliente, Long idEmpresa);

    // Identificacion unica dentro de la empresa (HU-03 esc. 2)
    Optional<Cliente> findByEmpresa_IdEmpresaAndIdentificacionIgnoreCase(Long idEmpresa, String identificacion);

    // Clientes activos, para registrar pedidos (HU-06)
    List<Cliente> findByEmpresa_IdEmpresaAndActivoTrueOrderByNombreAsc(Long idEmpresa);
}
