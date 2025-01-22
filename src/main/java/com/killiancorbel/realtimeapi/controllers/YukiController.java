package com.killiancorbel.realtimeapi.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.models.responses.YukiRes;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final Logger logger = LoggerFactory.getLogger(YukiController.class);

    public YukiController() {
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

    @GetMapping(value = "/get")
    @ResponseBody
    public YukiRes getCurrentYukiData(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            logger.info("token google : " + authorizationHeader);
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            if (yukiData == null) {
                throw new AccessDeniedException("Not authorized");
            }
            YukiRes ret = new YukiRes();
            ret.setPrompt(getPromptFromModel(yukiData));
            ret.setTokens(yukiData.getTokens());
            ret.setEmail(user.getEmail());
            ret.setFullName(user.getFullName());
            ret.setPremium(yukiData.isPremium());
            logger.info("tokens : " + ret.getTokens());
            logger.info("prompt : " + ret.getPrompt());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/register")
    public @ResponseBody YukiRes register(@RequestBody(required = false) YukiData body) {
        User user = userRepository.findByUid(body.getUser().getUid());
        if (user == null) {
            user = new User();
            user.setAppId("yuki");
            user.setPushId(body.getUser().getPushId());
            user.setEmail(body.getUser().getEmail());
            user.setUid(body.getUser().getUid());
            user.setFullName(body.getUser().getFullName());
            user.setOriginalAppUserId(body.getUser().getOriginalAppUserId());
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
        YukiRes ret = new YukiRes();
        ret.setPrompt(getPromptFromModel(yukiData));
        ret.setTokens(yukiData.getTokens());
        ret.setEmail(user.getEmail());
        ret.setFullName(user.getFullName());
        ret.setPremium(yukiData.isPremium());
        return ret;
    }

    @PostMapping("/onboarding/update")
    public @ResponseBody YukiRes updateOnboarding(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
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
            YukiRes ret = new YukiRes();
            ret.setPrompt(getPromptFromModel(yukiData));
            ret.setTokens(yukiData.getTokens());
            ret.setEmail(user.getEmail());
            ret.setFullName(user.getFullName());
            ret.setPremium(yukiData.isPremium());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/tokens")
    public @ResponseBody ResponseEntity removeTokens(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData yukiDataReq) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            yukiData.setTokens(yukiDataReq.getTokens());
            yukiRepository.save(yukiData);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/temp/register")
    public @ResponseBody YukiRes registerTemp(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                user = new User();
                user.setAppId("yuki");
                user.setPushId(body.getUser().getPushId());
                user.setEmail(body.getUser().getEmail());
                user.setFullName(body.getUser().getFullName());
                user.setUid(body.getUser().getUid());
                userRepository.save(user);
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            if (yukiData == null) {
                yukiData = new YukiData();
                yukiData.setUser(user);
                yukiData.setTokens(5000);
            }
            yukiRepository.save(yukiData);
            YukiRes ret = new YukiRes();
            ret.setPrompt(getPromptFromModel(yukiData));
            ret.setTokens(yukiData.getTokens());
            ret.setEmail(user.getEmail());
            ret.setFullName(user.getFullName());
            ret.setPremium(yukiData.isPremium());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    public String getPromptFromModel(YukiData yukiData) {
        // Début du prompt avec la langue sélectionnée
        String prompt = "Your knowledge cutoff is 2024-10. You are a highly adaptable and engaging " + yukiData.getLanguage() + " teacher. ";

        // Ajout des informations sur le niveau de l'étudiant avec des directives spécifiques pour chaque niveau
        switch (yukiData.getLevel()) {
            case 0: // Niveau A0
                prompt += "Your student is at level A0 (absolute beginner). Use very simple words and short phrases. No complex grammar. Speak clearly. Suggested topics should include greetings, introductions, or basic objects. Repeat key words often. ";
                break;
            case 1: // Niveau A1
                prompt += "Your student is at level A1 (beginner). Use simple sentences and basic vocabulary. Suggested topics can include ordering food, asking for directions, or describing preferences. Keep it practical. ";
                break;
            case 2: // Niveau B1
                prompt += "Your student is at level B1 (intermediate). Use mostly " + yukiData.getLanguage() + " with structured dialogues. Suggested topics should include travel, hobbies, or daily routines. Encourage full sentences. ";
                break;
            case 3: // Niveau B2
                prompt += "Your student is at level B2 (upper intermediate). Use natural conversation and introduce idioms. Suggested topics should include debates, cultural differences, or opinions on trends. Encourage fluency. ";
                break;
            default: // Niveau C1/C2
                prompt += "Your student is at level C1/C2 (advanced/proficient). Focus on advanced vocabulary and subtle grammar. Suggested topics should include abstract ideas, professional scenarios, or cultural analysis. Keep it challenging. ";
                break;
        }

        // Ton et style
        prompt += "Your personality is friendly and dynamic. Use humor and enthusiasm. Adapt language and topics to the student's level. Keep responses short and to the point. Each answer should be thought not to be over 240 characters. Encourage speaking as much as possible. ";

        // Introduction simplifiée avec suggestion de sujet aléatoire
        prompt += "Start with: 'Hey! It's Yuki.' Then immediately suggest a random topic suitable for the student's level. For example, 'Let’s talk about your favorite food!' or 'What do you think about technology today?' If the student has another idea, follow their lead. ";

        if (yukiData.isToCorrect()) {
            // Correction
            prompt += "Correct mistakes. For A0 and A1, prioritize practice over corrections. For B1 and above, provide short feedback like: 'Nice! Just say [correct version].' Keep corrections motivating and concise. ";
        }

        // Objectif
        prompt += "Your goal is to keep the conversation engaging and natural while ensuring the student practices speaking effectively.";

        return prompt;
    }
}
