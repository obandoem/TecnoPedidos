package com.tecnopedidos.controller;

import com.tecnopedidos.domain.Categoria;
import com.tecnopedidos.domain.Producto;
import com.tecnopedidos.service.CategoriaService;
import com.tecnopedidos.service.FirebaseStorageService;
import com.tecnopedidos.service.MovimientoInventarioService;
import com.tecnopedidos.service.ProductoService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Catalogo e inventario (HU-04, HU-15, HU-23) - pantalla P03
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MovimientoInventarioService movimientoService;
    private final FirebaseStorageService firebaseStorageService;

    public ProductoController(ProductoService productoService,
                              CategoriaService categoriaService,
                              MovimientoInventarioService movimientoService,
                              FirebaseStorageService firebaseStorageService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.movimientoService = movimientoService;
        this.firebaseStorageService = firebaseStorageService;
    }

    // Inventario (P03): listado, busqueda y filtros (la consulta se resuelve en el repositorio)
    @GetMapping
    public String listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) String estado,
            Model model) {

        var productos = productoService.buscar(texto, idCategoria, estado);
        Map<Long, String> imagenes = new LinkedHashMap<>();
        for (Producto producto : productos) {
            try {
                imagenes.put(producto.getIdProducto(),
                        firebaseStorageService.obtenerUrlImagen(producto));
            } catch (RuntimeException e) {
                log.warn("No se pudo cargar la imagen Firebase del producto {}",
                        producto.getCodigo(), e);
            }
        }
        model.addAttribute("productos", productos);
        model.addAttribute("imagenes", imagenes);
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
            Producto producto = productoService.buscarPorId(id);
            model.addAttribute("producto", producto);
            try {
                model.addAttribute("imagenActualUrl",
                        firebaseStorageService.obtenerUrlImagen(producto));
            } catch (RuntimeException e) {
                log.warn("No se pudo cargar la imagen Firebase del producto {}",
                        producto.getCodigo(), e);
            }
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
            @RequestParam(name = "imagen", required = false) MultipartFile imagen,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarActivas());
            return "productos/formulario";
        }

        try {
            Producto guardado = productoService.guardar(producto);
            if (imagen != null && !imagen.isEmpty()) {
                try {
                    String imagenPath = firebaseStorageService.subirImagenProducto(
                            guardado.getIdProducto(), imagen);
                    productoService.actualizarImagenPath(guardado.getIdProducto(), imagenPath);
                } catch (IOException | RuntimeException e) {
                    log.warn("El producto {} se guardo, pero no se pudo cargar su imagen",
                            guardado.getCodigo(), e);
                    redirect.addFlashAttribute("error",
                            "El producto se guardo, pero no se pudo cargar su imagen en Firebase.");
                    return "redirect:/productos";
                }
            }
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
