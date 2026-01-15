package com.huawei.codearts.timeline.service;

import com.huawei.codearts.timeline.dto.ContentDto;
import com.huawei.codearts.timeline.dto.PageResponse;
import com.huawei.codearts.timeline.dto.TagDto;
import com.huawei.codearts.timeline.entity.Content;
import com.huawei.codearts.timeline.entity.ContentTag;
import com.huawei.codearts.timeline.entity.Tag;
import com.huawei.codearts.timeline.repository.ContentRepository;
import com.huawei.codearts.timeline.repository.ContentTagRepository;
import com.huawei.codearts.timeline.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExploreService {

    private final ContentRepository contentRepository;
    private final TagRepository tagRepository;
    private final ContentTagRepository contentTagRepository;
    private final ContentService contentService;

    @Transactional(readOnly = true)
    public PageResponse<ContentDto> getExploreContent(int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Content> contentPage = contentRepository.findAllByOrderByCreatedAtDesc(pageable);

        return PageResponse.<ContentDto>builder()
                .content(contentPage.getContent().stream()
                        .map(c -> contentService.toDto(c, null))
                        .collect(Collectors.toList()))
                .currentPage(contentPage.getNumber())
                .totalPages(contentPage.getTotalPages())
                .totalElements(contentPage.getTotalElements())
                .hasNext(contentPage.hasNext())
                .hasPrevious(contentPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentDto> searchContent(String keyword, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Content> contentPage = contentRepository.searchByKeyword(keyword, pageable);

        return PageResponse.<ContentDto>builder()
                .content(contentPage.getContent().stream()
                        .map(c -> contentService.toDto(c, null))
                        .collect(Collectors.toList()))
                .currentPage(contentPage.getNumber())
                .totalPages(contentPage.getTotalPages())
                .totalElements(contentPage.getTotalElements())
                .hasNext(contentPage.hasNext())
                .hasPrevious(contentPage.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TagDto> getPopularTags(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit,
                org.springframework.data.domain.Sort.by("usageCount").descending());
        Page<Tag> tags = tagRepository.findAll(pageable);
        return tags.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentDto> getContentByTag(Long tagId, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<Content> contentPage = contentRepository.findByTagId(tagId, pageable);

        return PageResponse.<ContentDto>builder()
                .content(contentPage.getContent().stream()
                        .map(c -> contentService.toDto(c, null))
                        .collect(Collectors.toList()))
                .currentPage(contentPage.getNumber())
                .totalPages(contentPage.getTotalPages())
                .totalElements(contentPage.getTotalElements())
                .hasNext(contentPage.hasNext())
                .hasPrevious(contentPage.hasPrevious())
                .build();
    }

    private TagDto toDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .usageCount(tag.getUsageCount())
                .build();
    }
}
