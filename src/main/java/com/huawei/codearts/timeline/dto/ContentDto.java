package com.huawei.codearts.timeline.dto;

import com.huawei.codearts.timeline.entity.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {

    private Long id;

    private Long userId;

    private String username;

    private String userAvatar;

    private Content.ContentType contentType;

    private String textContent;

    private List<String> mediaPaths;

    private String location;

    private LocalDateTime createdAt;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean likedByCurrentUser;

    private List<String> tags;
}
