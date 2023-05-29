package com.example.telegrambot.listener;

import com.example.telegrambot.entity.NotificationTask;
import com.example.telegrambot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component

public class TelegramBotUpdatesListener implements UpdatesListener {
    //Logger используем sl4g и LoggerFactory тоже
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final Pattern pattern = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})\\s + ([А-я\\d\\s.,!?:]+)"
    );
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TelegramBotUpdatesListener(TelegramBot telegramBot,NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Hanldates update: {}", update);
                Message message = update.message();
                Long chatId = message.chat().id();
                String text = message.text();
                if ("/start".equals(text)) {
                    sendMessage(chatId, """
                            Привет!Давай встретимся в формате: 22.02.2023 21:00
                            """);

                } else if (text != null) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()){
                        LocalDateTime dateTime = parse(matcher.group(1));
                        if (Objects.isNull(dateTime)) {
                            sendMessage(chatId,"Некорректный формат даты");
                        }else {

                        }
                        String txt = matcher.group(2);
                        NotificationTask notificationTask = new NotificationTask();
                        notificationTask.setChatId(chatId);
                        notificationTask.setMessage(txt);
                        notificationTask.setNotificationDateTime(dateTime);
                        notificationTaskService.save(notificationTask);

                } else {
                        sendMessage(chatId,"Некорректный формат сообщения");
                    }
                    }

            });
        } catch (Exception e) {
            logger.error(e.getMessage(),e);

        }
        
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime,dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;

        }
    }
    private void sendMessage(Long chatId,String message) {
        SendMessage sendMessage = new SendMessage(chatId,message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error during sending message: {}",sendResponse.description());
        }
    }
}
