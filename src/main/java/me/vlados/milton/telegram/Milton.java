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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class Milton extends TelegramLongPollingBot {

    private final OpenAIService aiService;

    private final TelegramBotsApi botsApi;

    private boolean isFirstTime = true;
    private boolean isFirstHuman = true;
    private boolean isFirstTalos = true;
    private boolean isFirstConf = true;
    private boolean isInSession = false;

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
        // If isInSession is true and the message is not 'exit' then all user messages will be sent to the OpenAI
        // and generated text responses will be sent back to user
        if (isInSession && update.hasMessage() && !update.getMessage().getText().equals("exit")) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            System.out.println(messageText);

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

        // If is is the first time working with Milton (if '/start' was called once) then we will initialize the first reply
        else if (isFirstTime && update.hasMessage() && update.getMessage().getText().equals("/start")) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
            sendMessage.setText("""
                    Loading Library session... Done.
                    Mounting local disks... [47 million] distributed resources found
                    Connecting network drives..... Error: network inaccessible.
                    Loading Milton Library Assistant..... Done.
                    Initiating command prompt... Done.

                    Library archive session ready.""");

            sendMessage.setReplyMarkup(createInlineKeyboardList());

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            isFirstTime = false;
        }

        // If user has sent the message and if it is 'list' and '/start' was called before then list of documents will be listed
        else if (update.hasMessage() && update.getMessage().getText().equals("list") && !isFirstTime) {
            String userMessage = update.getMessage().getText();

            if (userMessage.equals("list")) {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setReplyMarkup(createInlineKeyboardReplies());
                message.setText("""
                        > list

                        Searching for locally cached resources...

                        webcrawl 2019/01/30\t\tthe_human_machine.html
                        loc\t\t\t\t\t\t\t\t\t\t\t\t0000/06/03\ttalos.eml
                        archive\t\t\t\t\t9998/05/01\tquestioning_doubt_conf.txt""");

                try {
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        // isInSession and 'exit' checks
        // If arguments are true, then the session with Milton will be terminated
        else if (isInSession && update.hasMessage() && update.getMessage().getText().equals("exit")) {
            isInSession = false;

            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setReplyMarkup(createInlineKeyboardReplies());
            message.setText("""
                    Terminating support session...Done
                    Resuming library archive session. ..Done
                    > list

                    Searching for locally cached resources...

                    webcrawl 2019/01/30\t\tthe_human_machine.html
                    loc\t\t\t\t\t\t\t\t\t\t\t\t0000/06/03\ttalos.eml
                    archive\t\t\t\t\t9998/05/01\tquestioning_doubt_conf.txt""");

            try {
                execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        
        // If the user has clicked the inline button then we will check which one was clicked exactly
        else if (update.hasCallbackQuery()) {
            // Handle callback data from inline buttons
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            SendMessage sendMessage = new SendMessage();

            if ("listCallbackData".equals(callbackData) || ("closeCallbackData").equals(callbackData)) {
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(createInlineKeyboardReplies());
                sendMessage.setText("""
                        Searching for locally cached resources...

                        webcrawl 2019/01/30\t\tthe_human_machine.html
                        loc\t\t\t\t\t\t\t\t\t\t\t\t0000/06/03\ttalos.eml
                        archive\t\t\t\t\t9998/05/01\tquestioning_doubt_conf.txt""");

                try {
                    execute(sendMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            if ("humanCallbackData".equals(callbackData)) {
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(createInlineKeyboardClose());
                sendMessage.setText("""
                        Topic #3:
                                            
                        One day you discover that you are not a human being, but a machine. Your life so far was real, no-one controlled you or programmed you to behave in some specific way; your physical and mental capacities are identical to those of an organic human being. But you were created in a lab.
                                            
                        No-one except you knows about this. Your family, your friends, they all think you are a regular human being like themselves. You could continue to live your life the way you have before and nothing would change.
                                            
                        How do you react?
                                            
                        Pay specific attention to these questions:
                                            
                        a) Does your concept of yourself change? Are you the same person you thought you were?
                                            
                        b) Does your understanding of the world itself change?
                                            
                        c) Do you reveal the information to others, or do you keep it to yourself? Why?
                                            
                        1500-2000 words. The 26th is the final deadline, no extensions will be granted. Submit via email or ##%66 61 63 65 62 6f 6f 6b 2e 63 6f 6d 2f 63 72 6f 74 65 61 6d""");

                try {
                    execute(sendMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                isFirstHuman = false;
            }

            if ("talosCallbackData".equals(callbackData)) {
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(createInlineKeyboardClose());
                sendMessage.setText("""
                        From: Alexandra Drennan
                                                
                        To: Noematics Mailing List
                                                
                        Subject: [NML] Talos Principle
                                                
                        Have you heard of the Talos Principle? It's this old philosophical concept about the impossibility of avoiding reality - no matter what you believe, if you lose your blood, you will die. I think that applies to our situation more than we'd like to admit. We could close our eyes and pretend that everything's going to be all right... but it won't change the physical reality of what's going to happen to our 4E 6F 20 6D 61 6E 20 69
                                                
                        I think that, as scientists, it is our duty to face the truth, and to ask ourselves the most important question: how can we help?
                                                
                        73 20 6C 69 62 65 72 61 74 65 64 20 66 72 6F 6D 20 66 65 61 72 20 77 68 6F 20 64 61 72 65 20 6E 6F 74 20 73 65 65 20 68 69 73
                                                
                        20 70 6C 61 63 65 20 69 6E 20 74 68 65 20 77 6F 72 6C 64 20 61 73 20 69 74 20 69 73 3B 20 6E 6F 20 6D 61 6E 20 63 61 6E 20 61 63 68 69 65 76 65 20 74 I think I have an idea 68 65 20 67 72 65 61 74 6E 65 73 73 20 6F 66 20 77 68 69 63 68 20 68 65 20 69 73 20 63 61 70 61 62 6C 65 20 75 6E 74 69 6C 20
                                                
                        68 65 20 68 61 73 20 61 6C 6C 6F 77 65 64 20 68 69 6D 73 65 6C 66 20 74 6F 20 73 65 65 20 68 69 73 20 6F 77 6E 20 6C 69 74 74 6C 65 6E 65 73 73 2E 20
                                                
                        Regards,
                                                
                        Alexandra""");

                try {
                    execute(sendMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                isFirstTalos = false;
            }

            if ("confCallbackData".equals(callbackData)) {
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(createInlineKeyboardClose());
                sendMessage.setText("""
                        Keynote Speech by N. Sarabhai, "Questioning Doubt"
                                                
                        They say "doubt everything," but I disagree. Doubt is useful in small amounts, but too much of it leads to apathy and confusion. No, don't doubt everything. QUESTION everything. That's the real trick. Doubt is just a lack of certainty. If you doubt everything, you'll doubt evolution, science, faith, morality, even reality itself - and you'll end up with nothing, because doubt doesn't give anything back. But questions have answers, you see. If you question everything, you'll find that a lot of what we believe is untrue… but you might also discover that some things ARE true. You might discover what your own beliefs are. And then you'll question them again, and again, eliminating flaws, discovering lies, until you get as close to the truth as you can.
                                                
                        Questioning is a lifelong process. That's precisely what makes it so unlike doubt. Questioning engages with reality, interrogating all it sees. Questioning leads to a constant assault on the intellectual status quo, where doubt is far more likely to lead to resigned acceptance. After all, when the possibility of truth is doubtful (excuse the pun), why not simply play along with the most convenient lie?e #%&%§/$\s
                                                
                        Questioning is progress, but doubt is stagnation.""");

                try {
                    execute(sendMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                isFirstConf = false;
            }

            if ("miltonCallbackData".equals(callbackData)) {
                sendMessage.setChatId(chatId);
                sendMessage.setText("""
                        Loading Milton Library Assistant. ..Done
                        Initiating plain language interface...Done
                        Support session opened.
                                                
                        [type 'Close' to leave MLA session]""");

                try {
                    execute(sendMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                sendMessage.setText("Hello, guest. How can I help you today?");

                try {
                    execute(sendMessage); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                isInSession = true;
            }
        }
    }

    // Creating buttons
    public InlineKeyboardMarkup createInlineKeyboardReplies() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline4 = new ArrayList<>();

        // button 1
        InlineKeyboardButton the_human_machine = new InlineKeyboardButton();

        if (isFirstHuman) {
            the_human_machine.setText("* the_human_machine.html");
        } else {
            the_human_machine.setText("the_human_machine.html");
        }

        the_human_machine.setCallbackData("humanCallbackData");
        rowInline.add(the_human_machine);

        // button 2
        InlineKeyboardButton talos = new InlineKeyboardButton();

        if (isFirstTalos) {
            talos.setText("* talos.eml");
        } else {
            talos.setText("talos.eml");
        }

        talos.setCallbackData("talosCallbackData");
        rowInline2.add(talos);

        // button 3
        InlineKeyboardButton questioning_doubt_conf = new InlineKeyboardButton();

        if (isFirstConf) {
            questioning_doubt_conf.setText("* questioning_doubt_conf.txt");
        } else {
            questioning_doubt_conf.setText("questioning_doubt_conf.txt");
        }

        questioning_doubt_conf.setCallbackData("confCallbackData");
        rowInline3.add(questioning_doubt_conf);

        // button 4
        InlineKeyboardButton mla = new InlineKeyboardButton();
        mla.setText("Speak to Milton");
        mla.setCallbackData("miltonCallbackData");
        rowInline4.add(mla);

        rowsInline.add(rowInline);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        rowsInline.add(rowInline4);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createInlineKeyboardList() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton list = new InlineKeyboardButton();
        list.setText("list");
        list.setCallbackData("listCallbackData");
        rowInline.add(list);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createInlineKeyboardClose() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton list = new InlineKeyboardButton();
        list.setText("Close");
        list.setCallbackData("closeCallbackData");
        rowInline.add(list);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    // Provide your own name
    @Override
    public String getBotUsername() {
        return "MiltonLibraryAssistant_Bot";
    }
}
