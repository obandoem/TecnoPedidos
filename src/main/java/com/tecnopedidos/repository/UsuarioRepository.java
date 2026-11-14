package com.tecnopedidos.repository;

import com.tecnopedidos.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Inicio de sesion por correo (HU-01)
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Busca por id solo dentro de la empresa (HU-02)
    Optional<Usuario> findByIdUsuarioAndEmpresa_IdEmpresa(Long idUsuario, Long idEmpresa);

    // Tecnicos activos de la empresa, para asignar pedidos (HU-08)
    List<Usuario> findByEmpresa_IdEmpresaAndRol_NombreAndActivoTrueOrderByNombreAsc(
            Long idEmpresa, String rol);
}
