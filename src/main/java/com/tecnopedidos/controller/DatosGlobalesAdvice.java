package com.tecnopedidos.controller;

import com.tecnopedidos.service.EmpresaActualService;
import com.tecnopedidos.service.ProductoService;
import com.tecnopedidos.service.UsuarioPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Datos disponibles en todas las vistas: usuario, rol, empresa y contador del menu
@ControllerAdvice
public class DatosGlobalesAdvice {

    private final EmpresaActualService empresaActual;
    private final ProductoService productoService;

    public DatosGlobalesAdvice(EmpresaActualService empresaActual,
                               ProductoService productoService) {
        this.empresaActual = empresaActual;
        this.productoService = productoService;
    }

    @ModelAttribute
    public void datosGlobales(Model model) {

        // En la pantalla de inicio de sesion todavia no hay usuario
        if (!empresaActual.haySesion()) {
            return;
        }

        UsuarioPrincipal usuario = empresaActual.getPrincipal().orElseThrow();
        String rol = usuario.getRol();

        model.addAttribute("usuarioNombre", usuario.getNombre());
        model.addAttribute("usuarioRol", rol);
        model.addAttribute("esAdmin", "ADMIN".equals(rol));
        model.addAttribute("esVendedor", "VENDEDOR".equals(rol));
        model.addAttribute("esTecnico", "TECNICO".equals(rol));
        model.addAttribute("nombreEmpresa", empresaActual.getEmpresa().getNombre());

        // Contador rojo junto a "Bajo stock" (no aplica al tecnico)
        if (!"TECNICO".equals(rol)) {
            model.addAttribute("cantidadBajoStock", productoService.contarBajoStock());
        }
    }
}
