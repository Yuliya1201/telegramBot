package com.example.telegrambot.timer;

import com.example.telegrambot.repositiry.NotificationTaskRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationTaskTimer {
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationTaskTimer(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void task() {
        notificationTaskRepository.findAllByNotificationTime(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES)
        ). forEach (notificationTask -> {
            telegramBot.execute(
                    new SendMessage(notificationTask.getChatId(),notificationTask.getMessage()));
            notificationTaskRepository.delete(notificationTask);

        });
    }
}
