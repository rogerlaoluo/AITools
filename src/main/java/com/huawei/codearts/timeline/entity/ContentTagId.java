package com.huawei.codearts.timeline.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTagId implements java.io.Serializable {
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "tag_id")
    private Long tagId;
}
