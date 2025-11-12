## Memory Palace Core

Memory Palace Core is a backend service for building a multi‑source document semantic indexing platform. It ingests documents, persists metadata, stores binaries in object storage, and prepares the foundation for parsing, chunking, and semantic search.

### Current scope
- Health via Spring Boot Actuator
- User and tenant model with JWT authentication (per‑tenant isolation)
- File upload to MinIO (S3‑compatible) with SHA‑256 based deduplication (tenant‑scoped)

### Tech stack
- Backend: Java 21, Spring Boot 3
- Persistence: PostgreSQL, Spring Data JPA (Hibernate)
- Object storage: MinIO (S3) via AWS SDK v2
- Auth: JWT (java‑jwt) and BCrypt password hashing
- Build: Maven
- Containerization: Dockerfile + Docker Compose
- Observability: Spring Boot Actuator

