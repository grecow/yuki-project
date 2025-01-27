package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CronController {
    @Autowired
    YukiRepository yukiRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePremium() {
        List<YukiData> premiums = yukiRepository.findAll();

        for (YukiData p : premiums) {
            if (p.isPremium()) {
                p.setTokens(10000);
                yukiRepository.save(p);
            }
        }
    }
}
