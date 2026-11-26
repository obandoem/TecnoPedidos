package com.tecnopedidos.controller;

import com.tecnopedidos.domain.CarritoPedido;
import com.tecnopedidos.domain.DetallePedido;
import com.tecnopedidos.domain.EstadoPedido;
import com.tecnopedidos.domain.Pedido;
import com.tecnopedidos.service.ClienteService;
import com.tecnopedidos.service.CodigoQrService;
import com.tecnopedidos.service.PedidoService;
import com.tecnopedidos.service.ProductoService;
import com.tecnopedidos.service.SeguimientoService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// Pedidos: listado (P06), nuevo pedido con carrito (P05), detalle y acciones
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final String CARRITO = "carritoPedido";

    private final PedidoService pedidoService;
    private final SeguimientoService seguimientoService;
    private final ClienteService clienteService;
        private final ProductoService productoService;
    private final CodigoQrService codigoQrService;

    public PedidoController(PedidoService pedidoService,
                            SeguimientoService seguimientoService,
                            ClienteService clienteService,
                            ProductoService productoService,
                            CodigoQrService codigoQrService) {
        this.pedidoService = pedidoService;
        this.seguimientoService = seguimientoService;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.codigoQrService = codigoQrService;
    }

    // El carrito vive en la sesion del usuario hasta confirmar el pedido
    private CarritoPedido carrito(HttpSession session) {
        CarritoPedido carrito = (CarritoPedido) session.getAttribute(CARRITO);
        if (carrito == null) {
            carrito = new CarritoPedido();
            session.setAttribute(CARRITO, carrito);
        }
        return carrito;
    }

    // ------------------------------------------------------------------
    // Listado (P06)
    // ------------------------------------------------------------------

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoPedido estado, Model model) {
        model.addAttribute("pedidos", pedidoService.buscar(estado));
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estado", estado);
        return "pedidos/listado";
    }

    // ------------------------------------------------------------------
    // Nuevo pedido con carrito (P05 / HU-06 y HU-07)
    // ------------------------------------------------------------------

    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {

        CarritoPedido carrito = carrito(session);
        List<DetallePedido> lineas = pedidoService.calcularLineas(carrito);

        model.addAttribute("carrito", carrito);
        model.addAttribute("lineas", lineas);
        model.addAttribute("total", pedidoService.calcularTotal(lineas));
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("productos", productoService.listarActivos());

        return "pedidos/nuevo";
    }

    // Un solo formulario con dos acciones: agregar producto o confirmar el pedido
    @PostMapping("/nuevo")
    public String procesar(@RequestParam(required = false) Long idCliente,
                           @RequestParam(defaultValue = "false") boolean requiereAsistencia,
                           @RequestParam(required = false) Long idProducto,
                           @RequestParam(required = false) Integer cantidad,
                           @RequestParam(defaultValue = "agregar") String accion,
                           HttpSession session,
                           RedirectAttributes redirect) {

        CarritoPedido carrito = carrito(session);
        carrito.setIdCliente(idCliente);
        carrito.setRequiereAsistencia(requiereAsistencia);

        try {
            if ("confirmar".equals(accion)) {
                Pedido pedido = pedidoService.registrar(carrito);

                // Solo despues de guardar se vacia el carrito:
                // reenviar la confirmacion ya no crea otro pedido (HU-06 esc. 4)
                carrito.vaciar();

                redirect.addFlashAttribute("mensaje",
                        "Pedido " + pedido.getCodigo() + " registrado correctamente");
                return "redirect:/pedidos/" + pedido.getIdPedido();
            }

            pedidoService.agregarLinea(carrito, idProducto, cantidad);

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());

        } catch (ObjectOptimisticLockingFailureException e) {
            // Otro usuario modifico las mismas existencias al mismo tiempo (HU-06 esc. 4)
            redirect.addFlashAttribute("error",
                    "Otro usuario modificó las existencias al mismo tiempo. Revise el pedido e intente de nuevo.");
        }

        return "redirect:/pedidos/nuevo";
    }

    @PostMapping("/nuevo/linea/{idProducto}/actualizar")
    public String actualizarLinea(@PathVariable Long idProducto,
                                  @RequestParam(required = false) Integer cantidad,
                                  HttpSession session, RedirectAttributes redirect) {
        try {
            pedidoService.actualizarLinea(carrito(session), idProducto, cantidad);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/nuevo";
    }

    @PostMapping("/nuevo/linea/{idProducto}/eliminar")
    public String eliminarLinea(@PathVariable Long idProducto, HttpSession session) {
        carrito(session).eliminar(idProducto);
        return "redirect:/pedidos/nuevo";
    }

    @PostMapping("/nuevo/descartar")
    public String descartar(HttpSession session) {
        carrito(session).vaciar();
        return "redirect:/pedidos/nuevo";
    }

    // ------------------------------------------------------------------
    // Detalle y acciones
    // ------------------------------------------------------------------

       @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Pedido pedido = pedidoService.buscarPorId(id);
            model.addAttribute("pedido", pedido);
            model.addAttribute("tecnicos", seguimientoService.tecnicosDisponibles());

            // Codigo QR con el enlace a la consulta publica (HU-13), por ejemplo:
            // http://localhost:8080/seguimiento-publico?codigo=TP-UZR3J7
            if (pedido.getCodigoSeguimiento() != null) {
                String enlace = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/seguimiento-publico")
                        .queryParam("codigo", pedido.getCodigoSeguimiento())
                        .toUriString();

                model.addAttribute("enlaceSeguimiento", enlace);
                model.addAttribute("qrSeguimiento", codigoQrService.generarQrBase64(enlace, 220));
            }

            return "pedidos/detalle";

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos";
        }
    }

    // Asignar o reasignar tecnico (HU-08) - solo administrador
    @PostMapping("/{id}/asignar")
    public String asignar(@PathVariable Long id,
                          @RequestParam(required = false) Long idTecnico,
                          @RequestParam(required = false) String motivo,
                          RedirectAttributes redirect) {
        return ejecutar(id, redirect, "Técnico asignado correctamente",
                () -> seguimientoService.asignarTecnico(id, idTecnico, motivo));
    }

    // Marcar como listo un pedido sin asistencia (HU-26 esc. 1)
    @PostMapping("/{id}/listo")
    public String listo(@PathVariable Long id, RedirectAttributes redirect) {
        return ejecutar(id, redirect, "Pedido marcado como listo",
                () -> seguimientoService.marcarListo(id));
    }

    // Registrar la entrega (HU-26 esc. 3)
    @PostMapping("/{id}/entregar")
    public String entregar(@PathVariable Long id,
                           @RequestParam(required = false) String comentario,
                           RedirectAttributes redirect) {
        return ejecutar(id, redirect, "Entrega registrada correctamente",
                () -> seguimientoService.entregar(id, comentario));
    }

    // Cancelar y devolver existencias (HU-24) - solo administrador
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(required = false) String motivo,
                           RedirectAttributes redirect) {
        return ejecutar(id, redirect, "Pedido cancelado y existencias devueltas",
                () -> pedidoService.cancelar(id, motivo));
    }

    // Ejecuta una accion sobre el pedido y regresa a su detalle con el resultado
    private String ejecutar(Long id, RedirectAttributes redirect, String exito, Runnable accion) {
        try {
            accion.run();
            redirect.addFlashAttribute("mensaje", exito);

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());

        } catch (ObjectOptimisticLockingFailureException e) {
            redirect.addFlashAttribute("error",
                    "Otro usuario modificó este pedido o sus existencias. Intente de nuevo.");
        }

        return "redirect:/pedidos/" + id;
    }
}
