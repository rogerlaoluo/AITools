package com.huawei.codearts.timeline.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private ContentType contentType;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "media_paths", columnDefinition = "TEXT")
    private String mediaPaths;

    @Column(length = 255)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private List<String> mediaPathList;

    @Transient
    private Integer likeCount;

    @Transient
    private Integer commentCount;

    @Transient
    private Boolean likedByCurrentUser;

    @Transient
    private User author;

    public enum ContentType {
        image, video, text, mixed
    }
}
