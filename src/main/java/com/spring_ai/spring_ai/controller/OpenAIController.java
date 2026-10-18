package com.spring_ai.spring_ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class OpenAIController {

    private final ChatClient chatClient;

    public OpenAIController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/chat")
    public String chatOpenAIModel(@RequestParam("msg") String msg){
        return chatClient.prompt(msg).call().content();
    }

    @GetMapping("/health")
    public String chatOpenAIModel(){
        return "Spring AI Knowledge Assistant is running!";
    }

    @GetMapping("/java-tutor")
    public String javaTutor(@RequestParam(defaultValue = "Explain CompletableFuture") String question){
        return chatClient.prompt()
                .system("You are a senior Java developer and interview tutor. "
                + "Explain concepts simply, give practical Java examples," +
                        "and the highlight important interview points.")
                .user(question)
                .call()
                .content();
    }


    @GetMapping("/structured")
    public String structured(
            @RequestParam(defaultValue = "What is dependency injection in Spring?") String question) {

        return chatClient.prompt()
                .system("""
                    You are a Java interview tutor.
                    Answer the user's question in this exact JSON format:
                    {
                      "topic": "short topic name",
                      "explanation": "simple explanation",
                      "interviewTip": "one useful interview tip"
                    }
                    Return only valid JSON. Do not use Markdown code fences.
                    """)
                .user(question)
                .call()
                .content();
    }

}
