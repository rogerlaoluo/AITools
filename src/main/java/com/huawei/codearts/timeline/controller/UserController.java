package com.huawei.codearts.timeline.controller;

import com.huawei.codearts.timeline.dto.ApiResponse;
import com.huawei.codearts.timeline.dto.PageResponse;
import com.huawei.codearts.timeline.dto.UserDto;
import com.huawei.codearts.timeline.security.UserPrincipal;
import com.huawei.codearts.timeline.service.ContentService;
import com.huawei.codearts.timeline.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ContentService contentService;

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        UserDto user = userService.getUserById(id, currentUserId);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile avatar,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (!currentUser.getId().equals(id)) {
            return ApiResponse.error("Unauthorized");
        }

        UserDto user = userService.updateProfile(id, bio, avatar);
        return ApiResponse.success("Profile updated successfully", user);
    }

    @GetMapping("/{id}/timeline")
    public ApiResponse<PageResponse> getUserTimeline(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ApiResponse.success(contentService.getUserTimeline(id, currentUserId, page, size));
    }

    @PostMapping("/{userId}/follow")
    public ApiResponse<Void> toggleFollow(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        userService.followUser(currentUser.getId(), userId);
        return ApiResponse.success("Follow status updated", null);
    }
}
