# Crear el frontend en React y conectarlo al backend

Esta guía indica cómo crear un proyecto React (con Vite), configurar Material UI y consumir la API del backend de Asecontin.

---

## 1. Crear el proyecto React con Vite

En una carpeta **hermana** al backend (o donde prefieras), crea la app:

```bash
# Desde la carpeta donde quieras el frontend (ej. junto a asecontin-backend/)
npm create vite@latest asecontin-frontend -- --template react
cd asecontin-frontend
npm install
```

Si prefieres **Create React App** (CRA):

```bash
npx create-react-app asecontin-frontend
cd asecontin-frontend
```

---

## 2. Instalar dependencias recomendadas

Según las INSTRUCCIONES del proyecto (React + Material UI + Axios):

```bash
# Material UI (core + componentes básicos)
npm install @mui/material @emotion/react @emotion/styled

# Cliente HTTP para la API
npm install axios

# React Router (rutas: público, login, panel admin)
npm install react-router-dom
```

Opcional para íconos de Material UI:

```bash
npm install @mui/icons-material
```

---

## 3. Variable de entorno para la URL del backend

Crea un archivo `.env` en la raíz del proyecto frontend:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Con **Vite** las variables deben empezar por `VITE_` para exponerse al cliente. Con CRA se usa `REACT_APP_` (ej. `REACT_APP_API_BASE_URL`).

Crea también `.env.example` (sin valores sensibles) para quien clone el repo:

```env
VITE_API_BASE_URL=http://localhost:8080
```

---

## 4. Configurar Axios (base URL e interceptores)

Crea un módulo que configure una instancia de Axios apuntando al backend y, si quieres, un interceptor para añadir el JWT en las peticiones al admin.

**Ejemplo:** `src/api/client.js` (o `client.ts` si usas TypeScript):

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
});

// Añadir token JWT a las peticiones al panel admin
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token'); // o donde guardes el token
  if (token && config.url?.startsWith('/api/admin')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Opcional: si el backend devuelve 401, redirigir a login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
```

---

## 5. Formato de respuestas del backend

- **Éxito:** `{ success: true, data: ... }` → usa `response.data.data`.
- **Error:** `{ success: false, message: "...", code: "..." }` → muestra `response.data.message`.
- **Listados paginados:** `data` es `{ content, totalElements, totalPages, number, size, first, last }` → la lista está en `data.content`.

Ejemplo de llamada para listar inmuebles públicos (paginado):

```javascript
// GET /api/public/inmuebles?page=0&size=20
const { data } = await api.get('/api/public/inmuebles', { params: { page: 0, size: 20 } });
if (data.success) {
  const inmuebles = data.data.content;
  const totalPages = data.data.totalPages;
}
```

Ejemplo de login:

```javascript
// POST /api/auth/login
const { data } = await api.post('/api/auth/login', { email, password });
if (data.success) {
  const { token, email: userEmail, nombre, rol } = data.data;
  localStorage.setItem('token', token);
  // redirigir al panel admin
}
```

---

## 6. CORS en el backend

El backend actual no tiene CORS configurado. Si el frontend corre en otro origen (ej. `http://localhost:5173` con Vite), el navegador bloqueará las peticiones hasta que el backend permita ese origen.

**Opciones:**

1. **Desarrollo:** Configurar CORS en el backend (Spring) permitiendo `http://localhost:5173` (y/o el puerto que use tu app).
2. **Alternativa en desarrollo:** Usar el proxy de Vite para que las peticiones a `/api` se reenvíen al backend (así el navegador ve mismo origen).

**Ejemplo de proxy en Vite** (`vite.config.js`):

```javascript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

Con este proxy, en el frontend usa la misma base que la app (ej. vacía o `/`) y las peticiones a `api.get('/api/public/inmuebles')` irán a `http://localhost:8080/api/public/inmuebles`. Entonces en `.env` podrías poner `VITE_API_BASE_URL=` (vacío) o `VITE_API_BASE_URL=http://localhost:5173` para que Axios use el mismo host que la SPA.

---

## 7. Estructura de carpetas sugerida

```
src/
  api/
    client.js          # instancia Axios + interceptores
    auth.js            # login, register, logout
    inmuebles.js       # listar público, detalle, etc.
    estados.js         # CRUD estados (admin)
    articulos.js       # listar público, CRUD admin
  components/         # componentes reutilizables
  pages/              # páginas por ruta (Login, Inmuebles, AdminEstados, etc.)
  context/            # opcional: AuthContext para usuario/token
  App.jsx
  main.jsx
```

---

## 8. Ejecutar backend y frontend

1. **Backend:** desde la carpeta del backend (`inmobiliaria/`):  
   `./gradlew bootRun`  
   → API en `http://localhost:8080`

2. **Frontend:** desde la carpeta del frontend:  
   `npm run dev` (Vite) o `npm start` (CRA)  
   → App en `http://localhost:5173` (Vite) o `http://localhost:3000` (CRA)

Asegúrate de que la URL en `.env` (o el proxy) apunte al mismo puerto donde corre el backend (por defecto 8080).

---

## 9. Resumen de endpoints útiles para el frontend

| Uso | Método | Endpoint | Auth |
|-----|--------|----------|------|
| Login | POST | `/api/auth/login` | No |
| Registro primer admin | POST | `/api/auth/register` | No |
| Listar inmuebles (público) | GET | `/api/public/inmuebles?page=0&size=20` | No |
| Detalle inmueble (público) | GET | `/api/public/inmuebles/{id}` | No |
| Listar artículos (público) | GET | `/api/public/articulos?page=0&size=20` | No |
| Contacto | POST | `/api/public/contacto` | No |
| Listar estados | GET | `/api/admin/estados?page=0&size=20` | JWT |
| CRUD inmuebles | GET/POST/PUT/DELETE | `/api/admin/inmuebles` | JWT |
| CRUD imágenes por inmueble | GET/POST/DELETE | `/api/admin/inmuebles/{id}/imagenes` | JWT |
| CRUD videos por inmueble | GET/POST/DELETE | `/api/admin/inmuebles/{id}/videos` | JWT |
| CRUD artículos | GET/POST/PUT/DELETE | `/api/admin/articulos` | JWT |

Documentación completa de la API (formato, paginación, ejemplos): ver **INSTRUCCIONES.md**, sección *API REST – Convenciones y paginación*.
