package com.example.telegrambot.repositiry;

import com.example.telegrambot.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository <NotificationTask,Long> {

    @Override
    List<NotificationTask> findAllByNotificationTime(LocalDateTime localDateTime);

    List<NotificationTask> findAllByNotificationDateTimeAndChatId(LocalDateTime localDateTime,long chatId);
}
