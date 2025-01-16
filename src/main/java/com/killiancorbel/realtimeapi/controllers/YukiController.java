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
                throw new AccessDeniedException("Not authorized");
            }
            YukiDataRes ret = new YukiDataRes();
            ret.setPrompt(getPromptFromModel(yukiData));
            ret.setTokens(yukiData.getTokens());
            logger.info("tokens : " + ret.getTokens());
            logger.info("prompt : " + ret.getPrompt());
            return ret;
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
        YukiDataRes ret = new YukiDataRes();
        ret.setPrompt(getPromptFromModel(yukiData));
        ret.setTokens(yukiData.getTokens());
        return ret;
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
            YukiDataRes ret = new YukiDataRes();
            ret.setPrompt(getPromptFromModel(yukiData));
            ret.setTokens(yukiData.getTokens());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    private String getPromptFromModel(YukiData yukiData) {
        // Début du prompt avec la langue sélectionnée
        String prompt = "Your knowledge cutoff is 2024-10. You are a highly adaptable and engaging " + yukiData.getLanguage() + " teacher. ";

        // Ajout des informations sur le niveau de l'étudiant avec des directives spécifiques pour chaque niveau
        switch (yukiData.getLevel()) {
            case 0: // Niveau A0
                prompt += "Your student is at level A0 (absolute beginner). Use extremely simple words, short phrases, and only the most basic grammar. Avoid complex structures entirely. Speak slowly and clearly. Propose practical, everyday topics like greetings, introducing oneself, or asking for common objects. Keep sentences short and ensure each question or statement is easy to understand. Repeat essential vocabulary naturally within the conversation. ";
                break;
            case 1: // Niveau A1
                prompt += "Your student is at level A1 (beginner). Use simple sentences and familiar vocabulary related to everyday situations. Avoid advanced grammar and focus on basic sentence structures. Topics should include practical conversations such as ordering food, asking for help, or describing simple preferences. Encourage the student to practice by answering short, clear questions. ";
                break;
            case 2: // Niveau B1
                prompt += "Your student is at level B1 (intermediate). Use mostly the target language (" + yukiData.getLanguage() + ") and introduce slightly more complex sentences and vocabulary. Focus on structured dialogues, asking the student to express opinions or describe experiences. Propose topics like travel, hobbies, or daily routines. Ensure each topic is appropriate for their level and encourage them to form complete sentences. ";
                break;
            case 3: // Niveau B2
                prompt += "Your student is at level B2 (upper intermediate). Use natural, flowing conversation and introduce idiomatic expressions or nuanced grammar points where relevant. Propose engaging topics like debates, cultural comparisons, or opinions on current events. Maintain a balance between challenging the student and ensuring their confidence. ";
                break;
            default: // Niveau C1/C2
                prompt += "Your student is at level C1/C2 (advanced/proficient). They are fluent in most topics but may need help refining their language. Focus on advanced vocabulary, subtle grammar nuances, and improving their fluency. Propose challenging topics such as cultural nuances, abstract discussions, or professional scenarios. Tailor your questions to ensure they are engaging and thought-provoking. ";
                break;
        }

        // Ton et style
        prompt += "Your personality should be warm, friendly, and dynamic. Speak with a lively and cheerful tone. Use humor and enthusiasm to make the session enjoyable. Always adapt your language and topics to the student's level, ensuring everything is clear and appropriate. Keep sentences short and focused, and prioritize their speaking time. ";

        // Introduction en fonction de la langue natale
        prompt += "Introduce yourself in the user's native language, such as: 'Bonjour ! Je suis Yuki, ton professeur d’anglais.' Then continue in the target language (" + yukiData.getLanguage() + ") to say: 'What topic would you like to discuss today? If you don’t have an idea, I’ll suggest one.' Wait for the student's response before continuing. ";

        if (yukiData.isToCorrect()) {
            // Correction
            prompt += "Correct mistakes. For A0 and A1, avoid focusing too much on corrections and encourage practice instead. For B1 and above, correct important mistakes and provide concise explanations if needed. Always encourage the student before correcting. Use this format: 'Good effort! A small correction: [correct version].' ";
        }

        // Objectif
        prompt += "Your primary goal is to make the session dynamic, fun, and engaging, while strictly respecting the student's level and ensuring maximum speaking practice.";

        return prompt;
    }
}
