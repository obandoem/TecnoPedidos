package com.tecnopedidos.controller;

import com.tecnopedidos.domain.Categoria;
import com.tecnopedidos.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Mantenimiento de categorias (HU-21) - pantalla P03
@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Listado de categorias
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listar());
        return "categorias/listado";
    }

    // Formulario de registro
    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categorias/formulario";
    }

    // Formulario de edicion
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model,
                         RedirectAttributes redirect) {
        try {
            model.addAttribute("categoria", categoriaService.buscarPorId(id));
            return "categorias/formulario";

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/categorias";
        }
    }

    // Guardar o actualizar categoria
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult result,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "categorias/formulario";
        }

        try {
            categoriaService.guardar(categoria);
            redirect.addFlashAttribute("mensaje", "Categoria guardada correctamente");
            return "redirect:/categorias";

        } catch (IllegalArgumentException e) {
            result.reject("categoria.error", e.getMessage());
            return "categorias/formulario";
        }
    }

    // Desactivar categoria (no se elimina fisicamente)
    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categoriaService.desactivar(id);
            redirect.addFlashAttribute("mensaje", "Categoria desactivada correctamente");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categorias";
    }

    // Reactivar categoria
    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categoriaService.activar(id);
            redirect.addFlashAttribute("mensaje", "Categoria activada correctamente");

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categorias";
    }
}
