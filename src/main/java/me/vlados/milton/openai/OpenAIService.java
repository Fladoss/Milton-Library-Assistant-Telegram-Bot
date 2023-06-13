package me.vlados.milton.openai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class OpenAIService {

    private OpenAiService openAiService;

    private final String openaiKey;

    private final String model;

    private final double temperature;

    private final String systemTask;

    public OpenAIService(
            @Value("${openai.key}") String key,
            @Value("${openai.model}") String model,
            @Value("${openai.temperature}") double temperature,
            @Value("${openai.system}") String systemTask) {
        this.openaiKey = key;
        this.model = model;
        this.temperature = temperature;
        this.systemTask = systemTask;
    }

    @PostConstruct
    public void init() {
        openAiService = new OpenAiService(openaiKey, Duration.ofSeconds(90));
    }

    public String generateResponse(String userPrompt) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .temperature(temperature)
                .messages(Arrays.asList(
                        new ChatMessage("system", systemTask),
                        new ChatMessage("user", userPrompt)))
                .build();

        StringBuilder response = new StringBuilder();

        openAiService.createChatCompletion(request)
                .getChoices()
                .forEach(choice -> response.append(choice.getMessage().getContent()));

        System.out.println("> User: " + userPrompt);
        System.out.println("> Milton: " + response);

        return response.toString();
    }
}
