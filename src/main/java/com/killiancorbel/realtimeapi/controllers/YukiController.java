package com.killiancorbel.realtimeapi.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.models.responses.YukiDataRes;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping(path="/yuki")
public class YukiController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(YukiController.class);

    private JwtUtil jwtUtil;
    public YukiController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount =
                        new FileInputStream(firebaseConfigPath);

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase application initialized");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody YukiDataRes getCurrentYukiData(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByEmail(decodedToken.getEmail());
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
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/register")
    public @ResponseBody YukiDataRes register(@RequestBody(required = false) YukiData body) {
        User user = userRepository.findByEmail(body.getUser().getEmail());
        if (user == null) {
            user = new User();
            user.setAppId("yuki");
            user.setPushId(body.getUser().getPushId());
            user.setEmail(body.getUser().getEmail());
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

    @PostMapping("/tokens")
    public @ResponseBody YukiDataRes removeTokens(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData yukiDataReq) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByEmail(decodedToken.getEmail());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            yukiData.setTokens(yukiDataReq.getTokens());
            yukiRepository.save(yukiData);
            return new YukiDataRes(yukiData);
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }
}
