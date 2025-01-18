package com.ageulin.mmm.dtos.requests;

import com.ageulin.mmm.enums.CohereInputType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CohereEmbeddingRequest(
    List<String> texts,
    @JsonProperty("input_type")
    CohereInputType inputType
) {
}
