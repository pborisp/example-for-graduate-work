package ru.skypro.homework.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {
    private String dirForUpload;

    public ImageService() {
    }

    private void createDirForUpload() {
        Path path = Paths.get(dirForUpload);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Directory created: " + path.toAbsolutePath());
            } else {
                System.out.println("Directory already exists: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Directory could not be created: " + dirForUpload, e);
        }
    }

    @Value("${file.upload.dir}")
    public void setDirForUpload(String dirForUpload) {
        this.dirForUpload = dirForUpload;
        if (this.dirForUpload == null || this.dirForUpload.isBlank()) {
            this.dirForUpload = "./upload";
        }
        createDirForUpload();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public String saveImage(MultipartFile file) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString()  + extension;
        Path path = Paths.get(dirForUpload, fileName);
        Files.copy(file.getInputStream(), path,  StandardCopyOption.REPLACE_EXISTING);
        return "/upload/" + fileName;
    }

}
