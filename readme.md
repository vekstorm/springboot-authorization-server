# Authorization Server — Vekstorm OAuth2 / OpenID Connect

Servidor de autorización y autenticación basado en **Spring Authorization Server 7.0.4** con **Spring Boot 4.1** (Java 21). Gestiona el ciclo completo de OAuth2 y OpenID Connect, almacenando clientes, usuarios, roles y permisos en PostgreSQL.

## Propósito

Proveer un **Authorization Server** administrable en tiempo real. Los clientes OAuth2 se almacenan en base de datos y pueden crearse, modificarse o eliminarse sin necesidad de redesplegar la aplicación. Soporta múltiples grant types, PKCE, refresh tokens, autenticación con Google OAuth2 y un panel de administración vía API REST.

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 4.1 (Spring Boot 3.x lineage) |
| Authorization Server | `spring-security-oauth2-authorization-server` 7.0.4 |
| Lenguaje | Java 21 |
| Base de datos | PostgreSQL (Neon.tech) |
| ORM | Spring Data JPA + Hibernate |
| Frontend (login/admin) | Thymeleaf + CSS |
| API Docs | Swagger (springdoc-openapi) 3.0.0 |
| Build | Maven |
| Lombok | Anotaciones para reducir boilerplate |

## Configuración (`application.yml`)

### Conexión a BD

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:5432/<db>?createDatabaseIfNotExist=true
    username: <user>
    password: <pass>
  jpa:
    hibernate:
      ddl-auto: update
    defer-datasource-initialization: true
```

### Puertos y URLs

```yaml
server:
  port: 9000
base:
  url: http://192.168.1.41
```

El **issuer** se construye automáticamente como `{base.url}:{server.port}` → `http://192.168.1.41:9000`.

### Valores por defecto (DataInitializer)

Al arrancar, se crean automáticamente (si no existen):

| Tipo | ID | Credenciales |
|---|---|---|
| Usuario admin | `admin@email.com` | `admin123` / Rol: `ROLE_ADMIN` |
| Usuario regular | `user@email.com` | `user123` / Rol: `ROLE_USER` |
| Cliente OIDC Debugger | `oidc-client` | secret: `secret` |
| Cliente Swagger UI | `swagger-ui` | sin secret (público, PKCE) |
| Cliente Identity App | `identity-client` | secret: `identity-secret` |

## Clientes registrados

### identity-client (confidencial)

- **Tipo**: Confidencial (`client_secret_basic`)
- **Secret**: `identity-secret` (almacenado como BCrypt hash)
- **Grant types**: `authorization_code`, `refresh_token`
- **PKCE**: `requireProofKey: true`
- **Redirect URIs**: `http://192.168.1.41:4200/authorized`
- **Post-logout Redirect URIs**: `http://192.168.1.41:4200/`
- **Scopes**: `openid`, `profile`, `offline_access`

Requiere Basic Auth en `/oauth2/token` con credenciales `identity-client:identity-secret`.

### oidc-client (público)

- **Tipo**: Público
- **Grant types**: `authorization_code`, `refresh_token`
- **Redirect URI**: `https://oauthdebugger.com/debug`
- **Scope**: `openid`
- Para depuración con OAuth Debugger.

### swagger-ui (público)

- **Tipo**: Público con PKCE
- **Redirect URI**: `http://192.168.1.41:9000/swagger-ui/oauth2-redirect.html`
- **Scope**: `openid`

## Security Filter Chains

El proyecto define 4 cadenas de filtros ordenadas por prioridad:

### `@Order(0)` — Swagger
Rutas de Swagger/API docs (público, sin seguridad).

### `@Order(1)` — Authorization Server
Endpoints OAuth2 (`/oauth2/*`, `/oauth2/oidc/*`). Requiere autenticación. Redirige a `/login` si no está autenticado.

### `@Order(2)` — API REST
Rutas `/api/v1/client/**`, `/api/v1/user/**`, `/api/v1/role/**`, `/api/v1/permission/**`. Autenticación vía JWT Bearer token.

### `@Order(3)` — Web (Login/Logout)
Páginas públicas: `/login`, `/register`, `/exit`, `/error`, `/main.css`, `/assets/**`, `/.well-known/**`.
Formulario de login con soporte para OAuth2 Google. Logout con `DynamicLogoutSuccessHandler`.

## Endpoints OAuth2 / OIDC

| Endpoint | Descripción |
|---|---|
| `GET /.well-known/oauth-authorization-server` | Metadata del servidor |
| `GET /oauth2/authorize` | Authorization endpoint (login + consent) |
| `POST /oauth2/token` | Token endpoint (código, refresh, client credentials) |
| `GET /oauth2/jwks` | Claves públicas JWKS |
| `POST /oauth2/revoke` | Revocación de tokens |
| `POST /oauth2/introspect` | Introspección de tokens |
| `GET /oauth2/oidc/logout` | RP-initiated logout (OIDC end_session_endpoint) |
| `GET /oauth2/oidc/userinfo` | UserInfo endpoint |

## Flujo de logout

### Desde el cliente Angular (RP-initiated logout)

El cliente redirige al **OIDC end_session_endpoint** (`/oauth2/oidc/logout`) con el `post_logout_redirect_uri` registrado. Spring AS valida contra los `postLogoutRedirectUris` del cliente y redirige al cliente.

### Desde el cliente vía `/exit`

El cliente Angular llama a:

```
GET /exit?client_id=identity-client
```

El servidor busca en BD el cliente, obtiene su primer `postLogoutRedirectUri` y redirige allí. Si no se especifica `client_id` o el cliente no tiene post-logout URIs configurados, redirige a `/login?logout`.

### Desde el propio servidor (Spring Security `/logout`)

El formulario POST `/logout` del `success.html` ejecuta el `DynamicLogoutSuccessHandler`. Si la cabecera `Origin` o `Referer` pertenece a un origen permitido (`ALLOWED_ORIGINS`), redirige a ese origen. En caso contrario, redirige a `/login?logout`.

## Refresh tokens

- Configurados con `reuseRefreshTokens(false)` — cada vez que se refresca un token, se emite uno nuevo y se invalida el anterior.
- Solo se emiten si el request de autorización incluye `scope=offline_access`.
- identity-client está configurado como confidencial con `client_secret_basic`, requisito de Spring AS para emitir refresh tokens.

## API REST de administración

Endpoints protegidos con JWT. Roles requeridos: `ROLE_ADMIN` o `ROLE_USER` con permisos específicos.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/client` | Listar clientes (paginado) |
| POST | `/api/v1/client` | Crear cliente |
| PUT | `/api/v1/client` | Actualizar cliente |
| DELETE | `/api/v1/client?clientId={id}` | Eliminar cliente |
| GET | `/api/v1/user` | Listar usuarios (paginado) |
| POST | `/api/v1/user` | Crear usuario |
| PUT | `/api/v1/user` | Actualizar usuario |
| DELETE | `/api/v1/user?userId={id}` | Eliminar usuario |
| GET | `/api/v1/role` | Listar roles |
| GET | `/api/v1/permission` | Listar permisos |

Documentación interactiva disponible en: `http://192.168.1.41:9000/swagger-ui.html`

## Probar el flujo completo

### Con OAuth Debugger

1. Abrir [https://oauthdebugger.com/](https://oauthdebugger.com/)
2. Configurar:
   - **Authorization URL**: `http://192.168.1.41:9000/oauth2/authorize`
   - **Token URL**: `http://192.168.1.41:9000/oauth2/token`
   - **Client ID**: `oidc-client`
   - **Client Secret**: (vacío — cliente público)
   - **Scope**: `openid`
   - **Redirect URI**: `https://oauthdebugger.com/debug`
   - **Grant Type**: `Authorization Code (PKCE)`
3. Iniciar sesión con `user@email.com` / `user123`

### Con el cliente Angular

1. Navegar a `http://192.168.1.41:4200/`
2. Iniciar sesión → redirige a `http://192.168.1.41:9000/login` → tras autenticación, redirige de vuelta al cliente con el `authorization_code`
3. El cliente canjea el código por tokens en `POST /oauth2/token` (con Basic Auth)
4. El cliente puede refrescar el token usando `POST /oauth2/token` con `grant_type=refresh_token`

## Ejecución

```bash
cd authorization-server
./mvnw spring-boot:run
```

Requiere Java 21 y conexión a PostgreSQL.
