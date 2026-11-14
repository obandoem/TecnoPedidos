package com.tecnopedidos.service;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Usuario autenticado que Spring Security guarda en la sesion.
// Conserva la empresa del usuario para aislar los datos (HU-02).
public class UsuarioPrincipal implements UserDetails {

    private final Long idUsuario;
    private final Long idEmpresa;
    private final String nombre;
    private final String correo;
    private final String password;
    private final String rol;
    private final boolean activo;

    public UsuarioPrincipal(Long idUsuario, Long idEmpresa, String nombre, String correo,
                            String password, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.idEmpresa = idEmpresa;
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    // Spring Security usa el prefijo ROLE_ para hasRole("ADMIN")
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }
}
