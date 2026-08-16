# Track Series

Track Series is a modern web application designed to help users discover, track, and organize their favorite TV series. It provides a centralized platform to monitor watch progress and stay updated on upcoming episodes.

## Core Architecture & Purpose

The application utilizes the free TVMaze API to fetch series data. To ensure fast response times and to protect the public API from rate limits, Track Series implements a local caching layer. When a user requests data, the backend first checks the local database. If the data is missing or outdated, it fetches it from TVMaze, saves it locally, and serves it. This hybrid approach guarantees high performance and stability.

## Features

- Search for new and existing TV series.
- Add series to a personal collection.
- Track watch progress (watched episodes vs. total episodes).
- Automatic background updates for ongoing series.
- Secure, token-based authentication.

## Tech Stack Overview

- **Frontend**: React 19, Vite, TailwindCSS (proxied via Nginx)
- **Backend**: Java 21, Spring Boot 4, Spring Security, JWT
- **Database**: MySQL 8.4 (with Flyway for schema migrations)
- **Infrastructure**: Docker & Docker Compose

## Quick Start (Docker)

The easiest way to run the entire stack locally is by using Docker Compose.

1. Ensure Docker and Docker Compose are installed on your machine.
2. Copy the example environment file and configure it:
   ```bash
   cp .env.example .env
   ```
   Provide strong values for `DB_ROOT_PASSWORD` and `JWT_SECRET` in the `.env` file.
3. Start the application:
   ```bash
   docker compose up -d --build
   ```
4. Access the application:
   - Frontend: http://localhost:9010
   - Backend API: http://localhost:8081

To stop the application, run:
```bash
docker compose down
```

For more detailed information, please read the specific documentation for the [Backend](track-series-backend/README.md) and [Frontend](track-series-frontend/README.md).
