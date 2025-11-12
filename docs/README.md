# Memory Palace Core - Setup Guide (Milestone 0-1)

This document explains how to run the backend with Docker, including PostgreSQL and MinIO. All comments and documentation are in English as required.

## Prerequisites
- Docker and Docker Compose installed
- Optional: JDK 21 and Maven (only needed if you want to run locally without Docker)

## Quick Start (Docker-first)
1. Build and start everything (app + dependencies):
   ```bash
   docker compose up -d --build
   ```
   This will:
   - Start PostgreSQL on port 5432
   - Start MinIO on ports 9000 (S3 API) and 9001 (console)
   - Build and run the Spring Boot app on port 8080

2. Verify the health endpoint (Actuator):
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Expected response:
   ```json
   {"status":"UP"}
   ```

3. Access MinIO Console:
   - URL: http://localhost:9001
   - Username: `minioadmin`
   - Password: `minioadmin123`

## Alternative: Run dependencies in Docker, app locally
1. Start only PostgreSQL and MinIO:
   ```bash
   docker compose up -d db minio
   ```
2. Run the app from source (ensure JDK 21 + Maven installed):
   ```bash
   mvn spring-boot:run
   ```
3. Verify (Actuator):
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Configuration
The application reads configuration from environment variables:
- Database:
  - `DB_HOST` (default `localhost`)
  - `DB_PORT` (default `5432`)
  - `DB_NAME` (default `memory_palace`)
  - `DB_USERNAME` (default `mp_user`)
  - `DB_PASSWORD` (default `mp_pass`)
- MinIO:
  - `MINIO_ENDPOINT` (default `http://localhost:9000`)
  - `MINIO_ACCESS_KEY` (default `minioadmin`)
  - `MINIO_SECRET_KEY` (default `minioadmin`)
  - `MINIO_BUCKET` (default `documents`)

Docker Compose sets these for the app container automatically. When running locally, you can export them in your shell if needed.

## Project Structure
- No custom health controller; using Spring Boot Actuator (`/actuator/health`)
- `controller`: Auth and profile APIs
- `service`: JWT and auth logic
- `security`: Request-scoped tenant/user context and JWT filter
- `repository`: Persistence layer
- `model`: Domain entities

## Milestone 1 API (Auth + Tenant)
Each user automatically gets a dedicated tenant at registration time.

### Register
```bash
curl -sS -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@example.com","password":"secret123","displayName":"User One"}'
```
Response:
```json
{"token":"<JWT>","userId":"...","tenantId":"...","email":"user1@example.com"}
```

### Login
```bash
curl -sS -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@example.com","password":"secret123"}'
```
Response is the same shape as register.

### Get current profile (requires Bearer token)
```bash
TOKEN="<JWT_FROM_ABOVE>"
curl -sS http://localhost:8080/api/me -H "Authorization: Bearer $TOKEN"
```
Response:
```json
{"id":"...","tenantId":"...","email":"user1@example.com","displayName":"User One"}
```

JWT configuration:
- Env: `JWT_SECRET` (default `dev-secret-change`), `JWT_TTL_SECONDS` (default `43200`)

## Milestone 2 API (File Upload & Dedup)
- Endpoint: `POST /api/files/upload` (multipart/form-data)
- Auth: Bearer token (from login/register)
- Behavior:
  - Computes SHA-256 for the file
  - If a file with the same hash already exists under the same tenant, returns the existing metadata with `duplicate=true`
  - Otherwise uploads to MinIO and returns the new metadata with `duplicate=false`

Example:
```bash
TOKEN="..."
curl -sS -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "title=My PDF"
```
Response:
```json
{
  "id":"...","tenantId":"...","ownerId":"...",
  "title":"My PDF","originalFilename":"document.pdf",
  "mimeType":"application/pdf","sizeBytes":12345,
  "sha256Hex":"...","s3Bucket":"documents","s3Key":"tenant/.../file.pdf",
  "createdAt":"...","duplicate":false
}
```

Scripts:
- `scripts/files/upload.sh <token> <path> [title]`
- `scripts/files/e2e_dedup.sh <email> <password> <path>`

## Next Steps
- Milestone 3: Document Parsing Pipeline


