package com.huawei.codearts.timeline.service;

import com.huawei.codearts.timeline.dto.CommentDto;
import com.huawei.codearts.timeline.dto.ContentDto;
import com.huawei.codearts.timeline.dto.PageResponse;
import com.huawei.codearts.timeline.entity.Content;
import com.huawei.codearts.timeline.entity.ContentTag;
import com.huawei.codearts.timeline.entity.ContentTagId;
import com.huawei.codearts.timeline.entity.Interaction;
import com.huawei.codearts.timeline.entity.Tag;
import com.huawei.codearts.timeline.entity.User;
import com.huawei.codearts.timeline.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ContentTagRepository contentTagRepository;
    private final InteractionRepository interactionRepository;
    private final FollowRepository followRepository;

    private static final String STORAGE_LOCATION = "./storage/media";

    @Transactional
    public ContentDto createContent(Long userId, String textContent, List<MultipartFile> files, String location, List<String> tagNames) {
        List<String> mediaPaths = new ArrayList<>();
        Content.ContentType contentType = Content.ContentType.text;

        if (files != null && !files.isEmpty()) {
            // Filter out empty files
            List<MultipartFile> validFiles = files.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .collect(Collectors.toList());

            if (!validFiles.isEmpty()) {
                mediaPaths = saveMediaFiles(validFiles);
                // Determine content type based on file count
                if (validFiles.size() > 1) {
                    contentType = Content.ContentType.mixed;
                } else {
                    // Check file type for single file
                    MultipartFile file = validFiles.get(0);
                    String filename = file.getOriginalFilename();
                    if (filename != null) {
                        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
                        contentType = ext.equals("mp4") || ext.equals("mov") || ext.equals("avi")
                                ? Content.ContentType.video : Content.ContentType.image;
                    } else {
                        contentType = Content.ContentType.image;
                    }
                }
            }
        }

        Content content = Content.builder()
                .userId(userId)
                .contentType(contentType)
                .textContent(textContent)
                .mediaPaths(mediaPaths.isEmpty() ? null : String.join(",", mediaPaths))
                .location(location)
                .build();

        content = contentRepository.save(content);

        // Save tags
        if (tagNames != null && !tagNames.isEmpty()) {
            saveTags(content.getId(), tagNames);
        }

        return toDto(content, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentDto> getUserTimeline(Long userId, Long currentUserId, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Content> contentPage = contentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return PageResponse.<ContentDto>builder()
                .content(contentPage.getContent().stream()
                        .map(c -> toDto(c, currentUserId))
                        .collect(Collectors.toList()))
                .currentPage(contentPage.getNumber())
                .totalPages(contentPage.getTotalPages())
                .totalElements(contentPage.getTotalElements())
                .hasNext(contentPage.hasNext())
                .hasPrevious(contentPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentDto> getGlobalTimeline(Long currentUserId, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Content> contentPage = contentRepository.findAllByOrderByCreatedAtDesc(pageable);

        return PageResponse.<ContentDto>builder()
                .content(contentPage.getContent().stream()
                        .map(c -> toDto(c, currentUserId))
                        .collect(Collectors.toList()))
                .currentPage(contentPage.getNumber())
                .totalPages(contentPage.getTotalPages())
                .totalElements(contentPage.getTotalElements())
                .hasNext(contentPage.hasNext())
                .hasPrevious(contentPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public ContentDto getContentById(Long contentId, Long currentUserId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        return toDto(content, currentUserId);
    }

    @Transactional
    public void likeContent(Long contentId, Long userId) {
        Optional<Interaction> existingLike = interactionRepository.findByUserIdAndTargetTypeAndTargetIdAndType(
                userId, Interaction.TargetType.CONTENT, contentId, Interaction.InteractionType.LIKE
        );

        if (existingLike.isPresent()) {
            interactionRepository.delete(existingLike.get());
        } else {
            Interaction like = Interaction.builder()
                    .userId(userId)
                    .targetType(Interaction.TargetType.CONTENT)
                    .targetId(contentId)
                    .type(Interaction.InteractionType.LIKE)
                    .build();
            interactionRepository.save(like);
        }
    }

    @Transactional
    public void addComment(Long contentId, Long userId, String commentText) {
        Interaction comment = Interaction.builder()
                .userId(userId)
                .targetType(Interaction.TargetType.CONTENT)
                .targetId(contentId)
                .type(Interaction.InteractionType.COMMENT)
                .content(commentText)
                .build();
        interactionRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long contentId) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 50);
        Page<Interaction> comments = interactionRepository.findByTargetTypeAndTargetIdAndTypeOrderByCreatedAtDesc(
                Interaction.TargetType.CONTENT, contentId, Interaction.InteractionType.COMMENT, pageable
        );

        return comments.getContent().stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    private List<String> saveMediaFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        try {
            Path uploadPath = Paths.get(STORAGE_LOCATION, datePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath);
                paths.add("/media/" + datePath + "/" + filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save media files", e);
        }

        return paths;
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")))
                .orElse("");
    }

    private void saveTags(Long contentId, List<String> tagNames) {
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .usageCount(0)
                                .createdAt(LocalDateTime.now())
                                .build();
                        return tagRepository.save(newTag);
                    });

            ContentTag contentTag = new ContentTag();
            contentTag.setId(new ContentTagId(contentId, tag.getId()));
            contentTagRepository.save(contentTag);

            tag.setUsageCount(tag.getUsageCount() + 1);
            tagRepository.save(tag);
        }
    }

    public ContentDto toDto(Content content, Long currentUserId) {
        User author = userRepository.findById(content.getUserId())
                .orElse(null);

        List<ContentTag> contentTags = contentTagRepository.findByContentId(content.getId());
        List<String> tagNames = contentTags.stream()
                .map(ct -> tagRepository.findById(ct.getId().getTagId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Tag::getName)
                .collect(Collectors.toList());

        Long likeCount = interactionRepository.countByTarget(
                Interaction.TargetType.CONTENT, content.getId(), Interaction.InteractionType.LIKE
        );

        Long commentCount = interactionRepository.countByTarget(
                Interaction.TargetType.CONTENT, content.getId(), Interaction.InteractionType.COMMENT
        );

        Boolean likedByCurrentUser = currentUserId != null &&
                interactionRepository.findByUserIdAndTargetTypeAndTargetIdAndType(
                        currentUserId, Interaction.TargetType.CONTENT, content.getId(), Interaction.InteractionType.LIKE
                ).isPresent();

        List<String> mediaPaths = content.getMediaPaths() != null && !content.getMediaPaths().isEmpty()
                ? Arrays.asList(content.getMediaPaths().split(","))
                : new ArrayList<>();

        return ContentDto.builder()
                .id(content.getId())
                .userId(content.getUserId())
                .username(author != null ? author.getUsername() : "Unknown")
                .userAvatar(author != null ? author.getAvatarUrl() : null)
                .contentType(content.getContentType())
                .textContent(content.getTextContent())
                .mediaPaths(mediaPaths)
                .location(content.getLocation())
                .createdAt(content.getCreatedAt())
                .likeCount(likeCount.intValue())
                .commentCount(commentCount.intValue())
                .likedByCurrentUser(likedByCurrentUser)
                .tags(tagNames)
                .build();
    }

    private CommentDto toCommentDto(Interaction interaction) {
        User user = userRepository.findById(interaction.getUserId())
                .orElse(null);

        return CommentDto.builder()
                .id(interaction.getId())
                .userId(interaction.getUserId())
                .username(user != null ? user.getUsername() : "Unknown")
                .userAvatar(user != null ? user.getAvatarUrl() : null)
                .content(interaction.getContent())
                .createdAt(interaction.getCreatedAt())
                .build();
    }
}
