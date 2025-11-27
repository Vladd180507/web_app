package com.proj.backend.repository;

import com.proj.backend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // === Použitie @Query pre istotu ===

    @Query("SELECT r FROM Resource r WHERE r.group.groupId = :groupId ORDER BY r.uploadedAt DESC")
    List<Resource> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT r FROM Resource r WHERE r.group.groupId = :groupId AND r.type = :type")
    List<Resource> findByGroupIdAndType(@Param("groupId") Long groupId, @Param("type") String type);

    @Query("SELECT r FROM Resource r WHERE r.uploadedBy.userId = :userId")
    List<Resource> findByUploadedBy(@Param("userId") Long userId);

    @Query("SELECT r FROM Resource r WHERE r.group.groupId = :groupId AND LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Resource> searchByTitle(@Param("groupId") Long groupId, @Param("keyword") String keyword);

    @Query("SELECT COUNT(r) FROM Resource r WHERE r.group.groupId = :groupId")
    Long countByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COUNT(r) FROM Resource r WHERE r.group.groupId = :groupId AND r.type = :type")
    Long countByGroupIdAndType(@Param("groupId") Long groupId, @Param("type") String type);

    @Query("SELECT r FROM Resource r WHERE r.group.groupId = :groupId AND r.uploadedAt > :date")
    List<Resource> findRecentResources(@Param("groupId") Long groupId, @Param("date") LocalDateTime date);

    boolean existsByGroupGroupIdAndTitle(Long groupId, String title);

    @Transactional
    void deleteByGroupGroupId(Long groupId);
}