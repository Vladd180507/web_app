package com.proj.backend.repository;

import com.proj.backend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // ✅ ЗМІНЕНО: DESC -> ASC
    // Тепер ресурси будуть йти в хронологічному порядку (від найстарішого до найновішого)
    @Query("SELECT r FROM Resource r WHERE r.group.groupId = :groupId ORDER BY r.uploadedAt ASC")
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
    List<Resource> findRecentResources(@Param("groupId") Long groupId, @Param("date") java.time.LocalDateTime date);
}