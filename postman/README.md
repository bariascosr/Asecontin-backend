# Colección Postman – Asecontin API

Incluye todos los endpoints del backend organizados en carpetas.

## Importar

1. Abre Postman.
2. **Import** → **File** → selecciona `Asecontin-API.postman_collection.json`.

## Uso

1. **Variables de colección:** `baseUrl` (por defecto `http://localhost:8080`) y `token` (se rellena al hacer Login o Register).
2. **Auth:** Ejecuta **Auth → Login** (o **Register** si es la primera vez). El token se guarda automáticamente y los requests de **Admin** lo usan en la cabecera `Authorization: Bearer {{token}}`.
3. **Público:** Los endpoints bajo *Público* no necesitan token.
4. **Admin:** Requieren token; si fallan con 401, vuelve a ejecutar Login.

## Estructura

- **Auth** – Login, Register (primer admin).
- **Público - Inmuebles** – Listar (paginado), detalle con galería.
- **Público - Artículos** – Listar, ver artículo.
- **Público - Contacto** – Enviar mensaje.
- **Admin - Me** – Comprobar token.
- **Admin - Usuarios** – Mi perfil (GET, PUT).
- **Admin - Estados** – CRUD estados.
- **Admin - Inmuebles** – CRUD inmuebles.
- **Admin - Imágenes** – Listar, agregar (normal o principal), marcar principal, eliminar.
- **Admin - Videos** – Listar, agregar, eliminar.
- **Admin - Artículos** – CRUD artículos.

Los IDs en las URLs (ej. `/inmuebles/1`, `/imagenes/2`) son ejemplos; cámbialos según tus datos.
