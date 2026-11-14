package com.tecnopedidos.domain;

// Estados del ciclo de vida de un pedido (HU-08, HU-09, HU-24, HU-26)
public enum EstadoPedido {
    PENDIENTE,
    ASIGNADO,
    EN_PROCESO,
    LISTO,
    ENTREGADO,
    CANCELADO
}
