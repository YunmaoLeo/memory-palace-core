# Architecture (Milestone 0)

This document outlines the minimal architecture for the backend and infrastructure introduced in Milestone 0.

## Overview
- Backend: Java 21 + Spring Boot 3.x
- Build: Maven
- Database: PostgreSQL 16 (Docker)
- Object Storage: MinIO (Docker)
- Containerization: Dockerfile for the backend, docker-compose for local orchestration

## Modules
- `controller`: HTTP endpoints (`/health`)
- `service`: Business logic (simple health status provider)
- `repository`: Placeholder for future JPA repositories
- `model`: Placeholder for future JPA entities and domain models

## Configuration
Application configuration is environment-driven. Defaults are set in `application.yaml`, with Docker Compose providing container-specific values.

Key env vars:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`

## Networking
All services run on a single Docker network via `docker-compose.yml`:
- `db` (PostgreSQL): 5432
- `minio` (S3 API/console): 9000/9001
- `app` (Spring Boot): 8080

## Health Check
Endpoint: `GET /health`  
Response: `{"status":"UP","timestamp":"<ISO-8601>"}`  
Purpose: Quick readiness verification for Milestone 0.


