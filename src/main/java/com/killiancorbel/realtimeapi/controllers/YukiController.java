package com.killiancorbel.realtimeapi.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.Achievement;
import com.killiancorbel.realtimeapi.models.Lesson;
import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.models.responses.YukiRes;
import com.killiancorbel.realtimeapi.repositories.AchievementRepository;
import com.killiancorbel.realtimeapi.repositories.LessonRepository;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.services.PromptService;
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
import java.util.List;

@RestController
@RequestMapping(path="/yuki")
public class YukiController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private AchievementRepository achievementRepository;
    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    @Autowired
    private PromptService promptService;
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
            ret.setPrompt(promptService.getPrompt(yukiData));
            ret.setTokens(yukiData.getTokens());
            ret.setEmail(user.getEmail());
            ret.setFullName(user.getFullName());
            ret.setPremium(yukiData.isPremium());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/register")
    public @ResponseBody YukiRes register(@RequestBody(required = false) YukiData body) {
        User user = userRepository.findByUid(body.getUser().getUid());
        if (user == null) {
            user = userRepository.findByOriginalAppUserId(body.getUser().getOriginalAppUserId());
            if (user == null) {
                user = new User();
                user.setAppId("yuki");
                user.setPushId(body.getUser().getPushId());
                user.setEmail(body.getUser().getEmail());
                user.setUid(body.getUser().getUid());
                user.setFullName(body.getUser().getFullName());
                user.setOriginalAppUserId(body.getUser().getOriginalAppUserId());
            } {
                user.setEmail(body.getUser().getEmail());
                user.setUid(body.getUser().getUid());
                user.setFullName(body.getUser().getFullName());
            }
            userRepository.save(user);
        }
        YukiData yukiData = yukiRepository.findByUser(user);
        if (yukiData == null) {
            yukiData = new YukiData();
            yukiData.setUser(user);
            yukiData.setTokens(15000);
        }
        yukiData.setLevel(body.getLevel());
        yukiData.setLanguage(body.getLanguage());
        yukiData.setToCorrect(body.isToCorrect());
        yukiRepository.save(yukiData);
        YukiRes ret = new YukiRes();
        ret.setPrompt(promptService.getPrompt(yukiData));
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
            ret.setPrompt(promptService.getPrompt(yukiData));
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
                yukiData.setTokens(15000);
            }
            yukiRepository.save(yukiData);
            YukiRes ret = new YukiRes();
            ret.setPrompt(promptService.getPrompt(yukiData));
            ret.setTokens(yukiData.getTokens());
            ret.setEmail(user.getEmail());
            ret.setFullName(user.getFullName());
            ret.setPremium(yukiData.isPremium());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @GetMapping("/lessons")
    public @ResponseBody List<Lesson> getLessons(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            return lessonRepository.findAllByLanguageAndPublished(yukiData.getLanguage(), true);
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @GetMapping("/achievements")
    public @ResponseBody List<Achievement> getAchievements(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            return achievementRepository.findAll();
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/after-lesson")
    public @ResponseBody ResponseEntity updateUser(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            yukiData.setVocabulary(body.getVocabulary());
            yukiData.setSentences(body.getSentences() + yukiData.getSentences());
            yukiData.setTimeStudied(body.getTimeStudied() + yukiData.getTimeStudied());
            if (!body.getLessonsDone().isEmpty()) {
                if (!yukiData.isDoneToday()) {
                    yukiData.setStreak(yukiData.getStreak() + 1);
                    if (yukiData.getStreak() > yukiData.getMaxStreak()) {
                        yukiData.setMaxStreak(yukiData.getStreak());
                    }
                }
                yukiData.setDoneToday(true);
                yukiData.addLessonsDone(body.getLessonsDone().get(0));
            }
            yukiRepository.save(yukiData);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new AccessDeniedException("Not authorized");
        }
    }
}
