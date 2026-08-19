# Authorization Server — Spring Boot OAuth2

Servidor de autorización OAuth2 / OpenID Connect construido con **Spring Authorization Server** (v7.0.4) sobre **Spring Boot 4.1** (Java 21 + Spring Framework 7). Gestiona autenticación de usuarios, emisión de tokens JWT firmados con RSA, registro de clientes OAuth2, y expone una API REST protegida para CRUD de usuarios, roles, permisos y clientes.

---

## Índice

- [Arquitectura y tecnologías](#arquitectura-y-tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuración](#configuración)
  - [application.yml](#applicationyml)
  - [Variables de entorno](#variables-de-entorno)
  - [application.yml.example](#applicationymlexample)
- [Seguridad y autenticación](#seguridad-y-autenticación)
  - [Tipos de autenticación (grant types)](#tipos-de-autenticación-grant-types)
  - [Métodos de autenticación de cliente](#métodos-de-autenticación-de-cliente)
  - [Flujo PKCE obligatorio](#flujo-pkce-obligatorio)
  - [Claims del JWT](#claims-del-jwt)
  - [OAuth2 Login (Google)](#oauth2-login-google)
- [API REST](#api-rest)
  - [UserController](#usercontroller)
  - [RoleController](#rolecontroller)
  - [PermissionController](#permissioncontroller)
  - [ClientController](#clientcontroller)
  - [Resumen de autoridades requeridas](#resumen-de-autoridades-requeridas)
- [Generación del cliente OpenAPI](#generación-del-cliente-openapi)
  - [Cómo springdoc-openapi genera la spec](#cómo-springdoc-openapi-genera-la-spec)
  - [Por qué `produces = "application/json"` es crítico](#por-qué-produces--applicationjson-es-crítico)
  - [Consumir la API desde el frontend](#consumir-la-api-desde-el-frontend)
- [Clientes OAuth2 preconfigurados](#clientes-oauth2-preconfigurados)
  - [oidc-client (OIDC Debugger)](#oidc-client-oidc-debugger)
  - [identity-client (main-app)](#identity-client-main-app)
  - [swagger-ui](#swagger-ui)
- [Ejecución](#ejecución)
  - [Requisitos](#requisitos)
  - [Desarrollo](#desarrollo)
  - [Perfil Maven para generar spec OpenAPI](#perfil-maven-para-generar-spec-openapi)
- [Modelo de datos](#modelo-de-datos)
  - [Entidades](#entidades)
  - [Seed data (DataInitializer)](#seed-data-datainitializer)

---

## Arquitectura y tecnologías

| Tecnología | Versión | Propósito |
|---|---|---|
| **Spring Boot** | 4.1.0 (Spring Framework 7) | Contenedor IoC, configuración automática, servlet embebido (Tomcat) |
| **Spring Authorization Server** | 7.0.4 | Protocolo OAuth2 / OpenID Connect: endpoints `/oauth2/authorize`, `/oauth2/token`, `/oauth2/revoke`, `/.well-known/openid-configuration` |
| **Spring Security** | 7.x | Filter chains, autenticación, autorización por método (`@PreAuthorize`) |
| **Spring Data JPA / Hibernate** | 7.x | ORM, repositorios, gestión transaccional |
| **PostgreSQL** | — | Base de datos (Neon.tech serverless o local) |
| **springdoc-openapi** | 3.0.0 | Spec OpenAPI 3.1 en `/api-docs` + Swagger UI en `/swagger-ui.html` |
| **Nimbus JOSE + JWT** | — | Generación y verificación de JWKs RSA, encoding/decoding de JWT |
| **Lombok** | — | Reducción de boilerplate (`@Slf4j`, `@RequiredArgsConstructor`, `@Builder`) |
| **Thymeleaf** | — | Templates HTML de login, registro, home y éxito |
| **BCrypt** | — | Hashing de contraseñas (vía `PasswordEncoder`) |

### Criterios de implementación

- **Sin Keycloak ni Okta**: El proyecto implementa un servidor de autorización propio desde cero usando la biblioteca oficial `spring-security-oauth2-authorization-server`. No depende de soluciones externas ni productos comerciales.
- **JWT autogestionado**: Las claves RSA se generan en memoria al arrancar (`KeyPairGenerator`), sin necesidad de keystore externo ni infraestructura PKI. Cada reinicio genera un nuevo par de claves → todos los tokens anteriores se invalidan.
- **PKCE obligatorio**: Todos los clientes OAuth2 deben usar `requireProofKey = true` (PKCE S256). No se permite el flujo `authorization_code` sin PKCE.
- **Protección por autoridades**: Cada endpoint REST verifica autoridades específicas mediante `@PreAuthorize`. No se usan roles genéricos.
- **dual auth**: Soporta tanto autenticación por formulario (usuarios locales) como OAuth2 Login (Google) para la consola web.
- **springdoc-openapi para generación de clientes**: La spec OpenAPI se genera automáticamente desde los controladores, lo que permite al frontend generar un cliente TypeScript tipado.

---

## Estructura del proyecto

```
authorization-server/
├── pom.xml                                    # Build Maven, dependencias, perfil openapi
├── README.md
├── src/
│   └── main/
│       ├── java/com/authcore/authapp/
│       │   ├── AuthApp.java                   # @SpringBootApplication + @EnableJpaAuditing
│       │   ├── configuration/
│       │   │   ├── DataInitializer.java       # Seed: permisos, roles, usuarios, clientes
│       │   │   └── OpenApiConfig.java         # Configuración de seguridad OAuth2 en Swagger
│       │   │   └── security/
│       │   │       ├── AuthorizationServerSecurityConfig.java  # 4 filter chains
│       │   │       ├── JwkConfiguration.java                  # RSA keys, JWT claims customizer
│       │   │       ├── CorsConfiguration.java                 # CORS para frontends
│       │   │       ├── PasswordEncoderConfig.java             # BCrypt
│       │   │       ├── FormLoginSuccessHandler.java           # Redirección post-login local
│       │   │       ├── OAuth2LoginSuccessHandler.java         # Redirección post-login Google
│       │   │       └── DynamicLogoutSuccessHandler.java       # Logout con redirect dinámico
│       │   ├── controllers/
│       │   │   ├── UserController.java        # CRUD usuarios
│       │   │   ├── RoleController.java        # CRUD roles
│       │   │   ├── PermissionController.java  # CRUD permisos
│       │   │   ├── ClientController.java      # CRUD clientes OAuth2
│       │   │   ├── LoginController.java       # Login, registro, logout, /success
│       │   │   └── HomeController.java        # Pantalla principal (consola web)
│       │   ├── models/
│       │   │   ├── AppUser.java               # Entidad usuario
│       │   │   ├── Role.java                  # Entidad rol
│       │   │   ├── Permission.java            # Entidad permiso
│       │   │   └── Client.java                # Entidad cliente OAuth2
│       │   ├── dto/
│       │   │   ├── AppResponseDto.java        # Respuesta genérica {status, message}
│       │   │   ├── user/                      # AppUserCreateDto, AppUserResponseDto, AppUserUpdateDto
│       │   │   ├── role/                      # RoleCreateDto, RoleResponseDto, RoleUpdateDto
│       │   │   ├── permission/                # PermissionCreateDto, PermissionResponseDto, PermissionUpdateDto
│       │   │   └── client/                    # ClientCreateDto, ClientResponseDto, ClientUpdateDto
│       │   ├── services/
│       │   │   ├── user/AppUserService.java
│       │   │   ├── role/RoleService.java
│       │   │   ├── permission/PermissionService.java
│       │   │   └── client/ClientService.java
│       │   ├── repository/
│       │   │   ├── AppUserRepository.java
│       │   │   ├── RoleRepository.java
│       │   │   ├── PermissionRepository.java
│       │   │   └── ClientRepository.java
│       │   └── utils/
│       │       └── UserDetailsServiceImpl.java # Carga usuarios por email (UserDetailsService)
│       └── resources/
│           ├── application.yml                # Configuración principal
│           ├── application.yml.example        # Ejemplo con variables de entorno
│           ├── static/main.css                # Estilos consola web
│           ├── templates/
│           │   ├── home.html                  # Home (consola admin)
│           │   ├── login.html                 # Formulario login + registro
│           │   └── success.html               # Post-login exitoso
│           ├── schema.sql                     # Esquema BD
│           └── data.sql                       # Datos iniciales
```

---

## Configuración

### application.yml

El archivo de configuración principal está en `src/main/resources/application.yml`. Incluye valores reales para desarrollo.

<details>
<summary>Ver application.yml completo</summary>

```yaml
spring:
  profiles:
    active: dev

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
              - openid
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

  datasource:
    url: jdbc:postgresql://<host>:5432/<db_name>?createDatabaseIfNotExist=true
    username: <db_user>
    password: <db_password>

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    defer-datasource-initialization: true
    hibernate:
      ddl-auto: update
    generate-ddl: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  sql:
    init:
      mode: always

logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG

server:
  port: 9000

base:
  url: http://localhost

defaults:
  admin-role: ROLE_ADMIN
  user-role: ROLE_USER
  admin-email: admin@email.com
  admin-password: admin123
  user-email: user@email.com
  user-password: user123
  client-id: oidc-client
  client-secret: secret
  client-name: OIDC Debugger
  client-scope: openid
  identity-client-secret: identity-secret
  client-credentials-grant-type: client_credentials
  authorization-code-grant-type: authorization_code
  refresh-token-grant-type: refresh_token
  redirect-uri: https://oauthdebugger.com/debug

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    oauth:
      client-id: swagger-ui
      use-pkce-with-authorization-code-grant: true
  api-docs:
    path: /api-docs
```
</details>

### Variables de entorno

| Variable | Descripción | Valor por defecto (desarrollo) |
|---|---|---|
| `GOOGLE_CLIENT_ID` | Client ID de Google OAuth 2.0 | _(real)_ |
| `GOOGLE_CLIENT_SECRET` | Client Secret de Google OAuth 2.0 | _(real)_ |
| `SPRING_DATASOURCE_URL` | URL de conexión PostgreSQL | `jdbc:postgresql://localhost:5432/usersdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuario BD | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña BD | `postgres` |
| `SERVER_PORT` | Puerto del servidor | `9000` |
| `BASE_URL` | URL base para CORS e issuer JWT | `http://localhost` |

### application.yml.example

El archivo `application.yml.example` es idéntico pero con variables de entorno simuladas (`${GOOGLE_CLIENT_ID}`, `${GOOGLE_CLIENT_SECRET}`, `${SPRING_DATASOURCE_URL}`) para usar en producción o entornos compartidos. Copia este archivo como `application.yml` y sustituye los valores:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Editar con los valores reales
```

---

## Seguridad y autenticación

### Tipos de autenticación (grant types)

El servidor soporta los siguientes **grant types** OAuth 2.0:

| Grant type | Endpoint | Uso | ¿Habilitado? |
|---|---|---|---|
| `authorization_code` | `GET /oauth2/authorize` + `POST /oauth2/token` | Frontend web (PKCE obligatorio) | ✅ Siempre |
| `refresh_token` | `POST /oauth2/token` | Renovar access token sin re-autenticar | ✅ Siempre |
| `client_credentials` | `POST /oauth2/token` | Comunicación máquina a máquina | ⚠️ Solo si el cliente lo incluye |

**No soportados** (explícitamente deshabilitados):
- `password` (deprecated en OAuth 2.1)
- `implicit` (deprecated en OAuth 2.1)
- `device_code`

### Métodos de autenticación de cliente

| Método | Descripción | Usado por |
|---|---|---|
| `client_secret_basic` | Basic Auth (cabecera `Authorization: Basic base64(client_id:client_secret)`) | `oidc-client`, `identity-client` |
| `none` | Cliente público sin secreto | `swagger-ui`, `identity-client` (para PKCE) |

### Flujo PKCE obligatorio

Todos los clientes tienen `requireProofKey = true`. El frontend debe:

1. Generar un `code_verifier` (43–128 caracteres aleatorios)
2. Calcular el `code_challenge` = `base64url(sha256(code_verifier))`
3. Enviar `code_challenge` y `code_challenge_method=S256` en la petición a `/oauth2/authorize`
4. Al canjear el código, enviar el `code_verifier` original a `/oauth2/token`

Sin PKCE, el servidor rechaza la petición con `error: "invalid_request"`.

### Claims del JWT

El access token JWT se personaliza en `JwkConfiguration.tokenCustomizer()` e incluye:

| Claim | Tipo | Ejemplo | Descripción |
|---|---|---|---|
| `iss` | string | `http://localhost:9000` | Issuer (configurado vía `AuthorizationServerSettings`) |
| `sub` | string | `admin@email.com` | Principal name (email) |
| `exp` | number (epoch) | `1719212345` | Fecha de expiración |
| `iat` | number (epoch) | `1719212045` | Fecha de emisión |
| `token_type` | string | `"access_token"` | Tipo de token |
| `user_id` | string (UUID) | `"5e2e6f6e-a44d-430d-8c96-55faa56e7301"` | ID del usuario en BD |
| `user` | string | `"admin@email.com"` | Nombre del principal (email) |
| `authorities` | string[] | `["user:read","user:write","ROLE_ADMIN"]` | Autoridades completas |
| `roles` | string[] | `["ROLE_ADMIN"]` | Autoridades que empiezan con `ROLE_` |
| `permissions` | string[] | `["user:read","user:write"]` | Autoridades que NO empiezan con `ROLE_` |

El id_token solo incluye `token_type: "id_token"` como claim personalizado.

**Importante**: El claim `user_id` es el UUID que usa el frontend para consultar el perfil vía `GET /api/v1/user/{id}`. Sin él, no se puede obtener el usuario autenticado.

### OAuth2 Login (Google)

El servidor soporta **Login con Google** como proveedor OAuth2 externo. Cuando un usuario se autentica con Google:

1. Spring Security redirige a `/oauth2/authorization/google`
2. Google valida al usuario y redirige de vuelta con un código
3. `OAuth2LoginSuccessHandler` crea o recupera el usuario local por email
4. Se genera una sesión JSESSIONID para la consola web

Las credenciales de Google (`client-id`, `client-secret`) se configuran en `spring.security.oauth2.client.registration.google`.

---

## API REST

Base path: `http://localhost:9000/api/v1`

Todos los endpoints REST requieren un **access token JWT** en la cabecera `Authorization: Bearer <token>`. El token se obtiene mediante el flujo `authorization_code` + PKCE.

### UserController

`@RequestMapping(value = "/api/v1/user", produces = "application/json")`

| Método | Endpoint | Autoridad | Cuerpo | Respuesta |
|---|---|---|---|---|
| `POST` | `/api/v1/user` | `user:write` | `AppUserCreateDto` | `AppResponseDto` (201) |
| `GET` | `/api/v1/user/{id}` | `user:read` | — | `AppUserResponseDto` (200) |
| `GET` | `/api/v1/user/all?search=&page=0&size=10` | `user:read` | — | `Page<AppUserResponseDto>` (200) |
| `PUT` | `/api/v1/user/{id}` | `user:write` | `AppUserUpdateDto` | `AppResponseDto` (200) |
| `DELETE` | `/api/v1/user/{id}` | `user:delete` | — | `AppResponseDto` (200) |

**AppUserResponseDto**:
```json
{
  "id": "5e2e6f6e-a44d-430d-8c96-55faa56e7301",
  "username": "admin",
  "email": "admin@email.com",
  "name": "Admin",
  "surname1": null,
  "surname2": null,
  "address": null,
  "phone": null,
  "metadata": null,
  "expired": false,
  "locked": false,
  "credentialsExpired": false,
  "disabled": false,
  "roles": ["ROLE_ADMIN"],
  "createdAt": "2026-06-21T20:08:28.132194",
  "updatedAt": "2026-06-22T16:57:37.181483",
  "facebook": false,
  "microsoft": false,
  "google": false,
  "gitHub": false
}
```

### RoleController

`@RequestMapping(value = "/api/v1/role", produces = "application/json")`

| Método | Endpoint | Autoridad | Cuerpo | Respuesta |
|---|---|---|---|---|
| `POST` | `/api/v1/role` | `role:write` | `RoleCreateDto` | `AppResponseDto` (201) |
| `GET` | `/api/v1/role/{id}` | `role:read` | — | `RoleResponseDto` (200) |
| `GET` | `/api/v1/role/all?search=&page=0&size=10` | `role:read` | — | `Page<RoleResponseDto>` (200) |
| `PUT` | `/api/v1/role/{id}` | `role:write` | `RoleUpdateDto` | `AppResponseDto` (200) |
| `DELETE` | `/api/v1/role/{id}` | `role:delete` | — | `AppResponseDto` (200) |

### PermissionController

`@RequestMapping(value = "/api/v1/permission", produces = "application/json")`

| Método | Endpoint | Autoridad | Cuerpo | Respuesta |
|---|---|---|---|---|
| `POST` | `/api/v1/permission` | `permission:write` | `PermissionCreateDto` | `AppResponseDto` (201) |
| `GET` | `/api/v1/permission/{id}` | `permission:read` | — | `PermissionResponseDto` (200) |
| `GET` | `/api/v1/permission/all?search=&page=0&size=10` | `permission:read` | — | `Page<PermissionResponseDto>` (200) |
| `PUT` | `/api/v1/permission/{id}` | `permission:write` | `PermissionUpdateDto` | `AppResponseDto` (200) |
| `DELETE` | `/api/v1/permission/{id}` | `permission:delete` | — | `AppResponseDto` (200) |
| `DELETE` | `/api/v1/permission/batch` | `permission:delete` | `List<String>` (IDs) | `AppResponseDto` (200) |

### ClientController

`@RequestMapping(value = "/api/v1/client", produces = "application/json")`

| Método | Endpoint | Autoridad | Cuerpo | Respuesta |
|---|---|---|---|---|
| `POST` | `/api/v1/client` | `client:write` | `ClientCreateDto` | `AppResponseDto` (201) |
| `PUT` | `/api/v1/client` | `client:write` | `ClientUpdateDto` | `AppResponseDto` (201) |
| `GET` | `/api/v1/client/all?clientName=&page=0&size=10` | `client:read` | — | `Page<ClientResponseDto>` (200) |
| `DELETE` | `/api/v1/client/{clientId}` | `client:delete` | — | `AppResponseDto` (200) |

### Resumen de autoridades requeridas

| Autoridad | Recurso |
|---|---|
| `user:read` | Ver usuarios |
| `user:write` | Crear/editar usuarios |
| `user:delete` | Eliminar usuarios |
| `role:read` | Ver roles |
| `role:write` | Crear/editar roles |
| `role:delete` | Eliminar roles |
| `permission:read` | Ver permisos |
| `permission:write` | Crear/editar permisos |
| `permission:delete` | Eliminar permisos |
| `client:read` | Ver clientes OAuth2 |
| `client:write` | Crear/editar clientes OAuth2 |
| `client:delete` | Eliminar clientes OAuth2 |

Las autoridades se asignan a través de los roles. El rol `ROLE_ADMIN` incluye todas las autoridades. El rol `ROLE_USER` incluye solo las de lectura (excepto `client:*`).

---

## Generación del cliente OpenAPI

### Cómo springdoc-openapi genera la spec

1. `springdoc-openapi-starter-webmvc-ui` escanea los controladores en busca de anotaciones `@RestController`, `@RequestMapping`, `@GetMapping`, etc.
2. Para cada endpoint, genera una entrada en la spec OpenAPI 3.1 con:
   - **URL** y método HTTP
   - **Parámetros** (path, query, request body)
   - **Códigos de respuesta** y tipos
   - **Requisitos de seguridad** (OAuth2)
3. La spec se sirve en `GET /api-docs`
4. `OpenApiConfig.java` añade el esquema de seguridad OAuth2 (authorization code flow) que permite probar los endpoints desde Swagger UI.

### Por qué `produces = "application/json"` es crítico

Sin `produces` en el `@RequestMapping`, Spring asume `*/*` como content type de respuesta. springdoc-openapi refleja esto como `"produces": ["*/*"]` en la spec.

Cuando el generador OpenAPI TypeScript procesa `["*/*"]`:

1. `selectHeaderAccept(['*/*'])` devuelve `'*/*'`
2. `isJsonMime('*/*')` → `false` (el regex espera `application/json` o `*+json`)
3. El `responseType` se settea a `'blob'`
4. Angular `HttpClient` devuelve un `Blob` en vez del objeto JSON parseado

Con `produces = "application/json"`:

1. `selectHeaderAccept(['application/json'])` devuelve `'application/json'`
2. `isJsonMime('application/json')` → `true`
3. `responseType` se queda como `'json'`
4. Angular `HttpClient` devuelve el objeto tipado correctamente

### Cómo se refleja en los controladores

```java
// ❌ Sin produces — la spec genera ["*/*"]
@RestController
@RequestMapping("/api/v1/user")
public class UserController { ... }

// ✅ Con produces — la spec genera ["application/json"]
@RestController
@RequestMapping(value = "/api/v1/user", produces = "application/json")
public class UserController { ... }
```

### Consumir la API desde el frontend

El frontend (`apps/main-app`) genera el cliente TypeScript con:

```bash
npm run generate:client
```

Esto:
1. Descarga la spec de `http://localhost:9000/api-docs`
2. Ejecuta `openapi-generator-cli` con el generador `typescript-angular`
3. Un script post-generación copia los servicios a `services/` y los DTOs a `services/dtos/`
4. Ajusta los imports para que apunten a la infraestructura correcta

El interceptor HTTP del frontend añade automáticamente el Bearer token del JWT a todas las peticiones contra la URL base del auth-server.

---

## Clientes OAuth2 preconfigurados

### oidc-client (OIDC Debugger)

| Propiedad | Valor |
|---|---|
| `clientId` | `oidc-client` |
| `clientSecret` | `secret` (BCrypt hasheado) |
| `authMethods` | `client_secret_basic`, `none` |
| `grantTypes` | `authorization_code`, `refresh_token`, `client_credentials` |
| `redirectUris` | `https://oauthdebugger.com/debug` |
| `scopes` | `openid`, `profile` |
| `requireProofKey` | `true` |

Útil para depurar el flujo OAuth2 con herramientas externas.

### identity-client (main-app)

| Propiedad | Valor |
|---|---|
| `clientId` | `identity-client` |
| `clientSecret` | `identity-secret` (BCrypt hasheado) |
| `authMethods` | `client_secret_basic`, `none` |
| `grantTypes` | `authorization_code`, `refresh_token` |
| `redirectUris` | `http://localhost:4200/`, `http://192.168.1.19:4200/` |
| `scopes` | `openid`, `profile`, `offline_access` |
| `requireProofKey` | `true` |

Usado por la aplicación Angular `identity-app`. El flujo es:

1. El frontend inicia PKCE y redirige a `/oauth2/authorize?client_id=identity-client&...`
2. El usuario se autentica (formulario o Google)
3. El servidor redirige al frontend con un `code`
4. El frontend canjea el código por tokens usando Basic Auth (`identity-client:identity-secret`)
5. El access token JWT se usa para llamar a la API REST

### swagger-ui

| Propiedad | Valor |
|---|---|
| `clientId` | `swagger-ui` |
| `clientSecret` | `null` (público) |
| `authMethods` | `none` |
| `grantTypes` | `authorization_code` |
| `redirectUris` | `http://localhost:9000/swagger-ui/oauth2-redirect.html` |
| `scopes` | `openid`, `profile`, `users.read`, `users.write`, `roles.read`, `roles.write` |
| `requireProofKey` | `true` |

Cliente público que permite probar los endpoints protegidos desde Swagger UI (`/swagger-ui.html`). Usa PKCE con redirección local.

---

## Ejecución

### Requisitos

- Java 21+
- Maven (incluye `mvnw` wrapper)
- PostgreSQL 15+ (local o remoto como Neon.tech)
- Node.js 22+ (solo para regenerar el cliente frontend)

### Desarrollo

1. **Clonar y configurar**

```bash
git clone <repo>
cd authorization-server
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Editar application.yml con tus credenciales de BD y Google OAuth2
```

2. **Arrancar**

```bash
./mvnw spring-boot:run
```

El servidor arranca en `http://localhost:9000`.

3. **Verificar**

```bash
# Swagger UI (requiere sesión en navegador)
open http://localhost:9000/swagger-ui.html

# Spec OpenAPI
curl http://localhost:9000/api-docs | jq .

# Well-known OpenID Configuration
curl http://localhost:9000/.well-known/openid-configuration | jq .
```

4. **Credenciales por defecto**

| Usuario | Email | Contraseña |
|---|---|---|
| Admin | `admin@email.com` | `admin123` |
| User | `user@email.com` | `user123` |

### Perfil Maven para generar spec OpenAPI

```bash
./mvnw compile -Pgenerate-openapi-spec
# → target/openapi.json
```

Este perfil usa el plugin `springdoc-openapi-maven-plugin` para:
1. Arrancar el servidor embebido
2. Llamar a `http://localhost:9000/api-docs`
3. Guardar la spec en `target/openapi.json`

> El servidor debe estar en ejecución en `localhost:9000` para que funcione. Si el puerto está ocupado, el plugin no arranca su propio servidor — falla. Úsalo solo con el servidor ya levantado.

---

## Modelo de datos

### Entidades

**`AppUser`** (tabla `app_user`)
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` (PK) | Autogenerado |
| `username` | `String` | Nombre de usuario único |
| `email` | `String` | Email único (usado como principal name) |
| `password` | `String` | BCrypt hasheado |
| `name` | `String` | Nombre visible |
| `surname1`, `surname2` | `String?` | Apellidos |
| `address`, `phone` | `String?` | Datos de contacto |
| `metadata` | `String?` | Metadatos JSON |
| `expired`, `locked` | `boolean` | Estado de la cuenta |
| `disabled` | `boolean` | Cuenta deshabilitada |
| `credentialsExpired` | `boolean` | Credenciales expiradas |
| `facebook`, `google`, `gitHub`, `microsoft` | `boolean` | Proveedor OAuth2 usado para el registro |
| `createdAt`, `updatedAt` | `LocalDateTime` | Timestamps (auditoría) |
| `roles` | `Set<Role>` | Many-to-Many con `app_user_role` |

**`Role`** (tabla `role`)
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` (PK) | Nombre del rol (ej: `ROLE_ADMIN`) |
| `name` | `String` | Nombre único |
| `description` | `String?` | Descripción |
| `active` | `boolean` | Si el rol está activo |
| `createdAt` | `LocalDateTime` | Timestamp |
| `permissions` | `Set<Permission>` | Many-to-Many con `role_permission` |

**`Permission`** (tabla `permission`)
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` (PK) | Nombre del permiso (ej: `user:read`) |
| `name` | `String` | Nombre único |
| `description` | `String?` | Descripción |
| `active` | `boolean` | Si el permiso está activo |
| `createdAt` | `LocalDateTime` | Timestamp |

**`Client`** (tabla `client`)
| Campo | Tipo | Descripción |
|---|---|---|
| `clientId` | `String` (PK) | Identificador del cliente OAuth2 |
| `clientSecret` | `String?` | BCrypt hasheado (null para clientes públicos) |
| `clientName` | `String?` | Nombre visible |
| `authenticationMethods` | `Set<ClientAuthenticationMethod>` | `client_secret_basic`, `none` |
| `authorizationGrantTypes` | `Set<AuthorizationGrantType>` | `authorization_code`, `refresh_token`, `client_credentials` |
| `redirectUris` | `Set<String>` | URIs permitidas post-autorización |
| `postLogoutRedirectUris` | `Set<String>` | URIs permitidas post-logout |
| `scopes` | `Set<String>` | Scopes permitidos |
| `requireProofKey` | `boolean` | PKCE obligatorio (true) |

### Seed data (DataInitializer)

Al arrancar, `DataInitializer` crea automáticamente:

**12 permisos**: `user:read`, `user:write`, `user:delete`, `client:read`, `client:write`, `client:delete`, `role:read`, `role:write`, `role:delete`, `permission:read`, `permission:write`, `permission:delete`

**2 roles**:
- `ROLE_ADMIN` → los 12 permisos
- `ROLE_USER` → permisos de lectura (excepto `client:*`)

**2 usuarios** (ver credenciales arriba)

**3 clientes OAuth2** (ver tabla de clientes arriba)

Si los datos ya existen (detectado por email del admin), se salta la inicialización. Los clientes OAuth2 se actualizan siempre (para reflejar cambios de configuración).

---

## Notas técnicas

- **Rotación de claves RSA**: Cada reinicio genera un nuevo par de claves. Todos los JWT emitidos antes del reinicio se invalidan. Para persistencia, se necesitaría un `KeyStore` en disco o un servicio externo.
- **CORS**: Configurado para `localhost:4200/4201` y `192.168.1.19:4200/4201`. Para producción, añadir los orígenes necesarios en `CorsConfiguration.java`.
- **Logout**: El endpoint `GET /exit` permite logout con redirección dinámica a orígenes permitidos. Los clientes OAuth2 pueden configurar `postLogoutRedirectUris` para logout post-redirección.
- **Swagger UI** está disponible en `/swagger-ui.html` sin autenticación, pero los endpoints requieren OAuth2 vía el cliente `swagger-ui`.
- **La consola web** (`/`) requiere autoridad `client:read`. Los usuarios con rol `ROLE_USER` ven la página `/success` sin acceso a admin.
- **El servicio `UserDetailsServiceImpl`** busca usuarios por EMAIL (no por username), ya que el email es el principal name en el flujo OAuth2.
