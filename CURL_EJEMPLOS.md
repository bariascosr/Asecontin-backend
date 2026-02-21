# Ejemplos de cURL por endpoint

Base URL por defecto: **http://localhost:8080** (ajustar si el backend corre en otro host/puerto).

- **Admin:** cabecera `Authorization: Bearer <token>`. Token con `POST /api/auth/login` o `POST /api/auth/register`.
- **Público:** sin token. Incluye listados, detalle, catálogos (ciudades, localidades, tipos, sectores), configuración institucional (Acerca de nosotros), medios (imagen/video), artículos y contacto.

---

## Autenticación

### POST /api/auth/login

Iniciar sesión. Devuelve `token`, `email`, `nombre`, `rol`.

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "tu-password"
  }'
```

### POST /api/auth/register

Registrar el primer (y único) administrador. Solo funciona si aún no hay usuarios.

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Administrador",
    "email": "admin@example.com",
    "password": "password123"
  }'
```

---

## API pública (sin autenticación)

### Inmuebles

### GET /api/public/inmuebles

Listar inmuebles con paginación. Los filtros son opcionales y se combinan. Filtros: `estadoId`, `localidadId`, `tipoId`, `precioMin`/`precioMax`, `areaMin`/`areaMax` (m²), `habitacionesMin`/`habitacionesMax`, `banosMin`/`banosMax`, `estratoMin`/`estratoMax`, `parqueaderosMin`/`parqueaderosMax`.

```bash
# Primera página, 20 por página
curl -s "http://localhost:8080/api/public/inmuebles?page=0&size=20"

# Con filtros básicos
curl -s "http://localhost:8080/api/public/inmuebles?localidadId=2&page=0&size=10"
curl -s "http://localhost:8080/api/public/inmuebles?tipoId=1&precioMin=100000000&precioMax=500000000&page=0&size=20"

# Con rangos: área (m²), habitaciones, baños, estrato, parqueaderos
curl -s "http://localhost:8080/api/public/inmuebles?areaMin=80&areaMax=150&habitacionesMin=2&habitacionesMax=4&banosMin=1&banosMax=3&estratoMin=3&estratoMax=5&parqueaderosMin=1&page=0&size=20"
```

### GET /api/public/inmuebles/{id}

Detalle de un inmueble (con galería de imágenes y videos). Las URLs de `imagenes` y `videos` pueden ser del propio backend (ej. `http://localhost:8080/api/public/medios/imagen/1`); el front las usa en `<img src>` o `<video src>`.

```bash
curl -s "http://localhost:8080/api/public/inmuebles/1"
```

### Catálogos (ciudades y tipos para formularios y filtros)

### GET /api/public/ciudades

Listar ciudades permitidas (capitales de departamentos de Colombia). Para dropdowns; las localidades se asocian a una ciudad. Al crear/actualizar un inmueble se envía `localidadId` (de GET /api/public/localidades).

```bash
curl -s "http://localhost:8080/api/public/ciudades"
```

### GET /api/public/localidades

Listar localidades (por ahora principalmente de Bogotá). Opcional: `ciudadId` para filtrar por ciudad. Para dropdowns y filtros en inmuebles.

```bash
# Todas las localidades
curl -s "http://localhost:8080/api/public/localidades"

# Solo localidades de Bogotá (obtener id de Bogotá con GET /api/public/ciudades; ej. 14)
curl -s "http://localhost:8080/api/public/localidades?ciudadId=14"
```

### GET /api/public/tipos-inmueble

Listar tipos de inmueble permitidos (Casa, Apartamento, Lote, etc.). Para selector y filtros; al crear/actualizar un inmueble se envía `tipoId` (id de uno de esta lista).

```bash
curl -s "http://localhost:8080/api/public/tipos-inmueble"
```

### GET /api/public/sectores

Listar sectores de ciudad (Oriente, Occidente, Norte, Sur, Centro, etc.). Para selector; al crear/actualizar un inmueble se puede enviar `sectorId` (id de uno de esta lista). Opcional.

```bash
curl -s "http://localhost:8080/api/public/sectores"
```

### GET /api/public/configuracion-inmobiliaria

Obtener la configuración institucional actual (misión, visión, términos y condiciones, política de tratamiento de datos, descripción) para la sección "Acerca de nosotros". Sin autenticación.

```bash
curl -s "http://localhost:8080/api/public/configuracion-inmobiliaria"
```

### Medios (archivos de imagen y video)

### GET /api/public/medios/imagen/{id}

Obtener el archivo de imagen por ID. Para usar en `<img src="...">`.

```bash
curl -s -o imagen.jpg "http://localhost:8080/api/public/medios/imagen/1"
```

### GET /api/public/medios/video/{id}

Obtener el archivo de video por ID. Para usar en `<video src="...">`.

```bash
curl -s -o video.mp4 "http://localhost:8080/api/public/medios/video/1"
```

### Artículos del blog

### GET /api/public/articulos

Listar artículos del blog (paginado).

```bash
curl -s "http://localhost:8080/api/public/articulos?page=0&size=20"
```

### GET /api/public/articulos/{id}

Ver un artículo.

```bash
curl -s "http://localhost:8080/api/public/articulos/1"
```

### Contacto

### POST /api/public/contacto

Enviar mensaje de contacto. Responde 202 Accepted (sin cuerpo).

```bash
curl -s -X POST http://localhost:8080/api/public/contacto \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "mensaje": "Me interesa el inmueble de la calle 100.",
    "inmuebleId": 1
  }' -w "\nHTTP %{http_code}\n"
```

*(`inmuebleId` es opcional.)*

---

## Panel administrador (requieren JWT)

Sustituir `$TOKEN` por el token obtenido en login/register. Ejemplo después de login:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"tu-password"}' \
  | jq -r '.data.token')
```

---

### GET /api/admin/me

Comprobar que el token es válido (devuelve email y mensaje).

```bash
curl -s "http://localhost:8080/api/admin/me" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/admin/usuarios/me

Obtener perfil del administrador autenticado.

```bash
curl -s "http://localhost:8080/api/admin/usuarios/me" \
  -H "Authorization: Bearer $TOKEN"
```

### PUT /api/admin/usuarios/me

Actualizar perfil (nombre, email y/o contraseña). Para cambiar contraseña es obligatorio enviar `passwordActual` y `nuevaPassword`.

```bash
# Solo nombre
curl -s -X PUT http://localhost:8080/api/admin/usuarios/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Admin Actualizado"}'

# Cambiar contraseña
curl -s -X PUT http://localhost:8080/api/admin/usuarios/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "passwordActual": "password123",
    "nuevaPassword": "nuevo-password-456"
  }'
```

---

### GET /api/admin/estados

Listar estados (paginado).

```bash
curl -s "http://localhost:8080/api/admin/estados?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/admin/estados/{id}

Obtener un estado por ID.

```bash
curl -s "http://localhost:8080/api/admin/estados/1" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/estados

Crear estado.

```bash
curl -s -X POST http://localhost:8080/api/admin/estados \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombreEstado": "disponible"}'
```

### PUT /api/admin/estados/{id}

Actualizar estado.

```bash
curl -s -X PUT http://localhost:8080/api/admin/estados/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombreEstado": "vendido"}'
```

### DELETE /api/admin/estados/{id}

Eliminar estado (204 sin cuerpo). Falla si está en uso por algún inmueble.

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/estados/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

### Localidades (por ciudad, principalmente Bogotá)

### GET /api/admin/localidades

Listar localidades (paginado).

```bash
curl -s "http://localhost:8080/api/admin/localidades?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/admin/localidades/{id}

Obtener localidad por ID.

```bash
curl -s "http://localhost:8080/api/admin/localidades/1" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/localidades

Crear localidad. Body: `nombre`, `ciudadId` (obligatorios). La ciudad debe existir; no se puede repetir el mismo nombre en la misma ciudad.

```bash
curl -s -X POST http://localhost:8080/api/admin/localidades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Chapinero", "ciudadId": 14}'
```

### PUT /api/admin/localidades/{id}

Actualizar localidad.

```bash
curl -s -X PUT http://localhost:8080/api/admin/localidades/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Chapinero (actualizado)", "ciudadId": 14}'
```

### DELETE /api/admin/localidades/{id}

Eliminar localidad (204 sin cuerpo).

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/localidades/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

### Configuración institucional (Acerca de nosotros)

### GET /api/admin/configuracion-inmobiliaria

Listar configuraciones (paginado; normalmente una sola fila).

```bash
curl -s "http://localhost:8080/api/admin/configuracion-inmobiliaria?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/admin/configuracion-inmobiliaria/{id}

Obtener una configuración por ID.

```bash
curl -s "http://localhost:8080/api/admin/configuracion-inmobiliaria/1" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/configuracion-inmobiliaria

Crear configuración institucional. Todos los campos del body son opcionales: `mision`, `vision`, `terminosCondiciones`, `politicaTratamientoDatos`, `descripcion`.

```bash
curl -s -X POST http://localhost:8080/api/admin/configuracion-inmobiliaria \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "mision": "Nuestra misión es...",
    "vision": "Ser la inmobiliaria de referencia...",
    "terminosCondiciones": "Al usar este sitio...",
    "politicaTratamientoDatos": "En cumplimiento de la Ley 1581 de 2012...",
    "descripcion": "Somos una inmobiliaria con años de experiencia."
  }'
```

### PUT /api/admin/configuracion-inmobiliaria/{id}

Actualizar configuración.

```bash
curl -s -X PUT http://localhost:8080/api/admin/configuracion-inmobiliaria/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "mision": "Misión actualizada.",
    "vision": "Visión actualizada.",
    "terminosCondiciones": "Términos actualizados.",
    "politicaTratamientoDatos": "Política actualizada.",
    "descripcion": "Descripción actualizada."
  }'
```

### DELETE /api/admin/configuracion-inmobiliaria/{id}

Eliminar configuración (204 sin cuerpo).

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/configuracion-inmobiliaria/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

### GET /api/admin/inmuebles

Listar inmuebles (paginado). Filtros opcionales y combinables: `estadoId`, `localidadId`, `tipoId`, `precioMin`/`precioMax`, `areaMin`/`areaMax`, `habitacionesMin`/`habitacionesMax`, `banosMin`/`banosMax`, `estratoMin`/`estratoMax`, `parqueaderosMin`/`parqueaderosMax`.

```bash
curl -s "http://localhost:8080/api/admin/inmuebles?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

curl -s "http://localhost:8080/api/admin/inmuebles?estadoId=1&localidadId=2&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

curl -s "http://localhost:8080/api/admin/inmuebles?areaMin=80&areaMax=200&habitacionesMin=2&parqueaderosMin=1&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/admin/inmuebles/{id}

Obtener un inmueble por ID.

```bash
curl -s "http://localhost:8080/api/admin/inmuebles/1" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/inmuebles

Crear inmueble (requiere JWT). `estadoId` y `localidadId` deben existir. Use `GET /api/public/localidades` para la lista de localidades. Campos opcionales: `parqueaderos`, `sectorId`, `areaM2`, `habitaciones`, `banos`, `estrato`, `valorAdministracion`, `anoConstruccion`, `amoblado`, `piso`. Si se envía `sectorId`, debe existir en `GET /api/public/sectores`.

```bash
curl -s -X POST http://localhost:8080/api/admin/inmuebles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Casa en Chapinero",
    "descripcion": "Amplia casa con jardín.",
    "precio": 450000000,
    "direccion": "Calle 72 # 10-34",
    "localidadId": 2,
    "tipoId": 1,
    "estadoId": 1,
    "etiquetas": "nuevo",
    "parqueaderos": 2,
    "sectorId": 1,
    "areaM2": 120.50,
    "habitaciones": 3,
    "banos": 2,
    "estrato": 4,
    "valorAdministracion": 350000,
    "anoConstruccion": 2018,
    "amoblado": false,
    "piso": 1
  }'
```

### PUT /api/admin/inmuebles/{id}

Actualizar inmueble. Mismos campos que crear; obligatorios: titulo, precio, direccion, localidadId, tipoId, estadoId. Opcionales: `parqueaderos`, `sectorId`, `areaM2`, `habitaciones`, `banos`, `estrato`, `valorAdministracion`, `anoConstruccion`, `amoblado`, `piso`.

```bash
curl -s -X PUT http://localhost:8080/api/admin/inmuebles/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Casa en Chapinero (actualizada)",
    "descripcion": "Amplia casa con jardín y garaje.",
    "precio": 480000000,
    "direccion": "Calle 72 # 10-34",
    "localidadId": 2,
    "tipoId": 1,
    "estadoId": 1,
    "etiquetas": "nuevo, rebajado",
    "parqueaderos": 2,
    "sectorId": 1,
    "areaM2": 120.50,
    "habitaciones": 3,
    "banos": 2,
    "estrato": 4,
    "valorAdministracion": 350000,
    "anoConstruccion": 2018,
    "amoblado": false,
    "piso": 1
  }'
```

### DELETE /api/admin/inmuebles/{id}

Eliminar inmueble (204 sin cuerpo).

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/inmuebles/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

### GET /api/admin/inmuebles/{inmuebleId}/imagenes

Listar imágenes de un inmueble. Máximo 7 imágenes por inmueble.

```bash
curl -s "http://localhost:8080/api/admin/inmuebles/1/imagenes" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/inmuebles/{inmuebleId}/imagenes

Agregar imagen (URL). Máximo 7 por inmueble. Opcional: `esPrincipal: true` para usarla como imagen de muestra en listados (solo una por inmueble).

```bash
# Sin marcar como principal
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/imagenes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"urlImagen": "https://storage.example.com/inmueble-1/foto1.jpg"}'

# Como imagen principal (de muestra en listados)
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/imagenes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"urlImagen": "https://storage.example.com/inmueble-1/portada.jpg", "esPrincipal": true}'
```

### POST /api/admin/inmuebles/{inmuebleId}/imagenes/upload

Subir imagen desde el front (multipart). Partes: `file` (archivo), `esPrincipal` (opcional, `true`/`false`). Formatos: jpg, png, gif, webp. La respuesta incluye `urlImagen` como URL pública (ej. `http://localhost:8080/api/public/medios/imagen/{id}`) para usar en `<img src>`.

```bash
# Subir archivo (reemplazar ruta del archivo)
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/imagenes/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/ruta/a/foto.jpg"

# Marcar como imagen principal
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/imagenes/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/ruta/a/portada.jpg" \
  -F "esPrincipal=true"
```

### POST /api/admin/inmuebles/{inmuebleId}/imagenes/upload-batch

Subir hasta 7 imágenes en una sola petición. Partes: varias `files` (mismo nombre, un archivo por parte) y opcionalmente `esPrincipalIndex` (índice 0-based: 0 = primera imagen es la principal). La respuesta es la lista de imágenes creadas con sus URLs públicas.

```bash
# Subir varias imágenes a la vez (reemplazar rutas)
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/imagenes/upload-batch \
  -H "Authorization: Bearer $TOKEN" \
  -F "files=@/ruta/foto1.jpg" \
  -F "files=@/ruta/foto2.jpg" \
  -F "files=@/ruta/foto3.jpg" \
  -F "esPrincipalIndex=0"

# Sin marcar ninguna como principal (omitir esPrincipalIndex)
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/imagenes/upload-batch \
  -H "Authorization: Bearer $TOKEN" \
  -F "files=@/ruta/foto1.jpg" \
  -F "files=@/ruta/foto2.jpg"
```

### PUT /api/admin/inmuebles/{inmuebleId}/imagenes/{imagenId}

Marcar una imagen existente como principal. Cuerpo: `{"esPrincipal": true}`. La que estaba como principal pasa a no serlo.

```bash
curl -s -X PUT http://localhost:8080/api/admin/inmuebles/1/imagenes/2 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"esPrincipal": true}'
```

### DELETE /api/admin/inmuebles/{inmuebleId}/imagenes/{imagenId}

Eliminar una imagen (204 sin cuerpo).

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/inmuebles/1/imagenes/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

### GET /api/admin/inmuebles/{inmuebleId}/videos

Listar videos de un inmueble. Máximo 1 video por inmueble.

```bash
curl -s "http://localhost:8080/api/admin/inmuebles/1/videos" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/inmuebles/{inmuebleId}/videos

Agregar video (URL). Error 409 si el inmueble ya tiene 1 video.

```bash
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/videos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"urlVideo": "https://storage.example.com/inmueble-1/recorrido.mp4"}'
```

### POST /api/admin/inmuebles/{inmuebleId}/videos/upload

Subir video desde el front (multipart). Parte: `file`. Formatos: mp4, webm, mov, avi, mkv, m4v, ogv, 3gp. Máximo 1 video por inmueble. La respuesta incluye `urlVideo` como URL pública (ej. `http://localhost:8080/api/public/medios/video/{id}`).

```bash
curl -s -X POST http://localhost:8080/api/admin/inmuebles/1/videos/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/ruta/a/recorrido.mp4"
```

### DELETE /api/admin/inmuebles/{inmuebleId}/videos/{videoId}

Eliminar el video (204 sin cuerpo).

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/inmuebles/1/videos/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

### GET /api/admin/articulos

Listar artículos (paginado).

```bash
curl -s "http://localhost:8080/api/admin/articulos?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### GET /api/admin/articulos/{id}

Obtener un artículo por ID.

```bash
curl -s "http://localhost:8080/api/admin/articulos/1" \
  -H "Authorization: Bearer $TOKEN"
```

### POST /api/admin/articulos

Crear artículo.

```bash
curl -s -X POST http://localhost:8080/api/admin/articulos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Tendencias del mercado inmobiliario 2025",
    "contenido": "Contenido completo del artículo..."
  }'
```

### PUT /api/admin/articulos/{id}

Actualizar artículo.

```bash
curl -s -X PUT http://localhost:8080/api/admin/articulos/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Tendencias del mercado inmobiliario 2025 (actualizado)",
    "contenido": "Contenido actualizado del artículo..."
  }'
```

### DELETE /api/admin/articulos/{id}

Eliminar artículo (204 sin cuerpo).

```bash
curl -s -X DELETE "http://localhost:8080/api/admin/articulos/1" \
  -H "Authorization: Bearer $TOKEN" -w "\nHTTP %{http_code}\n"
```

---

## Respuestas (resumen)

- **Éxito con datos:** Siempre envuelto en `{ "success": true, "data": ... }`. En listados, `data` es un objeto paginado (véase más abajo).
- **Error (4xx/5xx):** `{ "success": false, "message": "...", "code": "...", "errors": [...] }`.
- **DELETE correcto:** 204 No Content, sin cuerpo.

Para formatear JSON en terminal: `| jq` (ej. `curl -s ... | jq`).

---

## Campos de las respuestas (nombre, tipo y ejemplo)

Todas las respuestas JSON de éxito comparten un **envoltorio** y luego un **payload** en `data` que varía según el endpoint. Las fechas vienen en formato ISO-8601 (ej. `"2025-02-17T10:30:00"`).

### Envoltorio común (éxito)

| Campo     | Tipo    | Ejemplo   | Descripción                          |
|-----------|---------|-----------|--------------------------------------|
| `success` | boolean | `true`    | Siempre `true` en respuestas exitosas |
| `data`    | object  | (véase abajo) | Payload según el endpoint          |
| `message` | string  | `null` o `"Creado correctamente"` | Opcional                    |

---

### Envoltorio de error

| Campo     | Tipo   | Ejemplo                    | Descripción                          |
|-----------|--------|----------------------------|--------------------------------------|
| `success` | boolean | `false`                   | Siempre `false` en errores            |
| `message` | string | `"El email ya está en uso"` | Mensaje para el cliente           |
| `code`    | string | `"CONFLICT"`              | Opcional. Valores: `BAD_REQUEST`, `CONFLICT`, `NOT_FOUND`, `VALIDATION_ERROR`, `UNAUTHORIZED`, `INTERNAL_ERROR` |
| `errors`  | array  | (véase `CampoError`)      | Opcional; en validación, lista por campo |

**Elemento de `errors` (CampoError):**

| Campo    | Tipo   | Ejemplo   |
|----------|--------|-----------|
| `campo`  | string | `"email"` |
| `mensaje`| string | `"no debe estar vacío"` |

---

### Objeto paginado (listados)

Cuando `data` es una página (estados, inmuebles, artículos), tiene esta forma:

| Campo           | Tipo   | Ejemplo   | Descripción                          |
|-----------------|--------|-----------|--------------------------------------|
| `content`       | array  | `[ {...}, {...} ]` | Elementos de la página actual   |
| `totalElements` | number | `45`      | Total de elementos en todo el conjunto |
| `totalPages`    | number | `3`       | Total de páginas                     |
| `number`        | number | `0`       | Número de página actual (0-based)    |
| `size`          | number | `20`      | Tamaño de página                    |
| `first`         | boolean | `true`   | `true` si es la primera página       |
| `last`          | boolean | `false`  | `true` si es la última página        |

Cada elemento de `content` depende del recurso (EstadoResponse, InmuebleResponse, ArticuloResponse, etc.); se describen abajo.

---

### AuthResponse (login, register)

Contenido de `data` en `POST /api/auth/login` y `POST /api/auth/register`.

| Campo   | Tipo   | Ejemplo                          |
|---------|--------|----------------------------------|
| `token` | string | `"eyJhbGciOiJIUzI1NiIsInR5cCI6..."` |
| `email` | string | `"admin@example.com"`            |
| `nombre`| string | `"Administrador"`                |
| `rol`   | string | `"ADMINISTRADOR"`                |

---

### UsuarioResponse (perfil admin)

Contenido de `data` en `GET /api/admin/usuarios/me` y `PUT /api/admin/usuarios/me`.

| Campo   | Tipo   | Ejemplo                |
|---------|--------|------------------------|
| `id`    | number | `1`                    |
| `nombre`| string | `"Administrador"`      |
| `email` | string | `"admin@example.com"`  |
| `rol`   | string | `"ADMINISTRADOR"`      |

---

### EstadoResponse (estados)

Contenido de cada elemento en `content` (listado) o `data` (obtener por ID). Usado en `/api/admin/estados`.

| Campo         | Tipo   | Ejemplo        |
|---------------|--------|----------------|
| `id`          | number | `1`            |
| `nombreEstado`| string | `"disponible"` |

---

### SectorResponse (catálogo sectores)

Contenido de cada elemento en el array `data` de `GET /api/public/sectores`. Usado para el selector de sector al crear/actualizar inmueble.

| Campo   | Tipo   | Ejemplo     |
|---------|--------|-------------|
| `id`    | number | `1`         |
| `nombre`| string | `"Oriente"` |

---

### LocalidadResponse (localidades por ciudad)

Contenido de cada elemento en el array `data` de `GET /api/public/localidades` y en el CRUD admin.

| Campo          | Tipo   | Ejemplo        |
|----------------|--------|----------------|
| `id`           | number | `1`            |
| `nombre`       | string | `"Chapinero"`  |
| `ciudadId`     | number | `14`           |
| `ciudadNombre` | string | `"Bogotá"`     |

---

### ConfiguracionInmobiliariaResponse (Acerca de nosotros)

Contenido de `data` en `GET /api/public/configuracion-inmobiliaria` y en el CRUD admin de configuración institucional.

| Campo                     | Tipo   | Ejemplo / Descripción                    |
|---------------------------|--------|------------------------------------------|
| `id`                      | number | `1`                                      |
| `mision`                  | string | Texto de la misión (o `null`)            |
| `vision`                  | string | Texto de la visión (o `null`)            |
| `terminosCondiciones`     | string | Términos y condiciones (o `null`)        |
| `politicaTratamientoDatos` | string | Política de tratamiento de datos (o `null`) |
| `descripcion`             | string | Descripción / Acerca de nosotros (o `null`) |
| `fechaCreacion`           | string | ISO 8601, ej. `"2025-02-19T12:00:00"`   |
| `fechaActualizacion`      | string | ISO 8601                                 |

---

### InmuebleResponse (inmuebles en listados y CRUD admin)

Contenido de cada elemento en `content` (listados) o `data` (obtener/crear/actualizar). Usado en `/api/admin/inmuebles` y en listado/detalle público cuando no se pide galería. Incluye `imagenPrincipal` para la imagen de muestra en cards/listados.

| Campo             | Tipo   | Ejemplo                    |
|-------------------|--------|----------------------------|
| `id`              | number | `1`                        |
| `titulo`          | string | `"Casa en Chapinero"`      |
| `descripcion`     | string | `"Amplia casa con jardín."` o `null` |
| `precio`          | number | `450000000`                |
| `direccion`       | string | `"Calle 72 # 10-34"`       |
| `localidadId`      | number | `2`                        |
| `localidadNombre`  | string | `"Chapinero"`              |
| `ciudadNombre`      | string | `"Bogotá"`                 |
| `tipo`            | string | Nombre del tipo (ej. `"Casa"`). En request se envía `tipoId` (número). |
| `estadoId`        | number | `1`                        |
| `estadoNombre`    | string | `"disponible"`             |
| `etiquetas`       | string | `"nuevo"` o `null`         |
| `parqueaderos`    | number | `2` (por defecto 0)        |
| `sectorId`        | number | `1` o `null`               |
| `sectorNombre`    | string | `"Oriente"` o `""`         |
| `areaM2`          | number | `120.50` o `null`          |
| `habitaciones`    | number | `3` o `null`               |
| `banos`           | number | `2` o `null`               |
| `estrato`         | number | `4` (1–6) o `null`         |
| `valorAdministracion` | number | `350000` o `null`      |
| `anoConstruccion` | number | `2018` o `null`            |
| `amoblado`        | boolean | `false` (por defecto)     |
| `piso`            | number | `1` o `null`               |
| `fechaCreacion`   | string | `"2025-02-17T10:00:00"`    |
| `fechaActualizacion` | string | `"2025-02-17T12:30:00"` |
| `imagenPrincipal` | string | URL de la imagen de muestra (puede ser `http://localhost:8080/api/public/medios/imagen/{id}` si se subió por upload, o URL externa) o `null` |

---

### InmuebleDetallePublicoResponse (detalle con galería)

Contenido de `data` en `GET /api/public/inmuebles/{id}`. Incluye todos los campos de InmuebleResponse (`parqueaderos`, `sectorId`, `sectorNombre`, `areaM2`, `habitaciones`, `banos`, `estrato`, `valorAdministracion`, `anoConstruccion`, `amoblado`, `piso`, etc.) más:

| Campo     | Tipo     | Ejemplo                                      |
|-----------|----------|----------------------------------------------|
| `imagenes`| string[] | URLs de imágenes (del backend, ej. `/api/public/medios/imagen/1`, o externas) |
| `videos`  | string[] | URLs de videos (del backend o externas) |

---

### ImagenResponse (imágenes por inmueble)

Contenido de cada elemento en `content` (listado) o `data` (crear/actualizar/upload). Usado en `/api/admin/inmuebles/{id}/imagenes`. Tras subir con `/upload`, `urlImagen` es la URL pública del backend (ej. `http://localhost:8080/api/public/medios/imagen/{id}`).

| Campo      | Tipo    | Ejemplo                              |
|------------|---------|--------------------------------------|
| `id`       | number  | `1`                                  |
| `inmuebleId` | number | `1`                                |
| `urlImagen`| string  | `"https://storage.example.com/foto.jpg"` |
| `esPrincipal` | boolean | `true` o `false` (imagen de muestra en listados) |

---

### VideoResponse (videos por inmueble)

Contenido de cada elemento en `content` (listado) o `data` (crear). Usado en `/api/admin/inmuebles/{id}/videos`.

| Campo     | Tipo   | Ejemplo                                  |
|-----------|--------|------------------------------------------|
| `id`      | number | `1`                                      |
| `inmuebleId` | number | `1`                                  |
| `urlVideo`| string | `"https://storage.example.com/recorrido.mp4"` |

---

### ArticuloResponse (blog/noticias)

Contenido de cada elemento en `content` (listados) o `data` (obtener/crear/actualizar). Usado en `/api/admin/articulos` y `/api/public/articulos`.

| Campo            | Tipo   | Ejemplo                         |
|------------------|--------|---------------------------------|
| `id`             | number | `1`                             |
| `titulo`         | string | `"Tendencias 2025"`             |
| `contenido`      | string | `"Contenido completo..."`       |
| `fechaPublicacion` | string | `"2025-02-17T08:00:00"`      |

---

### GET /api/admin/me (usuario actual)

Contenido de `data`: objeto con dos campos (no es UsuarioResponse).

| Campo     | Tipo   | Ejemplo               |
|-----------|--------|-----------------------|
| `email`   | string | `"admin@example.com"` |
| `message` | string | `"Acceso autorizado"`  |
