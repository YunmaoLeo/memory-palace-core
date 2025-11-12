# Memory Palace Project Overview

Title: Multi-Source Document Semantic Indexing Platform — Development Roadmap (for Cursor)

Goal:
The platform enables users to upload or link documents (PDF, Google Docs, Notion, images, etc.), automatically parse and index them, and perform semantic search across all stored content.  
Core principle: push heavy LLM tasks (parsing, embedding) to pre-processing; keep query-time fast via embeddings and vector search.

The platform will be further deployed to AWS service, all the components should be related

───────────────────────────────
MILESTONE 0 — Project Setup
───────────────────────────────
**Objective:** Establish a minimal running Java backend project with database and object storage.

Includes:
- Initialize Java 21 + Spring Boot 3.x project.
- Set up Docker Compose with PostgreSQL + MinIO.
- Create `/health` endpoint to verify the system is up.
- Define basic project structure: `controller`, `service`, `repository`, `model`.

Tech stack:
- Backend: Java + Spring Boot
- DB: PostgreSQL (+ pgvector later)
- Object Storage: MinIO (S3-compatible)
- Build: Maven or Gradle

Success check:
- `curl localhost:8080/health` → 200 OK
- Docker Compose starts DB and MinIO successfully.

───────────────────────────────
MILESTONE 1 — User & Tenant System
───────────────────────────────
**Objective:** Add minimal user and tenant management.

Includes:
- Tables: `tenant`, `user`
- JWT authentication (simple mock, no OAuth yet)
- Each user belongs to a tenant (multi-tenant logic placeholder)
- Middleware adds `tenantId` to every request context

Success check:
- Can register/login users.
- Each user only sees their own data.

───────────────────────────────
MILESTONE 2 — File Upload & Storage
───────────────────────────────
**Objective:** Allow users to upload documents and store them in object storage.

Includes:
- Endpoint: `POST /upload` for PDF, DOCX, or image
- Compute file hash (SHA256) for deduplication
- Store file metadata in DB (title, MIME, size, URI)
- Upload binary file to MinIO bucket

Success check:
- Uploaded file metadata is saved.
- Same file uploaded twice → detected as duplicate.

───────────────────────────────
MILESTONE 3 — Document Parsing Pipeline
───────────────────────────────
**Objective:** Extract text and structure from uploaded files.

Includes:
- Asynchronous pipeline (can use ExecutorService or simple queue)
- Parse PDFs into structured text (basic version: page → text)
- Extract sections/headers (if available)
- Save results into DB: `document`, `module`, `chunk`
- Store parsing status in DB (UPLOADED → PARSED)

Tech:
- Libraries: Apache PDFBox or iText for PDF, Apache POI for DOCX
- Text splitting: 1000–1500 characters per chunk

Success check:
- Upload document → automatically parsed → chunks visible in DB.
- Query `/document/{id}` → returns parsed sections and text blocks.

───────────────────────────────
MILESTONE 4 — Text Embedding & Vector Search
───────────────────────────────
**Objective:** Enable semantic search using text embeddings.

Includes:
- Generate vector embeddings for each chunk (use open-source model like `e5-base` or `bge-small`).
- Store vectors in pgvector extension or external vector DB (Qdrant/Weaviate optional).
- Endpoint: `POST /search { query }` → returns relevant chunks/modules.

Tech:
- Models: `intfloat/e5-base`, `BAAI/bge-base-en`
- Library: Java client calling a Python microservice (for embeddings), or local inference via REST.

Success check:
- Search “similar meaning” queries return semantically relevant text chunks.
- Response includes document title, matched snippet, similarity score.

───────────────────────────────
MILESTONE 5 — Basic Hybrid Search (Optional)
───────────────────────────────
**Objective:** Combine keyword (BM25) and embedding search.

Includes:
- Add OpenSearch or ElasticSearch instance.
- Perform parallel keyword + vector retrieval, merge results by score.
- Simple weighted fusion (no re-ranker yet).

Success check:
- Keyword-only and hybrid results differ; hybrid yields better recall.

───────────────────────────────
MILESTONE 6 — Image & Code Support (Optional)
───────────────────────────────
**Objective:** Extend parsing and retrieval to new content types.

Includes:
- For images:
  - Extract text via OCR (Tesseract)
  - Generate caption using BLIP/CLIP model
  - Save both in DB
- For code:
  - Parse source files into functions/classes
  - Generate code embeddings using `bge-code` model
- Search queries can specify modality: text / image / code

Success check:
- Searching for “diagram about Kafka” returns related images.
- Searching for “function that connects to database” returns relevant code snippet.

───────────────────────────────
MILESTONE 7 — Batch Processing & Concurrency
───────────────────────────────
**Objective:** Improve throughput and reliability of async processing.

Includes:
- Process multiple documents concurrently (thread pool, message queue optional)
- Batch embedding generation (group chunks before embedding)
- Retry and resume failed tasks
- Add simple monitoring endpoints (e.g. `/stats`)

Success check:
- Multiple uploads process in parallel without blocking.
- Can re-run embedding step without duplicating data.

───────────────────────────────
MILESTONE 8 — Versioning & Incremental Updates
───────────────────────────────
**Objective:** Handle updated documents efficiently.

Includes:
- Compute file hash; if changed → create new version record.
- Reuse previous chunks if content unchanged.
- Maintain mapping: document → multiple versions.

Success check:
- Upload new version of same file; only changed chunks reprocessed.

───────────────────────────────
MILESTONE 9 — Basic Access Control
───────────────────────────────
**Objective:** Ensure users only access their own data.

Includes:
- Each document belongs to a tenant and user.
- Middleware filters queries by tenant.
- Simple roles: owner, member.

Success check:
- Tenant A cannot access Tenant B’s documents.

───────────────────────────────
MILESTONE 10 — Simple Dashboard or API Playground
───────────────────────────────
**Objective:** Visualize and test all functionalities.

Includes:
- Add basic frontend or Swagger UI
- Upload, parse, and search through a single interface.
- Display search results grouped by document/module with highlights.

Success check:
- End-to-end demo: upload → parse → embed → search works via one UI.

───────────────────────────────
MILESTONE 11 — Optional Enhancements
───────────────────────────────
Ideas for later:
- Add re-ranking model (MiniLM or small cross-encoder)
- Add summaries/snippets generated by LLM (cached, not per-query)
- Add Google Docs/Notion connectors for remote sync
- Integrate Redis cache for query results
- Add usage metrics and background task monitoring

───────────────────────────────
End Goal:
A working backend where:
- Users upload various document types.
- Files are parsed, chunked, embedded, and indexed.
- Queries return semantically relevant pieces of content instantly.
- System is modular, concurrent, and database-driven — focusing on backend skill growth.