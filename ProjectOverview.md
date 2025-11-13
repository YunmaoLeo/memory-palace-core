# Memory Palace Project Overview

Title: Multi-Source Document Semantic Indexing Platform — Development Roadmap (for Cursor)

Goal:  
A backend-focused platform where users upload or link documents (PDF, Google Docs, Notion, images, code, etc.), the system automatically parses and indexes them, and enables fast semantic search across all stored content.  

**Core principle:**  
Push heavy AI/LLM work into *preprocessing* (parsing, embedding, summarization) so that **query-time is fast**, low-cost, and scalable — powered primarily by vector search.

Deployment target: AWS EC2 + S3/MinIO + RDS PostgreSQL.

────────────────────────────────────
MILESTONE 0 — Project Setup
────────────────────────────────────
**Objective:** Create a minimal working backend with DB + object storage.

Includes:
- Initialize Java 21 + Spring Boot 3.x.
- Docker Compose with PostgreSQL + MinIO.
- `/health` endpoint.
- Base directory structure: `controller`, `service`, `repository`, `model`.

Tech:
- Java, Spring Boot  
- PostgreSQL (pgvector later)  
- MinIO  
- Docker Compose

Success:
- `curl localhost:8080/health` → OK  
- DB + MinIO running through Docker Compose

────────────────────────────────────
MILESTONE 1 — User & Tenant System
────────────────────────────────────
**Objective:** Implement minimal authentication & multi-tenant separation.

Includes:
- Tables: `tenant`, `user`
- JWT-based auth (no OAuth for now)
- Each user bound to a tenant
- Middleware injects `tenantId` into request context

Success:
- Register/login works  
- Users only see their own tenant’s resources

────────────────────────────────────
MILESTONE 2 — File Upload & Storage
────────────────────────────────────
**Objective:** Store uploaded documents and metadata.

Includes:
- `POST /upload` for TXT/PDF/DOCX/images
- Compute SHA256 hash for deduplication
- Store metadata: title, MIME type, size, URI
- Upload binary content to MinIO bucket

Success:
- File metadata stored in DB
- Duplicate file detected by hash

────────────────────────────────────
MILESTONE 3 — Document Parsing Pipeline
────────────────────────────────────
**Objective:** Parse and chunk uploaded files — start with TXT first; decouple ingest and processing. Add baseline PDF text support.**

Includes:
- Decoupled flow:
  - Ingest: `POST /upload` → store binary in MinIO + metadata in `stored_file` (tenant-scoped SHA256 dedup)
  - Process: `POST /api/documents/start-from-file?storedFileId=...` → read from MinIO, parse, chunk, persist
- Scope v1:
  - TXT (`text/plain` or `.txt`): Paragraph-first splitting with sentence fallback, target window ~800–1200 chars; hard-cut exceptionally long sentences
  - PDF (`application/pdf`): Per-page text extraction (PDFBox) → paragraph-first packing into chunks by size; record `page_start/page_end`
- Data model:
  - `document` (tenant/owner/title)
  - `document_version` (links `stored_file`, `version_num`, `status`, `error_message`)
- `document_chunk` (ord, modality, text, token_count, text_sha256, page_start/page_end, bbox_json?, ocr_text?, caption?/alt_text?)
  - `modality`: TEXT | IMAGE | TABLE | CODE (defaults to TEXT; IMAGE/TABLE reserved for later)
  - For PDF text chunks: `modality=TEXT`, set page ranges
- Status transitions: `UPLOADED → PARSING → READY` (failures → `FAILED`)
- Endpoints:
  - `POST /api/documents/start-from-file` → returns `documentId`, `versionId`, `chunkCount`
  - `GET /api/documents/version/{id}/chunks` → list chunks (ordered by `ord`)
- Execution: initially synchronous; can be made async later (see Milestone 7)

Success:
- Upload a TXT → start processing → chunks are created and queryable
- Version status moves to READY; chunk list reflects expected segmentation

────────────────────────────────────
MILESTONE 4 — LLM Enrichment + Text Embedding & Vector Search
────────────────────────────────────
**Objective:** Enrich chunks with LLM metadata and enable semantic search with embeddings.**

Includes:
- Enrichment (OpenAI or compatible):
  - Per chunk: generate `summary`, `keywords`, `auto_section_title`, `category`
  - Cost control: batching, retries, provider/model configuration
- Embedding:
  - Generate embeddings using OpenAI Embedding API (no local model maintenance)
  - Store vectors in PostgreSQL with pgvector; add appropriate indexes
- Search:
  - Endpoint: `POST /search { query }`
  - Vector similarity retrieval → ranked chunks/documents with snippets and scores

Enrichment strategy (design update):
- Mixed approach for quality and scalability:
  - Light document-level pass: produce `doc_outline`, `doc_themes`, `glossary`, `global_tags`
  - Chunk-level passes (parallel): each chunk uses its text + neighbor context (prev/next) + selective `doc_outline/glossary`
- Why not single whole-document request:
  - Context/output tokens explode on long docs; higher cost/timeout risk
  - Parallelism and retries: chunk-level isolates failures; better throughput
  - Incremental updates and caching: re-run only changed chunks; cache by `text_sha256`
  - Output alignment: 1:1 with `ord`, easier auditing and reprocessing
- Per-chunk inputs:
  - `doc_title`, `chunk_text`, `prev_chunk_text`, `next_chunk_text`, `ord`, `total_chunks`, optional `language_hint`
- Per-chunk outputs (structured):
  - `summary`, `keywords`, `entities [{name,type,salience}]`, `questions [{q,a_draft,confidence,evidence_offsets}]`,
    `links [{related_ord,relation,score,reason}]`, `section {auto_section_title,hierarchy}`, `tags`,
    `quality {language,token_count_estimate}`
- Storage:
  - Keep scalar fields (`summary`, `keywords`, `auto_section_title`, `category`)
  - Add JSON fields for richer data (entities/questions/links/tags); consider normalization later
- Cost/control:
  - Two tiers: light pass by default; deep pass (entities/questions/links) async/optional
  - Caching key: `(version_id, ord, enrichment_version, model)`; reuse when `text_sha256` unchanged

**Optional LLM enhancements:**
- **Query Rewriting:**  
  Use OpenAI (cheap model) to expand user query into multiple semantic variants, increasing recall.  
- **Optional RAG Answer Mode:**  
  `/search?mode=answer` →  
  LLM summarizes retrieved chunks into a short answer.

Success:
- Similarity queries return semantically relevant chunks  
- Response includes snippet + score + document reference

────────────────────────────────────
MILESTONE 5 — Basic Hybrid Search (Optional)
────────────────────────────────────
**Objective:** Combine keyword retrieval + vector search.

Includes:
- Add ElasticSearch / OpenSearch  
- Perform BM25 search + pgvector search  
- Weighted ranking merge

Success:
- Hybrid results outperform keyword-only

────────────────────────────────────
MILESTONE 6 — Image & Code Support (Optional)
────────────────────────────────────
**Objective:** Extend platform beyond text.

Includes:
- Images:
  - OCR via Tesseract
  - Captioning via OpenAI Vision (or BLIP)
- Code:
  - Parse file → extract classes/functions
  - Generate embeddings via OpenAI code embedding model

Success:
- Query “database diagram” returns relevant images  
- Query “connect to PostgreSQL” returns code snippets

────────────────────────────────────
MILESTONE 7 — Batch Processing & Concurrency
────────────────────────────────────
**Objective:** Improve pipeline throughput.

Includes:
- Thread pool for parallel document parsing
- Batch embedding requests (OpenAI supports multi-text embedding)
- Retry failed jobs
- Basic `/stats` endpoint

Success:
- Multiple documents process concurrently without blocking  
- Embedding step can be safely retried

────────────────────────────────────
MILESTONE 8 — Versioning & Incremental Updates
────────────────────────────────────
**Objective:** Handle updated documents efficiently.

Includes:
- Hash comparison → detect change  
- Reuse unchanged chunks across versions  
- Version mapping per document

Success:
- Upload new version → only changed chunks reprocessed

────────────────────────────────────
MILESTONE 9 — Basic Access Control
────────────────────────────────────
**Objective:** Ensure isolation across tenants.

Includes:
- Every resource tied to tenant/user  
- Query filtered by tenantId  
- Simple roles: owner/member

Success:
- Tenant isolation validated

────────────────────────────────────
MILESTONE 10 — Simple Dashboard / API Playground
────────────────────────────────────
**Objective:** Provide basic UI for testing.

Includes:
- Minimal React frontend or Swagger UI
- Upload → parse → embed → search flows visible in one interface
- Highlight snippets, grouped by document/module

**Optional LLM enhancement:**
- When viewing a document, auto-generate:
  - Document summary
  - Section summaries
  - Preview (AI-generated TOC)

Success:
- Full pipeline demo from UI

────────────────────────────────────
MILESTONE 11 — Optional Enhancements
────────────────────────────────────
Ideas:
- Re-ranking via small cross-encoder
- Summaries & synthetic snippets (cached)
- Notion / Google Docs connectors
- Redis-based query cache
- Usage metrics, background job dashboard

────────────────────────────────────
End Goal:
A production-quality backend where:
- Users upload heterogeneous documents  
- Files are parsed → chunked → annotated → embedded  
- Vector search returns meaningful results instantly  
- Optional RAG “answer mode” enhances experience  
- System is modular, concurrent, cloud-deployable  
- Focus: backend design, async processing, and scalable search architecture