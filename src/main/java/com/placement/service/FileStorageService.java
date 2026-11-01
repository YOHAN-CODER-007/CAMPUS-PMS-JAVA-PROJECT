package com.placement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service handling local file storage for student resume uploads (PDF only, max 2MB).
 */
@Service
public class FileStorageService {

    private final Path uploadDirectory;

    public FileStorageService(@Value("${file.upload-dir:uploads/resumes}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory: " + uploadDir, e);
        }
    }

    /**
     * Stores an uploaded PDF resume and returns the stored filename.
     */
    public String storeResume(MultipartFile file, Long studentId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed (.pdf).");
        }

        // Limit file size to 2MB (2 * 1024 * 1024 bytes)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 2MB.");
        }

        // Generate clean unique filename: student_{id}_{timestamp}_{uuid}.pdf
        String cleanFileName = "student_" + studentId + "_" + System.currentTimeMillis() + ".pdf";
        Path targetPath = this.uploadDirectory.resolve(cleanFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return cleanFileName;
    }

    /**
     * Loads a stored resume as a Spring Resource for viewing or downloading.
     */
    public Resource loadResumeAsResource(String filename) {
        try {
            Path filePath = this.uploadDirectory.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Resume file not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading resume file: " + filename, e);
        }
    }

    /**
     * Deletes a student's resume PDF from disk.
     */
    public void deleteResume(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }
        try {
            Path filePath = this.uploadDirectory.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
