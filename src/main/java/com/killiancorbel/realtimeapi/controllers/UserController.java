package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.requests.LoginReq;
import com.killiancorbel.realtimeapi.models.requests.RegisterReq;
import com.killiancorbel.realtimeapi.models.responses.LoginRes;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerErrorException;

import java.util.List;

@RestController
@RequestMapping(path="/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    private JwtUtil jwtUtil;
    public UserController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(path = "/login")
    public @ResponseBody LoginRes login(@RequestBody LoginReq inputUser) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(inputUser.getEmail(), inputUser.getPassword()));
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            String token = jwtUtil.createToken(user);
            LoginRes res = new LoginRes();
            res.setEmail(user.getEmail());
            res.setToken(token);
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
