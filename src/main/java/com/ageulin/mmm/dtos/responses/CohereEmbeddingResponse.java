package com.ageulin.mmm.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CohereEmbeddingResponse(
    String id,
    List<String> texts,
    List<float[]> embeddings,
    @JsonProperty("response_type")
    String responseType,
    @JsonProperty("embedding_types")
    List<String> embeddingTypes
) {
}
