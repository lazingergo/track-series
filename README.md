# Track Series

Track Series is a simple web app to search and follow TV series.

You can discover shows, track what you watch, and keep an eye on upcoming episodes in one place.

The app uses the free TVMaze API, but it does not rely on it for every request. Data fetched from TVMaze is stored in a local database and reused later. This reduces external API traffic, improves response time, and keeps the app more stable.

## Features

- Search TV series
- Track selected series
- Organize watch progress and upcoming episodes
- Reuse previously fetched API data from the local database

## Why The API + Local Database Approach Is Useful

- Protects the free public API from unnecessary repeated requests
- Improves performance by serving known data from local storage
- Reduces risk when the external API is slow or temporarily unavailable
- Gives better control over your own data lifecycle

## Tech Stack

- Frontend: React + Vite + Nginx
- Backend: Spring Boot (Java 21)
- Database: MySQL
- External data source: TVMaze API (free)

## Quick Start

### 1. Prerequisites

- Docker
- Docker Compose

### 2. Create environment file

```bash
cp .env.example .env
```

### 3. Set required values in .env

- DB_ROOT_PASSWORD
- JWT_SECRET

Optional values:

- DB_NAME (default: track_series)

### 4. Start the application

```bash
docker compose up -d --build
```

### 5. Open the app

- Frontend: http://localhost
- Backend: http://localhost:8081

### 6. Stop the application

```bash
docker compose down
```

## Documentation

- Backend implementation details: [track-series-backend/README.md](track-series-backend/README.md)
- Frontend implementation details: [track-series-frontend/README.md](track-series-frontend/README.md)

## Project Structure

```text
track-series/
  track-series-frontend/   Frontend application
  track-series-backend/    Backend API and business logic
  docker-compose.yml       Full local stack
```

