package com.killiancorbel.realtimeapi.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.*;
import com.killiancorbel.realtimeapi.models.responses.PlaygroundRes;
import com.killiancorbel.realtimeapi.models.responses.YukiRes;
import com.killiancorbel.realtimeapi.repositories.*;
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
    private LessonDoneRepository lessonDoneRepository;
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
            ret.setStreak(yukiData.getStreak());
            ret.setMaxStreak(yukiData.getMaxStreak());
            ret.setTimeStudied(yukiData.getTimeStudied());
            ret.setSentences(yukiData.getSentences());
            ret.setVocabulary(yukiData.getVocabulary());
            ret.setDoneToday(yukiData.isDoneToday());
            ret.setAchievements(yukiData.getAchievements());
            ret.setLanguage(yukiData.getLanguage());
            ret.setLessonsDone(yukiData.getLessonsDone());
            ret.setNotifications(yukiData.isNotifications());
            ret.setXp(yukiData.getXp());
            ret.setUserLevel(yukiData.getCalculatedLevel());
            ret.setXpForNextLevel(yukiData.getXpForNextLevel());
            ret.setGoal(yukiData.getGoal());
            ret.setDiscoveryDone(yukiData.isDiscoveryDone());
            ret.setInTrial(yukiData.isInTrial());
            ret.setTrialDaysLeft(yukiData.getTrialDaysLeft());
            ret.setCanStartConversation(yukiData.canStartConversation());
            ret.setCorrectionsEnabled(yukiData.isPremium() || yukiData.isInTrial());
            ret.setTotalConversations(yukiData.getTotalConversations());
            return ret;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/discovery/complete")
    public @ResponseBody ResponseEntity<java.util.Map<String, Object>> completeDiscovery(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) throw new AccessDeniedException("no user");
            YukiData yukiData = yukiRepository.findByUser(user);
            yukiData.setDiscoveryDone(true);
            // Award XP for discovery lesson
            int wordsLearned = (int) body.getOrDefault("wordsLearned", 8);
            int phrasesLearned = (int) body.getOrDefault("phrasesLearned", 3);
            int xpGained = wordsLearned * 2 + phrasesLearned * 10;
            yukiData.addXp(xpGained);
            yukiRepository.save(yukiData);
            return ResponseEntity.ok(java.util.Map.of(
                "xpGained", xpGained,
                "wordsLearned", wordsLearned,
                "phrasesLearned", phrasesLearned
            ));
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/trial/start")
    public @ResponseBody ResponseEntity<java.util.Map<String, Object>> startTrial(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) throw new AccessDeniedException("no user");
            YukiData yukiData = yukiRepository.findByUser(user);
            if (yukiData.isPremium()) {
                return ResponseEntity.ok(java.util.Map.of("status", "already_premium"));
            }
            if (yukiData.getTrialStartDate() != null) {
                return ResponseEntity.ok(java.util.Map.of("status", "already_trialed", "daysLeft", yukiData.getTrialDaysLeft()));
            }
            yukiData.setTrialStartDate(java.time.LocalDateTime.now());
            yukiData.setTrialEndDate(java.time.LocalDateTime.now().plusDays(7));
            yukiData.setTokens(15000);
            yukiRepository.save(yukiData);
            return ResponseEntity.ok(java.util.Map.of("status", "trial_started", "daysLeft", 7));
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

            // XP calculation
            int xpGained = 0;
            xpGained += body.getSentences() * 2;           // 2 XP per sentence
            xpGained += (body.getTimeStudied() / 60) * 5;  // 5 XP per minute studied
            xpGained += body.getVocabulary();               // 1 XP per word

            if (!body.getLessonsDone().isEmpty()) {
                if (!yukiData.isDoneToday()) {
                    yukiData.setStreak(yukiData.getStreak() + 1);
                    if (yukiData.getStreak() > yukiData.getMaxStreak()) {
                        yukiData.setMaxStreak(yukiData.getStreak());
                    }
                    xpGained += 10; // Streak bonus
                }
                yukiData.setDoneToday(true);
                xpGained += 25; // Lesson completion bonus
                LessonDone d = new LessonDone();
                d.setLesson_id(body.getLessonsDone().get(0).getLesson_id());
                d.setDate(body.getLessonsDone().get(0).getDate());
                lessonDoneRepository.save(d);
                yukiData.addLessonsDone(d);
            }

            yukiData.addXp(xpGained);
            yukiRepository.save(yukiData);

            // Return XP gained for frontend animation
            return ResponseEntity.ok(java.util.Map.of(
                "xpGained", xpGained,
                "totalXp", yukiData.getXp(),
                "level", yukiData.getCalculatedLevel()
            ));
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new AccessDeniedException("Not authorized");
        }
    }

    @GetMapping("/playground")
    public @ResponseBody PlaygroundRes getPlaygroundPrompt(@RequestHeader("Authorization") String authorizationHeader){
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            PlaygroundRes res = new PlaygroundRes();
            res.setPrompt(promptService.getPlaygroundPrompt(yukiData));
            res.setLanguage(yukiData.getLanguage());
            return res;
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/language/update")
    public @ResponseBody ResponseEntity updateLanguage(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            yukiData.setLanguage(body.getLanguage());
            yukiRepository.save(yukiData);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/notifications")
    public @ResponseBody ResponseEntity updateNotifications(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            yukiData.setNotifications(body.isNotifications());
            yukiRepository.save(yukiData);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping("/update/push/{id}")
    public @ResponseBody ResponseEntity updateNotifications(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String id) {

        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("no user");
            }
            user.setPushId(id);
            userRepository.save(user);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public ResponseEntity deleteUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        } catch (FirebaseAuthException e) {
            throw new AccessDeniedException("Not authorized");
        }
        User user = userRepository.findByUid(decodedToken.getUid());
        if (user == null) {
            throw new AccessDeniedException("no user");
        }
        YukiData yd = yukiRepository.findByUser(user);
        yukiRepository.delete(yd);
        userRepository.delete(user);
        return ResponseEntity.ok("ok");
    }
}
