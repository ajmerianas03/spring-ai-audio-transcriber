package com.ai.audio.transcriber.dto;

class FileStateResponse {
    String name;
    String state; // We need to wait for "ACTIVE"
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
