package com.tecnopedidos.controller;

import com.tecnopedidos.domain.Pedido;
import com.tecnopedidos.service.PedidoService;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// P10 - Seguimiento publico del pedido con codigo, sin iniciar sesion (HU-13)
@Controller
@RequestMapping("/seguimiento-publico")
public class SeguimientoPublicoController {

    private final PedidoService pedidoService;

    public SeguimientoPublicoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // El codigo llega por la URL: /seguimiento-publico?codigo=TP-XXXXXX
    // (asi el codigo QR puede abrir la consulta directamente)
    @GetMapping
    public String consultar(@RequestParam(required = false) String codigo, Model model) {

        model.addAttribute("codigo", codigo);

        if (codigo != null && !codigo.isBlank()) {
            Optional<Pedido> pedido = pedidoService.buscarPorCodigoSeguimiento(codigo);

            if (pedido.isPresent()) {
                model.addAttribute("pedido", pedido.get());
            } else {
                model.addAttribute("noEncontrado", true);
            }
        }

        return "seguimiento-publico";
    }
}