package com.ai.audio.transcriber.dto;


public record FileMetadataRequest(FileWrapper file) {
    public record FileWrapper(String display_name) {}
}
