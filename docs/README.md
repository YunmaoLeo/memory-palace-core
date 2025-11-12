# Memory Palace Core - Milestone 0 Setup Guide

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

2. Verify the health endpoint:
   ```bash
   curl http://localhost:8080/health
   ```
   Expected response:
   ```json
   {"status":"UP","timestamp":"<ISO-8601>"}
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
3. Verify:
   ```bash
   curl http://localhost:8080/health
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
- `controller`: REST endpoints (e.g., `/health`)
- `service`: Business logic (e.g., health status provider)
- `repository`: Persistence layer (reserved for future milestones)
- `model`: Domain entities (reserved for future milestones)

## Next Steps
- Milestone 1: User & Tenant System
- Milestone 2: File Upload & Storage
- Milestone 3: Document Parsing Pipeline


