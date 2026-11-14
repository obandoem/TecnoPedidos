package com.tecnopedidos.domain;

// Tipos de movimiento de inventario (HU-05)
public enum TipoMovimiento {

    // Movimientos manuales que registra el administrador
    ENTRADA("Entrada", 1, true),
    AJUSTE_POSITIVO("Ajuste positivo", 1, true),
    AJUSTE_NEGATIVO("Ajuste negativo", -1, true),

    // Movimientos que solo generan las operaciones de pedidos (HU-05 esc. 4)
    SALIDA_PEDIDO("Salida por pedido", -1, false),
    DEVOLUCION_CANCELACION("Devolucion por cancelacion", 1, false);

    private final String descripcion;
    private final int signo;
    private final boolean manual;

    TipoMovimiento(String descripcion, int signo, boolean manual) {
        this.descripcion = descripcion;
        this.signo = signo;
        this.manual = manual;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // +1 suma existencias, -1 las resta
    public int getSigno() {
        return signo;
    }

    public boolean isManual() {
        return manual;
    }
}
