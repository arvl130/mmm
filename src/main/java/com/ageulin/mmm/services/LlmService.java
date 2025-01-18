package com.ageulin.mmm.services;

import com.ageulin.mmm.dtos.requests.CohereEmbeddingRequest;
import com.ageulin.mmm.dtos.responses.CohereEmbeddingResponse;
import com.ageulin.mmm.enums.CohereInputType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.List;

@Service
public class LlmService {
    private final BedrockRuntimeClient bedrockRuntimeClient;

    public LlmService() {
        this.bedrockRuntimeClient =  BedrockRuntimeClient.builder().build();
    }

    public CohereEmbeddingResponse generateEmbeddings(List<String> inputText) {
        var om = new ObjectMapper();
        var modelId = "cohere.embed-english-v3";

        try {
            // Encode and send the request to the Bedrock Runtime.
            var request = new CohereEmbeddingRequest(
                inputText, CohereInputType.SEARCH_QUERY
            );
            var json = om.writeValueAsString(request);
            var response = this.bedrockRuntimeClient
                .invokeModel(builder -> builder
                    .body(SdkBytes.fromUtf8String(json))
                    .modelId(modelId)
                );

            // Decode the response body.
            return om.readValue(response.body().asUtf8String(), CohereEmbeddingResponse.class);
        } catch (SdkClientException e) {
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", modelId, e.getMessage());
            throw new ServerErrorException("SDK Client exception occurred.", e);
        } catch (JsonProcessingException e) {
            throw new ServerErrorException("JSON processing error occurred.", e);
        }
    }
}
