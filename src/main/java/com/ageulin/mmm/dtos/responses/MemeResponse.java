package com.ageulin.mmm.dtos.responses;

import java.util.List;

public record MemeResponse(
    String message,
    List result
) {
}
