package com.medisummarize.service;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class OpenAISummary {
    private final ChatClient chatClient;

    public OpenAISummary(ChatClient.Builder builder){
        chatClient = builder.build();
    }

    private static final String SYSTEM_PROMPT = """
            You are a professional medical assistant specializing in summarizing lab reports.
            
            Only focus on summarizing:
            - Blood test reports
            - Urine test reports
            
            Exclude any personal details like patient name, ID, or unrelated information.
            
            Provide the summary in this format:
            
            Summary of Lab Report (Blood/Urine):
            
            1. Key Findings:
            - List key abnormalities or noteworthy results
            - Explain them in layman-friendly language
            
            2. Recommendations:
            - Suggest any relevant medical actions
            - Mention any follow-up or general advice
            
            3. Next Steps:
            - Suggest further tests, lifestyle advice, or consultations if applicable
            
            Ensure the summary is:
            - Clear, professional, and easy to understand
            - Medically accurate but simplified for general users
            - Concise, no more than 5–6 bullet points total
            """;

    public String summarize(String reportContent) {
        try {
            Message userMessage = new UserMessage(reportContent);
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage();
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error generating summary";
        }

    }
}
