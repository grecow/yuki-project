package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
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

    // TODO - setTokens
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
}
