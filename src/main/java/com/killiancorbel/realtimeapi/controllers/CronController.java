package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CronController {
    @Autowired
    YukiRepository yukiRepository;
    @Autowired
    NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePremium() {
        List<YukiData> premiums = yukiRepository.findAll();

        for (YukiData p : premiums) {
            if (!p.isDoneToday()) {
                p.setStreak(0);
            }
            p.setDoneToday(false);
            if (p.isPremium()) {
                p.setTokens(15000);
            }
            yukiRepository.save(p);
        }
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendNotifications() {
        List<YukiData> allUsers = yukiRepository.findAll();
        for (YukiData u : allUsers) {
            if (u.isNotifications()) {
                notificationService.sendDailyNotification(u.getUser().getPushId(), u.getLanguage());
            }
        }
    }
}
