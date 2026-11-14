package com.tecnopedidos.controller;

import com.tecnopedidos.service.EmpresaActualService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Inicio de sesion (P01) y pagina de acceso denegado
@Controller
public class LoginController {

    private final EmpresaActualService empresaActual;

    public LoginController(EmpresaActualService empresaActual) {
        this.empresaActual = empresaActual;
    }

    @GetMapping("/login")
    public String login() {
        // Si ya inicio sesion, no tiene sentido mostrar el formulario
        if (empresaActual.haySesion()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}
