# Track Series Backend

The backend of the Track Series application is a robust RESTful API built with Spring Boot. It serves as the central bridge between the React frontend, the local MySQL database, and the external TVMaze API.

## Tech Stack

- **Java 21**: The core programming language.
- **Spring Boot 4**: Framework for dependency injection, REST endpoints, and application context.
- **Spring Security & JWT**: For securing endpoints and handling stateless user authentication.
- **Spring Data JPA & Hibernate**: For database interactions and ORM.
- **Flyway**: For automated database schema migrations.
- **MySQL**: The primary relational database.

## How It Works

1. **API Abstraction**: The backend exposes endpoints under `/api/` for the frontend to consume.
2. **Local Data Persistence**: Instead of proxying every request to TVMaze, the backend downloads the series metadata and episode lists and stores them in the local MySQL database.
3. **Scheduled Refresh**: A scheduled background job automatically checks for new episodes of "ongoing" series every week. 
4. **Concurrency Control**: Manual refreshes are protected by pessimistic database locking to prevent race conditions and duplicate entries.

## Database Backup Mechanism

Data integrity is a priority. The Docker environment includes an automated database backup mechanism.

- A dedicated container (`trackseries-db-backup`) runs alongside the main database.
- It executes a scheduled `mysqldump` script at a configurable interval (defined by `BACKUP_INTERVAL_SECONDS` in the `.env` file, default is every 24 hours).
- Backups are saved as `.sql` files in the host directory specified by `BACKUP_DIR` (default is `./backups`).
- To prevent disk exhaustion, the script automatically deletes backup files older than 14 days.

## Running Locally (Development)

If you wish to run the backend outside of Docker:

1. Ensure Java 21 and a local MySQL instance are installed.
2. Create the `track_series` database manually if Flyway does not have permissions.
3. Set the required environment variables (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, etc.).
4. Run the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```
   The backend will start on `http://localhost:8080`.
