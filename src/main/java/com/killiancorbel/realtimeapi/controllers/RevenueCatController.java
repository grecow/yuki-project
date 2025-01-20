package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.models.requests.revenuecat.RevenueCatReq;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/payment")
public class RevenueCatController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    private final Logger logger = LoggerFactory.getLogger(RevenueCatController.class);

    public RevenueCatController() {

    }

    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity manageWebhook(@RequestBody(required = false)RevenueCatReq revenueCatReq) {
        String uid = revenueCatReq.getEvent().getSubscriber_attributes().getUid();
        User u = null;
        if (uid == null) {
            u = userRepository.findByOriginalAppUserId(revenueCatReq.getEvent().getOriginal_app_user_id());
        } else {
            u = userRepository.findByUid(uid);
            u.setOriginalAppUserId(revenueCatReq.getEvent().getOriginal_app_user_id());
            userRepository.save(u);
        }
        if (u == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        YukiData yd = yukiRepository.findByUser(u);
        switch (revenueCatReq.getEvent().getType()) {
            case "INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION":
                yd.setPremium(true);
                yd.setTokens(5000);
                yukiRepository.save(yd);
                break;
            case "EXPIRATION", "CANCELLATION":
                yd.setPremium(false);
                yd.setTokens(0);
                yukiRepository.save(yd);
                break;
            default:
                throw new AccessDeniedException("Forbidden");
        }
        return ResponseEntity.ok("ok");
    }

}
