package com.tecnopedidos.controller;

import com.tecnopedidos.service.EmpresaActualService;
import com.tecnopedidos.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Panel de inicio (P02)
@Controller
public class InicioController {

    private final EmpresaActualService empresaActual;
    private final PedidoService pedidoService;

    public InicioController(EmpresaActualService empresaActual, PedidoService pedidoService) {
        this.empresaActual = empresaActual;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {

        // El tecnico va directo a sus pedidos asignados (HU-01 esc. 4)
        if (empresaActual.esRol("TECNICO")) {
            return "redirect:/tecnico/pedidos";
        }

        model.addAttribute("pendientesEntrega", pedidoService.contarPendientesEntrega());
        model.addAttribute("listos", pedidoService.contarListos());
        model.addAttribute("recientes", pedidoService.recientes());

        return "inicio";
    }
}
