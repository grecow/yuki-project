package com.killiancorbel.realtimeapi.controllers;

import java.time.LocalDate;

import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.services.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

import com.killiancorbel.realtimeapi.models.Stat;
import com.killiancorbel.realtimeapi.repositories.StatRepository;

@Controller
public class CronController {
    private final Logger logger = LoggerFactory.getLogger(CronController.class);

    @Autowired
    YukiRepository yukiRepository;
    @Autowired
    StatRepository statRepository;
    @Autowired
    NotificationService notificationService;

    /**
     * Midnight: reset daily state, manage streaks, refill premium tokens.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyReset() {
        List<YukiData> allUsers = yukiRepository.findAll();
        int doneTodays = 0;

        for (YukiData yd : allUsers) {
            if (!yd.isDoneToday()) {
                yd.setStreak(0);
            } else {
                doneTodays++;
            }
            yd.setDoneToday(false);
            yd.setDailyConversationsUsed(0);
            if (yd.isPremium() || yd.isInTrial()) {
                yd.setTokens(15000);
            }
            yukiRepository.save(yd);
        }

        Stat stat = new Stat();
        stat.setDate(LocalDate.now().toString());
        stat.setType("doneToday");
        stat.setValue(doneTodays);
        statRepository.save(stat);
        logger.info("Daily reset done. {} users completed today.", doneTodays);
    }

    /**
     * 9 AM: daily practice reminder.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void morningReminder() {
        List<YukiData> allUsers = yukiRepository.findAll();
        for (YukiData yd : allUsers) {
            if (yd.isNotifications() && yd.getUser().getPushId() != null) {
                notificationService.sendDailyNotification(yd.getUser().getPushId(), yd.getLanguage());
            }
        }
        logger.info("Morning reminders sent.");
    }

    /**
     * 8 PM: streak risk notification — remind users who haven't practiced today.
     */
    @Scheduled(cron = "0 0 20 * * ?")
    public void streakRiskReminder() {
        List<YukiData> allUsers = yukiRepository.findAll();
        for (YukiData yd : allUsers) {
            if (yd.isNotifications() && !yd.isDoneToday() && yd.getStreak() > 0 && yd.getUser().getPushId() != null) {
                notificationService.sendStreakRiskNotification(yd.getUser().getPushId(), yd.getLanguage(), yd.getStreak());
            }
        }
        logger.info("Streak risk reminders sent.");
    }
}
