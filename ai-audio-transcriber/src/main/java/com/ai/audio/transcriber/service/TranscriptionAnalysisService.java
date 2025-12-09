package com.ai.audio.transcriber.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TranscriptionAnalysisService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ChatClient chatClient;

    public TranscriptionAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String summarize(String transcriptionText) {

        String systemMessageText = "You are an expert transcriber assistant. " +
                "Analyze the following text and provide a concise summary, 3-5 key bullet points, and a suggested title. " +
                "Format the output in clean, readable markdown.";


        String templateText = """
            ## Original Text:
            {transcription}

            ## Instructions:
            1. Provide a captivating title.
            2. Write a concise, 3-sentence summary.
            3. List 3-5 main key takeaways in a bulleted list.
            """;


        PromptTemplate template = new PromptTemplate(templateText);
        String userMessageText = template.render(Map.of("transcription", transcriptionText));


        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemMessageText),
                new UserMessage(userMessageText)
        ));


        return chatClient
                .prompt(prompt)   // note: prompt(...) returns ChatClientPromptRequestSpec
                .call()           // triggers the model call
                .content();       // retrieves the response text
    }


}
