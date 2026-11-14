-- =====================================================================
-- TecnoPedidos - Datos de demostracion (ficticios)
-- Ejecutar despues de 01_esquema.sql, sobre una base de datos vacia.
-- =====================================================================

USE tecnopedidos;

-- ---------------------------------------------------------------------
-- Empresas: la 1 es la empresa de demostracion que usa la aplicacion
-- mientras no exista inicio de sesion. La 2 sirve para comprobar que
-- los datos de otra empresa no aparecen (HU-02).
-- ---------------------------------------------------------------------
INSERT INTO empresa (id_empresa, nombre, telefono, correo, activo) VALUES
(1, 'TecnoPlus S.A.', '2222-0001', 'contacto@tecnoplus.cr', TRUE),
(2, 'CompuRepuestos del Sur', '2771-0002', 'info@compurepuestos.cr', TRUE);

INSERT INTO rol (id_rol, nombre) VALUES
(1, 'ADMIN'),
(2, 'VENDEDOR'),
(3, 'TECNICO');

-- ---------------------------------------------------------------------
-- Usuarios de prueba. Contrasena de todos: Demo2026*
-- Se guarda como hash BCrypt, nunca en texto plano (HU-11 esc. 2).
-- ---------------------------------------------------------------------
INSERT INTO usuario (id_usuario, id_empresa, id_rol, nombre, correo, password, activo) VALUES
(1, 1, 1, 'María Rodríguez',    'admin@tecnoplus.cr',    '{bcrypt}$2a$10$cgknfGOYkgR20oyKxJbL9e801URvX1NcBxIiOlvQ/UG7.Jlu8DwMG', TRUE),
(2, 1, 2, 'Victor Vendedor',    'vendedor@tecnoplus.cr', '{bcrypt}$2a$10$cgknfGOYkgR20oyKxJbL9e801URvX1NcBxIiOlvQ/UG7.Jlu8DwMG', TRUE),
(3, 1, 3, 'Tania Tecnica',      'tecnico@tecnoplus.cr',  '{bcrypt}$2a$10$cgknfGOYkgR20oyKxJbL9e801URvX1NcBxIiOlvQ/UG7.Jlu8DwMG', TRUE),
(4, 1, 3, 'Tomas Tecnico',      'tecnico2@tecnoplus.cr', '{bcrypt}$2a$10$cgknfGOYkgR20oyKxJbL9e801URvX1NcBxIiOlvQ/UG7.Jlu8DwMG', TRUE),
(5, 2, 1, 'Admin CompuRepuestos', 'admin@compurepuestos.cr', '{bcrypt}$2a$10$cgknfGOYkgR20oyKxJbL9e801URvX1NcBxIiOlvQ/UG7.Jlu8DwMG', TRUE);

-- ---------------------------------------------------------------------
-- Categorias
-- ---------------------------------------------------------------------
INSERT INTO categoria (id_categoria, id_empresa, nombre, descripcion, activo) VALUES
(1, 1, 'Periféricos', 'Teclados, mouse, audífonos y accesorios de entrada', TRUE),
(2, 1, 'Monitores', 'Pantallas y monitores para escritorio', TRUE),
(3, 1, 'Almacenamiento', 'Discos SSD, HDD y memorias USB', TRUE),
(4, 1, 'Componentes', 'Memorias RAM, fuentes de poder y repuestos internos', TRUE),
(5, 1, 'Cables y adaptadores', 'Cables de video, USB y adaptadores', TRUE),
(6, 2, 'Repuestos de laptop', 'Baterías, cargadores y pantallas', TRUE);

-- ---------------------------------------------------------------------
-- Productos (algunos con bajo stock o agotados para el reporte HU-10)
-- ---------------------------------------------------------------------
INSERT INTO producto (id_producto, id_empresa, codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria, activo, version) VALUES
(1,  1, 'TEC-001', 'Teclado mecánico RGB', 'Switches rojos, distribución en español', 24500.00, 12, 5, 1, TRUE, 0),
(2,  1, 'MOU-001', 'Mouse inalámbrico', 'Receptor USB, 1600 DPI', 9500.00, 3, 5, 1, TRUE, 0),
(3,  1, 'MON-001', 'Monitor 24" Full HD', 'Panel IPS, 75 Hz, HDMI', 89900.00, 4, 3, 2, TRUE, 0),
(4,  1, 'SSD-001', 'SSD NVMe 1 TB', 'PCIe 4.0, lectura 5000 MB/s', 52000.00, 0, 4, 3, TRUE, 0),
(5,  1, 'RAM-001', 'Memoria RAM DDR4 16 GB', '3200 MHz, para escritorio', 31500.00, 6, 6, 4, TRUE, 0),
(6,  1, 'FUE-001', 'Fuente de poder 650 W', 'Certificación 80+ Bronze', 45000.00, 8, 3, 4, TRUE, 0),
(7,  1, 'CAB-001', 'Cable HDMI 2 m', 'HDMI 2.0, 4K', 3500.00, 25, 10, 5, TRUE, 0),
(8,  1, 'ADP-001', 'Adaptador USB-C a HDMI', 'Compatible con laptops y tablets', 12900.00, 2, 4, 5, TRUE, 0),
(9,  1, 'PAS-001', 'Pasta térmica 4 g', 'Para procesadores de escritorio y laptop', 4900.00, 15, 5, 4, TRUE, 0),
(10, 1, 'AUD-001', 'Audífonos con micrófono', 'Conector 3.5 mm y USB', 18900.00, 7, 3, 1, TRUE, 0),
(11, 2, 'BAT-001', 'Batería para laptop', 'Compatible con varios modelos', 38000.00, 5, 2, 6, TRUE, 0);

-- ---------------------------------------------------------------------
-- Movimientos de inventario inicial: justifican el stock de cada
-- producto, porque el stock solo cambia mediante movimientos (HU-05)
-- ---------------------------------------------------------------------
INSERT INTO movimiento_inventario (id_empresa, id_producto, id_usuario, tipo, cantidad, stock_anterior, stock_resultante, motivo, fecha) VALUES
(1, 1,  1, 'ENTRADA', 12, 0, 12, 'Inventario inicial', NOW()),
(1, 2,  1, 'ENTRADA', 3,  0, 3,  'Inventario inicial', NOW()),
(1, 3,  1, 'ENTRADA', 4,  0, 4,  'Inventario inicial', NOW()),
(1, 5,  1, 'ENTRADA', 6,  0, 6,  'Inventario inicial', NOW()),
(1, 6,  1, 'ENTRADA', 8,  0, 8,  'Inventario inicial', NOW()),
(1, 7,  1, 'ENTRADA', 25, 0, 25, 'Inventario inicial', NOW()),
(1, 8,  1, 'ENTRADA', 2,  0, 2,  'Inventario inicial', NOW()),
(1, 9,  1, 'ENTRADA', 15, 0, 15, 'Inventario inicial', NOW()),
(1, 10, 1, 'ENTRADA', 7,  0, 7,  'Inventario inicial', NOW()),
(2, 11, 5, 'ENTRADA', 5,  0, 5,  'Inventario inicial', NOW());

-- ---------------------------------------------------------------------
-- Clientes (para la rama feature/pedidos)
-- ---------------------------------------------------------------------
INSERT INTO cliente (id_cliente, id_empresa, identificacion, nombre, correo, telefono, activo) VALUES
(1, 1, '1-1111-1111', 'Laura Jiménez', 'laura.jimenez@correo.cr', '8888-1111', TRUE),
(2, 1, '2-2222-2222', 'Oficina Contable Ruiz', 'compras@contableruiz.cr', '2233-4455', TRUE),
(3, 1, '3-3333-3333', 'Diego Mora', NULL, '8777-3333', TRUE),
(4, 2, '4-4444-4444', 'Cliente de otra empresa', NULL, NULL, TRUE);
