package com.memorypalace.core.repository.document;

import com.memorypalace.core.model.Document;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
}


