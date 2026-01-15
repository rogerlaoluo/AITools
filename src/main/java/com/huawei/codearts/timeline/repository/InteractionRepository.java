package com.huawei.codearts.timeline.repository;

import com.huawei.codearts.timeline.entity.Interaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, Interaction.TargetType targetType, Long targetId, Interaction.InteractionType type
    );

    @Query("SELECT COUNT(i) FROM Interaction i WHERE i.targetType = :targetType AND i.targetId = :targetId AND i.type = :type")
    Long countByTarget(@Param("targetType") Interaction.TargetType targetType,
                       @Param("targetId") Long targetId,
                       @Param("type") Interaction.InteractionType type);

    Page<Interaction> findByTargetTypeAndTargetIdAndTypeOrderByCreatedAtDesc(
            Interaction.TargetType targetType, Long targetId, Interaction.InteractionType type, Pageable pageable
    );

    void deleteByUserIdAndTargetTypeAndTargetIdAndType(
            Long userId, Interaction.TargetType targetType, Long targetId, Interaction.InteractionType type
    );
}
