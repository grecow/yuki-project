package com.killiancorbel.realtimeapi.controllers;

import java.nio.file.AccessDeniedException;

import com.killiancorbel.realtimeapi.controllers.UserController;

@RestController
@RequestMapping(path="/app")
public class AppController {
    @Autowired
    private UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(AppController.class);

    private JwtUtil jwtUtil;
    public AppController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/get/{id}")
    public @ResponseBody User getCurrentUser() {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new AccessDeniedException("Forbidden");
        }
        return user;
    }
}
