# TecnoPedidos

**Curso:** SC-403 Desarrollo de Aplicaciones Web y Patrones · Universidad Fidélitas
**Grupo 4:** Roberto Araujo Elizondo, Jose Julian Jaime Potosme, Emeth Isaac Obando Mora, Alberth Ramses Vivas Chavarria
**Prototipo:** https://coral-badger-68682688.figma.site/

Aplicación web transaccional multiempresa para PYMEs que venden accesorios y repuestos de computación y brindan asistencia técnica. Centraliza el catálogo, el inventario, los clientes, los pedidos y el seguimiento técnico. Cada empresa ve solo sus propios datos.

## Tecnologías

- Java 21 y Spring Boot 4 (Spring MVC)
- Spring Security: inicio de sesión, roles y contraseñas BCrypt
- Thymeleaf (plantillas, fragmentos y archivos de idioma)
- Bootstrap 5.3 y Bootstrap Icons
- Spring Data JPA / Hibernate y MySQL 8
- Firebase Admin SDK: Firebase Storage para imágenes de productos
- NetBeans como entorno de desarrollo

## Estructura del proyecto

```
src/main/java/com/tecnopedidos/
├── config/       Seguridad, idioma y Firebase (FirebaseConfig)
├── domain/       Entidades JPA, enumeraciones y carrito del pedido
├── repository/   Repositorios Spring Data (consultas JPQL)
├── service/      Reglas de negocio, transacciones y FirebaseStorageService
└── controller/   Controladores MVC
src/main/resources/
├── templates/
│   ├── layout/plantilla.html   Fragmentos: head, sidebar, topbar, idioma, mensajes,
│   │                           estadoBadge, historial, scripts
│   ├── login.html  inicio.html  acceso-denegado.html
│   ├── productos/  categorias/  inventario/  clientes/  pedidos/  tecnico/
├── messages.properties / messages_en.properties
├── Firebase/
│   ├── tecnopedidos-735e7-fi.json   Credencial de servicio usada por Firebase Admin
│   └── product-images/              Ilustraciones SVG del catálogo de demostración
└── application.properties
database/
├── 01_esquema.sql   Creación de la base de datos y las 10 tablas
└── 02_datos.sql     Datos de demostración
```

## Cómo ejecutar

1. **Base de datos:** en MySQL Workbench ejecute `database/01_esquema.sql` y luego `database/02_datos.sql`. Para reiniciar desde cero, quite el `--` de la línea `DROP DATABASE` de `01_esquema.sql`.
2. **Credenciales locales:** copie `src/main/resources/application-local.example.properties` como `application-local.properties` en la misma carpeta y escriba su usuario y contraseña de MySQL. Ese archivo no se sube a GitHub.
3. **Firebase Storage:** conserve el archivo de servicio en `src/main/resources/Firebase/tecnopedidos-735e7-fi.json`. `FirebaseConfig` lo carga desde el classpath. El servicio busca el bucket predeterminado del proyecto (`.firebasestorage.app` o `.appspot.com`). Si usa un bucket personalizado, defina `FIREBASE_STORAGE_BUCKET` con el nombre exacto mostrado en Firebase, sin el prefijo `gs://`. En PowerShell: `$env:FIREBASE_STORAGE_BUCKET = "nombre-del-bucket"`.
4. **Ejecutar:** abra el proyecto en NetBeans y presione *Run*, o use `mvn spring-boot:run`.
5. Abra http://localhost:8080 e inicie sesión. Al abrir **Inventario** (`/productos`) por primera vez, se cargan en Firebase Storage las ilustraciones del catálogo de demostración.

La cuenta de servicio debe tener permisos para consultar el bucket y leer y escribir objetos. El archivo JSON contiene una llave privada; no la pegues en el README, en el código ni en mensajes o capturas.

### Imágenes de productos

- Los productos de demostración tienen una ilustración SVG correspondiente a su tipo de producto. Se copia a Firebase Storage al consultar el inventario y la interfaz muestra la imagen desde Firebase.
- Al crear o editar un producto, el formulario permite cargar una imagen propia JPG, PNG o WebP de hasta 5 MB. Al dejar el campo vacío, se muestra una ilustración de catálogo según la categoría.
- La ruta del objeto de Firebase se guarda en `producto.imagen_path`; Hibernate actualiza la tabla existente con `spring.jpa.hibernate.ddl-auto=update`.

### Usuarios de prueba (datos ficticios)

Contraseña de todos: **Demo2026***

| Rol | Correo | Empresa |
|---|---|---|
| Administrador | admin@tecnoplus.cr | TecnoPlus S.A. |
| Vendedor | vendedor@tecnoplus.cr | TecnoPlus S.A. |
| Técnico | tecnico@tecnoplus.cr | TecnoPlus S.A. |
| Técnico | tecnico2@tecnoplus.cr | TecnoPlus S.A. |
| Administrador | admin@compurepuestos.cr | CompuRepuestos del Sur (para comprobar el aislamiento) |

## Roles y permisos

| Función | Administrador | Vendedor | Técnico |
|---|:-:|:-:|:-:|
| Inicio con indicadores | ✔ | ✔ | — (va a *Mis pedidos*) |
| Consultar inventario y bajo stock | ✔ | ✔ | — |
| Productos, categorías y movimientos | ✔ | — | — |
| Clientes | ✔ | ✔ | — |
| Registrar pedidos, marcar listo, entregar | ✔ | ✔ | — |
| Asignar técnico y cancelar pedidos | ✔ | — | — |
| Bandeja y avances técnicos | — | — | ✔ (solo sus pedidos) |

Los permisos se aplican en el menú, en las vistas y en el servidor (`SeguridadConfig`). Un acceso no permitido muestra la página *Acceso denegado*.

## Flujo de un pedido

```
Vendedor: carrito → Confirmar ──► PENDIENTE  (descuenta stock: SALIDA_PEDIDO)
   │
   ├─ Sin asistencia:  Vendedor "Marcar como listo" ──► LISTO
   │
   └─ Con asistencia:  Admin asigna técnico ──► ASIGNADO
                       Técnico registra avance ──► EN_PROCESO ──► LISTO
LISTO ── Vendedor registra la entrega ──► ENTREGADO   (no vuelve a descontar stock)
Cualquier estado abierto ── Admin cancela ──► CANCELADO (devuelve stock: DEVOLUCION_CANCELACION)
```

- El pedido se registra en **una sola transacción**: pedido, detalles, salidas de inventario e historial. Si un producto no tiene stock suficiente, no se guarda nada.
- Los precios se calculan en el servidor y cada detalle conserva el **precio histórico**.
- `Producto` usa **bloqueo optimista** (`@Version`): si dos usuarios descuentan las mismas existencias a la vez, el segundo recibe un aviso en lugar de sobrescribir el stock.

## Modelo de datos

| Tabla | Descripción |
|---|---|
| `empresa` | Organizaciones que usan la plataforma |
| `rol` | ADMIN, VENDEDOR, TECNICO |
| `usuario` | Cuentas internas por empresa (contraseña BCrypt) |
| `categoria` | Categorías del catálogo |
| `producto` | Catálogo e inventario, incluida la ruta de imagen en Firebase Storage |
| `movimiento_inventario` | Entradas, ajustes, salidas y devoluciones (trazabilidad del stock) |
| `cliente` | Clientes de cada empresa |
| `pedido` | Pedidos (tabla transaccional) |
| `detalle_pedido` | Líneas del pedido con el precio histórico |
| `seguimiento_pedido` | Historial de estados, comentarios públicos y observaciones internas |

## Estado del avance

**Prioridad alta (14 de 14 implementadas)**

| Historia | Descripción | Estado |
|---|---|---|
| HU-01 | Iniciar sesión | ✔ Implementada |
| HU-02 | Aislar la información por empresa | ✔ Implementada |
| HU-03 | Registrar clientes | ✔ Implementada |
| HU-04 | Registrar productos | ✔ Implementada |
| HU-05 | Movimientos de inventario | ✔ Implementada |
| HU-06 | Registrar un pedido | ✔ Implementada |
| HU-07 | Preparar el detalle y calcular el total | ✔ Implementada |
| HU-08 | Asignar o reasignar un técnico | ✔ Implementada |
| HU-09 | Registrar avance técnico | ✔ Implementada |
| HU-10 | Consultar productos con bajo stock | ✔ Implementada |
| HU-21 | Mantener categorías | ✔ Implementada |
| HU-24 | Cancelar pedidos y devolver existencias | ✔ Implementada |
| HU-25 | Consultar mis pedidos asignados | ✔ Implementada |
| HU-26 | Preparar y registrar la entrega | ✔ Implementada |

**Prioridad media y baja**

| Historia | Estado |
|---|---|
| HU-15 Buscar productos, HU-23 Actualizar productos | ✔ Implementadas |
| HU-16 Cambiar idioma | ✔ Implementada: menú, títulos, etiquetas, botones y estados. Algunos textos secundarios (títulos de modales y ayudas) siguen solo en español |
| HU-18 Indicadores operativos | ✔ Implementada en Inicio |
| HU-22 Consultar y actualizar clientes | Parcial: búsqueda y edición; falta la desactivación |
| HU-14 Filtrar historial de pedidos | Parcial: filtro por estado |
| HU-11 Usuarios y roles (Administración) | Pendiente |
| HU-12 Datos de la empresa | Pendiente |
| HU-13 Seguimiento público con código | Pendiente |
| HU-17 Notificaciones por correo (Spring Mail) | Pendiente |
| HU-19 Exportar inventario CSV | Pendiente |
| HU-20 Imágenes de productos (Firebase) | ✔ Implementada: imágenes del catálogo y carga de imágenes JPG, PNG y WebP |

## Navegación (coherente con el prototipo)

| Pantalla del prototipo | Ruta |
|---|---|
| P01 Inicio de sesión | `/login` |
| P02 Inicio operativo | `/` |
| P03 Catálogo e inventario | `/productos` (Categorías y Movimientos se abren desde aquí) |
| P04 Clientes | `/clientes` |
| P05 Nuevo pedido | `/pedidos/nuevo` |
| P06 Pedidos | `/pedidos` y `/pedidos/{id}` |
| P07 Mis pedidos (técnico) | `/tecnico/pedidos` |
| P08 Productos con bajo stock | `/inventario/bajo-stock` |
| P09 Administración | Pendiente (HU-11 y HU-12) |
| P10 Seguimiento público | Pendiente (HU-13) |

**Prioridad del reporte de bajo stock:** *Crítico* si el producto está agotado o tiene la mitad del mínimo o menos; *Advertencia* si está en el mínimo o por debajo, pero sobre la mitad.

## Flujo de trabajo en GitHub

1. `main` contiene siempre una versión estable.
2. Cada integrante trabaja en su rama `feature/...` creada desde `main`.
3. Al terminar una historia, se abre un **pull request** hacia `main`.
4. Otro integrante revisa el código y lo prueba antes de aprobarlo. Nadie aprueba su propio PR.
5. Antes de empezar a trabajar, cada integrante actualiza su rama con `git pull origin main`.

## Notas para el desarrollo

- **Empresa y usuario actuales:** use siempre `EmpresaActualService`; nunca tome la empresa de un formulario.
- **Stock:** no modifique `producto.stock` directamente. Use `MovimientoInventarioService.aplicar(...)` dentro de la transacción de la operación.
- **Historial del pedido:** cada cambio de estado se registra con `PedidoService.registrarSeguimiento(...)`.
- **Vistas:** use los fragmentos de `layout/plantilla.html`. El menú lateral muestra cada opción según el rol (`esAdmin`, `esVendedor`, `esTecnico`).
