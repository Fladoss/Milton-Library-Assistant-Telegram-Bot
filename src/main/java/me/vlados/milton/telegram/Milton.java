package me.vlados.milton.telegram;

import jakarta.annotation.PostConstruct;
import me.vlados.milton.openai.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Milton extends TelegramLongPollingBot {

    private final OpenAIService aiService;

    private final TelegramBotsApi botsApi;

    private boolean isFirstTime = true;

    @Autowired
    public Milton(@Value("${telegram.bot.token}") String botToken, OpenAIService aiService, TelegramBotsApi botsApi) {
        super(botToken);
        this.aiService = aiService;
        this.botsApi = botsApi;
    }

    @PostConstruct
    public void registerBot() {
        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (isFirstTime) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("""
                        Loading Library session... Done.
                        Mounting local disks... [47 million] distributed resources found
                        Connecting network drives..... Error: network inaccessible.
                        Loading Milton Library Assistant..... Done.
                        Initiating command prompt... Done.
                        
                        Library archive session ready.""");

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                isFirstTime = false;
            }

            String response = aiService.generateResponse(messageText);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(response);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "MiltonLibraryAssistant_Bot";
    }
}
