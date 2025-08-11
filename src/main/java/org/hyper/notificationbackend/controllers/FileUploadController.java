package org.hyper.notificationbackend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "false")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.url:http://localhost:8090/uploads}")
    private String uploadBaseUrl;

    // Upload a single file and return the URL
    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("File upload request received: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
            
            // Validate file
            if (file.isEmpty()) {
                System.out.println("Error: No file provided");
                return ResponseEntity.badRequest().body("Error: No file provided");
            }

            // Check file size (1000MB limit to match application.properties)
            long maxFileSize = 1000 * 1024 * 1024; // 1000MB (1GB)
            if (file.getSize() > maxFileSize) {
                System.out.println("Error: File size exceeds limit: " + file.getSize() + " > " + maxFileSize);
                return ResponseEntity.badRequest().body("Error: File size exceeds 1000MB limit");
            }

            // Validate file type
            String contentType = file.getContentType();
            System.out.println("File content type: " + contentType);
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
                System.out.println("Error: Invalid file type: " + contentType);
                return ResponseEntity.badRequest().body("Error: Only image and video files are allowed");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("Upload directory: " + uploadPath.toAbsolutePath());
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating upload directory: " + uploadPath);
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            System.out.println("Saving file to: " + filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File saved successfully: " + uniqueFilename);

            // Return file URL
            String fileUrl = uploadBaseUrl + "/" + uniqueFilename;
            System.out.println("File URL: " + fileUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("filename", uniqueFilename);
            response.put("originalFilename", originalFilename);
            response.put("size", file.getSize());
            response.put("contentType", contentType);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.out.println("IO Exception during file upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("General exception during file upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Upload multiple files
    @PostMapping("/upload-files")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files.length == 0) {
                return ResponseEntity.badRequest().body("Error: No files provided");
            }

            java.util.List<Object> filesList = new java.util.ArrayList<>();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("files", filesList);

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Use the single file upload logic
                    ResponseEntity<?> singleResponse = uploadFile(file);
                    if (singleResponse.getStatusCode().is2xxSuccessful()) {
                        filesList.add(singleResponse.getBody());
                    } else {
                        return singleResponse; // Return error if any file fails
                    }
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading files: " + e.getMessage());
        }
    }

    // Delete a file
    @DeleteMapping("/delete-file/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok(Map.of("success", true, "message", "File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error deleting file: " + e.getMessage());
        }
    }
}
