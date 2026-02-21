# 📄 Especificación Inicial - Asecontin

## 🎯 Objetivo
Desarrollar una plataforma web (Asecontin) que permita a los usuarios **buscar, filtrar y visualizar inmuebles disponibles para compra o renta**, con un **panel administrativo privado** para gestionar los inmuebles.

---

## 🧩 Alcance Funcional

- Público objetivo: personas interesadas en **comprar o rentar casas y apartamentos**.
- Zona privada para **administrador**:
  - Crear, editar y eliminar inmuebles.
- Funcionalidades para usuarios:
  - Filtros de búsqueda (precio, ubicación, tipo de inmueble, etc.).
  - Buscador avanzado.
  - Visualización de inmuebles con **estado** (disponible, vendido, rentado).
  - Mapa interactivo con ubicación de inmuebles.
  - Contacto directo con agente vía formulario, chat o WhatsApp.
  - Visualización de precios en **COP**.
  - Galería de imágenes por inmueble.
  - Sección para videos de recorridos.
  - Integración con redes sociales.
  - Diseño **responsivo** para móviles y tablets.
  - Etiquetas destacadas para inmuebles (“nuevo”, “rebajado”, “lujo”).
- Idioma inicial: **Español**.
- Roles: **Administrador** (único por ahora).
- Panel administrativo para gestión de inmuebles.
- Blog o sección de noticias inmobiliarias (opcional).

---

## ⚙️ Alcance Técnico

- **Backend** (implementado):
  - Lenguaje: **Java 25**.
  - Framework: **Spring Boot 4** (WebFlux, reactivo).
  - API: **REST** (JSON).
  - Base de datos: **PostgreSQL** vía **R2DBC** (reactivo).
  - Autenticación: **JWT** (para administrador).
  - Documentación automática: **SpringDoc / Swagger OpenAPI 3**.
  - Pruebas unitarias e integración.
  - Caching: habilitado.
  - Despliegue: **Google Cloud Platform (GCP)**.
  - Versionamiento: Git (opcional).
  - Monitoreo: no requerido inicialmente.
  - Migraciones de BD: no necesarias en primera fase.
  - Almacenamiento de imágenes/videos: **Opción A** implementada — archivos en carpeta del servidor (`app.media.upload-dir`), subida por multipart (`POST .../imagenes/upload`, `POST .../videos/upload`), URLs públicas servidas por el backend (`GET /api/public/medios/imagen/{id}`, `GET /api/public/medios/video/{id}`). También se admite registrar URLs externas por JSON.
  
- **Frontend**:
  - Framework: **React**.
  - Librería de estilos: **Material UI**.
  - Consumo de API REST con Axios o Fetch.
  - Pruebas unitarias: Jest + React Testing Library.
  - Responsivo.
  - Estado global: no requerido inicialmente (se puede evaluar Redux/Zustand si escala).
  - Internacionalización: no requerida en primera fase.
  - SSR: no, será SPA.

---

## 🗄️ Diseño de Base de Datos

### Entidades principales
- **Usuario** (solo administrador por ahora)
  - id_usuario (PK)
  - nombre
  - email
  - contraseña (encriptada)
  - rol

- **Inmueble**
  - id_inmueble (PK)
  - título
  - descripción
  - precio (COP)
  - dirección
  - ciudad
  - tipo (casa, apartamento)
  - estado_id (FK → Estado)
  - etiquetas
  - fecha_creación
  - fecha_actualización

- **Estado**
  - id_estado (PK)
  - nombre_estado (ej. disponible, vendido, rentado)

- **Imagen**
  - id_imagen (PK)
  - inmueble_id (FK → Inmueble)
  - url_imagen

- **Video**
  - id_video (PK)
  - inmueble_id (FK → Inmueble)
  - url_video

- **Blog/Noticias** (opcional)
  - id_articulo (PK)
  - título
  - contenido
  - fecha_publicación

---

## 🔗 Relaciones

- **Usuario → Inmueble**: solo hay un administrador; gestiona los inmuebles desde el panel (no se guarda en BD quién creó cada inmueble).  
- **Inmueble → Estado**: cada inmueble *tiene* un estado.  
- **Inmueble → Imagen**: un inmueble *tiene muchas* imágenes.  
- **Inmueble → Video**: un inmueble *tiene muchos* videos.  
- **Blog**: independiente, no relacionado directamente con inmuebles (sirve para contenido adicional).  

---

## 🛠️ Script SQL Inicial (PostgreSQL)

Script completo: creación de la base de datos, schema, tablas y datos iniciales (estados, tipos de inmueble, ciudades de Colombia). Ejecutar en orden.

```sql
-- ========== CREAR BASE DE DATOS Y SCHEMA ==========
CREATE DATABASE asecontin_db;
\c asecontin_db;
CREATE SCHEMA imb;

-- ========== TABLAS ==========

-- Usuario (único administrador)
CREATE TABLE imb.usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    contraseña VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Estado del inmueble (disponible, vendido, rentado)
CREATE TABLE imb.estado (
    id_estado SERIAL PRIMARY KEY,
    nombre_estado VARCHAR(50) NOT NULL
);

-- Catálogo de tipos de inmueble
CREATE TABLE imb.tipo_inmueble (
    id_tipo SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Ciudades permitidas (Colombia: capitales de departamentos)
CREATE TABLE imb.ciudad (
    id_ciudad SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- Localidades por ciudad (por ahora principalmente Bogotá; permite expandir a otras ciudades)
CREATE TABLE imb.localidad (
    id_localidad SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ciudad_id INT NOT NULL REFERENCES imb.ciudad(id_ciudad),
    UNIQUE(ciudad_id, nombre)
);

-- Sectores de la ciudad (oriente, occidente, norte, sur, etc.)
CREATE TABLE imb.sector (
    id_sector SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Inmueble (localidad_id referencia imb.localidad; sector_id opcional)
CREATE TABLE imb.inmueble (
    id_inmueble SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(15,2) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    localidad_id INT NOT NULL REFERENCES imb.localidad(id_localidad),
    tipo_id INT NOT NULL REFERENCES imb.tipo_inmueble(id_tipo),
    estado_id INT REFERENCES imb.estado(id_estado),
    etiquetas VARCHAR(100),
    parqueaderos INT NOT NULL DEFAULT 0,
    sector_id INT REFERENCES imb.sector(id_sector),
    area_m2 NUMERIC(10,2),
    habitaciones INT,
    banos INT,
    estrato INT,
    valor_administracion NUMERIC(12,2),
    ano_construccion INT,
    amoblado BOOLEAN NOT NULL DEFAULT false,
    piso INT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Imagen (url_imagen: ruta en disco o URL externa; es_principal: una por inmueble)
CREATE TABLE imb.imagen (
    id_imagen SERIAL PRIMARY KEY,
    inmueble_id INT REFERENCES imb.inmueble(id_inmueble) ON DELETE CASCADE,
    url_imagen VARCHAR(255) NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT false
);

-- Video
CREATE TABLE imb.video (
    id_video SERIAL PRIMARY KEY,
    inmueble_id INT REFERENCES imb.inmueble(id_inmueble) ON DELETE CASCADE,
    url_video VARCHAR(255) NOT NULL
);

-- Blog / Noticias
CREATE TABLE imb.blog (
    id_articulo SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Contenido institucional (Acerca de nosotros: misión, visión, términos, política de datos, etc.)
-- Una sola fila por instalación; se puede ampliar con más columnas si se necesitan más secciones.
CREATE TABLE imb.configuracion_inmobiliaria (
    id_config SERIAL PRIMARY KEY,
    mision TEXT,
    vision TEXT,
    terminos_condiciones TEXT,
    politica_tratamiento_datos TEXT,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== DATOS INICIALES ==========

-- Estados de inmueble
INSERT INTO imb.estado (nombre_estado) VALUES
    ('Disponible'),
    ('Vendido'),
    ('Rentado');

-- Tipos de inmueble
INSERT INTO imb.tipo_inmueble (nombre) VALUES
    ('Casa'),
    ('Apartamento'),
    ('Lote'),
    ('Apartaestudio'),
    ('Local comercial'),
    ('Oficina'),
    ('Finca'),
    ('Bodega');

-- Ciudades (capitales de los 32 departamentos de Colombia)
INSERT INTO imb.ciudad (nombre) VALUES
    ('Leticia'),
    ('Medellín'),
    ('Arauca'),
    ('Barranquilla'),
    ('Cartagena'),
    ('Tunja'),
    ('Manizales'),
    ('Florencia'),
    ('Yopal'),
    ('Popayán'),
    ('Valledupar'),
    ('Quibdó'),
    ('Montería'),
    ('Bogotá'),
    ('Inírida'),
    ('San José del Guaviare'),
    ('Neiva'),
    ('Riohacha'),
    ('Santa Marta'),
    ('Villavicencio'),
    ('Pasto'),
    ('Cúcuta'),
    ('Mocoa'),
    ('Armenia'),
    ('Pereira'),
    ('San Andrés'),
    ('Bucaramanga'),
    ('Sincelejo'),
    ('Ibagué'),
    ('Cali'),
    ('Mitú'),
    ('Puerto Carreño');

-- Localidades de Bogotá (20 localidades oficiales)
INSERT INTO imb.localidad (nombre, ciudad_id)
SELECT n.nombre, c.id_ciudad
FROM (VALUES
    ('Usaquén'), ('Chapinero'), ('Santa Fe'), ('San Cristóbal'), ('Usme'),
    ('Tunjuelito'), ('Bosa'), ('Kennedy'), ('Fontibón'), ('Engativá'),
    ('Suba'), ('Barrios Unidos'), ('Teusaquillo'), ('Los Mártires'), ('Antonio Nariño'),
    ('Puente Aranda'), ('La Candelaria'), ('Rafael Uribe Uribe'), ('Ciudad Bolívar'), ('Sumapaz')
) AS n(nombre)
CROSS JOIN (SELECT id_ciudad FROM imb.ciudad WHERE nombre = 'Bogotá' LIMIT 1) c;

-- Sectores de ciudad
INSERT INTO imb.sector (nombre) VALUES
    ('Oriente'),
    ('Occidente'),
    ('Norte'),
    ('Sur'),
    ('Centro'),
    ('Noroccidente'),
    ('Nororiente'),
    ('Suroriente'),
    ('Suroccidente');

-- Contenido institucional (una sola fila; valores de ejemplo para "Acerca de nosotros")
INSERT INTO imb.configuracion_inmobiliaria (mision, vision, terminos_condiciones, politica_tratamiento_datos, descripcion) VALUES
(
    'Ofrecer soluciones inmobiliarias confiables que conecten a las personas con el hogar o espacio comercial que buscan, con transparencia y acompañamiento profesional.',
    'Ser la inmobiliaria de referencia en la región, reconocida por la calidad del servicio, la variedad de opciones y el trato cercano con nuestros clientes.',
    'Al usar este sitio y nuestros servicios, el usuario acepta los términos aquí publicados. Los precios y disponibilidad pueden cambiar sin previo aviso. Las imágenes y descripciones son orientativas. Para transacciones se aplicarán contratos y condiciones específicas. Contáctenos para más información.',
    'En cumplimiento de la Ley 1581 de 2012 (Colombia), tratamos sus datos personales solo para gestionar consultas, citas y servicios solicitados. No compartimos su información con terceros para fines comerciales no autorizados. Puede ejercer sus derechos de acceso, corrección y supresión contactando a la inmobiliaria.',
    'Somos una inmobiliaria con años de experiencia en la región. Nos especializamos en venta y arriendo de casas, apartamentos y locales. Nuestro equipo está listo para asesorarlo.'
);
```

El script anterior crea la base desde cero e incluye:

- **Tabla `imb.localidad`**: localidades por ciudad (por ahora las 20 de Bogotá). Incluye INSERT; `ciudad_id` referencia `imb.ciudad`. Permite ampliar a otras ciudades después.
- **Tabla `imb.sector`**: creación e INSERT de los sectores (Oriente, Occidente, Norte, Sur, Centro, Noroccidente, Nororiente, Suroriente, Suroccidente). No se usan ALTER para esta tabla.
- **Tabla `imb.inmueble`**: creación con todos los campos desde el inicio (título, descripción, precio, dirección, localidad_id, tipo_id, estado_id, etiquetas, parqueaderos, sector_id, area_m2, habitaciones, banos, estrato, valor_administracion, ano_construccion, amoblado, piso, fecha_creacion, fecha_actualizacion). La columna `localidad_id` referencia `imb.localidad(id_localidad)`. No se usan ALTER para añadir estos campos.
- **Tabla `imb.configuracion_inmobiliaria`**: contenido para la sección "Acerca de nosotros" (misión, visión, términos y condiciones, política de tratamiento de datos, descripción). Una sola fila; incluye INSERT con valores de ejemplo.

**Si la BD ya existe** y fue creada con una versión anterior (sin tabla `sector` ni los campos nuevos de inmueble), puede ejecutar el script de actualización `scripts/sectores-y-campos-inmueble.sql`, que usa ALTER para añadir lo que falte.




# 📑 Plan de Creación - Asecontin

## 🏗️ Fases de Desarrollo

### 1. Configuración Inicial
- **Backend**
  - Crear proyecto base en **Spring Boot (Java 21)**.
  - Configurar conexión a **PostgreSQL**.
  - Integrar **Swagger/OpenAPI** para documentación.
  - Configurar seguridad con **Spring Security + JWT**.
  - Configurar almacenamiento externo (AWS S3 o GCP Storage).
  - Inicializar repositorio en **Git**.

- **Frontend**
  - Crear proyecto base en **React**.
  - Configurar **Material UI**.
  - Definir estructura de carpetas (components, pages, services).
  - Configurar Axios para consumo de API.
  - Configurar Jest + React Testing Library.

---

### 2. Módulos Backend (Spring Boot)

1. **Autenticación y Seguridad**
   - Registro de administrador (único).
   - Login con JWT.
   - Middleware para validar tokens.

2. **Gestión de Usuarios**
   - CRUD de administrador (solo uno en primera fase).
   - Encriptación de contraseñas (BCrypt).

3. **Gestión de Estados**
   - CRUD de estados (disponible, vendido, rentado).

4. **Gestión de Inmuebles**
   - CRUD de inmuebles.
   - Relación con estados.
   - Relación con imágenes y videos.
   - Filtros de búsqueda (precio, ciudad, tipo).

5. **Gestión de Imágenes**
   - Subida de imágenes a almacenamiento externo.
   - Guardar referencia en BD.
   - CRUD de imágenes por inmueble.

6. **Gestión de Videos**
   - Subida de videos a almacenamiento externo.
   - Guardar referencia en BD.
   - CRUD de videos por inmueble.

7. **Módulo de Blog/Noticias (opcional)**
   - CRUD de artículos.
   - Endpoint público para visualización.

8. **API Pública**
   - Endpoints para búsqueda de inmuebles.
   - Endpoints para filtros.
   - Endpoint para contacto con agente.

9. **Pruebas**
   - Unitarias con JUnit.
   - Integración con Spring Test.

---

### 3. Módulos Frontend (React + Material UI)

1. **Autenticación**
   - Pantalla de login para administrador.
   - Manejo de sesión con JWT.

2. **Panel Administrativo**
   - Dashboard con listado de inmuebles.
   - Formularios para crear/editar inmuebles.
   - Gestión de estados.
   - Subida de imágenes y videos.
   - Gestión de artículos del blog (opcional).

3. **Página Pública**
   - Home con buscador y filtros.
   - Listado de inmuebles con tarjetas.
   - Detalle de inmueble:
     - Galería de imágenes.
     - Video de recorrido.
     - Estado del inmueble.
     - Etiquetas destacadas.
   - Mapa interactivo con ubicación.
   - Botón de contacto (formulario, chat, WhatsApp).
   - Integración con redes sociales.

4. **Diseño Responsivo**
   - Adaptación para móviles y tablets.
   - Uso de breakpoints de Material UI.

5. **Pruebas**
   - Unitarias con Jest.
   - Testing de componentes con React Testing Library.

---

### 4. Despliegue
- **Backend**
  - Desplegar en **Google Cloud Platform (GCP)**.
  - Configurar base de datos en GCP.
  - Configurar almacenamiento de imágenes/videos.

- **Frontend**
  - Desplegar en GCP (App Engine o Cloud Run).
  - Configurar dominio y SSL.

---

### 5. Mantenimiento y Escalabilidad
- Monitoreo básico de logs.
- Evaluar integración de **Redux/Zustand** para manejo de estado global.
- Escalabilidad horizontal en GCP.
- Evaluar internacionalización en fases futuras.

---

## ✅ Checklist de Módulos

| Área         | Módulo                        | Estado Inicial |
|--------------|-------------------------------|----------------|
| Backend      | Autenticación JWT             | ✅ |
| Backend      | CRUD Usuarios                 | ✅ |
| Backend      | CRUD Estados                  | ✅ |
| Backend      | CRUD Inmuebles                | ✅ |
| Backend      | CRUD Imágenes                 | ✅ |
| Backend      | CRUD Videos                   | ✅ |
| Backend      | API Pública                   | ✅ |
| Backend      | Blog/Noticias (opcional)      | ✅ |
| Backend      | Mejoras de calidad (formato API, logs, límites, excepciones) | ✅ |
| Frontend     | Login Administrador           | ⬜ |
| Frontend     | Dashboard Inmuebles           | ⬜ |
| Frontend     | Gestión Estados               | ⬜ |
| Frontend     | Subida Imágenes/Videos        | ⬜ |
| Frontend     | Página Pública (buscador)     | ⬜ |
| Frontend     | Detalle Inmueble              | ⬜ |
| Frontend     | Mapa Interactivo              | ⬜ |
| Frontend     | Contacto Agente               | ⬜ |
| Frontend     | Blog/Noticias (opcional)      | ⬜ |
| Infraestructura | Despliegue en GCP          | ⬜ |

---

## 📋 Mejoras de Calidad (Backend)

### 1. Formato estándar de respuestas del servidor

- **Objetivo:** Que todas las respuestas de la API sigan un formato común para que el cliente pueda leer de forma consistente qué ocurrió.
- **Qué se implementará:**
  - **Éxito:** Respuesta estándar con `success: true`, `data` (payload) y opcionalmente `message`. Códigos 200/201.
  - **Error de cliente (4xx):** Formato estándar de error con `success: false`, `message` claro en español indicando qué debe corregir el cliente (ej. "El email ya está en uso", "La contraseña actual es incorrecta"), y opcionalmente `code` (ej. `VALIDATION_ERROR`, `CONFLICT`) y `errors` (lista de errores por campo si aplica). Códigos 400, 404, 409 según el caso.
  - Así el cliente distingue claramente entre éxito y error, y en errores 400 sabe que debe ajustar la petición.

### 2. Logs en los flujos

- **Objetivo:** Trazabilidad y depuración.
- **Qué se implementará:**
  - Logs **INFO** en operaciones relevantes (login, registro, CRUD de inmuebles, estados, imágenes, videos, artículos).
  - Logs **WARN** cuando se rechace una petición por validación o regla de negocio.
  - Logs **ERROR** cuando se capture una excepción no controlada (junto con el manejo global de excepciones).

### 3. Límite de imágenes y videos por inmueble

- **Regla:** Máximo **7 imágenes** y máximo **1 video** por inmueble.
- **Recomendación de implementación:**
  - **Backend (recomendado):** Validar en el servicio al agregar imagen o video: si el inmueble ya tiene 7 imágenes (o 1 video), devolver error 400 con mensaje claro ("Este inmueble ya tiene el máximo de 7 imágenes"). Ventaja: la regla se cumple aunque el cliente llame la API directamente; el backend es la fuente de verdad.
  - **Frontend:** Mostrar solo hasta 7 imágenes y 1 video en formularios y deshabilitar el botón "Agregar" al llegar al límite; opcionalmente validar antes de enviar. Mejora la UX y evita peticiones innecesarias.
  - **Base de datos:** No es recomendable imponer el límite solo con constraints en la BD (requeriría triggers o chequeos complejos). La validación en backend es más clara y permite mensajes de error útiles.
- **Qué se implementará:** Validación en **backend** (servicios de imagen/video) para rechazar la creación cuando se supere el límite, con mensaje explícito. El frontend puede además aplicar el límite en la UI.

### 4. Manejo global de excepciones

- **Objetivo:** Controlar cualquier excepción en todo el flujo (controladores, servicios, repositorios) para evitar caídas del servicio y devolver respuestas controladas al cliente.
- **Qué se implementará:**
  - **Manejo centralizado:** Un manejador global (p. ej. `@ControllerAdvice` en MVC o `WebExceptionHandler` en WebFlux) que capture:
    - Excepciones de negocio conocidas (`IllegalArgumentException`, `IllegalStateException`, etc.) y las convierta en respuestas con código HTTP y cuerpo estándar (véase punto 1).
    - `ResponseStatusException` respetando su código y mensaje.
    - **Cualquier otra excepción** no prevista (incluidas las que ocurran en servicios, repositorios o filtros): responder con 500 y mensaje genérico al cliente (ej. "Error interno del servidor"), y **registrar la excepción completa en log (ERROR)** para diagnóstico. El servicio no debe caerse; la petición recibe una respuesta controlada.
  - Así se evitan respuestas en bruto o caídas del servicio por excepciones no capturadas en cualquier parte del flujo.

---

## 🌐 API REST – Convenciones y paginación

### Formato de respuestas

- **Éxito (200/201):** `{ "success": true, "data": ... }`. El campo `message` es opcional.
- **Error (4xx/5xx):** `{ "success": false, "message": "...", "code": "..." }`. En validación puede incluir `errors` (lista de `{ "campo", "mensaje" }`).
- **Listados:** Son **paginados**. `data` no es un array sino un objeto de página (véase más abajo).

### Paginación

Los siguientes endpoints devuelven una **página** de resultados:

| Método | Endpoint | Parámetros de paginación |
|--------|----------|--------------------------|
| GET | `/api/admin/estados` | `page`, `size` |
| GET | `/api/admin/localidades` | `page`, `size` |
| GET | `/api/admin/inmuebles` | `page`, `size` (+ filtros opcionales) |
| GET | `/api/public/inmuebles` | `page`, `size` (+ filtros opcionales) |
| GET | `/api/admin/configuracion-inmobiliaria` | `page`, `size` |
| GET | `/api/admin/articulos` | `page`, `size` |
| GET | `/api/public/articulos` | `page`, `size` |

- **Parámetros:** `page` (número de página, 0-based; por defecto `0`), `size` (tamaño de página; por defecto `20`, máximo `100`).
- **Filtros de listado de inmuebles** (`GET /api/public/inmuebles` y `GET /api/admin/inmuebles`): todos opcionales y combinables — `estadoId`, `localidadId`, `tipoId`, `precioMin`/`precioMax`, `areaMin`/`areaMax` (m²), `habitacionesMin`/`habitacionesMax`, `banosMin`/`banosMax`, `estratoMin`/`estratoMax`, `parqueaderosMin`/`parqueaderosMax`.
- **Localidades:** `GET /api/public/localidades` devuelve todas las localidades (o solo las de una ciudad con `ciudadId`). Por ahora principalmente Bogotá (20 localidades). CRUD en `/api/admin/localidades` (JWT). Cada localidad tiene `ciudadId`; al crear/actualizar la ciudad debe existir y no se puede repetir el mismo nombre en la misma ciudad.
- **Configuración institucional (Acerca de nosotros):** `GET /api/public/configuracion-inmobiliaria` devuelve la configuración actual (misión, visión, términos, política de datos, descripción) sin autenticación. El CRUD (listar, crear, actualizar, eliminar) está en `/api/admin/configuracion-inmobiliaria` y requiere JWT.
- **Estructura de `data` en listados:**

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "totalElements": 45,
    "totalPages": 3,
    "number": 0,
    "size": 20,
    "first": true,
    "last": false
  }
}
```

- Los elementos de la página están en `data.content`. El resto son metadatos de paginación.

### Ejemplos con curl

Para **un ejemplo de curl de cada endpoint** del proyecto, ver el archivo **CURL_EJEMPLOS.md** en la raíz del repositorio.

**Login (obtener token):**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"tu-password"}' | jq
```

**Listar inmuebles públicos (paginado, primera página, 20 por página):**

```bash
curl -s "http://localhost:8080/api/public/inmuebles?page=0&size=20" | jq
```

**Listar inmuebles con filtro y paginación:**

```bash
curl -s "http://localhost:8080/api/public/inmuebles?localidadId=2&page=0&size=10" | jq
```

**Listar estados (admin, con token):**

```bash
TOKEN="<token-obtenido-del-login>"
curl -s "http://localhost:8080/api/admin/estados?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Ejemplo de respuesta de error (400):**

```json
{
  "success": false,
  "message": "El email ya está en uso",
  "code": "CONFLICT"
}
```