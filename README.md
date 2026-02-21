# Asecontin – Backend

API REST del backend de Asecontin: gestión de inmuebles, estados, imágenes, videos, blog y autenticación JWT para el panel de administración.

## Requisitos

- **Java 25** (o la versión configurada en `build.gradle`)
- **PostgreSQL** (tablas en el schema `imb`; ver script en `INSTRUCCIONES.md`)
- **Gradle** (wrapper incluido: `./gradlew`)

## Configuración

1. Crear la base de datos y el schema `imb` con el script SQL de `INSTRUCCIONES.md`.
2. Ajustar `src/main/resources/application.properties`:
   - `spring.r2dbc.url`: host, puerto, nombre de BD y schema (`imb`).
   - `spring.r2dbc.username` / `spring.r2dbc.password`: credenciales de PostgreSQL.
   - `jwt.secret`: clave segura en producción (mínimo 256 bits).

## Ejecutar la aplicación

```bash
./gradlew bootRun
```

Por defecto el servidor queda en **http://localhost:8080**.

## Documentación de la API (Swagger)

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON:** http://localhost:8080/api-docs  

Los endpoints protegidos requieren JWT: en Swagger UI, usar **Authorize** e introducir `Bearer <token>` (token obtenido con `POST /api/auth/login` o `POST /api/auth/register`).

**Colección Postman:** en la carpeta `postman/` está `Asecontin-API.postman_collection.json` con todos los endpoints organizados. Tras importarla, ejecuta Login o Register y el token se guarda para las peticiones Admin (ver `postman/README.md`).

## Pruebas

```bash
./gradlew test
```

## Formato de respuestas

- **Éxito:** `{ "success": true, "data": ... }` (códigos 200/201).
- **Error:** `{ "success": false, "message": "...", "code": "..." }` (4xx/5xx). En validación puede incluir `errors` por campo.
- **Listados paginados:** Todos los listados (estados, inmuebles, artículos) son paginados. `data` es un objeto: `content` (array de la página), `totalElements`, `totalPages`, `number` (página 0-based), `size`, `first`, `last`. Query params: `page` (default 0), `size` (default 20, max 100).

Ejemplos de **curl** para cada endpoint: **CURL_EJEMPLOS.md**. Más detalle en **INSTRUCCIONES.md**, sección *API REST – Convenciones y paginación*.

## Límites de negocio

- **Imágenes por inmueble:** máximo 7 (error 409 si se supera). Una de ellas puede marcarse como **imagen principal** (de muestra en listados); se incluye en la respuesta de inmuebles como `imagenPrincipal`.
- **Videos por inmueble:** máximo 1 (error 409 si se supera).

## Recomendaciones y aspectos a tener en cuenta

- **Producción:** No usar `jwt.secret` en claro en el repo. Configurar `JWT_SECRET` y `JWT_EXPIRATION_MS` por variables de entorno (o `SPRING_APPLICATION_JSON`). La clave JWT debe tener al menos 32 caracteres (256 bits) para HS256.
- **CORS:** Cuando exista frontend en otro origen, configurar CORS (p. ej. `@Configuration` con `WebFilter` o `CorsWebFilter`) y permitir solo los orígenes necesarios.
- **Contacto:** El endpoint `POST /api/public/contacto` acepta el mensaje y responde 202; aún no envía email ni persiste en BD. Integrar con un servicio de correo o una tabla `contacto` cuando se requiera.
- **Health:** Para despliegue (GCP, load balancers) se expone `GET /actuator/health` (público, sin detalles). El resto de endpoints de Actuator no se exponen.
- **Seguridad:** Las rutas no declaradas explícitamente se deniegan (`anyExchange().denyAll()`). Solo están permitidos `/api/auth/**`, `/api/public/**`, Swagger y `/actuator/health`.

## Frontend en React

Para crear un proyecto React (Vite, Material UI, Axios) y conectarlo a este backend, ver **FRONTEND.md**.

## Especificación y plan

Ver **INSTRUCCIONES.md** para alcance funcional, diseño de BD, plan de desarrollo, mejoras de calidad y **convenciones de la API (formato, paginación, ejemplos curl)**.
