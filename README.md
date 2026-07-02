# Students Application

A Spring Boot REST API for managing students (CRUD). This project was built to learn Spring Boot's layered architecture, and uses **Keycloak** as an OAuth2 Resource Server for security.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Domain model](#domain-model)
- [API endpoints](#api-endpoints)
- [Security (Keycloak + JWT)](#security-keycloak--jwt)
- [Running the project](#running-the-project)
- [Testing with Postman](#testing-with-postman)
- [Error handling](#error-handling)

---

## Tech stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Main language |
| Spring Boot | 3.5.16 | Framework |
| Spring Web (MVC) | — | REST controllers |
| Spring Data JPA / Hibernate | — | Database access (ORM) |
| PostgreSQL | driver 42.7.10 | Database |
| Spring Security + OAuth2 Resource Server | — | Bearer JWT authentication |
| Keycloak | 26.0.1 (docker) | Identity Provider — issues tokens |
| Lombok | 1.18.44 | Reduces boilerplate code |
| Jakarta Validation | — | DTO validation (`@NotBlank`) |
| Maven | — | Build tool |

---

## Architecture

The project follows the classic 5-layer Spring Boot structure:

```
HTTP request
    ↓
Controller   → only calls the service, handles HTTP status codes
    ↓
Service      → business logic, DTO ↔ Entity conversion
    ↓
Repository   → talks to the database only (Spring Data JPA)
    ↓
PostgreSQL
```

Before a request reaches the Controller, it also passes through the **security layer**:

```
Client (Bearer <JWT>)
    ↓
Spring Security Filter Chain
    → JwtDecoder — validates the token against Keycloak's public key
    → CustomJwtAuthenticationConverter — extracts roles from the token
    ↓
@PreAuthorize check (at controller level)
    ↓
Controller
```

---

## Domain model

### `Student` (entity)

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | `@GeneratedValue(IDENTITY)` — auto-generated |
| `firstName` | `String` | First name |
| `lastName` | `String` | Last name |
| `age` | `String` | Age |

### DTOs

- **`StudentRequestDTO`** — data coming from the client (used for `create`/`update`). Validated: `firstName`, `lastName`, `age` are all `@NotBlank`.
- **`StudentResponseDTO`** — data returned to the client (includes `id`).

**Rule:** `@RequestBody` always uses `StudentRequestDTO`; method return types always use `StudentResponseDTO`.

---

## API endpoints

Base path: `/students`

| Method | Path | Description | Access |
|---|---|---|---|
| `GET` | `/students` | Get all students | Any authenticated user |
| `GET` | `/students/id?id={id}` | Get one student | Any authenticated user |
| `POST` | `/students` | Create a new student | `ROLE_ADMIN` |
| `PUT` | `/students/{id}` | Update a student | `ROLE_ADMIN` |
| `DELETE` | `/students?id={id}` | Delete a student | `ROLE_ADMIN` |

**Request body** (`POST`/`PUT`, `StudentRequestDTO`):
```json
{
  "firstName": "Ali",
  "lastName": "Valiyev",
  "age": "20"
}
```

**Response body** (`StudentResponseDTO`):
```json
{
  "id": 1,
  "firstName": "Ali",
  "lastName": "Valiyev",
  "age": "20"
}
```

Requests without a token, or with an insufficient role, return `401 Unauthorized` or `403 Forbidden` (handled automatically by Spring Security).

---

## Security (Keycloak + JWT)

This application does not check usernames/passwords or issue tokens itself — that responsibility belongs to **Keycloak**. Our app only acts as a **Resource Server** (the party that validates incoming tokens).

### Flow

1. The client sends username/password to Keycloak's token endpoint → Keycloak returns a JWT (`access_token`)
2. The client sends `Authorization: Bearer <token>` on every request to `/students/**`
3. `SecurityConfig` (`config/SecurityConfig.java`) — `oauth2ResourceServer().jwt(...)` validates the token against Keycloak's public key, resolved from the configured `issuer-uri` (signature, expiration, issuer)
4. `CustomJwtAuthenticationConverter` (`security/CustomJwtAuthenticationConverter.java`) — converts the token's `realm_access.roles` claim into Spring Security `GrantedAuthority` objects
5. `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` at the controller level grants or denies access based on role

Sessions are **STATELESS** — the server keeps no session state; every request is validated independently via its token.

---

## Running the project

### 1. PostgreSQL

A `students_application` database is required (matches `application.yaml`):

```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/students_application
  hikari:
    username: postgres
    password: 123
```

### 2. Keycloak

Using the docker-compose file already available in the `global-services` project:

```bash
cd ../global-services
docker compose -f keycloak.yml up -d
```

Keycloak: `http://localhost:9080` (admin / admin).

**Important:** the current `issuer-uri` in `application.yaml` (`http://localhost:9080/realms/master`) is a temporary placeholder. Once the `students-app` realm, `students-service` client, and `ROLE_ADMIN`/`ROLE_USER` roles are created in Keycloak, update it to:

```yaml
issuer-uri: http://localhost:9080/realms/students-app
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

App: `http://localhost:8080`

---

## Testing with Postman

**1. Get a token from Keycloak:**

```
POST http://localhost:9080/realms/students-app/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=students-service
client_secret=<client secret from Keycloak>
username=<test user>
password=<password>
```

**2. Use the token to call the API:**

```
GET http://localhost:8080/students
Authorization: Bearer <access_token>
```

---

## Error handling

`GlobalExceptionHandler` (`exception/GlobalExceptionHandler.java`):

| Exception | HTTP status |
|---|---|
| `StudentNotFoundException` | `404 Not Found` |
| `MethodArgumentNotValidException` (validation error) | `400 Bad Request` |

Authentication/authorization errors (`401`/`403`) are returned automatically by Spring Security — no custom code needed.

---