package com.huawei.codearts.timeline.repository;

import com.huawei.codearts.timeline.entity.ContentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentTagRepository extends JpaRepository<ContentTag, com.huawei.codearts.timeline.entity.ContentTagId> {

    @Query("SELECT ct FROM ContentTag ct WHERE ct.id.contentId = :contentId")
    List<ContentTag> findByContentId(@Param("contentId") Long contentId);

    @Query("SELECT ct FROM ContentTag ct WHERE ct.id.tagId = :tagId")
    List<ContentTag> findByTagId(@Param("tagId") Long tagId);

    @Query("DELETE FROM ContentTag ct WHERE ct.id.contentId = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
