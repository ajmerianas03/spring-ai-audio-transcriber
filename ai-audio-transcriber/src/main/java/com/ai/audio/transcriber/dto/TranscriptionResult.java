package com.ai.audio.transcriber.dto;

public record TranscriptionResult(String transcription, String analysis, Long recordId) {}

