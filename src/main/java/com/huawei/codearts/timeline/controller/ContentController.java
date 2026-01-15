package com.huawei.codearts.timeline.controller;

import com.huawei.codearts.timeline.dto.ApiResponse;
import com.huawei.codearts.timeline.dto.CommentDto;
import com.huawei.codearts.timeline.dto.ContentDto;
import com.huawei.codearts.timeline.dto.PageResponse;
import com.huawei.codearts.timeline.security.UserPrincipal;
import com.huawei.codearts.timeline.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    public ApiResponse<ContentDto> createContent(
            @RequestParam(required = false) String textContent,
            @RequestParam(value = "media", required = false) MultipartFile[] media,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String contentType,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        // Filter out empty files from array
        List<MultipartFile> mediaList = null;
        if (media != null && media.length > 0) {
            mediaList = new java.util.ArrayList<>();
            for (MultipartFile file : media) {
                if (file != null && !file.isEmpty()) {
                    mediaList.add(file);
                }
            }
            if (mediaList.isEmpty()) {
                mediaList = null;
            }
        }

        ContentDto content = contentService.createContent(
                currentUser.getId(), textContent, mediaList, location, tags);
        return ApiResponse.success("Content created successfully", content);
    }

    @GetMapping("/global")
    public ApiResponse<PageResponse<ContentDto>> getGlobalTimeline(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long userId = currentUser != null ? currentUser.getId() : null;
        PageResponse<ContentDto> response = contentService.getGlobalTimeline(userId, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ContentDto> getContent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long userId = currentUser != null ? currentUser.getId() : null;
        ContentDto content = contentService.getContentById(id, userId);
        return ApiResponse.success(content);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Void> likeContent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        contentService.likeContent(id, currentUser.getId());
        return ApiResponse.success("Like updated successfully", null);
    }

    @PostMapping("/{id}/comment")
    public ApiResponse<Void> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        contentService.addComment(id, currentUser.getId(), request.getContent());
        return ApiResponse.success("Comment added successfully", null);
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<CommentDto>> getComments(@PathVariable Long id) {
        List<CommentDto> comments = contentService.getComments(id);
        return ApiResponse.success(comments);
    }

    public static class CommentRequest {
        private String content;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
