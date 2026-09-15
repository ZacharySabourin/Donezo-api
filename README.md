# Donezo-API

A production-ready, security-focused RESTful API for multi-user Todo management built with **Spring Boot 4.1**, **Spring Security 6**, **Spring JDBC**, and backed by PostgreSQL. This application serves as the backend for a multi-user Todo management system, featuring cookie-based JWT authentication, SPA-ready CSRF double-submit cookies, and standardized RFC 9457 Problem Details for HTTP APIs error responses.

---

## Key Features

* **JWT Authentication:** Stateless session management using encrypted JWTs stored in secure, `HttpOnly` cookies.
* **SPA-Ready CSRF Protection:** Double-submit cookie pattern (`XSRF-TOKEN`).
* **High-Performance Data Layer:** Pure SQL execution using Spring's `NamedParameterJdbcTemplate` with customized `RowMapper` implementations for optimized database performance.
* **Partial Updates (PATCH):** Type-safe partial entity updates using Java 17+ `Optional<T>` fields within DTO records.
* **Standardized Exception Handling:** Centralized exception translation via `@RestControllerAdvice` delivering RFC 9457 `ProblemDetail` responses.
* **Comprehensive Validation:** Strict input validation on DTO payloads using Jakarta Validation constraints (`@NotBlank`, `@Size`, `@NotNull`).

---

## Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot 3.x (Spring Web, Spring Security)
* **Database Access:** Spring JDBC (`NamedParameterJdbcTemplate`)
* **Security:** JWT (JSON Web Tokens), CSRF cookies, BCrypt Password Hashing
* **Documentation:** Standardized Javadoc across all layers

---

## Getting Started & Setup

This guide walks you through setting up and running the API either for local development or within Docker containers.

---

### Prerequisites

Ensure you have the following installed on your machine:

* **Java 17** or higher
* **Apache Maven 3.9+**
* **Docker** & **Docker Compose**
* **Make** (build automation utility)

---

### Environment Variables Configuration

The application requires specific environment variables for database connectivity and JWT security.

Create an `.env` file in the project root or export the following variables in your terminal:

```bash
export TODO_DB=tododb
export POSTGRES_USER=postgres
export POSTGRES_PWD=secretpassword
export JWT_SECRET=your_super_secret_key_that_is_at_least_256_bits_long
export DONEZO-URL=the_url_of_your_frontend_app
```

---

### Local Development (dev Profile)

In local development mode, the application runs on port 8090, connects to PostgreSQL on localhost:10100, allows CORS origins from <http://localhost:5173> (Vite SPA default), and enables TRACE level security logs.

1. Run the Application
Execute the dev-start recipe from the Makefile:

```bash
make dev-start
```

API Base URL: `http://localhost:8090/api`

Active Profile: `dev`

Ensure your local PostgreSQL database is running on port 10100 and matching your configured credentials.

### Containerized Deployment (Docker)

To run the fully containerized application stack via Docker Compose, run the following command to clean, compile, package, rebuild Docker images, and start the service in detached mode:

```bash
make compile-start
```

API Base URL: `http://localhost:10100/api`

Active Profile: default

To bring down containerized services:

```bash
docker compose down
```

### Useful Makefile Commands

| Command              | Descriptionmake                                                       |
|----------------------|-----------------------------------------------------------------------|
| `dev-start`          | Runs the application locally using the dev Spring profile (port 8080) |
| `make clean`         | Cleans the Maven build directory                                      |
| `make compile-quick` | Builds and packages the JAR, skipping unit tests                      |
| `make compile-start` | Rebuilds the app, rebuilds Docker images, and starts docker compose   |
| `make test`          | Cleans and runs the automated test suite                              |
