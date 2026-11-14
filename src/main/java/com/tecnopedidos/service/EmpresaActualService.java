package com.tecnopedidos.service;

import com.tecnopedidos.domain.Empresa;
import com.tecnopedidos.domain.Usuario;
import com.tecnopedidos.repository.EmpresaRepository;
import com.tecnopedidos.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Punto unico para obtener el usuario y la empresa de la sesion (HU-02).
// Los servicios NUNCA toman la empresa desde un formulario: la obtienen aqui,
// a partir del usuario autenticado.
@Service
public class EmpresaActualService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public EmpresaActualService(EmpresaRepository empresaRepository,
                                UsuarioRepository usuarioRepository) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Usuario autenticado, si existe
    public Optional<UsuarioPrincipal> getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public boolean haySesion() {
        return getPrincipal().isPresent();
    }

    private UsuarioPrincipal principalObligatorio() {
        return getPrincipal().orElseThrow(() ->
                new IllegalStateException("No hay una sesion iniciada"));
    }

    public Long getIdEmpresa() {
        return principalObligatorio().getIdEmpresa();
    }

    public Long getIdUsuario() {
        return principalObligatorio().getIdUsuario();
    }

    public String getRol() {
        return principalObligatorio().getRol();
    }

    public boolean esRol(String rol) {
        return getPrincipal().map(p -> p.getRol().equals(rol)).orElse(false);
    }

    @Transactional(readOnly = true)
    public Empresa getEmpresa() {
        return empresaRepository.findById(getIdEmpresa())
                .orElseThrow(() -> new IllegalStateException("La empresa de la sesion no existe"));
    }

    @Transactional(readOnly = true)
    public Usuario getUsuario() {
        return usuarioRepository.findById(getIdUsuario())
                .orElseThrow(() -> new IllegalStateException("El usuario de la sesion no existe"));
    }
}
