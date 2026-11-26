package com.tecnopedidos.service;

import com.tecnopedidos.domain.EstadoPedido;
import com.tecnopedidos.domain.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Aviso por correo al cliente cuando cambia el estado de su pedido (HU-17)
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String urlBase;
    private final boolean activo;

    public CorreoService(JavaMailSender mailSender,
                         @Value("${spring.mail.username:}") String remitente,
                         @Value("${app.url-base:http://localhost:8080}") String urlBase,
                         @Value("${app.correo.activo:false}") boolean activo) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.urlBase = urlBase;
        this.activo = activo;
    }

    // Envia el aviso. Si falla, NO detiene el cambio de estado: solo lo anota en el log
    public void avisarCambioEstado(Pedido pedido, EstadoPedido nuevo, String comentario) {

        String destino = pedido.getCliente().getCorreo();

        if (destino == null || destino.isBlank()) {
            log.info("Pedido {} sin correo del cliente: no se envia aviso", pedido.getCodigo());
            return;
        }

        String enlace = urlBase + "/seguimiento-publico?codigo=" + pedido.getCodigoSeguimiento();

        StringBuilder texto = new StringBuilder();
        texto.append("Hola ").append(pedido.getCliente().getNombre()).append(",\n\n");
        texto.append("Su pedido ").append(pedido.getCodigoSeguimiento())
             .append(" en ").append(pedido.getEmpresa().getNombre())
             .append(" cambió de estado a: ").append(nombreEstado(nuevo)).append(".\n");

        if (comentario != null && !comentario.isBlank()) {
            texto.append("Comentario: ").append(comentario).append("\n");
        }

        texto.append("\nPuede consultar el avance en:\n").append(enlace).append("\n\n");
        texto.append("Gracias por su preferencia.\nTecnoPedidos");

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destino);
        mensaje.setSubject("Pedido " + pedido.getCodigoSeguimiento() + ": " + nombreEstado(nuevo));
        mensaje.setText(texto.toString());

        if (!activo) {
            log.info("Correo desactivado (app.correo.activo=false). Aviso para {}:\n{}", destino, texto);
            return;
        }

        try {
            mailSender.send(mensaje);
            log.info("Aviso enviado a {} del pedido {}", destino, pedido.getCodigoSeguimiento());
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
        }
    }

    // Nombre del estado para el cliente
    private String nombreEstado(EstadoPedido estado) {
        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case ASIGNADO -> "Asignado a un técnico";
            case EN_PROCESO -> "En proceso";
            case LISTO -> "Listo para entregar";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }
}