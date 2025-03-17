package com.killiancorbel.realtimeapi.controllers;

import java.util.Date;

import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.services.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

import com.killiancorbel.realtimeapi.models.Stat;
import com.killiancorbel.realtimeapi.repositories.StatRepository;

@Controller
public class CronController {
    @Autowired
    YukiRepository yukiRepository;
    @Autowired
    StatRepository statRepository;
    @Autowired
    NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePremium() {
        List<YukiData> premiums = yukiRepository.findAll();
        int doneTodays = 0;

        for (YukiData p : premiums) {
            if (p.getLessonsDone() != null && p.getLessonsDone().size() < 9) {
                if (!p.isDoneToday()) {
                    p.setStreak(0);
                } else {
                    doneTodays = doneTodays + 1;
                }
                p.setDoneToday(false);
                yukiRepository.save(p);
            }
            if (p.isPremium()) {
                p.setTokens(15000);
                yukiRepository.save(p);
            }
        }
        Stat stat = new Stat();
        stat.setDate(new Date().toString());
        stat.setType("doneToday");
        stat.setValue(doneTodays);
        statRepository.save(stat);
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
