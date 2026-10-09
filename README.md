# Documentación funcional del prototipo TecnoPedidos

**Curso:** SC-403 — Desarrollo de Aplicaciones y Patrones  
**Grupo:** 4  
**Tipo de documento:** Análisis funcional y documentación de interfaces  
**Versión:** 1.0  
**Fecha:** 9 de octubre de 2026  
**Estado:** Documentación del prototipo navegable actual

---

## 1. Presentación

### 1.1 Nombre y propósito

**TecnoPedidos** es una propuesta de aplicación web para pequeñas y medianas empresas que venden accesorios, componentes y repuestos de computación y que también brindan asistencia técnica.

Su propósito es centralizar, en una misma solución, la consulta de inventario, el registro de clientes, la creación y seguimiento de pedidos, la asignación de trabajo técnico y la consulta pública del avance de un pedido.

### 1.2 Alcance del prototipo

El prototipo actual representa:

- Acceso de demostración para tres perfiles internos.
- Consulta pública para un cliente externo.
- Tablero operativo con indicadores y accesos rápidos.
- Catálogo e inventario con datos de ejemplo.
- Registro simulado de clientes.
- Creación guiada de un pedido en cuatro pasos.
- Listado y detalle de pedidos.
- Asignación simulada de técnicos, entrega y cancelación.
- Bandeja de trabajo técnico y actualización simulada de estados.
- Reporte de productos con bajo stock.
- Administración visual de usuarios y datos de la empresa.
- Seguimiento público de un pedido.

El prototipo es una implementación frontend en React con datos locales. No demuestra autenticación real, persistencia, autorización en servidor, envío de notificaciones, generación de archivos ni transacciones de inventario.

### 1.3 Implementación académica prevista

La implementación académica está prevista con:

- **Java** como lenguaje de programación.
- **Spring Boot** para la aplicación, reglas de negocio y servicios.
- **Thymeleaf** para generar las vistas del lado del servidor.
- **Bootstrap** para la presentación y los componentes visuales.
- **Base de datos relacional** para persistir usuarios, empresas, clientes, productos, existencias, pedidos y seguimientos.
- **NetBeans** como entorno de desarrollo.

Estas tecnologías corresponden a la solución prevista. **No se debe interpretar que el prototipo actual ya dispone de ese backend.**

### 1.4 Roles identificados

| Rol | Funciones representadas |
| --- | --- |
| Administrador | Consulta el tablero, inventario, clientes y pedidos; abre el registro de productos; asigna técnicos; simula cancelaciones; consulta bajo stock; administra visualmente usuarios y datos de empresa. |
| Vendedor | Consulta el tablero, inventario, clientes, pedidos y bajo stock; registra clientes; inicia y confirma pedidos. |
| Técnico | Consulta su bandeja, abre un pedido asignado y registra avances con comentario público. |
| Cliente externo | Consulta el avance público de un pedido mediante un código, sin entrar al área interna. |

### 1.5 Convención de identificadores

Se conservan los identificadores **P01–P10** usados para organizar el alcance del prototipo. Los paneles, pestañas y modales se documentan como estados o componentes de su pantalla principal, no como pantallas nuevas.

### 1.6 Criterio para el estado de una función

| Estado | Significado en este documento |
| --- | --- |
| Diseñada visualmente | El control o contenido se ve, pero no se comprobó una acción asociada. |
| Interactiva dentro del prototipo | La acción cambia de pantalla o modifica un estado temporal de la interfaz. |
| Simulada con datos de ejemplo | Usa información fija o cambia únicamente el estado local del navegador. |
| Pendiente de implementación | Requiere lógica, persistencia, integración o seguridad no presentes. |
| No verificable | La información disponible no permite confirmar su comportamiento. |

---

## 2. Inventario de pantallas

| ID | Nombre de pantalla | Rol que la utiliza | Objetivo | Pantallas relacionadas |
| --- | --- | --- | --- | --- |
| P01 | Inicio de sesión | Administrador, Vendedor, Técnico y acceso hacia Cliente externo | Representar el ingreso y ofrecer accesos de demostración. | P02, P07, P10 |
| P02 | Inicio operativo | Administrador y Vendedor | Resumir la operación y facilitar accesos frecuentes. | P03, P04, P05, P06, P08 |
| P03 | Catálogo e inventario | Administrador y Vendedor | Consultar productos, precios, existencias y disponibilidad. | P02, P05, P08 |
| P04 | Clientes | Administrador y Vendedor | Consultar y registrar clientes. | P02, P05 |
| P05 | Nuevo pedido | Administrador y Vendedor | Guiar el registro de una venta en cuatro pasos. | P02, P04, P06 |
| P06 | Pedidos | Administrador y Vendedor | Consultar pedidos, abrir su detalle y representar acciones de gestión. | P02, P05, P07, P10 |
| P07 | Mis pedidos | Técnico | Consultar asignaciones y registrar avances técnicos. | P01, P06, P10 |
| P08 | Productos con bajo stock | Administrador y Vendedor | Identificar faltantes y prioridades de reposición. | P02, P03 |
| P09 | Administración | Administrador | Consultar usuarios y editar visualmente datos de la empresa. | P01 |
| P10 | Seguimiento público | Cliente externo | Consultar el estado y la información pública de un pedido. | P01, P06, P07 |

---

## 3. Fichas de pantallas

> **Nota sobre las capturas:** no fue posible adjuntar capturas reales porque el servidor supervisado del prototipo no fue accesible desde las herramientas de captura de este entorno. No se generaron imágenes sustitutas. La sección 7 contiene la lista exacta de capturas que deben obtenerse desde el panel de vista previa.

### 3.1 P01 — Inicio de sesión

**Objetivo:** representar el acceso del equipo y ofrecer entrada directa a cada perfil de demostración y al seguimiento público.

**Usuarios autorizados:** Administrador, Vendedor, Técnico y Cliente externo.

**Cómo se llega:** es la pantalla inicial. También aparece al pulsar la tarjeta del usuario para cerrar la sesión o al usar **Acceso de equipo** desde P10.

**Captura requerida:** Figura 1. *Pantalla de inicio de sesión de TecnoPedidos con credenciales y accesos de demostración.*

**Campos visibles**

| Campo | Obligatorio comprobable | Observación |
| --- | --- | --- |
| Correo electrónico | No | Tiene el valor de ejemplo `admin@tecnoplus.cr`. |
| Contraseña | No | Tiene el valor de ejemplo `tecno2025`; se puede mostrar u ocultar. |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Ingresar | Activa una espera breve. | Acceso simulado siempre como Administrador; no valida los campos. | P02 |
| Mostrar contraseña | Alterna el tipo del campo. | Muestra u oculta el texto. | P01 |
| Administrador | Selecciona perfil de demostración. | Inicia una sesión local como Administrador. | P02 |
| Vendedor | Selecciona perfil de demostración. | Inicia una sesión local como Vendedor. | P02 |
| Técnico | Selecciona perfil de demostración. | Inicia una sesión local como Técnico. | P07 |
| Consultar un pedido como cliente | Abre la consulta pública. | Sale del contexto interno. | P10 |
| ¿Olvidaste tu contraseña? | Sin acción comprobable. | No produce cambios. | Pendiente |
| ES | Control visual de idioma. | No cambia el idioma. | Pendiente |

**Validaciones y mensajes:** no hay validación de credenciales, campos vacíos ni mensajes de error. Se muestra el estado temporal **Ingresando…**.

**Estados representados:** formulario con datos, contraseña visible/oculta y carga. No se representan error de acceso, recuperación de contraseña ni bloqueo.

**Historias de usuario:** Por verificar; no se encontró el documento de historias.

**Pendientes:** autenticación real, recuperación de contraseña, sesiones, cierre seguro, validaciones, internacionalización y autorización en servidor.

---

### 3.2 P02 — Inicio operativo

**Objetivo:** resumir indicadores de pedidos e inventario y ofrecer accesos rápidos.

**Usuarios autorizados:** Administrador y Vendedor.

**Cómo se llega:** después del acceso de esos perfiles o desde **Inicio** en la barra lateral.

**Captura requerida:** Figura 2. *Inicio operativo con indicadores, pedidos recientes y accesos rápidos.*

**Campos visibles:** no contiene campos editables.

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Nuevo pedido | Inicia el asistente. | Muestra el paso 1. | P05 |
| Pendientes de entrega | Abre pedidos. | Muestra el listado general, sin aplicar filtro. | P06 |
| Listos para entregar | Abre pedidos. | Muestra el listado general, sin aplicar filtro. | P06 |
| Productos con bajo stock | Abre el reporte. | Muestra productos bajo el mínimo. | P08 |
| Ver todos / pedido reciente | Abre pedidos. | No abre directamente el pedido seleccionado. | P06 |
| Consultar inventario | Abre catálogo. | Muestra productos de ejemplo. | P03 |
| Clientes | Abre clientes. | Muestra el listado. | P04 |

**Validaciones y mensajes:** no aplica.

**Estados representados:** tablero con datos fijos. No se representan carga, tablero vacío ni error.

**Historias de usuario:** Por verificar.

**Pendientes:** indicadores calculados desde base de datos, filtros contextuales, actualización real y fechas dinámicas. La fecha y hora mostradas son fijas.

---

### 3.3 P03 — Catálogo e inventario

**Objetivo:** consultar productos, categorías, precios, stock mínimo, stock actual y disponibilidad.

**Usuarios autorizados:** Administrador y Vendedor. Las acciones de creación y movimiento solo se muestran al Administrador.

**Cómo se llega:** barra lateral **Inventario** o acceso rápido desde P02.

**Capturas requeridas:**

- Figura 3. *Listado de catálogo e inventario con estados de disponibilidad.*
- Figura 4. *Panel lateral para registrar un producto nuevo.*

**Campos visibles**

| Campo | Obligatorio comprobable | Observación |
| --- | --- | --- |
| Buscar por código o nombre | No | No filtra datos. |
| Categoría y estado | No | Controles visuales; no se comprobó filtrado. |
| Código del producto | No | Sin marca de obligatorio ni validación. |
| Nombre del producto | No | Sin marca de obligatorio ni validación. |
| Categoría | No | Solo contiene la opción “Seleccionar”. |
| Precio | No | Sin validación monetaria. |
| Descripción | No | Campo de texto libre. |
| Imagen | No | Área visual; no se comprobó carga de archivo. |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Nuevo producto | Abre panel lateral. | Muestra formulario y aviso de stock cero. | P03, panel |
| Cancelar / × / fondo | Cierra panel. | Descarta visualmente el formulario. | P03 |
| Crear producto | Sin acción comprobable. | No agrega un producto. | Pendiente |
| Categorías | Sin acción comprobable. | No abre mantenimiento. | Pendiente |
| Registrar movimiento | Sin acción comprobable. | No modifica existencias. | Pendiente |
| Menú de cada producto | Control visual. | No despliega opciones. | Pendiente |

**Validaciones y mensajes:** solo se presenta un aviso informativo: el producto iniciará con stock cero y luego requerirá una entrada. No se validan campos, formatos, duplicados ni archivos.

**Estados representados:** listado con datos, productos disponibles y con bajo stock, panel de creación. No hay estado vacío, agotado en los datos actuales, confirmación ni error.

**Historias de usuario:** Por verificar.

**Pendientes:** búsquedas y filtros, CRUD de productos y categorías, movimientos, carga de imagen, validación, persistencia, concurrencia y cálculo transaccional de existencias.

---

### 3.4 P04 — Clientes

**Objetivo:** consultar clientes y representar el registro de uno nuevo.

**Usuarios autorizados:** Administrador y Vendedor.

**Cómo se llega:** barra lateral **Clientes** o acceso rápido desde P02.

**Capturas requeridas:**

- Figura 5. *Listado de clientes activos.*
- Figura 6. *Panel de registro de cliente con campos completos.*
- Figura 7. *Confirmación visual de cliente registrado.*

**Campos visibles**

| Campo | Obligatorio comprobable |
| --- | --- |
| Buscar por nombre o identificación | No |
| Nombre o razón social | Sí, marcado con `*` |
| Identificación | Sí, marcado con `*` |
| Teléfono | Sí, marcado con `*` |
| Correo electrónico | Sí, marcado con `*` |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Nuevo cliente | Abre panel lateral. | Muestra datos de ejemplo precargados. | P04, panel |
| Guardar cliente | Cierra el panel y muestra confirmación. | Tras una espera breve navega al nuevo pedido; no agrega el cliente al listado. | P05 |
| Cancelar / × / fondo | Cierra panel. | No muestra confirmación. | P04 |
| × de confirmación | Cierra el aviso. | Oculta el mensaje. | P04 |
| Menú de cada cliente | Control visual. | No despliega acciones. | Pendiente |

**Validaciones y mensajes:** los cuatro campos están marcados como obligatorios, pero el prototipo permite guardar sin comprobarlos. Mensaje presente: **Cliente registrado correctamente**.

**Estados representados:** listado con datos, formulario y confirmación. No hay listado vacío, errores, duplicados ni detalle de historial.

**Historias de usuario:** Por verificar.

**Pendientes:** validaciones, persistencia, unicidad de identificación, búsqueda, edición, detalle, manejo de errores y confirmación real de guardado.

---

### 3.5 P05 — Nuevo pedido

**Objetivo:** representar el registro de un pedido mediante cuatro pasos.

**Usuarios autorizados:** Administrador y Vendedor.

**Cómo se llega:** **Nuevo pedido** desde P02, después de guardar un cliente en P04 o mediante accesos rápidos. El botón del listado P06 no tiene navegación implementada.

**Capturas requeridas:** Figuras 8 a 11, una para cada paso del asistente.

**Campos y selecciones visibles**

| Paso | Campo o selección | Obligatorio comprobable | Comportamiento |
| --- | --- | --- | --- |
| 1 | Buscar cliente | No | No filtra; Clínica Santa Elena aparece seleccionada de forma fija. |
| 2 | Buscar producto | No | No filtra; muestra un SSD con cantidad 2. |
| 2 | Cantidad | No | Los controles `−`, `+` y eliminar no cambian el producto. |
| 3 | Requiere asistencia | Selección presente | Sí permite alternar entre Sí y No. |
| 4 | Revisión | No aplica | Muestra cliente, producto, asistencia y totales fijos. |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Continuar | Avanza un paso. | No valida selección ni disponibilidad. | Siguiente paso |
| Anterior | Retrocede un paso. | Conserva la selección de asistencia. | Paso anterior |
| Editar | Regresa a la sección elegida. | Permite volver al paso 1, 2 o 3. | Paso indicado |
| Confirmar pedido | Muestra **Confirmando…** y espera. | Regresa al listado; no inserta una fila nueva. | P06 |
| Volver a pedidos | Abandona el asistente. | No pide confirmación por cambios sin guardar. | P06 |

**Validaciones y mensajes:** el texto indica que solo se pueden agregar cantidades disponibles y que los productos se reservan al confirmar, pero esas reglas no se ejecutan. No hay errores ni confirmación final persistente.

**Estados representados:** cuatro pasos, indicador de progreso, asistencia Sí/No, revisión, estado Pendiente y carga de confirmación.

**Historias de usuario:** Por verificar.

**Pendientes:** selección real, cantidades, cálculo dinámico, impuestos configurables, validación de stock, reserva transaccional, persistencia, código de pedido, confirmación y manejo de concurrencia.

---

### 3.6 P06 — Pedidos

**Objetivo:** consultar pedidos y representar acciones sobre su detalle.

**Usuarios autorizados:** Administrador y Vendedor. Algunas acciones se condicionan visualmente por rol y estado.

**Cómo se llega:** barra lateral **Pedidos**, indicadores de P02 o finalización de P05.

**Capturas requeridas:**

- Figura 12. *Listado general de pedidos y estados.*
- Figura 13. *Detalle de un pedido pendiente.*
- Figura 14. *Modal de asignación de técnico.*
- Figura 15. *Modal de cancelación de pedido.*
- Figura 16. *Detalle de pedido listo con acción de entrega.*

**Campos visibles**

| Estado/componente | Campo | Obligatorio comprobable |
| --- | --- | --- |
| Listado | Búsqueda, categoría y estado | No; no filtran |
| Asignación | Técnico disponible | Selección presente, sin validación |
| Asignación | Nota interna | No; se indica opcional |
| Cancelación | Motivo de cancelación | Sí, marcado con `*`, pero no validado |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Fila de pedido | Abre el detalle. | Muestra resumen, comentarios e historial. | P06, detalle |
| ← Pedidos | Cierra el detalle. | Regresa al listado. | P06 |
| Asignar técnico | Abre modal en pedidos Pendientes para Administrador. | Permite escoger técnico. | P06, modal |
| Confirmar asignación | Cambia estado local. | Muestra Asignado y Daniel Mora; no persiste. | P06, detalle |
| Registrar entrega | Cambia estado local de un pedido Listo. | Muestra Entregado; no solicita confirmación. | P06, detalle |
| Cancelar pedido | Abre modal para Administrador cuando no está Entregado. | Presenta advertencia y motivo. | P06, modal |
| Cancelar pedido del modal | Cierra el modal. | No cambia el estado a Cancelado ni devuelve stock. | P06, detalle |
| Nuevo pedido | Sin acción comprobable. | No abre P05 desde este listado. | Pendiente |

**Validaciones y mensajes:** hay avisos sobre el cambio a Asignado y la devolución de existencias, pero la devolución no se ejecuta. El motivo obligatorio no se valida. No hay mensajes de error o confirmación.

**Estados representados:** Pendiente, Asignado, En proceso, Listo y Entregado; detalle; modales de asignación y cancelación. El estado Cancelado no llega a representarse.

**Historias de usuario:** Por verificar.

**Pendientes:** persistencia, filtros, detalle consistente por pedido, cancelación real, devolución transaccional, permisos del servidor, auditoría, confirmaciones y actualización de bandeja técnica.

---

### 3.7 P07 — Mis pedidos

**Objetivo:** mostrar asignaciones del técnico y representar el avance del trabajo.

**Usuario autorizado:** Técnico.

**Cómo se llega:** acceso como Técnico o elemento único **Mis pedidos** de su navegación.

**Capturas requeridas:**

- Figura 17. *Bandeja de pedidos asignados al técnico.*
- Figura 18. *Detalle técnico con comentario público vacío y botón deshabilitado.*
- Figura 19. *Detalle técnico en estado En proceso después de registrar comentario.*

**Campos visibles**

| Campo | Obligatorio comprobable | Comportamiento |
| --- | --- | --- |
| Comentario público | Sí, marcado con `*` | Vacío deshabilita la actualización. |
| Observaciones internas | No | No se conserva al salir. |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Tarjeta de pedido | Abre detalle. | Todas las tarjetas conducen al mismo pedido de ejemplo `TP-2025-0083`. | P07, detalle |
| Iniciar trabajo | Requiere comentario no vacío. | Cambia Asignado a En proceso localmente. | P07, detalle |
| Marcar como listo | Requiere comentario no vacío. | Cambia En proceso a Listo localmente. | P07, detalle |
| ← Mis pedidos | Cierra detalle. | Regresa a tarjetas. | P07 |
| Pestañas Asignados/En proceso/Listos | Solo Asignados está activa. | Las otras no filtran ni cambian de pestaña. | Pendiente |

**Validaciones y mensajes:** ayuda presente: **Escribe un comentario para habilitar la actualización**. No se valida longitud, contenido ni errores de guardado.

**Estados representados:** bandeja con datos, comentario vacío, botón deshabilitado, Asignado, En proceso y Listo. No hay confirmación ni error.

**Historias de usuario:** Por verificar.

**Pendientes:** filtros de pestañas, datos por técnico, detalle correcto por tarjeta, persistencia de comentarios, historial, notificación al cliente, autorización y manejo de errores.

---

### 3.8 P08 — Productos con bajo stock

**Objetivo:** presentar productos cuyo stock actual es menor o igual al mínimo.

**Usuarios autorizados:** Administrador y Vendedor. Exportar CSV solo aparece para Administrador.

**Cómo se llega:** barra lateral **Bajo stock** o indicador de P02.

**Captura requerida:** Figura 20. *Reporte de productos con bajo stock, faltante y prioridad.*

**Campos visibles:** búsqueda, categoría y estado; ninguno está marcado como obligatorio y no filtran.

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Exportar CSV | Control visual para Administrador. | No descarga archivo. | Pendiente |
| Actualizar reporte | Control visual. | No recalcula datos ni hora. | Pendiente |
| Buscar/filtros | Entrada visual. | No cambia el listado. | Pendiente |

**Validaciones y mensajes:** no aplica.

**Estados representados:** productos en prioridad Crítico y Advertencia. No hay estado vacío, carga ni error.

**Historias de usuario:** Por verificar.

**Pendientes:** consulta real, filtros, actualización, exportación, permisos del servidor y criterios confirmados de prioridad.

---

### 3.9 P09 — Administración

**Objetivo:** representar la consulta de usuarios y la edición de datos de la empresa.

**Usuario autorizado:** Administrador.

**Cómo se llega:** opción **Administración** de la barra lateral del Administrador.

**Capturas requeridas:**

- Figura 21. *Pestaña Usuarios y roles.*
- Figura 22. *Pestaña Datos de la empresa.*

**Campos visibles**

| Campo | Obligatorio comprobable | Observación |
| --- | --- | --- |
| Nombre de la empresa | No | Valor de ejemplo editable localmente. |
| Teléfono | No | Sin máscara ni validación. |
| Correo electrónico | No | Sin validación. |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Usuarios y roles | Cambia pestaña. | Muestra tabla de usuarios. | P09 |
| Datos de la empresa | Cambia pestaña. | Muestra formulario. | P09 |
| Nuevo usuario | Sin acción comprobable. | No abre formulario. | Pendiente |
| Menú de usuario | Control visual. | No presenta acciones. | Pendiente |
| Guardar cambios | Sin acción comprobable. | No confirma ni persiste. | Pendiente |

**Validaciones y mensajes:** no hay validaciones ni mensajes.

**Estados representados:** tabla con usuarios activos y formulario con datos. No hay usuarios inactivos, vacío, confirmación ni error.

**Historias de usuario:** Por verificar.

**Pendientes:** CRUD de usuarios, roles y permisos, validaciones, persistencia, separación por empresa, auditoría y controles de seguridad.

---

### 3.10 P10 — Seguimiento público

**Objetivo:** permitir que un cliente consulte información pública de un pedido sin entrar al área interna.

**Usuario autorizado:** Cliente externo.

**Cómo se llega:** enlace **Consultar un pedido como cliente** desde P01.

**Capturas requeridas:**

- Figura 23. *Consulta pública antes de mostrar resultados.*
- Figura 24. *Resultado público con progreso y último comentario.*

**Campos visibles**

| Campo | Obligatorio comprobable | Observación |
| --- | --- | --- |
| Código de pedido | No | Precargado con `TP-2025-0083`; no se valida. |

**Botones y enlaces**

| Elemento | Acción | Resultado | Destino |
| --- | --- | --- | --- |
| Consultar | Muestra resultado fijo. | Cualquier contenido presenta el mismo pedido de ejemplo. | P10, resultado |
| Acceso de equipo | Regresa al acceso interno. | Cierra la vista pública. | P01 |
| ES | Control visual. | No cambia idioma. | Pendiente |

**Validaciones y mensajes:** se muestra el aviso de privacidad del código. No hay validación de código vacío, inexistente o formato inválido, ni mensajes de error.

**Estados representados:** consulta inicial y resultado con estado En proceso. No hay pedido no encontrado, error, carga ni otros estados de pedido.

**Historias de usuario:** Por verificar.

**Pendientes:** consulta segura al backend, códigos no predecibles, validaciones, protección de datos, límites de intentos, resultados reales e internacionalización.

---

## 4. Flujos principales

### 4.1 Acceso interno

**Rol → punto de entrada → pasos → resultado**

1. Administrador o Vendedor → P01 → seleccionar acceso de demostración o pulsar **Ingresar** → P02.
2. Técnico → P01 → seleccionar **Técnico** → P07.

**Interrupciones o conexiones faltantes:**

- Las credenciales no se validan.
- **Ingresar** siempre selecciona Administrador.
- No hay recuperación de contraseña, cierre de sesión de servidor ni control de sesiones.
- Los roles son estados locales, no permisos comprobados en servidor.

### 4.2 Gestión de clientes

**Administrador/Vendedor → P04 → Nuevo cliente → completar formulario → Guardar cliente → confirmación temporal → P05.**

**Resultado:** se representa un cliente registrado y se inicia un pedido.

**Faltantes:** el cliente no se agrega al listado ni se persiste; los campos obligatorios no se validan; no se representan edición, detalle ni duplicados.

### 4.3 Consulta y gestión de inventario

**Administrador/Vendedor → P03 → consultar tabla y estados.**

Ruta adicional del Administrador: **P03 → Nuevo producto → panel de registro**.

**Resultado:** consulta visual de datos de ejemplo.

**Faltantes:** búsqueda, filtros, creación, categorías y movimientos no ejecutan operaciones. No hay actualización real de stock.

### 4.4 Creación de pedidos

**Administrador/Vendedor → P02 o P04 → P05 → seleccionar cliente → revisar producto y cantidad → elegir asistencia → revisar → confirmar → P06.**

**Resultado:** se simula una confirmación y se vuelve al listado.

**Faltantes:** las selecciones de cliente y producto son fijas; cantidades no cambian; no se agrega el pedido al listado; no se reserva inventario; no hay transacción ni confirmación persistente.

### 4.5 Gestión de pedidos y asignación técnica

**Administrador → P06 → abrir pedido Pendiente → Asignar técnico → confirmar → estado Asignado.**

**Resultado:** cambia temporalmente el estado y el nombre del técnico en el detalle.

**Faltantes:** no se comprueba que el pedido llegue realmente a la bandeja del técnico; no hay persistencia, notificación ni control de concurrencia.

### 4.6 Entrega o cancelación

**Administrador/Vendedor → P06 → abrir pedido Listo → Registrar entrega → estado Entregado.**

**Administrador → P06 → abrir pedido no entregado → Cancelar pedido → escribir motivo → Cancelar pedido.**

**Resultado:** la entrega cambia temporalmente el estado. La cancelación solo cierra el modal.

**Faltantes:** motivo no validado, cancelación no aplicada, existencias no devueltas y ausencia de confirmaciones o auditoría.

### 4.7 Trabajo técnico

**Técnico → P07 → abrir tarjeta → escribir comentario público → Iniciar trabajo → En proceso → Marcar como listo → Listo.**

**Resultado:** transición local de estado con comentario obligatorio para habilitar el botón.

**Faltantes:** todas las tarjetas abren el mismo pedido; no se persisten comentario u observación; las pestañas no filtran; no se conecta con P06 o P10 en tiempo real.

### 4.8 Seguimiento del cliente

**Cliente externo → P01 → Consultar un pedido como cliente → P10 → introducir código → Consultar → resultado.**

**Resultado:** se muestra el avance fijo de `TP-2025-0083`.

**Faltantes:** cualquier código produce el mismo resultado; no hay error de pedido inexistente; el avance técnico no actualiza este resultado.

---

## 5. Trazabilidad preliminar

No se encontró un documento de historias de usuario en el repositorio. Por esa razón no se inventan identificadores, descripciones ni criterios de aceptación. Se requiere el contenido oficial de las historias para completar la correspondencia.

| Historia de usuario | Pantalla | Acción representada | Evidencia | Pendiente |
| --- | --- | --- | --- | --- |
| Por verificar | P01 | Acceso por perfil y entrada pública | Botones de demostración y cambio de vista | Vincular con historia y criterios oficiales |
| Por verificar | P02 | Consulta de indicadores | Tarjetas y accesos rápidos | Confirmar indicadores requeridos |
| Por verificar | P03 | Consulta y alta visual de productos | Tabla y panel Nuevo producto | Confirmar reglas de catálogo e inventario |
| Por verificar | P04 | Registro de cliente | Panel, campos obligatorios y aviso de éxito | Confirmar validaciones y duplicados |
| Por verificar | P05 | Creación de pedido | Asistente de cuatro pasos | Confirmar cálculo, stock y aceptación |
| Por verificar | P06 | Gestión de pedidos | Listado, detalle, asignación, entrega y cancelación | Confirmar transiciones y permisos |
| Por verificar | P07 | Actualización técnica | Comentario obligatorio y cambios de estado | Confirmar historial y notificaciones |
| Por verificar | P08 | Consulta de bajo stock | Reporte y prioridades | Confirmar fórmula y exportación |
| Por verificar | P09 | Administración | Pestañas de usuarios y empresa | Confirmar alcance de roles y multiempresa |
| Por verificar | P10 | Seguimiento público | Código, progreso y comentario público | Confirmar seguridad y datos visibles |

**Solicitud:** facilitar el documento de historias de usuario para completar identificadores, criterios de aceptación y trazabilidad bidireccional.

---

## 6. Estado consolidado de las funciones

| Función | Diseñada | Interactiva | Simulada con ejemplos | Pendiente de implementación | No verificable |
| --- | :---: | :---: | :---: | :---: | :---: |
| Acceso y selección de rol | Sí | Sí | Sí | Autenticación y sesiones | — |
| Navegación por rol | Sí | Sí | Sí | Permisos de servidor | — |
| Tablero e indicadores | Sí | Sí | Sí | Cálculos y actualización real | — |
| Búsqueda y filtros | Sí | No | — | Sí | — |
| Consulta de inventario | Sí | Sí | Sí | Persistencia y consulta de BD | — |
| Alta de producto | Sí | Panel solamente | Sí | Guardado y validación | — |
| Movimientos de inventario | Sí | No | — | Sí | Reglas académicas exactas |
| Consulta de clientes | Sí | Sí | Sí | Persistencia | — |
| Registro de cliente | Sí | Sí | Sí | Validación y guardado real | — |
| Creación de pedido | Sí | Sí | Sí | Transacción y persistencia | — |
| Cálculo de totales | Sí | No dinámico | Sí | Reglas de cálculo | Política fiscal final |
| Listado y detalle de pedidos | Sí | Sí | Sí | Consulta persistente | — |
| Asignación de técnico | Sí | Sí | Sí | Persistencia y notificación | — |
| Entrega de pedido | Sí | Sí | Sí | Transacción y auditoría | — |
| Cancelación y devolución de stock | Sí | Modal solamente | No | Sí | Regla final de devolución |
| Avance técnico | Sí | Sí | Sí | Persistencia y notificación | — |
| Bajo stock | Sí | Sí | Sí | Consulta y exportación | Criterio final de prioridad |
| Administración de usuarios | Sí | Pestaña solamente | Sí | CRUD, seguridad y persistencia | Matriz de permisos |
| Datos de empresa | Sí | Edición local | Sí | Validación y guardado | Reglas multiempresa |
| Seguimiento público | Sí | Sí | Sí | Consulta segura y errores | Política de privacidad |
| Java/Spring Boot/Thymeleaf/Bootstrap/BD | Prevista | No | No | Implementación académica | Arquitectura detallada |

---

## 7. Plan de capturas reales

Las capturas deben obtenerse desde el prototipo actual, preferiblemente con una ventana de escritorio de tamaño constante. No deben recrearse en otra herramienta.

| Figura | Pantalla y estado | Pasos para llegar | Archivo sugerido | Pie de figura |
| --- | --- | --- | --- | --- |
| 1 | P01, estado inicial | Abrir el prototipo o cerrar sesión | `figura-01-inicio-sesion.png` | Pantalla de inicio de sesión con credenciales y accesos de demostración. |
| 2 | P02, Administrador | P01 → Administrador | `figura-02-inicio-operativo.png` | Inicio operativo con indicadores, pedidos recientes y accesos rápidos. |
| 3 | P03, listado | Administrador → Inventario | `figura-03-inventario-listado.png` | Catálogo con existencias, mínimos y estados. |
| 4 | P03, nuevo producto | P03 → Nuevo producto | `figura-04-inventario-nuevo-producto.png` | Panel lateral para registrar un producto. |
| 5 | P04, listado | Administrador → Clientes | `figura-05-clientes-listado.png` | Listado de clientes activos. |
| 6 | P04, formulario | P04 → Nuevo cliente | `figura-06-clientes-registro.png` | Panel de registro de cliente con campos obligatorios. |
| 7 | P04, confirmación | P04 → Nuevo cliente → Guardar; capturar de inmediato | `figura-07-clientes-confirmacion.png` | Mensaje de registro correcto antes de abrir el pedido. |
| 8 | P05, paso 1 | P02 → Nuevo pedido | `figura-08-pedido-paso-1.png` | Paso 1: selección del cliente. |
| 9 | P05, paso 2 | Paso 1 → Continuar | `figura-09-pedido-paso-2.png` | Paso 2: producto y cantidad de ejemplo. |
| 10 | P05, paso 3 | Paso 2 → Continuar | `figura-10-pedido-paso-3.png` | Paso 3: selección de asistencia técnica. |
| 11 | P05, paso 4 | Paso 3 → Continuar | `figura-11-pedido-paso-4.png` | Paso 4: revisión, impuestos y total. |
| 12 | P06, listado | Barra lateral → Pedidos | `figura-12-pedidos-listado.png` | Listado general de pedidos y estados. |
| 13 | P06, detalle Pendiente | P06 → `TP-2025-0084` | `figura-13-pedido-detalle.png` | Detalle de pedido pendiente con resumen e historial. |
| 14 | P06, asignación | Figura 13 → Asignar técnico | `figura-14-pedido-asignar.png` | Modal de asignación de técnico. |
| 15 | P06, cancelación | Cerrar modal → Cancelar pedido | `figura-15-pedido-cancelar.png` | Modal de cancelación y advertencia de inventario. |
| 16 | P06, pedido Listo | Volver → `TP-2025-0079` | `figura-16-pedido-listo.png` | Detalle de pedido listo con opción de registrar entrega. |
| 17 | P07, bandeja | Cerrar sesión → Técnico | `figura-17-tecnico-bandeja.png` | Bandeja de pedidos asignados al técnico. |
| 18 | P07, validación | Abrir primera tarjeta sin escribir comentario | `figura-18-tecnico-comentario-requerido.png` | Comentario público vacío y actualización deshabilitada. |
| 19 | P07, En proceso | Escribir comentario → Iniciar trabajo | `figura-19-tecnico-en-proceso.png` | Pedido técnico después de iniciar el trabajo. |
| 20 | P08, reporte | Administrador → Bajo stock | `figura-20-bajo-stock.png` | Reporte de productos con faltante y prioridad. |
| 21 | P09, usuarios | Administrador → Administración | `figura-21-administracion-usuarios.png` | Pestaña de usuarios y roles. |
| 22 | P09, empresa | P09 → Datos de la empresa | `figura-22-administracion-empresa.png` | Formulario de datos de la empresa. |
| 23 | P10, consulta inicial | Cerrar sesión → Consultar un pedido como cliente | `figura-23-seguimiento-consulta.png` | Consulta pública antes de mostrar resultados. |
| 24 | P10, resultado | P10 → Consultar | `figura-24-seguimiento-resultado.png` | Progreso y último comentario público del pedido. |

---

## 8. Inconsistencias y elementos faltantes observados

1. El botón **Ingresar** no usa las credenciales y siempre abre el perfil Administrador.
2. Los campos de acceso no están marcados ni validados como obligatorios.
3. Las fechas, horas, cantidades e indicadores son fijos.
4. Las tarjetas de indicadores abren listados sin aplicar el filtro anunciado.
5. Las búsquedas y filtros visibles no modifican las tablas.
6. **Crear producto**, **Categorías** y **Registrar movimiento** no ejecutan acciones.
7. El formulario de producto no identifica campos obligatorios ni guarda información.
8. El registro de cliente marca campos obligatorios, pero no los valida ni agrega el cliente al listado.
9. El registro de cliente navega automáticamente a P05 después de un tiempo breve, lo que dificulta conservar la captura de confirmación.
10. El asistente de pedido permite avanzar sin validar cliente, producto o cantidad.
11. Los controles de cantidad y eliminación del producto no funcionan.
12. Los totales del pedido son fijos y no responden a cambios.
13. Confirmar un pedido no agrega un nuevo registro al listado.
14. El botón **Nuevo pedido** de P06 no abre el asistente.
15. Los datos del detalle, como correo y producto, se reutilizan aunque cambie el pedido seleccionado.
16. La asignación de técnico cambia solo el estado local y no demuestra conexión con P07.
17. El motivo de cancelación aparece como obligatorio, pero no se valida.
18. Confirmar la cancelación solo cierra el modal; no cambia el estado ni devuelve existencias.
19. Registrar entrega no solicita confirmación ni demuestra una transacción.
20. Todas las tarjetas técnicas abren el mismo pedido de ejemplo.
21. Las pestañas técnicas **En proceso** y **Listos** no cambian el contenido.
22. Los comentarios y observaciones técnicas no se persisten ni actualizan P06 o P10.
23. **Exportar CSV** y **Actualizar reporte** no ejecutan acciones.
24. En P09 se anuncia un total de seis usuarios, pero la tabla muestra cuatro.
25. **Nuevo usuario**, menús de usuario y **Guardar cambios** no ejecutan acciones.
26. P10 muestra siempre el mismo pedido sin importar el código escrito.
27. No existe estado de pedido no encontrado ni mensajes de error públicos.
28. El selector de idioma no modifica el contenido.
29. No se dispone de historias de usuario para verificar alcance, identificadores y criterios de aceptación.
30. No se dispone de evidencia de autenticación, persistencia, base de datos, permisos de servidor, transacciones, notificaciones ni auditoría.
31. La arquitectura prevista con Java, Spring Boot, Thymeleaf, Bootstrap y base de datos relacional todavía debe diseñarse e implementarse; no forma parte del backend del prototipo actual.
