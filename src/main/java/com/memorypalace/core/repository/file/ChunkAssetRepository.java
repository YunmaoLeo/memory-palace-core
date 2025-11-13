package com.memorypalace.core.repository.file;

import com.memorypalace.core.model.ChunkAsset;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkAssetRepository extends JpaRepository<ChunkAsset, UUID> {
}


