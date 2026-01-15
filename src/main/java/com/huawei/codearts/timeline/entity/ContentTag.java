package com.huawei.codearts.timeline.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "content_tags", indexes = {
    @Index(name = "idx_tag_id", columnList = "tag_id"),
    @Index(name = "idx_content_id", columnList = "content_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTag {

    @EmbeddedId
    private ContentTagId id;
}
