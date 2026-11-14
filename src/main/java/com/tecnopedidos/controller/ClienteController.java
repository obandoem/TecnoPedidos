package com.tecnopedidos.controller;

import com.tecnopedidos.domain.Cliente;
import com.tecnopedidos.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Clientes (HU-03 y HU-22) - pantalla P04
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String texto, Model model) {
        model.addAttribute("clientes", clienteService.buscar(texto));
        model.addAttribute("texto", texto);
        return "clientes/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("cliente", clienteService.buscarPorId(id));
            return "clientes/formulario";

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente,
                          BindingResult result,
                          RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "clientes/formulario";
        }

        try {
            clienteService.guardar(cliente);
            redirect.addFlashAttribute("mensaje", "Cliente guardado correctamente");
            return "redirect:/clientes";

        } catch (IllegalArgumentException e) {
            result.reject("cliente.error", e.getMessage());
            return "clientes/formulario";
        }
    }
}
