package com.huawei.codearts.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private Long userId;

    private String username;

    private String userAvatar;

    private String content;

    private LocalDateTime createdAt;
}
