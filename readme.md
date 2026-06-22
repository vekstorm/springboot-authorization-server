# Authorization Server

Servidor OAuth2 / OpenID Connect basado en **Spring Authorization Server 7.0.4** con **Spring Boot 4.1** (Java 21). Almacena clientes, usuarios, roles y permisos en PostgreSQL y provee API REST para administración en tiempo real.

## Estrategia general

El servidor centraliza autenticación y autorización mediante **tokens JWT firmados con RSA**. Los clientes OAuth2 se registran en base de datos (no en properties), permitiendo crearlos, modificarlos o eliminarlos sin redesplegar. El control de acceso se basa en **roles y permisos**:

- **Usuarios** tienen roles asignados (tabla `app_user_role`)
- **Roles** tienen permisos asignados (tabla `role_permissions`)
- **Permisos** son strings planos (`user:read`, `client:write`, etc.) que se incluyen en el JWT como authorities
- Los endpoints REST se protegen con `@PreAuthorize("hasAuthority('xxx')")`

No se puede borrar un permiso si está asignado a algún rol, ni un rol si está asignado a algún usuario — el servidor retorna `409 Conflict` con el detalle.

## Soporte OAuth2

### Grant types admitidos

| Grant type | Soporte | Notas |
|---|---|---|
| Authorization Code | Completo | Con PKCE opcional/requerido por cliente |
| Refresh Token | Completo | Con rotación (cada refresh invalida el anterior), requiere `offline_access` scope |
| Client Credentials | Soporte completo | Para clientes confidenciales |

### No soportados (deprecados por seguridad)

- Implicit grant
- Resource Owner Password (ROPC)
- Device Code (no implementado en el flujo real)

### PKCE

Se configura por cliente (`requireProofKey: true/false`). Los clientes públicos deben usar PKCE obligatoriamente.

### Refresh tokens

- Rotación activa: `reuseRefreshTokens(false)` — cada refresh emite un nuevo refresh token e invalida el anterior
- Solo se emiten si el request de autorización incluye `scope=offline_access`
- Clientes confidenciales deben autenticarse con `client_secret_basic` en `/oauth2/token`

## Stack

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 4.1 |
| Auth Server | spring-security-oauth2-authorization-server 7.0.4 |
| Java | 21 |
| BD | PostgreSQL |
| ORM | Spring Data JPA + Hibernate (ddl-auto: update) |
| Frontend | Thymeleaf + CSS + JS vanilla |
| API docs | springdoc-openapi 3.0.0 (Swagger UI) |
| Build | Maven + Lombok |

## Clientes pre-registrados (DataInitializer)

| Client ID | Tipo | Secret | Grant types | PKCE | Redirect URIs |
|---|---|---|---|---|---|
| `identity-client` | Confidencial | `identity-secret` (BCrypt) | authorization_code, refresh_token | Sí | `<frontend_url>/`, `<frontend_url>/`, `<custom_scheme>://callback` |
| `oidc-client` | Público | — | authorization_code, refresh_token | No | `https://oauthdebugger.com/debug` |
| `swagger-ui` | Público | — | authorization_code, refresh_token | Sí | `<swagger_redirect_url>` |

### identity-client (cliente frontend)

Autenticación: `client_secret_basic` en `/oauth2/token`. Requiere PKCE. Post-logout redirect: `<frontend_url>/`. Scopes: `openid`, `profile`, `offline_access`.

## Seguridad: filter chains (por orden)

### `@Order(0)` — Swagger
Rutas públicas de Swagger (`/swagger-ui/**`, `/v3/api-docs/**`).

### `@Order(1)` — Authorization Server (`/oauth2/**`)
Endpoints OAuth2/OIDC. Requiere autenticación. Usa `OAuth2AuthorizationServerConfiguration`. Redirige a `/login` si no autenticado.

### `@Order(2)` — API REST (`/api/v1/**`)
Endpoints de administración. Autenticación vía **JWT Bearer** token. Autorización por permisos (`@PreAuthorize`). CORS configurado dinámicamente según los orígenes permitidos.

### `@Order(3)` — Web (`/login`, `/register`, `/exit`, etc.)
Páginas públicas: login con formulario o Google OAuth2, registro, logout. Estáticas en `/assets/**`, `.well-known/**`. Logout con `DynamicLogoutSuccessHandler` (redirige al origen si está en lista blanca).

## Endpoints OAuth2 / OIDC

| Endpoint | Descripción |
|---|---|
| `GET /.well-known/oauth-authorization-server` | Metadata OAuth2 |
| `GET /.well-known/openid-configuration` | Metadata OIDC |
| `GET /oauth2/authorize` | Authorization endpoint |
| `POST /oauth2/token` | Token endpoint |
| `GET /oauth2/jwks` | Claves públicas RSA |
| `POST /oauth2/revoke` | Revocación de tokens |
| `POST /oauth2/introspect` | Introspección |
| `GET /oauth2/userinfo` | UserInfo OIDC |
| `GET /oauth2/oidc/logout` | RP-initiated logout OIDC |
| `GET /logout` | Formulario de confirmación logout |
| `POST /logout` | Ejecuta logout |

## API REST de administración

Todas requieren JWT con permisos específicos.

### Clientes

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/v1/client/all?page=&size=&clientName=` | `client:read` | Lista paginada |
| POST | `/api/v1/client` | `client:write` | Crear |
| PUT | `/api/v1/client` | `client:write` | Actualizar |
| DELETE | `/api/v1/client/{clientId}` | `client:delete` | Eliminar |

### Usuarios

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/v1/user/all?page=&size=&search=` | `user:read` | Lista paginada |
| GET | `/api/v1/user/{id}` | `user:read` | Por ID |
| POST | `/api/v1/user` | `user:write` | Crear |
| PUT | `/api/v1/user/{id}` | `user:write` | Actualizar |
| DELETE | `/api/v1/user/{id}` | `user:delete` | Eliminar |

### Roles

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/v1/role/all?page=&size=&search=` | `role:read` | Lista paginada |
| GET | `/api/v1/role/{id}` | `role:read` | Por ID |
| POST | `/api/v1/role` | `role:write` | Crear |
| PUT | `/api/v1/role/{id}` | `role:write` | Actualizar |
| DELETE | `/api/v1/role/{id}` | `role:delete` | Eliminar (solo si no tiene usuarios asignados) |

### Permisos

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/v1/permission/all?page=&size=&search=` | `permission:read` | Lista paginada |
| GET | `/api/v1/permission/{id}` | `permission:read` | Por ID |
| POST | `/api/v1/permission` | `permission:write` | Crear |
| PUT | `/api/v1/permission/{id}` | `permission:write` | Actualizar |
| DELETE | `/api/v1/permission/{id}` | `permission:delete` | Eliminar (solo si no tiene roles asignados) |
| DELETE | `/api/v1/permission/batch` | `permission:delete` | Eliminar batch (solo si ninguno tiene roles asignados) |

Swagger UI: `<servidor_base>/swagger-ui/index.html`

## Modelo de datos

```
Client (clients)
├── clientId, clientSecret, clientName
├── authenticationMethods (ElementCollection)
├── authorizationGrantTypes (ElementCollection)
├── redirectUris, postLogoutRedirectUris (ElementCollection)
├── scopes (ElementCollection)
├── requireProofKey (boolean)
└── ...

User (app_user)
├── username, email, password (BCrypt)
├── name, surname1/2, phone, address
├── disabled, expired, locked, credentialsExpired
├── isGoogle, isMicrosoft, isFacebook, isGitHub
└── roles → Role (ManyToMany → app_user_role)

Role (role)
├── name (ROLE_ADMIN, ROLE_USER, etc.)
├── description, active
└── permissions → Permission (ManyToMany → role_permissions)

Permission (permissions)
├── name (user:read, client:write, etc.)
├── description, active
└── (inverse side, no direct relationship mapping)
```

### Permisos pre-creados

`user:read`, `user:write`, `user:delete`, `client:read`, `client:write`, `client:delete`, `role:read`, `role:write`, `role:delete`, `permission:read`, `permission:write`, `permission:delete`.

### Roles pre-creados

| Rol | Permisos |
|---|---|
| `ROLE_ADMIN` | Todos los permisos |
| `ROLE_USER` | Solo lectura: `user:read`, `client:read`, `role:read`, `permission:read` |

### Usuarios pre-creados

| Username | Email | Password | Rol |
|---|---|---|---|
| `admin` | `admin@email.com` | `admin123` | ROLE_ADMIN |
| `user` | `user@email.com` | `user123` | ROLE_USER |

## Logout

Hay tres formas de cerrar sesión:

### 1. RP-initiated logout (OIDC)
Cliente redirige a `GET /oauth2/oidc/logout?post_logout_redirect_uri=...&id_token_hint=...`. Spring AS valida contra los `postLogoutRedirectUris` del cliente.

### 2. Endpoint `/exit`
`GET /exit?client_id=<client_id>`. El servidor busca el cliente en BD, obtiene su primer `postLogoutRedirectUri` y redirige allí. Si no hay `client_id` o no hay URIs configuradas, redirige a `/login?logout`.

### 3. Spring Security (`POST /logout`)
Desde la página de administración Thymeleaf. El `DynamicLogoutSuccessHandler` redirige al origen si está en `ALLOWED_ORIGINS`, o a `/login?logout` en caso contrario.

## Autenticación con Google OAuth2

El servidor soporta login con Google. En `/login` hay un botón "Login with Google". La configuración requiere `spring.security.oauth2.client.registration.google.client-id` y `client-secret` en `application.yml`. Tras autenticarse con Google, se crea un usuario local si no existe (por email) y se genera una sesión.

## Configuración

### `application.yml`

```yaml
server:
  port: <puerto>

base:
  url: <base_url>

spring:
  datasource:
    url: jdbc:postgresql://<host>:5432/<db>
    username: <user>
    password: <pass>
  jpa:
    hibernate:
      ddl-auto: update
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
```

El issuer se construye como `{base.url}:{server.port}`. Si se cambia el puerto o la URL base, hay que actualizar `CorsConfiguration.java` y `DynamicLogoutSuccessHandler.java` con los orígenes permitidos.

### CORS

Configurado en `CorsConfiguration.java`. Los orígenes permitidos se definen mediante variables de entorno o configuración, e incluyen las URLs del frontend y esquemas personalizados para aplicaciones nativas (ej. `capacitor://localhost`).

## Ejecución

```bash
cd authorization-server
./mvnw spring-boot:run
```

Requiere Java 21 y PostgreSQL accesible.

## Probar el flujo

### Con OAuth Debugger

1. Abrir [https://oauthdebugger.com/](https://oauthdebugger.com/)
2. Authorization URL: `<servidor_base>/oauth2/authorize`
3. Token URL: `<servidor_base>/oauth2/token`
4. Client ID: `oidc-client`, Scope: `openid`
5. Redirect URI: `https://oauthdebugger.com/debug`
6. Grant Type: `Authorization Code (PKCE)` (aunque no requiere PKCE, funciona)
7. Iniciar sesión con `admin@email.com` / `admin123`

### Con el frontend

```bash
cd apps/<frontend-app>
ng serve --host 0.0.0.0
```

Ir a `<frontend_url>` e iniciar sesión. El cliente redirige al servidor, recibe el código, lo canjea por tokens (Basic Auth) y redirige al panel de administración.

### Con Swagger UI

Ir a `<servidor_base>/swagger-ui/index.html`, autenticarse con OAuth2 usando `swagger-ui` como client ID (PKCE) y credenciales de usuario.

### Con curl (Client Credentials)

```bash
curl -X POST <servidor_base>/oauth2/token \
  -H "Authorization: Basic $(echo -n <client_id>:<client_secret> | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=openid"
```

### Obtener token con Authorization Code (PKCE)

```bash
# 1. Generar code verifier y challenge
CODE_VERIFIER=$(openssl rand -base64 48 | tr -d '=+/')
CODE_CHALLENGE=$(echo -n "$CODE_VERIFIER" | openssl dgst -sha256 -binary | openssl base64 -A | tr '+/' '-_' | tr -d '=')

# 2. Navegar en navegador a:
echo "<servidor_base>/oauth2/authorize?response_type=code&client_id=<client_id>&redirect_uri=<frontend_url>/&scope=openid+offline_access&code_challenge_method=S256&code_challenge=$CODE_CHALLENGE"

# 3. Copiar el code de la redirect y canjear:
AUTH_CODE=<code>
curl -X POST <servidor_base>/oauth2/token \
  -H "Authorization: Basic $(echo -n <client_id>:<client_secret> | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&client_id=<client_id>&redirect_uri=<frontend_url>/&code=$AUTH_CODE&code_verifier=$CODE_VERIFIER"
```

Nota: `openssl` en Windows puede requerir Git Bash o WSL.
