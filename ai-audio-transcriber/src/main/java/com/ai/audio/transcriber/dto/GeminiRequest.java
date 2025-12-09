package com.ai.audio.transcriber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiRequest(List<Content> contents) {

    public record Content(List<Part> parts) {}

    // Interface to allow mixing TextPart and FileDataPart in the list
    public interface Part {}

    public record TextPart(@JsonProperty("text") String text) implements Part {}

    public record FileDataPart(@JsonProperty("file_data") FileData fileData) implements Part {}

    public record FileData(
            @JsonProperty("mime_type") String mimeType,
            @JsonProperty("file_uri") String fileUri
    ) {}
}