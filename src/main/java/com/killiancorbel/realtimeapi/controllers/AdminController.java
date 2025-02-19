package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.Achievement;
import com.killiancorbel.realtimeapi.models.Lesson;
import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.repositories.AchievementRepository;
import com.killiancorbel.realtimeapi.repositories.LessonRepository;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private AchievementRepository achievementRepository;

    @GetMapping(value = "/users")
    @ResponseBody
    public List<YukiData> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        List<YukiData> users = yukiRepository.findAll();
        return users;
    }

    @PostMapping(value = "/user/delete/{id}")
    @ResponseBody
    public ResponseEntity deleteUser(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer id) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        User u = userRepository.findById(id).get();
        YukiData yd = yukiRepository.findByUser(u);
        yukiRepository.delete(yd);
        userRepository.delete(u);
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value =  "/yd/tokens")
    @ResponseBody
    public YukiData updateTokens(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) YukiData yukiDataReq) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        YukiData yd = yukiRepository.findById(yukiDataReq.getId().intValue()).get();
        yd.setTokens(yukiDataReq.getTokens());
        yukiRepository.save(yd);
        return yd;
    }

    @GetMapping(value = "/lessons/get")
    @ResponseBody
    public List<Lesson> getLessons(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        return lessonRepository.findAll();
    }

    @PostMapping(value = "/lessons/post")
    @ResponseBody
    public ResponseEntity postLessons(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) List<Lesson> lessons) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        lessonRepository.deleteAll();
        lessonRepository.saveAll(lessons);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/achievements")
    @ResponseBody
    public List<Achievement> getAchievements(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        return achievementRepository.findAll();
    }

    @PostMapping(value = "/achievement/edit")
    @ResponseBody
    public ResponseEntity addAchievement(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) Achievement achievement) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        if (achievement.getId() != null) {
            Achievement a = achievementRepository.findById(achievement.getId().intValue()).get();
            a.setType(achievement.getType());
            a.setValue(achievement.getValue());
            achievementRepository.save(a);
        } else {
            Achievement a = new Achievement();
            a.setType(achievement.getType());
            a.setValue(achievement.getValue());
            achievementRepository.save(a);
        }
        return ResponseEntity.ok("ok");
    }

    @PostMapping(value = "/achievement/delete")
    @ResponseBody
    public ResponseEntity deleteAchievement(@RequestHeader("Authorization") String authorizationHeader, @RequestBody(required = false) Achievement achievement) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        if (achievement.getId() != null) {
            Achievement a = achievementRepository.findById(achievement.getId().intValue()).get();
            achievementRepository.delete(a);
        }
        return ResponseEntity.ok("ok");
    }
}
