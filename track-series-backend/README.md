# Track Series Backend

Spring Boot backend for the Track Series application.

It handles authentication, series search/tracking business logic, integration with TVMaze, and persistence in MySQL.

## Main Responsibilities

- Expose REST API endpoints for frontend clients
- Handle authentication and security
- Query external TVMaze API when needed
- Store and reuse fetched data in local database
- Manage tracked series and episode state

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway
- MySQL

## Run Locally

### 1. Prerequisites

- JDK 21
- MySQL running locally

### 2. Configure database and secret

You can use either environment variables or property files.

Common required values:
- DB_HOST
- DB_PORT
- DB_NAME
- DB_USERNAME
- DB_PASSWORD or DB_ROOT_PASSWORD
- JWT_SECRET (or LOCAL_JWT_SECRET for local-only setup)

### 3. Start backend

```bash
./gradlew bootRun
```

Default application port inside app: 8080.

## Profiles

The backend supports multiple Spring profiles through application property files.

Typical files:
- application.properties
- application-develop.properties
- application-local.properties
- application-main.properties

Choose active profile with environment variable:

```bash
SPRING_PROFILES_ACTIVE=local
```

## Implementation Notes

- API data strategy:
    - First read from local database
    - Call TVMaze only when data is missing or needs refresh
    - Persist fetched results for future requests
- This approach keeps external API usage lower and improves response times for repeated queries.

## Database

- MySQL is used as the primary data store.
- Flyway manages schema migration.

If needed, create database manually:

```sql
CREATE DATABASE track_series;
```

