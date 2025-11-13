package com.memorypalace.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.memorypalace.core.config.OpenAIClient;
import com.memorypalace.core.model.ChunkEmbedding;
import com.memorypalace.core.model.DocumentChunk;
import com.memorypalace.core.repository.document.ChunkEmbeddingRepository;
import com.memorypalace.core.repository.document.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenAIEmbeddingService {
    private final OpenAIClient client;
    private final String embeddingModel;
    private final DocumentChunkRepository chunkRepo;
    private final ChunkEmbeddingRepository embeddingRepo;

    public OpenAIEmbeddingService(OpenAIClient client,
                                  DocumentChunkRepository chunkRepo,
                                  ChunkEmbeddingRepository embeddingRepo,
                                  @Value("${openai.models.embedding:text-embedding-3-small}") String embeddingModel) {
        this.client = client;
        this.chunkRepo = chunkRepo;
        this.embeddingRepo = embeddingRepo;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public int embedVersion(UUID versionId) {
        if (!client.isConfigured()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }
        List<DocumentChunk> chunks = chunkRepo.findByVersionOrdered(versionId);
        if (chunks.isEmpty()) return 0;
        int created = 0;
        // Batch inputs to respect token/size limits; keep simple batches of 64 texts
        int batchSize = 64;
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<DocumentChunk> batch = chunks.subList(i, end);
            List<String> inputs = batch.stream().map(DocumentChunk::getText).toList();
            List<double[]> vectors = embedTexts(inputs);
            for (int j = 0; j < batch.size(); j++) {
                DocumentChunk c = batch.get(j);
                // skip if exists
                if (embeddingRepo.findByChunkAndModel(c.getId(), embeddingModel).isPresent()) continue;
                double[] vec = vectors.get(j);
                ChunkEmbedding e = new ChunkEmbedding();
                e.setChunk(c);
                e.setModel(embeddingModel);
                e.setDim(vec.length);
                // store as JSON array string
                ArrayNode arr = client.mapper().createArrayNode();
                for (double v : vec) arr.add(v);
                e.setEmbeddingJson(arr.toString());
                embeddingRepo.save(e);
                created++;
            }
        }
        return created;
    }

    public List<double[]> embedTexts(List<String> texts) {
        ObjectNode body = client.mapper().createObjectNode();
        body.put("model", embeddingModel);
        ArrayNode input = client.mapper().createArrayNode();
        for (String t : texts) input.add(t);
        body.set("input", input);
        JsonNode resp = client.post("/embeddings", body);
        ArrayNode data = (ArrayNode) resp.path("data");
        List<double[]> res = new ArrayList<>();
        for (JsonNode item : data) {
            ArrayNode arr = (ArrayNode) item.path("embedding");
            double[] vec = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                vec[i] = arr.get(i).asDouble();
            }
            res.add(vec);
        }
        return res;
    }
}


