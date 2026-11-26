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
- **ZXing (Google)**: genera el código QR del seguimiento público (tecnología investigada, HU-13)
- **Spring Mail**: aviso por correo al cliente cuando cambia el estado de su pedido (HU-17)
- NetBeans como entorno de desarrollo

## Estructura del proyecto

```
src/main/java/com/tecnopedidos/
├── config/       Seguridad, idioma y Firebase (FirebaseConfig)
├── domain/       Entidades JPA, enumeraciones y carrito del pedido
├── repository/   Repositorios Spring Data (consultas JPQL)
├── service/      Reglas de negocio, transacciones, FirebaseStorageService,
│                 CodigoQrService (QR con ZXing) y CorreoService (Spring Mail)
└── controller/   Controladores MVC (incluye SeguimientoPublicoController)
src/main/resources/
├── templates/
│   ├── layout/plantilla.html   Fragmentos: head, sidebar, topbar, idioma, mensajes,
│   │                           estadoBadge, historial, scripts
│   ├── login.html  inicio.html  acceso-denegado.html  seguimiento-publico.html
│   ├── productos/  categorias/  inventario/  clientes/  pedidos/  tecnico/
├── messages.properties / messages_en.properties
├── Firebase/
│   ├── tecnopedidos-735e7-fi.json   Credencial de servicio (no se sube a GitHub)
│   └── product-images/              Ilustraciones SVG del catálogo de demostración
├── application.properties                 Configuración general (sin contraseñas)
└── application-local.example.properties   Ejemplo de las credenciales locales
database/
├── 01_esquema.sql   Creación de la base de datos y las 10 tablas
└── 02_datos.sql     Datos de demostración
```

## Cómo ejecutar

1. **Base de datos:** en MySQL Workbench ejecute `database/01_esquema.sql` y luego `database/02_datos.sql`. Para reiniciar desde cero, quite el `--` de la línea `DROP DATABASE` de `01_esquema.sql`.
2. **Credenciales locales:** copie `src/main/resources/application-local.example.properties` como `application-local.properties` en la misma carpeta y escriba su usuario y contraseña de MySQL. Ese archivo no se sube a GitHub. `application.properties` no tiene contraseñas: los valores de `application-local.properties` tienen prioridad.
3. **Firebase Storage:** coloque el archivo de servicio en `src/main/resources/Firebase/tecnopedidos-735e7-fi.json` (pídalo al grupo; no se sube a GitHub). `FirebaseConfig` lo carga desde el classpath. El servicio busca el bucket predeterminado del proyecto (`.firebasestorage.app` o `.appspot.com`). Si usa un bucket personalizado, defina `FIREBASE_STORAGE_BUCKET` con el nombre exacto mostrado en Firebase, sin el prefijo `gs://`. En PowerShell: `$env:FIREBASE_STORAGE_BUCKET = "nombre-del-bucket"`.
4. **Correo (opcional):** vea la sección *Aviso por correo*. Si no se configura, el proyecto funciona igual y el aviso solo se escribe en el log.
5. **Ejecutar:** abra el proyecto en NetBeans y presione *Run*, o use `mvn spring-boot:run`.
6. Abra http://localhost:8080 e inicie sesión. Al abrir **Inventario** (`/productos`) por primera vez, se cargan en Firebase Storage las ilustraciones del catálogo de demostración.

La cuenta de servicio debe tener permisos para consultar el bucket y leer y escribir objetos. El archivo JSON contiene una llave privada; no la pegues en el README, en el código ni en mensajes o capturas.

### Imágenes de productos

- Los productos de demostración tienen una ilustración SVG correspondiente a su tipo de producto. Se copia a Firebase Storage al consultar el inventario y la interfaz muestra la imagen desde Firebase.
- Al crear o editar un producto, el formulario permite cargar una imagen propia JPG, PNG o WebP de hasta 5 MB. Al dejar el campo vacío, se muestra una ilustración de catálogo según la categoría.
- La ruta del objeto de Firebase se guarda en `producto.imagen_path`; Hibernate actualiza la tabla existente con `spring.jpa.hibernate.ddl-auto=update`.

### Seguimiento público y código QR (HU-13)

- Cada pedido recibe un **código de seguimiento** único al registrarse (por ejemplo `TP-8F3K2A`). Usa letras y números fáciles de leer, sin O, 0, I, 1 ni L.
- En el detalle del pedido (`/pedidos/{id}`), la tarjeta *Seguimiento para el cliente* muestra el código y un **código QR** generado con ZXing (`CodigoQrService`).
- Al escanear el QR, o al entrar a `/seguimiento-publico` y escribir el código, el cliente ve el estado y el historial público de su pedido **sin iniciar sesión**. No se muestran las observaciones internas.
- La página está disponible en español e inglés.

### Aviso por correo (HU-17)

- Cada vez que el pedido **cambia de estado** (registrado, asignado, en proceso, listo, entregado o cancelado), `CorreoService` envía un correo al cliente con el nuevo estado, el comentario público y el enlace de seguimiento. Un avance técnico que no cambia el estado no envía correo.
- Si el cliente no tiene correo registrado, no se envía nada y queda anotado en el log.
- Si el envío falla, el cambio de estado se guarda igual; el error solo se anota en el log.
- En `application.properties` el envío está **apagado** (`app.correo.activo=false`): el aviso se escribe en el log en lugar de enviarse.

Para activarlo en su computadora con Gmail:

1. En su cuenta de Google active la **verificación en dos pasos** y cree una **contraseña de aplicación** (Cuenta de Google → Seguridad → Contraseñas de aplicaciones).
2. En su `application-local.properties` agregue:
   ```
   spring.mail.username=su_correo@gmail.com
   spring.mail.password=la_contrasena_de_aplicacion
   app.correo.activo=true
   ```
3. Reinicie el proyecto. Para probar, registre un cliente con un correo real y cambie el estado de uno de sus pedidos.

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
| Seguimiento público con código | Cualquier persona, sin iniciar sesión |||

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

En cada cambio de estado se avisa al cliente por correo (HU-17).
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
| `pedido` | Pedidos (tabla transaccional), con su código de seguimiento público |
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
| HU-13 Seguimiento público con código | ✔ Implementada: código único, página pública P10 y código QR (ZXing) |
| HU-17 Notificaciones por correo (Spring Mail) | ✔ Implementada: aviso al cliente en cada cambio de estado |
| HU-20 Imágenes de productos (Firebase) | ✔ Implementada: imágenes del catálogo y carga de imágenes JPG, PNG y WebP |
| HU-22 Consultar y actualizar clientes | Parcial: búsqueda y edición; falta la desactivación |
| HU-14 Filtrar historial de pedidos | Parcial: filtro por estado |
| HU-11 Usuarios y roles (Administración) | Pendiente |
| HU-12 Datos de la empresa | Pendiente |
| HU-19 Exportar inventario CSV | Pendiente |

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
| P10 Seguimiento público | `/seguimiento-publico` o `/seguimiento-publico?codigo=TP-XXXXXX` (sin iniciar sesión) |

**Prioridad del reporte de bajo stock:** *Crítico* si el producto está agotado o tiene la mitad del mínimo o menos; *Advertencia* si está en el mínimo o por debajo, pero sobre la mitad.

## Flujo de trabajo en GitHub

1. `main` contiene siempre una versión estable.
2. Cada integrante trabaja en su rama `feature/...` creada desde `main`.
3. Al terminar una historia, se abre un **pull request** hacia `main`.
4. Otro integrante revisa el código y lo prueba antes de aprobarlo. Nadie aprueba su propio PR.
5. Antes de empezar a trabajar, cada integrante actualiza su rama con `git pull origin main`.
6. **Nunca suba contraseñas ni llaves.** `application-local.properties` y el JSON de Firebase están en `.gitignore`; revise con `git status` antes de cada commit.

## Notas para el desarrollo

- **Empresa y usuario actuales:** use siempre `EmpresaActualService`; nunca tome la empresa de un formulario.
- **Stock:** no modifique `producto.stock` directamente. Use `MovimientoInventarioService.aplicar(...)` dentro de la transacción de la operación.
- **Historial del pedido:** cada cambio de estado se registra con `PedidoService.registrarSeguimiento(...)`. Ese mismo método llama a `CorreoService` para avisar al cliente, así que no hace falta enviar el correo en otro lugar.
- **Vistas:** use los fragmentos de `layout/plantilla.html`. El menú lateral muestra cada opción según el rol (`esAdmin`, `esVendedor`, `esTecnico`).
