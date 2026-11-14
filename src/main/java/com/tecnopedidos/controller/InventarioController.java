package com.tecnopedidos.controller;

import com.tecnopedidos.domain.TipoMovimiento;
import com.tecnopedidos.service.MovimientoInventarioService;
import com.tecnopedidos.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Movimientos de inventario (HU-05) y reporte de bajo stock (HU-10)
@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private final MovimientoInventarioService movimientoService;
    private final ProductoService productoService;

    public InventarioController(MovimientoInventarioService movimientoService,
                                ProductoService productoService) {
        this.movimientoService = movimientoService;
        this.productoService = productoService;
    }

    // Historial de movimientos, opcionalmente de un producto
    @GetMapping("/movimientos")
    public String movimientos(@RequestParam(required = false) Long idProducto,
                              Model model) {

        model.addAttribute("movimientos", movimientoService.buscar(idProducto));
        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("tiposMovimiento", movimientoService.tiposManuales());
        model.addAttribute("idProducto", idProducto);

        return "inventario/movimientos";
    }

    // Registro de una entrada o ajuste manual
    @PostMapping("/movimientos")
    public String registrar(@RequestParam(required = false) Long idProducto,
                            @RequestParam(required = false) TipoMovimiento tipo,
                            @RequestParam(required = false) Integer cantidad,
                            @RequestParam(required = false) String motivo,
                            RedirectAttributes redirect) {
        try {
            movimientoService.registrarManual(idProducto, tipo, cantidad, motivo);
            redirect.addFlashAttribute("mensaje", "Movimiento registrado correctamente");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        if (idProducto == null) {
            return "redirect:/inventario/movimientos";
        }

        return "redirect:/inventario/movimientos?idProducto=" + idProducto;
    }

    // Reporte de productos en el minimo o por debajo (P08)
    @GetMapping("/bajo-stock")
    public String bajoStock(Model model) {
        model.addAttribute("productos", productoService.buscarBajoStock());
        return "inventario/bajo-stock";
    }
}
