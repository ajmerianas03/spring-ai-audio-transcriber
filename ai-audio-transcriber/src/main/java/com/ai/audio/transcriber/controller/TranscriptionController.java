//package com.ai.audio.transcriber.controller;
//
//import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
//import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
//import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
//import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
//import org.springframework.ai.openai.api.OpenAiAudioApi;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/transcribe")
//public class TranscriptionController{
//    private final OpenAiAudioTranscriptionModel transcriptionModel;
//
//    public TranscriptionController(@Value("${spring.ai.openai.api-key}") String apiKey) {
//        OpenAiAudioApi openAiAudioApi = new OpenAiAudioApi(apiKey);
//        this.transcriptionModel
//                = new OpenAiAudioTranscriptionModel(openAiAudioApi);
//    }
//
//    @PostMapping
//    public ResponseEntity<String> transcribeAudio(@RequestParam("file")MultipartFile file) throws IOException {
//        File tempFile = File.createTempFile("audio",".wav");
//        file.transferTo(tempFile);
//        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
//                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
//                .withLanguage("en")
//                .withTemperature(0f)
//                .build();
//
//        FileSystemResource audioFile = new FileSystemResource(tempFile);
//
//        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
//        AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);
//
//        tempFile.delete();
//        return new ResponseEntity<>(response.getResult().getOutput(), HttpStatus.OK);
//    }
//}


package com.ai.audio.transcriber.controller;

import com.ai.audio.transcriber.dto.TranscriptionResult;
import com.ai.audio.transcriber.model.TranscriptionRecord;
import com.ai.audio.transcriber.service.TranscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/transcribe")
public class TranscriptionController {

    private final TranscriptionService transcriptionService;


    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping
    public ResponseEntity<TranscriptionResult> transcribeAudio(@RequestParam("file") MultipartFile file, @RequestParam(value = "model", defaultValue = "gemini") String model) throws IOException {

        if (file == null || file.isEmpty()) {
            System.out.println("No file received!");
        } else {
            System.out.println("File received: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());
        }

        TranscriptionResult result = transcriptionService.transcribeAndAnalyze(file,model);


        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<TranscriptionRecord>> getHistory() {


        List<TranscriptionRecord> history = transcriptionService.getHistory();


        return ResponseEntity.ok(history);
    }
}

/*
package com.ai.audio.transcriber.controller;


import com.ai.audio.transcriber.dto.TranscriptionResult;
import com.ai.audio.transcriber.model.TranscriptionRecord;
import com.ai.audio.transcriber.model.User;
import com.ai.audio.transcriber.repository.TranscriptionRepository;
import com.ai.audio.transcriber.repository.UserRepository;
import com.ai.audio.transcriber.service.TranscriptionAnalysisService;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transcribe")
public class TranscriptionController {
    private static final int DAILY_LIMIT = 4;



    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final TranscriptionAnalysisService analysisService;
    private final TranscriptionRepository transcriptionRepository;
    private final UserRepository userRepository;


    public TranscriptionController(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            TranscriptionAnalysisService analysisService,
            TranscriptionRepository transcriptionRepository,
            UserRepository userRepository) {


        OpenAiAudioApi openAiAudioApi = new OpenAiAudioApi(apiKey);
        this.transcriptionModel = new OpenAiAudioTranscriptionModel(openAiAudioApi);

        this.analysisService = analysisService;
        this.transcriptionRepository = transcriptionRepository;
        this.userRepository = userRepository;
    }




    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated.");
        }

       String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("Authenticated user not found in database."));
    }

    @PostMapping
    public ResponseEntity<TranscriptionResult> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {


        User currentUser = getCurrentAuthenticatedUser();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();


        long callsToday = transcriptionRepository.countByUserIdAndCreatedDateAfter(
                currentUser.getId(),
                startOfDay
        );

        if (callsToday >= DAILY_LIMIT) {
           String message = String.format("Daily transcription limit reached. You have made %d calls (Max: %d).", DAILY_LIMIT, DAILY_LIMIT);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
        }
        File tempFile = File.createTempFile("audio", ".wav");
        file.transferTo(tempFile);

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withLanguage("en")
                .withTemperature(0f)
                .build();

        FileSystemResource audioFile = new FileSystemResource(tempFile);
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);

        String transcriptionText = response.getResult().getOutput();
        tempFile.delete();


        String analysisResult = analysisService.summarize(transcriptionText);


        TranscriptionRecord record = new TranscriptionRecord();
        record.setUser(currentUser);
        record.setOriginalFileName(file.getOriginalFilename());
        record.setFullTranscription(transcriptionText);
        record.setAiAnalysis(analysisResult);

        TranscriptionRecord savedRecord = transcriptionRepository.save(record);


        return new ResponseEntity<>(
                new TranscriptionResult(
                        savedRecord.getFullTranscription(),
                        savedRecord.getAiAnalysis(),
                        savedRecord.getId()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<TranscriptionRecord>> getHistory() {
        User currentUser = getCurrentAuthenticatedUser();


        List<TranscriptionRecord> history = transcriptionRepository.findByUserIdOrderByCreatedDateDesc(currentUser.getId());


        return ResponseEntity.ok(history);
    }
}*/
