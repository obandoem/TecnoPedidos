-- =====================================================================
-- TecnoPedidos - Esquema de base de datos (MySQL 8)
-- Ejecutar primero este script y luego 02_datos.sql
-- =====================================================================

-- Para reiniciar desde cero, descomente la siguiente linea:
-- DROP DATABASE IF EXISTS tecnopedidos;

CREATE DATABASE IF NOT EXISTS tecnopedidos
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE tecnopedidos;

-- ---------------------------------------------------------------------
-- Organizacion y seguridad
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS empresa (
    id_empresa BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS rol (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- ---------------------------------------------------------------------
-- Catalogo e inventario
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_categoria_empresa_nombre UNIQUE (id_empresa, nombre),
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

CREATE TABLE IF NOT EXISTS producto (
    id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL,
    id_categoria BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_producto_empresa_codigo UNIQUE (id_empresa, codigo),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0),
    CONSTRAINT fk_producto_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria) ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Clientes y pedidos (tablas transaccionales)
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    identificacion VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(120),
    telefono VARCHAR(30),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_cliente_empresa_identificacion UNIQUE (id_empresa, identificacion),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    id_cliente BIGINT NOT NULL,
    id_vendedor BIGINT,
    id_tecnico BIGINT,
    requiere_asistencia BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(20) NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha_creacion DATETIME NOT NULL,
    CONSTRAINT fk_pedido_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_pedido_vendedor FOREIGN KEY (id_vendedor) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_pedido_tecnico FOREIGN KEY (id_tecnico) REFERENCES usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pedido BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id_movimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    id_usuario BIGINT,
    id_pedido BIGINT,
    tipo VARCHAR(30) NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_resultante INT NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_movimiento_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_movimiento_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
);

CREATE TABLE IF NOT EXISTS seguimiento_pedido (
    id_seguimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pedido BIGINT NOT NULL,
    id_usuario BIGINT,
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20) NOT NULL,
    comentario_publico VARCHAR(500),
    observacion_interna VARCHAR(500),
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_seguimiento_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
    CONSTRAINT fk_seguimiento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- Verificacion
SHOW TABLES;
