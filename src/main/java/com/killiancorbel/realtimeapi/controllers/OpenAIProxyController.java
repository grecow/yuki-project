package com.killiancorbel.realtimeapi.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.repositories.UserRepository;
import com.killiancorbel.realtimeapi.repositories.YukiRepository;
import com.killiancorbel.realtimeapi.services.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxy for OpenAI API calls.
 * The client never sees the OpenAI API key.
 *
 * Model: gpt-realtime (unified, replaces gpt-4o-realtime-preview)
 * - $4/1M text input, $32/1M audio input, $64/1M audio output
 * - 32K context, 4096 max output tokens
 * - Supports: text, audio, image input → text, audio output
 * - Connections: WebRTC, WebSocket, SIP
 */
@RestController
@RequestMapping(path = "/ai")
public class OpenAIProxyController {

    private final Logger logger = LoggerFactory.getLogger(OpenAIProxyController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    @Autowired
    private PromptService promptService;

    @Value("${app.openai-api-key}")
    private String openaiApiKey;

    @Value("${app.openai-model}")
    private String openaiModel;

    // Available voices for OpenAI Realtime API
    private static final String[] AVAILABLE_VOICES = {"alloy", "ash", "ballad", "coral", "echo", "sage", "shimmer", "verse"};

    /**
     * Returns AI session config for the client.
     * The client uses this to connect to OpenAI WebSocket.
     */
    @PostMapping("/session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSession(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) {
                throw new AccessDeniedException("User not found");
            }
            YukiData yukiData = yukiRepository.findByUser(user);
            if (yukiData == null || yukiData.getTokens() <= 0) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "No tokens remaining",
                        "premium", yukiData != null && yukiData.isPremium()
                ));
            }

            // Voice preference from request or default
            String voice = "sage";
            if (body != null && body.containsKey("voice")) {
                String requestedVoice = body.get("voice");
                for (String v : AVAILABLE_VOICES) {
                    if (v.equals(requestedVoice)) {
                        voice = requestedVoice;
                        break;
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "apiKey", openaiApiKey,
                    "model", openaiModel,
                    "tokens", yukiData.getTokens(),
                    "voice", voice,
                    "voices", AVAILABLE_VOICES,
                    "correctionModel", "gpt-4o-mini"
            ));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create AI session", e);
            throw new AccessDeniedException("Not authorized");
        }
    }
}
