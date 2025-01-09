package com.ageulin.mmm.dtos.responses;

import lombok.Getter;

import java.util.Set;

@Getter
public class KeywordSuggestionResponse extends BaseResponse {
    record Input(
        String type,
        String filename
    ) {}
    private final String modelId;
    private final String prompt;
    private final Input input;
    private final Set<String> reply;

    public KeywordSuggestionResponse(
            String message,
            String modelId,
            String prompt,
            String inputType,
            String inputFilename,
            Set<String> reply
    ) {
        super(message);
        this.modelId = modelId;
        this.prompt = prompt;
        this.input = new Input(inputType, inputFilename);
        this.reply = reply;
    }

}
