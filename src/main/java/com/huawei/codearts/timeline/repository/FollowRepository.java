package com.huawei.codearts.timeline.repository;

import com.huawei.codearts.timeline.entity.Follow;
import com.huawei.codearts.timeline.entity.FollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    @Query("SELECT f FROM Follow f WHERE f.id.followerId = :followerId AND f.id.followingId = :followingId")
    Optional<Follow> findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Follow f WHERE f.id.followerId = :followerId AND f.id.followingId = :followingId")
    boolean existsByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Query("SELECT f FROM Follow f WHERE f.id.followerId = :followerId ORDER BY f.createdAt DESC")
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(@Param("followerId") Long followerId, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.id.followingId = :followingId ORDER BY f.createdAt DESC")
    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(@Param("followingId") Long followingId, Pageable pageable);

    @Query("DELETE FROM Follow f WHERE f.id.followerId = :followerId AND f.id.followingId = :followingId")
    void deleteByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.id.followingId = :followingId")
    Long countByFollowingId(@Param("followingId") Long followingId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.id.followerId = :followerId")
    Long countByFollowerId(@Param("followerId") Long followerId);
}
