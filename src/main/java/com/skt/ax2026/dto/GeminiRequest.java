package com.skt.ax2026.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiRequest(
    List<Content> contents,
    GenerationConfig generationConfig
) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
    public record GenerationConfig(
        @JsonProperty("response_mime_type") String responseMimeType
    ) {}
}
