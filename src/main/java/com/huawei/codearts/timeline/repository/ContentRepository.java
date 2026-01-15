package com.huawei.codearts.timeline.repository;

import com.huawei.codearts.timeline.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    Page<Content> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Content> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.textContent LIKE %:keyword% ORDER BY c.createdAt DESC")
    Page<Content> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Content c JOIN ContentTag ct ON c.id = ct.id.contentId WHERE ct.id.tagId = :tagId ORDER BY c.createdAt DESC")
    Page<Content> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.userId IN (SELECT f.id.followingId FROM Follow f WHERE f.id.followerId = :userId) ORDER BY c.createdAt DESC")
    Page<Content> findFollowingContent(@Param("userId") Long userId, Pageable pageable);
}
