package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.models.responses.YukiDataRes;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/yuki")
public class YukiController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(YukiController.class);

    private JwtUtil jwtUtil;
    public YukiController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/get/{id}")
    public @ResponseBody YukiDataRes getCurrentYukiData(@PathVariable String id) {
        User user = userRepository.findByPushId(id);
        if (user == null) {
            throw new AccessDeniedException("no user");
        }
        YukiData yukiData = yukiRepository.findByUser(user);
        if (yukiData == null) {
            yukiData = new YukiData();
            yukiData.setUser(user);
            yukiData.setTokens(5000);
            yukiRepository.save(yukiData);
        }
        return new YukiDataRes(yukiData);
    }

    @PostMapping("/register/{id}")
    public @ResponseBody YukiDataRes register(@PathVariable String id, @RequestBody(required = false) YukiData body) {
        User user = userRepository.findByPushId(id);
        if (user == null) {
            user = new User();
            user.setAppId("yuki");
            user.setPushId(id);
            userRepository.save(user);
        }
        YukiData yukiData = yukiRepository.findByUser(user);
        if (yukiData == null) {
            yukiData = new YukiData();
            yukiData.setUser(user);
            yukiData.setTokens(5000);
        }
        yukiData.setLevel(body.getLevel());
        yukiData.setLanguage(body.getLanguage());
        yukiData.setToCorrect(body.isToCorrect());
        yukiRepository.save(yukiData);
        return new YukiDataRes(yukiData);
    }

    @PostMapping("/tokens/{id}")
    public @ResponseBody YukiDataRes removeTokens(@PathVariable String id, @RequestBody(required = false) YukiDataRes yukiDataReq) {
        User user = userRepository.findByPushId(id);
        YukiData yukiData = yukiRepository.findByUser(user);
        yukiData.setTokens(yukiDataReq.getTokens());
        yukiRepository.save(yukiData);
        return new YukiDataRes(yukiData);
    }
}
