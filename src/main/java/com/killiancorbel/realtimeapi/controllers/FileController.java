package com.killiancorbel.realtimeapi.controllers;

import com.killiancorbel.realtimeapi.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path="/file")
public class FileController {
    @Autowired
    private StorageService storageService;

    @GetMapping("/lessons")
    public List<String> getAllImages(@RequestHeader("Authorization") String authorizationHeader) throws IOException {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        return List.of(storageService.getAllUidsFromFolder("lessons\\"));
    }

    @GetMapping("/lesson/{uid}")
    public ResponseEntity getLessonImage(@PathVariable String uid) {
        try {
            byte[] imageData = storageService.downloadImage(uid, "lessons\\");
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/png"))
                    .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
        }
    }

    @PostMapping("/lesson/add")
    public ResponseEntity addLessonImage(@RequestHeader("Authorization") String authorizationHeader, @RequestParam("file") MultipartFile file) throws IOException {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        storageService.uploadImage(file, "lessons\\");
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/lesson/delete/{uid}")
    public ResponseEntity deleteLessonImage(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String uid) throws IOException {
        String token = authorizationHeader.replace("Bearer ", "");
        if (!token.equals("sgp123")) {
            throw new AccessDeniedException("Forbidden");
        }
        storageService.deleteImage(uid, "lessons\\");
        return ResponseEntity.ok("ok");
    }
}
