# track-series

Dockerized setup for:
- frontend (React + Nginx)
- backend (Spring Boot)
- database (MySQL)
- automatic DB backup container

## 1) First-time setup

1. Copy environment template:

```bash
cp .env.example .env
```

2. Edit `.env` and set at least:
- `DB_ROOT_PASSWORD`
- `JWT_SECRET`

## 2) Start all services

```bash
docker compose up -d --build
```

Services:
- Frontend: http://localhost
- Backend: http://localhost:8080
- DB: localhost:3306

## 3) Stop services

```bash
docker compose down
```

To also remove DB data volume:

```bash
docker compose down -v
```

## 4) Logs

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
docker compose logs -f db-backup
```

## 5) Backups

Backups are created by `db-backup` service into:

```text
./backups
```

Behavior:
- dump every `BACKUP_INTERVAL_SECONDS` (default: daily)
- old dumps older than 14 days are deleted

## 6) Restore a backup

Example restore command:

```bash
cat backups/trackseries-YYYYMMDD-HHMMSS.sql | docker compose exec -T db \
	mysql -uroot -p"$DB_ROOT_PASSWORD" "$DB_NAME"
```

## 7) Update after code changes

```bash
docker compose up -d --build
```

Recommended update flow:
1. verify backup exists (`./backups`)
2. rebuild and restart with command above
3. check backend/frontend logs

