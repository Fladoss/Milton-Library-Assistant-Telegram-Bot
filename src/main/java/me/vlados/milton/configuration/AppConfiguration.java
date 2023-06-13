package me.vlados.milton.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfiguration {
    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

//    @Bean
//    public OpenAIService openAIService(OpenAIProperties properties) {
//        return new OpenAIService(properties.getKey(), properties.getModel(), properties.getTemperature(), properties.getSystemTask());
//    }
//
//    @Bean
//    public Milton milton(TelegramBotsApi botsApi, MiltonProperties properties, OpenAIService openAIService) {
//        return new Milton(botsApi, properties.getToken(), openAIService);
//    }
}