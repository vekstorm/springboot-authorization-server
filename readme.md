
# Conceptos

OAuth2 --> Es un proceso de autorización

OpenId --> Proceso de autenticación

OpenIdConnect --> Engloba a ambos (autorización y autenticación)

# Definición de un puerto de arranque personalizado 

En el fichero (application.properties):

    server.port= 9000


# Endpoints de SpringSecurity

Una vez configurado y arrancado el servidor:

http://localhost:9000/.well-known/oauth-authorization-server

    {
        "issuer": "http://localhost:9000",
        "authorization_endpoint": "http://localhost:9000/oauth2/authorize",
        "device_authorization_endpoint": "http://localhost:9000/oauth2/device_authorization",
        "token_endpoint": "http://localhost:9000/oauth2/token",
        "token_endpoint_auth_methods_supported": [
            "client_secret_basic",
            "client_secret_post",
            "client_secret_jwt",
            "private_key_jwt",
            "tls_client_auth",
            "self_signed_tls_client_auth"
        ],
        "jwks_uri": "http://localhost:9000/oauth2/jwks",
        "response_types_supported": [
            "code"
        ],
        "grant_types_supported": [
            "authorization_code",
            "client_credentials",
            "refresh_token",
            "urn:ietf:params:oauth:grant-type:device_code",
            "urn:ietf:params:oauth:grant-type:token-exchange"
        ],
        "revocation_endpoint": "http://localhost:9000/oauth2/revoke",
        "revocation_endpoint_auth_methods_supported": [
            "client_secret_basic",
            "client_secret_post",
            "client_secret_jwt",
            "private_key_jwt",
            "tls_client_auth",
            "self_signed_tls_client_auth"
        ],
        "introspection_endpoint": "http://localhost:9000/oauth2/introspect",
        "introspection_endpoint_auth_methods_supported": [
            "client_secret_basic",
            "client_secret_post",
            "client_secret_jwt",
            "private_key_jwt",
            "tls_client_auth",
            "self_signed_tls_client_auth"
        ],
        "code_challenge_methods_supported": [
            "S256"
        ],
        "tls_client_certificate_bound_access_tokens": true,
        "dpop_signing_alg_values_supported": [
            "RS256",
            "RS384",
            "RS512",
            "PS256",
            "PS384",
            "PS512",
            "ES256",
            "ES384",
            "ES512"
        ]
    }

# Conexión a base de datos PostgreSQL

* Dependencia:


    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>YOUR_VERSION</version>
    </dependency>


* Propiedades de conexión a BD PostgresSQL


    spring.datasource.url=jdbc:postgresql://<DB_URL>:<DB_PORT>/<DB_NAME>?createDatabaseIfNotExist=true
    spring.datasource.username=<DB_USERNAME>
    spring.datasource.password=<DB_PASS>
    
    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
    
    spring.jpa.defer-datasource-inizialization=true
    spring.jpa.hibernate.ddl-auto=false
    spring.jpa.generate-ddl=false
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true


# Validaciones

Las validaciones deben incluirse tanto en la creación de la entidad para que la base de datos se configure con las restricciones adecuadas, como en los DTO's que usaremos en los controladores a la hora de aportar objetos de entrada en el cuerpo de la petición. 

Para la entidad usaremos las validaciones propias de jakarta especificándolas dentro de @Column. Esto se incluye al importar las dependencias requeridas para la gestión de la base de datos:

    import jakarta.persistence.*;

Ejemplo de uso 

    @Column(name = "username", unique = true, length = 50, nullable = false)

Para usar en el DTO las anotaciones de validación de Hibernate Validator (@NotNull, @Size, @Email, etc.) incluiremos una dependencia:

    <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

Ejemplo de uso:

    @NotBlank(message = "username is required")
    @Size(min = 1, max = 50)
    String username;

    @NotBlank
    @Size(min = 1, max = 100)
    String password;

    @NotBlank
    @Email(message = "Must use a valid email")
    String email;

Para más tipos de validación en función de los tipos de datos a usar https://hibernate.org/validator/


https://oauthdebugger.com/