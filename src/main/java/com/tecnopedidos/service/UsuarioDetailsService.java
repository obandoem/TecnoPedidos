package com.tecnopedidos.service;

import com.tecnopedidos.domain.Usuario;
import com.tecnopedidos.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Carga el usuario para el inicio de sesion (HU-01)
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));

        // Cuenta o empresa inactiva: acceso rechazado con mensaje generico (HU-01 esc. 2)
        boolean activo = usuario.isActivo() && usuario.getEmpresa().isActivo();

        return new UsuarioPrincipal(
                usuario.getIdUsuario(),
                usuario.getEmpresa().getIdEmpresa(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getPassword(),
                usuario.getRol().getNombre(),
                activo);
    }
}
