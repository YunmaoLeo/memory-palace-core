package com.memorypalace.core.repository.document;

import com.memorypalace.core.model.ChunkEmbedding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChunkEmbeddingRepository extends JpaRepository<ChunkEmbedding, UUID> {
    @Query("select e from ChunkEmbedding e where e.chunk.id = :chunkId and e.model = :model")
    Optional<ChunkEmbedding> findByChunkAndModel(@Param("chunkId") UUID chunkId, @Param("model") String model);
}


