package com.resumeanalyzer.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class ResumeStorageService {

    private final Path uploadPath;

    public ResumeStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create upload directory: " + uploadPath, exception);
        }
    }

    public StoredResume store(MultipartFile file) {
        validateResumeFile(file);

        String originalName = cleanFileName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String storedName = UUID.randomUUID() + extension;
        Path target = uploadPath.resolve(storedName).normalize();

        if (!target.startsWith(uploadPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid resume file path.");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredResume(originalName, storedName, file.getContentType(), file.getSize());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store resume file.");
        }
    }

    public Resource load(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file was not found.");
        }

        try {
            Path file = uploadPath.resolve(storedName).normalize();
            if (!file.startsWith(uploadPath) || !Files.exists(file)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file was not found.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file was not readable.");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file was not found.");
        }
    }

    public String extractReadableText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        try {
            byte[] bytes = file.getBytes();
            int maxBytes = Math.min(bytes.length, 2_000_000);
            String text = new String(bytes, 0, maxBytes, StandardCharsets.UTF_8);
            return text.replaceAll("[^A-Za-z0-9+#.\\s-]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        } catch (IOException exception) {
            return "";
        }
    }

    public void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please upload a resume file.");
        }

        String fileName = cleanFileName(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        boolean allowed = fileName.endsWith(".pdf")
                || fileName.endsWith(".doc")
                || fileName.endsWith(".docx")
                || fileName.endsWith(".txt");

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed resume formats are PDF, DOC, DOCX and TXT.");
        }
    }

    private String cleanFileName(String fileName) {
        String fallback = fileName == null || fileName.isBlank() ? "resume.pdf" : fileName;
        String clean = Paths.get(fallback).getFileName().toString();
        clean = clean.replaceAll("[^A-Za-z0-9._-]", "_");
        return clean.isBlank() ? "resume.pdf" : clean;
    }

    private String extensionOf(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ".pdf";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
