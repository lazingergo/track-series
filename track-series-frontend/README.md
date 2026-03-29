# Track Series Frontend

React frontend for the Track Series application.

This app provides the user interface for searching series, managing tracked shows, and viewing watch-related pages.

## Main Responsibilities

- Render UI pages and navigation
- Handle auth state on the client
- Call backend API endpoints
- Display search and tracking results

## Tech Stack

- React 19
- React Router
- Axios
- Vite
- Nginx (in Docker production image)

## Run Locally

### 1. Prerequisites

- Node.js 22+
- npm

### 2. Install dependencies

```bash
npm install
```

### 3. Start development server

```bash
npm run dev
```

Default dev URL:
- http://localhost:5173

## Build

```bash
npm run build
```

Build output is generated in:
- dist/

## API Communication

- In Docker, frontend API requests are proxied by Nginx from /api to backend service.
- For local development, ensure backend is running and API base URL is configured as needed.

## Implementation Notes

- Routing and page composition live under src/pages and related components.
- Shared API communication logic is centralized under src/api.
- Authentication state is handled via context/hooks in src/context.
