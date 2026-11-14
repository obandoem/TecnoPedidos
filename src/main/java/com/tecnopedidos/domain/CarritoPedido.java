package com.tecnopedidos.domain;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

// Carrito del pedido en preparacion (HU-07).
// Se guarda en la sesion del vendedor hasta confirmar el pedido.
// Solo almacena ids y cantidades: los precios se calculan siempre en el servidor
// con el precio vigente del catalogo (HU-07 esc. 2).
public class CarritoPedido implements Serializable {

    private Long idCliente;
    private boolean requiereAsistencia;

    // idProducto -> cantidad (conserva el orden en que se agregaron)
    private final Map<Long, Integer> lineas = new LinkedHashMap<>();

    // Si el producto ya esta en el carrito, se suma la cantidad en una sola linea (HU-07 esc. 1)
    public void agregar(Long idProducto, int cantidad) {
        lineas.merge(idProducto, cantidad, Integer::sum);
    }

    public void actualizar(Long idProducto, int cantidad) {
        if (lineas.containsKey(idProducto)) {
            lineas.put(idProducto, cantidad);
        }
    }

    public void eliminar(Long idProducto) {
        lineas.remove(idProducto);
    }

    public void vaciar() {
        lineas.clear();
        idCliente = null;
        requiereAsistencia = false;
    }

    public boolean isVacio() {
        return lineas.isEmpty();
    }

    public Map<Long, Integer> getLineas() {
        return lineas;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public boolean isRequiereAsistencia() {
        return requiereAsistencia;
    }

    public void setRequiereAsistencia(boolean requiereAsistencia) {
        this.requiereAsistencia = requiereAsistencia;
    }
}
