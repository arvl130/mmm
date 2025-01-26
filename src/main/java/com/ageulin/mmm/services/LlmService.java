package com.ageulin.mmm.services;

import com.ageulin.mmm.dtos.requests.CohereEmbeddingRequest;
import com.ageulin.mmm.dtos.responses.CohereEmbeddingResponse;
import com.ageulin.mmm.enums.CohereInputType;
import com.ageulin.mmm.utils.EnvironmentVariableUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;

import java.util.List;

@Service
public class LlmService {
    private final String AWS_BEDROCK_MODEL_ID;
    private final BedrockRuntimeClient bedrockRuntimeClient;

    public LlmService() {
        this.AWS_BEDROCK_MODEL_ID = EnvironmentVariableUtils
            .getenvOrFail("AWS_BEDROCK_MODEL_ID");

        var builder = BedrockRuntimeClient.builder();
        var awsRegion = System.getenv("AWS_REGION");

        if (null == awsRegion || awsRegion.isEmpty()) {
            builder.region(Region.AP_SOUTHEAST_1);
        }

        this.bedrockRuntimeClient =  builder.build();
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

    public ConverseResponse generateResponse(SystemContentBlock systemContentBlock, Message... message) {
        return this.bedrockRuntimeClient.converse(request -> request
                .modelId(this.AWS_BEDROCK_MODEL_ID)
                .system(systemContentBlock)
                .messages(message)
                .inferenceConfig(config -> config
                        .maxTokens(512)
                        .temperature(0.5F)
                        .topP(0.9F)));
    }

    public String getModelId() {
        return this.AWS_BEDROCK_MODEL_ID;
    }
}
