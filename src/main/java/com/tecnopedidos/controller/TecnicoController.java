package com.tecnopedidos.controller;

import com.tecnopedidos.domain.EstadoPedido;
import com.tecnopedidos.service.SeguimientoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Bandeja y avances del tecnico (HU-25 y HU-09) - pantalla P07
@Controller
@RequestMapping("/tecnico/pedidos")
public class TecnicoController {

    private final SeguimientoService seguimientoService;

    public TecnicoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping
    public String bandeja(@RequestParam(required = false) EstadoPedido estado, Model model) {
        model.addAttribute("pedidos", seguimientoService.bandeja(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("estadosBandeja",
                new EstadoPedido[]{EstadoPedido.ASIGNADO, EstadoPedido.EN_PROCESO, EstadoPedido.LISTO});
        return "tecnico/bandeja";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("pedido", seguimientoService.buscarAsignado(id));
            return "tecnico/detalle";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Pedido inexistente, de otra empresa o ya reasignado (HU-25 esc. 3)
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/tecnico/pedidos";
        }
    }

    @PostMapping("/{id}/avance")
    public String avance(@PathVariable Long id,
                         @RequestParam(required = false) EstadoPedido nuevoEstado,
                         @RequestParam(required = false) String comentarioPublico,
                         @RequestParam(required = false) String observacionInterna,
                         RedirectAttributes redirect) {
        try {
            seguimientoService.registrarAvance(id, nuevoEstado, comentarioPublico, observacionInterna);
            redirect.addFlashAttribute("mensaje", "Avance registrado correctamente");

        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            // Si ya no tiene el pedido, vuelve a la bandeja
            if ("No tiene asignado este pedido".equals(e.getMessage())) {
                return "redirect:/tecnico/pedidos";
            }

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/tecnico/pedidos/" + id;
    }
}
