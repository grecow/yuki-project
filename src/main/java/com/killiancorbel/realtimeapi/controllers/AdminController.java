package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.*;
import com.killiancorbel.realtimeapi.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import java.util.List;

@RestController
@RequestMapping(path="/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StatRepository statRepository;
    @Autowired
    private YukiRepository yukiRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private LessonDoneRepository lessonDoneRepository;
    @Autowired
    private AchievementRepository achievementRepository;

    @Value("${app.admin-token}")
    private String adminToken;

    private void verifyAdmin(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals(adminToken)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    @GetMapping(value = "/users")
    @ResponseBody
    public List<YukiData> getAllUsers(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(authorizationHeader);
        return yukiRepository.findAll(PageRequest.of(page, Math.min(size, 100))).getContent();
    }

    @PostMapping(value = "/user/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer id) {
        verifyAdmin(authorizationHeader);
        User u = userRepository.findById(id).orElseThrow(() -> new AccessDeniedException("User not found"));
        YukiData yd = yukiRepository.findByUser(u);
        if (yd != null) yukiRepository.delete(yd);
        userRepository.delete(u);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/yd/tokens")
    @ResponseBody
    public YukiData updateTokens(@RequestHeader("Authorization") String authorizationHeader, @RequestBody YukiData yukiDataReq) {
        verifyAdmin(authorizationHeader);
        YukiData yd = yukiRepository.findById(yukiDataReq.getId().intValue())
                .orElseThrow(() -> new AccessDeniedException("YukiData not found"));
        yd.setTokens(yukiDataReq.getTokens());
        yukiRepository.save(yd);
        return yd;
    }

    @GetMapping(value = "/lessons/get")
    @ResponseBody
    public List<Lesson> getLessons(@RequestHeader("Authorization") String authorizationHeader) {
        verifyAdmin(authorizationHeader);
        return lessonRepository.findAll();
    }

    @PostMapping(value = "/lessons/post")
    @ResponseBody
    public ResponseEntity<String> postLessons(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Lesson lessons) {
        verifyAdmin(authorizationHeader);
        Lesson l;
        if (lessons.getId() != null) {
            l = lessonRepository.findById(lessons.getId().intValue())
                    .orElseThrow(() -> new AccessDeniedException("Lesson not found"));
        } else {
            l = new Lesson();
        }
        l.setDescription(lessons.getDescription());
        l.setLanguage(lessons.getLanguage());
        l.setImage(lessons.getImage());
        l.setLesson_key(lessons.getLesson_key());
        l.setTitle(lessons.getTitle());
        l.setPublished(lessons.isPublished());
        l.setPrompt(lessons.getPrompt());
        lessonRepository.save(l);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/achievements")
    @ResponseBody
    public List<Achievement> getAchievements(@RequestHeader("Authorization") String authorizationHeader) {
        verifyAdmin(authorizationHeader);
        return achievementRepository.findAll();
    }

    @PostMapping(value = "/achievement/edit")
    @ResponseBody
    public ResponseEntity<String> addAchievement(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Achievement achievement) {
        verifyAdmin(authorizationHeader);
        Achievement a;
        if (achievement.getId() != null) {
            a = achievementRepository.findById(achievement.getId().intValue())
                    .orElseThrow(() -> new AccessDeniedException("Achievement not found"));
        } else {
            a = new Achievement();
        }
        a.setType(achievement.getType());
        a.setValue(achievement.getValue());
        achievementRepository.save(a);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/achievement/delete")
    @ResponseBody
    public ResponseEntity<String> deleteAchievement(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Achievement achievement) {
        verifyAdmin(authorizationHeader);
        if (achievement.getId() != null) {
            Achievement a = achievementRepository.findById(achievement.getId().intValue())
                    .orElseThrow(() -> new AccessDeniedException("Achievement not found"));
            achievementRepository.delete(a);
        }
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/lesson/delete")
    @ResponseBody
    public ResponseEntity<String> deleteLesson(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Lesson lesson) {
        verifyAdmin(authorizationHeader);
        if (lesson.getId() != null) {
            Lesson l = lessonRepository.findById(lesson.getId().intValue())
                    .orElseThrow(() -> new AccessDeniedException("Lesson not found"));
            lessonRepository.delete(l);
        }
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/reset-lessons/{id}")
    @ResponseBody
    public ResponseEntity<String> resetLessons(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer id) {
        verifyAdmin(authorizationHeader);
        YukiData yd = yukiRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("YukiData not found"));
        yd.getLessonsDone().clear();
        yukiRepository.save(yd);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/done-today/{id}/{value}")
    @ResponseBody
    public ResponseEntity<String> doneToday(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer id, @PathVariable Boolean value) {
        verifyAdmin(authorizationHeader);
        YukiData yd = yukiRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("YukiData not found"));
        yd.setDoneToday(value);
        yukiRepository.save(yd);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/premium/{id}/{value}")
    @ResponseBody
    public ResponseEntity<String> premium(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer id, @PathVariable Boolean value) {
        verifyAdmin(authorizationHeader);
        YukiData yd = yukiRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("YukiData not found"));
        yd.setPremium(value);
        yukiRepository.save(yd);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/stats")
    @ResponseBody
    public List<Stat> getStats(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(authorizationHeader);
        return statRepository.findAll(PageRequest.of(page, Math.min(size, 100))).getContent();
    }

    @GetMapping(value = "/dashboard")
    @ResponseBody
    public java.util.Map<String, Object> getDashboard(@RequestHeader("Authorization") String authorizationHeader) {
        verifyAdmin(authorizationHeader);
        List<YukiData> all = yukiRepository.findAll();
        long totalUsers = all.size();
        long premiumUsers = all.stream().filter(YukiData::isPremium).count();
        long activeToday = all.stream().filter(YukiData::isDoneToday).count();
        long inTrial = all.stream().filter(YukiData::isInTrial).count();
        double avgStreak = all.stream().mapToInt(YukiData::getStreak).average().orElse(0);
        double avgXp = all.stream().mapToInt(YukiData::getXp).average().orElse(0);
        long totalLessons = lessonRepository.count();

        return java.util.Map.of(
                "totalUsers", totalUsers,
                "premiumUsers", premiumUsers,
                "activeToday", activeToday,
                "inTrial", inTrial,
                "avgStreak", Math.round(avgStreak * 10.0) / 10.0,
                "avgXp", Math.round(avgXp),
                "totalLessons", totalLessons,
                "conversionRate", totalUsers > 0 ? Math.round((premiumUsers * 100.0 / totalUsers) * 10.0) / 10.0 : 0
        );
    }
}
