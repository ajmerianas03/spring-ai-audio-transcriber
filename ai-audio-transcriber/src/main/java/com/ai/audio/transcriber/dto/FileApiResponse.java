package com.ai.audio.transcriber.dto;


public record FileApiResponse(FilePayload file) {
    public record FilePayload(String name, String uri, String mimeType, String state) {}
}
