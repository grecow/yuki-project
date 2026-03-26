package com.killiancorbel.realtimeapi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${app.storage-path}")
    private String storagePath;

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        String uid = UUID.randomUUID().toString();
        Path dirPath = Paths.get(storagePath, folder);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(uid);
        file.transferTo(filePath.toFile());
        return uid;
    }

    public byte[] downloadImage(String uid, String folder) throws IOException {
        String sanitizedUid = Paths.get(uid).getFileName().toString();
        Path filePath = Paths.get(storagePath, folder, sanitizedUid);
        return Files.readAllBytes(filePath);
    }

    public void deleteImage(String uid, String folder) throws IOException {
        String sanitizedUid = Paths.get(uid).getFileName().toString();
        Path filePath = Paths.get(storagePath, folder, sanitizedUid);
        Files.deleteIfExists(filePath);
    }

    public String[] getAllUidsFromFolder(String folder) {
        File dir = Paths.get(storagePath, folder).toFile();
        if (!dir.exists()) return new String[0];
        String[] files = dir.list();
        return files != null ? files : new String[0];
    }
}
