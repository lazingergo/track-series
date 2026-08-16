# Track Series Frontend

The frontend of the Track Series application is a Single Page Application (SPA) built with React. It provides a clean, responsive, and interactive user interface for managing TV series collections.

## Tech Stack

- **React 19**: The core UI library.
- **Vite**: A fast build tool and development server.
- **React Router**: For client-side routing and navigation between pages (Profile, Series Details, Search).
- **Axios**: For making HTTP requests to the backend API.
- **TailwindCSS**: For rapid, utility-first styling.

## How It Works

1. **Authentication**: User sessions are managed via JWT (JSON Web Tokens). Upon successful login or registration, the token is stored in the browser's local storage and attached to the `Authorization` header of all subsequent API requests via Axios interceptors.
2. **State Management**: The application uses React's native hooks (`useState`, `useEffect`, `useContext`) to manage local component state and global authentication state.
3. **API Communication**: The frontend interacts exclusively with the local Spring Boot backend. It does not communicate directly with the TVMaze API.
4. **Production Routing**: In the Docker production environment, an Nginx server serves the static React files and acts as a reverse proxy, forwarding requests starting with `/api` to the backend container to bypass CORS issues.

## Running Locally (Development)

1. Ensure Node.js (version 22+) and npm are installed.
2. Install the project dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   The application will be available at `http://localhost:5173`. 
   Note: The Vite development server is configured to proxy `/api` requests to `http://localhost:8080` (the default local backend port).

## Building for Production

To create an optimized production build:
```bash
npm run build
```
The compiled static assets will be generated in the `dist/` directory.
