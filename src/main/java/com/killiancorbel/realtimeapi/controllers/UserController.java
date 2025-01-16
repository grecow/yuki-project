package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.requests.LoginReq;
import com.killiancorbel.realtimeapi.models.requests.RegisterReq;
import com.killiancorbel.realtimeapi.models.responses.LoginRes;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerErrorException;

@RestController
@RequestMapping(path="/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController() {
    }

    @PostMapping(path = "/login")
    public @ResponseBody LoginRes login(@RequestBody LoginReq inputUser) {
        try {
            User user = userRepository.findByEmail("");
            LoginRes res = new LoginRes();
            res.setEmail(user.getEmail());
            res.setToken("token");
            return res;
        }catch (BadCredentialsException e){
            throw new BadCredentialsException(e.getMessage(), e.getCause());
        }catch (Exception e){
            throw new ServerErrorException(e.getMessage(), e.getCause());
        }
    }

    @PostMapping("/register")
    public @ResponseBody User register(@RequestBody(required = false) RegisterReq inputUser) {
        User newUser = new User();
        newUser.setEmail(inputUser.getEmail());
        newUser.setPassword(inputUser.getPassword());
        userRepository.save(newUser);
        logger.info("User created : " + newUser.getEmail());
        return newUser;
    }

    @GetMapping("/get")
    public @ResponseBody User getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new AccessDeniedException("Forbidden");
        }
        return user;
    }

    @PostMapping("/delete")
    public @ResponseBody User deleteUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new AccessDeniedException("Forbidden");
        }
        userRepository.delete(user);
        return user;
    }
}
