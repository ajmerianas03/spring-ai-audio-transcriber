package com.ai.audio.transcriber.service;

import com.ai.audio.transcriber.dto.FileApiResponse;
import com.ai.audio.transcriber.dto.FileMetadataRequest;
import com.ai.audio.transcriber.dto.GeminiRequest;
import com.ai.audio.transcriber.dto.TranscriptionResult;
import com.ai.audio.transcriber.model.TranscriptionRecord;
import com.ai.audio.transcriber.model.User;
import com.ai.audio.transcriber.repository.TranscriptionRepository;
import com.ai.audio.transcriber.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TranscriptionService {

    private static final int WINDOW_LIMIT = 4;
    private static final long WINDOW_SECONDS = 24 * 60 * 60;

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final TranscriptionAnalysisService analysisService;
    private final TranscriptionRepository transcriptionRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper; //this is for JSON parsing

    @Value("${gemini.api.key}")
    private String apiKey;

    public TranscriptionService(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            TranscriptionAnalysisService analysisService,
            TranscriptionRepository transcriptionRepository,
            UserRepository userRepository,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;

        OpenAiAudioApi openAiAudioApi = new OpenAiAudioApi(apiKey);
        this.transcriptionModel = new OpenAiAudioTranscriptionModel(openAiAudioApi);

        this.analysisService = analysisService;
        this.transcriptionRepository = transcriptionRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));
    }

    public TranscriptionResult transcribeAndAnalyze(MultipartFile file, String model) throws IOException {
        User currentUser = getCurrentAuthenticatedUser();

        // 1. Rate Limit Check
        checkSlidingWindowLimit(currentUser.getId());

        String transcriptionText;
        String analysisResult;

        // 2. Logic Split
        if ("gemini".equalsIgnoreCase(model)) {
            // Gemini does both transcription and analysis in one go
            String rawGeminiResponse = performGeminiTranscription(file);

            // Extract clean text from the JSON response
            String cleanText = extractTextFromGeminiResponse(rawGeminiResponse);

            transcriptionText = cleanText;
            analysisResult = cleanText; // Using the same text for both as requested

            System.out.println("this is analysis result "+ analysisResult);
            // Save immediately and return
            TranscriptionRecord savedRecord = saveTranscriptionRecord(currentUser, file.getOriginalFilename(), transcriptionText, analysisResult);

            return new TranscriptionResult(
                    savedRecord.getFullTranscription(),
                    savedRecord.getAiAnalysis(),
                    savedRecord.getId()
            );

        } else {
            // OpenAI Path: Transcribe first, then Analyze separately
            transcriptionText = performTranscription(file);

            if (transcriptionText == null || transcriptionText.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Transcription failed or returned empty result.");
            }

            // Explicit Analysis Step
            analysisResult = analysisService.summarize(transcriptionText);

            TranscriptionRecord savedRecord = saveTranscriptionRecord(currentUser, file.getOriginalFilename(), transcriptionText, analysisResult);

            return new TranscriptionResult(
                    savedRecord.getFullTranscription(),
                    savedRecord.getAiAnalysis(),
                    savedRecord.getId()
            );
        }
    }

    public List<TranscriptionRecord> getHistory() {
        User currentUser = getCurrentAuthenticatedUser();
        return transcriptionRepository.findByUserIdOrderByCreatedDateDesc(currentUser.getId());
    }

    // --- Helper Methods ---

    private void checkSlidingWindowLimit(Long userId) {
        LocalDateTime windowStart = LocalDateTime.now().minusSeconds(WINDOW_SECONDS);
        List<TranscriptionRecord> recentCalls = transcriptionRepository.findByUserIdAndCreatedDateAfter(userId, windowStart);
        if (recentCalls.size() >= WINDOW_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Sliding window limit reached.");
        }
    }

    private String performGeminiTranscription(MultipartFile file) {
        Path tempFile = null;
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = (originalFileName != null && originalFileName.contains("."))
                    ? originalFileName.substring(originalFileName.lastIndexOf("."))
                    : ".tmp";
            tempFile = Files.createTempFile("upload-" + UUID.randomUUID(), extension);
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            return processAudioFile(tempFile);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini processing failed", e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) { }
            }
        }
    }

    // New Helper to parse Gemini JSON
    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            // Navigate: candidates[0] -> content -> parts[0] -> text
            if (root.has("candidates") && root.get("candidates").isArray() && !root.get("candidates").isEmpty()) {
                JsonNode candidate = root.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    JsonNode parts = candidate.get("content").get("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
            return "No text content found in response.";
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini JSON: " + jsonResponse);
            return "Error parsing AI response.";
        }
    }

    private String performTranscription(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("audio", ".wav");
        file.transferTo(tempFile);
        try {
            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                    .withLanguage("en")
                    .withTemperature(0f)
                    .build();

            FileSystemResource audioResource = new FileSystemResource(tempFile);
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);
            return response.getResult().getOutput();
        } finally {
            tempFile.delete();
        }
    }

    private TranscriptionRecord saveTranscriptionRecord(User user, String fileName, String transcriptionText, String analysisResult) {
        TranscriptionRecord record = new TranscriptionRecord();
        record.setUser(user);
        record.setOriginalFileName(fileName);
        record.setFullTranscription(transcriptionText);
        record.setAiAnalysis(analysisResult);
        return transcriptionRepository.save(record);
    }

    public String processAudioFile(Path path) throws IOException {
        long numBytes = Files.size(path);
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) mimeType = "audio/mpeg";

        System.out.println("1. Detected file: " + mimeType + " , size: " + numBytes);

        // Step 1: Init Upload
        String uploadUrl = initiatedUpload(mimeType, numBytes, "MyAudioFile");
        System.out.println("2. Session url obtained.");

        // Step 2: Upload Bytes
        String fileUri = uploadBytes(uploadUrl, path, numBytes);
        System.out.println("3. File uploaded uri: " + fileUri);

        // Step 3 (NEW): Wait for File to be ACTIVE
        waitForFileActive(fileUri);

        // Step 4: Generate Content
        String response = generateContent(fileUri, mimeType);
        System.out.println("4. Gemini response: " + response);

        return response;
    }

    /**
     * Polls the Google Files API until the file state is ACTIVE.
     * Throws exception if it fails or times out.
     */
    private void waitForFileActive(String fileUri) {
        System.out.println("Checking file state for: " + fileUri);

        // The fileUri is like https://.../files/abc12345
        // We can GET that URI directly with the API Key to check status
        String checkUrl = fileUri + "?key=" + apiKey;

        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                String state = webClient.get()
                        .uri(checkUrl)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(json -> json.path("state").asText())
                        .block();

                System.out.println("File State: " + state);

                if ("ACTIVE".equals(state)) {
                    return; // Ready to go!
                } else if ("FAILED".equals(state)) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google failed to process the audio file.");
                }

                // Wait 2 seconds before retry
                Thread.sleep(2000);

            } catch (Exception e) {
                System.err.println("Error checking file state: " + e.getMessage());
                // Don't break immediately, give it a chance
            }
        }
        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "File processing timed out.");
    }

//    public String processAudioFile(Path path) throws IOException {
//        long numBytes = Files.size(path);
//        String mimeType = Files.probeContentType(path);
//        if (mimeType == null) mimeType = "audio/mpeg";
//
//        System.out.println("1. Detected file: " + mimeType + " , size: " + numBytes);
//
//        String uploadUrl = initiatedUpload(mimeType, numBytes, "MyAudioFile");
//        System.out.println("2. Session url obtained: " + uploadUrl);
//
//        String fileUri = uploadBytes(uploadUrl, path, numBytes);
//        System.out.println("3. File uploaded uri: " + fileUri);
//
//        String response = generateContent(fileUri, mimeType);
//        System.out.println("4. Gemini response: " + response);
//
//        return response;
//    }

//    private String generateContent(String fileUri, String mimeType) {
//        // Use gemini-1.5-flash or 1.5-pro (2.5-flash is not a standard public endpoint yet, reverting to 1.5 for safety)
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
//
//        // Updated prompt to match your dual-purpose need
//        var textPart = new GeminiRequest.TextPart("Transcribe this audio clip and provide a brief summary of the content.");
//        var filePart = new GeminiRequest.FileDataPart(new GeminiRequest.FileData(mimeType, fileUri));
//
//        var content = new GeminiRequest.Content(List.of(textPart, filePart));
//        var request = new GeminiRequest(List.of(content));
//
//        return webClient.post()
//                .uri(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(String.class) // Returns raw JSON
//                .block();
//    }


    private String generateContent(String fileUri, String mimeType) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        var textPart = new GeminiRequest.TextPart("Transcribe this audio clip and provide a brief summary of the content.");
        var filePart = new GeminiRequest.FileDataPart(new GeminiRequest.FileData(mimeType, fileUri));
        var content = new GeminiRequest.Content(List.of(textPart, filePart));
        var request = new GeminiRequest(List.of(content));

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                System.err.println("Gemini Generate Error Body: " + body);
                                return Mono.error(new ResponseStatusException(response.statusCode(), body));
                            });
                })
                .bodyToMono(String.class)
                .block();
    }
    private String uploadBytes(String uploadUrl, Path path, long numBytes) {
        System.out.println("DEBUG: Uploading bytes to: " + uploadUrl);

        return webClient.post()
                .uri(uploadUrl)
                .header("Content-Length", String.valueOf(numBytes))
                .header("X-Goog-Upload-Offset", "0")
                // CRITICAL FIX: Changed from "finalize" to "upload, finalize"
                .header("X-Goog-Upload-Command", "upload, finalize")
                .body(BodyInserters.fromResource(new FileSystemResource(path)))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    // This helper will print the actual error body from Google if it fails again
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                System.err.println("Google Upload Error Body: " + body);
                                return Mono.error(new ResponseStatusException(response.statusCode(), "Upload failed: " + body));
                            });
                })
                .bodyToMono(FileApiResponse.class)
                .map(response -> response.file().uri())
                .block();
    }
//    private String uploadBytes(String uploadUrl, Path path, long numBytes) {
//        return webClient.post()
//                .uri(uploadUrl)
//                .header("Content-Length", String.valueOf(numBytes))
//                .header("X-Goog-Upload-Offset", "0") // FIXED: Removed space " 0" -> "0"
//                .header("X-Goog-Upload-Command", "finalize")
//                .body(BodyInserters.fromResource(new FileSystemResource(path)))
//                .retrieve()
//                .bodyToMono(FileApiResponse.class)
//                .map(response -> response.file().uri())
//                .block();
//    }

    private String initiatedUpload(String mimeType, long numBytes, String displayName) {
        String baseUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files";

        return webClient.post()
                .uri(baseUrl)
                .header("x-goog-api-key", apiKey)
                .header("X-Goog-Upload-Protocol", "resumable")
                .header("X-Goog-Upload-Command", "start")
                // FIXED: Changed Content_Length to Content-Length
                .header("X-Goog-Upload-Header-Content-Length", String.valueOf(numBytes))
                .header("X-Goog-Upload-Header-Content-Type", mimeType)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new FileMetadataRequest(new FileMetadataRequest.FileWrapper(displayName)))
                .retrieve()
                .toBodilessEntity()
                .map(responseEntity -> responseEntity.getHeaders().getFirst("x-goog-upload-url"))
                .block();
    }
}