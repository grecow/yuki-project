package com.killiancorbel.realtimeapi.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class StorageService {
    private final String FOLDER_PATH = "C:\\Users\\thekt\\Pictures\\yuki\\";

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        String uid = UUID.randomUUID().toString();
        String filepath = FOLDER_PATH + folder + uid;
        file.transferTo(new File(filepath));

        return uid;
    }

    public byte[] downloadImage(String uid, String folder) throws IOException {
        String filepath = FOLDER_PATH + folder + uid;
        byte[] image = Files.readAllBytes(new File(filepath).toPath());

        return image;
    }

    public void deleteImage(String uid, String folder) throws IOException {
        String filepath = FOLDER_PATH + folder + uid;
        File file = new File(filepath);
        file.delete();
    }

    public String[] getAllUidsFromFolder(String folder) throws IOException {
        String dirPath = FOLDER_PATH + folder;
        File file = new File(dirPath);
        return file.list();
    }
}
