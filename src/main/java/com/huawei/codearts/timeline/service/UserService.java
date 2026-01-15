package com.huawei.codearts.timeline.service;

import com.huawei.codearts.timeline.dto.UserDto;
import com.huawei.codearts.timeline.entity.Follow;
import com.huawei.codearts.timeline.entity.FollowId;
import com.huawei.codearts.timeline.entity.User;
import com.huawei.codearts.timeline.repository.FollowRepository;
import com.huawei.codearts.timeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String AVATAR_STORAGE_LOCATION = "./storage/avatars";

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long followerCount = followRepository.countByFollowingId(id);
        Long followingCount = followRepository.countByFollowerId(id);
        Boolean isFollowing = currentUserId != null && followRepository.existsByFollowerIdAndFollowingId(currentUserId, id);

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    @Transactional
    public UserDto updateProfile(Long userId, String bio, MultipartFile avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (bio != null) {
            user.setBio(bio);
        }

        if (avatar != null && !avatar.isEmpty()) {
            String avatarPath = saveAvatar(userId, avatar);
            user.setAvatarUrl(avatarPath);
        }

        user = userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        } else {
            Follow follow = Follow.builder()
                    .id(new FollowId(followerId, followingId))
                    .build();
            followRepository.save(follow);
        }
    }

    private String saveAvatar(Long userId, MultipartFile file) {
        try {
            Path uploadPath = Paths.get(AVATAR_STORAGE_LOCATION, userId.toString());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return "/media/avatars/" + userId + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar", e);
        }
    }

    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .build();
    }
}
