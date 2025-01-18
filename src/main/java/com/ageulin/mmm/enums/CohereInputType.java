package com.ageulin.mmm.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CohereInputType {
    SEARCH_DOCUMENT("search_document"),
    SEARCH_QUERY("search_query"),
    CLASSIFICATION("classification"),
    CLUSTERING("clustering");

    public final String label;

    CohereInputType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getJsonValue() {
        return label;
    }
}
