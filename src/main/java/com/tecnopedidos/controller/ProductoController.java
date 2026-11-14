package com.tecnopedidos.controller;

import com.tecnopedidos.domain.Categoria;
import com.tecnopedidos.domain.Producto;
import com.tecnopedidos.service.CategoriaService;
import com.tecnopedidos.service.MovimientoInventarioService;
import com.tecnopedidos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Catalogo e inventario (HU-04, HU-15, HU-23) - pantalla P03
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MovimientoInventarioService movimientoService;

    public ProductoController(ProductoService productoService,
                              CategoriaService categoriaService,
                              MovimientoInventarioService movimientoService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.movimientoService = movimientoService;
    }

    // Inventario (P03): listado, busqueda y filtros (la consulta se resuelve en el repositorio)
    @GetMapping
    public String listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) String estado,
            Model model) {

        model.addAttribute("productos",
                productoService.buscar(texto, idCategoria, estado));
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("categoriasActivas", categoriaService.listarActivas());
        model.addAttribute("tiposMovimiento", movimientoService.tiposManuales());
        model.addAttribute("texto", texto);
        model.addAttribute("idCategoria", idCategoria);
        model.addAttribute("estado", estado);

        return "productos/listado";
    }

    // Formulario para registrar productos
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Producto producto = new Producto();
        producto.setCategoria(new Categoria());

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.listarActivas());

        return "productos/formulario";
    }

    // Formulario para editar productos
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model,
                         RedirectAttributes redirect) {
        try {
            model.addAttribute("producto", productoService.buscarPorId(id));
            model.addAttribute("categorias", categoriaService.listarActivas());
            return "productos/formulario";

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos";
        }
    }

    // Registro y actualizacion
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("producto") Producto producto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarActivas());
            return "productos/formulario";
        }

        try {
            productoService.guardar(producto);
            redirect.addFlashAttribute("mensaje", "Producto guardado correctamente");
            return "redirect:/productos";

        } catch (IllegalArgumentException e) {
            result.reject("producto.error", e.getMessage());
            model.addAttribute("categorias", categoriaService.listarActivas());
            return "productos/formulario";
        }
    }

    // Desactivar producto (conserva su historial)
    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            productoService.desactivar(id);
            redirect.addFlashAttribute("mensaje", "Producto desactivado correctamente");

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/productos";
    }

    // Reactivar producto
    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            productoService.activar(id);
            redirect.addFlashAttribute("mensaje", "Producto activado correctamente");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/productos?estado=INACTIVO";
    }
}
