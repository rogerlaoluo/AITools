package com.huawei.codearts.timeline.controller;

import com.huawei.codearts.timeline.dto.ApiResponse;
import com.huawei.codearts.timeline.dto.PageResponse;
import com.huawei.codearts.timeline.dto.TagDto;
import com.huawei.codearts.timeline.service.ExploreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExploreController {

    private final ExploreService exploreService;

    @GetMapping("/explore")
    public ApiResponse<PageResponse> getExplore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ApiResponse.success(exploreService.getExploreContent(page, size));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ApiResponse.success(exploreService.searchContent(q, page, size));
    }

    @GetMapping("/tags")
    public ApiResponse<List<TagDto>> getTags(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(exploreService.getPopularTags(limit));
    }

    @GetMapping("/tags/{id}/content")
    public ApiResponse<PageResponse> getContentByTag(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ApiResponse.success(exploreService.getContentByTag(id, page, size));
    }
}
