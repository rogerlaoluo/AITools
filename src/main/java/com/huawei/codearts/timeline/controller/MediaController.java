package com.huawei.codearts.timeline.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class MediaController {

    @Value("${app.storage.location:./storage}")
    private String storageLocation;

    @GetMapping("/media/**")
    public ResponseEntity<Resource> getMedia(HttpServletRequest request) {
        try {
            String path = extractRemainingPath(request, "/media/");
            return serveMediaFile(path, "media");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/media/avatars/**")
    public ResponseEntity<Resource> getAvatar(HttpServletRequest request) {
        try {
            String path = extractRemainingPath(request, "/media/avatars/");
            return serveMediaFile(path, "avatars");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String extractRemainingPath(HttpServletRequest request, String prefix) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());
        return path.substring(prefix.length());
    }

    private ResponseEntity<Resource> serveMediaFile(String filePath, String type) throws IOException {
        Path fullPath = Paths.get(storageLocation, type, filePath);

        if (!Files.exists(fullPath) || !Files.isReadable(fullPath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new PathResource(fullPath);
        String contentType = determineContentType(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private String determineContentType(String filePath) {
        String extension = "";
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filePath.length() - 1) {
            extension = filePath.substring(lastDot + 1).toLowerCase();
        }

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "mp4":
                return "video/mp4";
            default:
                return "application/octet-stream";
        }
    }
}
