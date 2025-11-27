package com.proj.backend.repository;

import com.proj.backend.model.Membership;
import com.proj.backend.model.MembershipRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByGroupGroupId(Long groupId);

    Optional<Membership> findByUserUserIdAndGroupGroupId(Long userId, Long groupId);

    boolean existsByUserUserIdAndGroupGroupId(Long userId, Long groupId);

    List<Membership> findByUserUserId(Long userId); // все группы пользователя

    Optional<Membership> findByUserUserIdAndGroupGroupIdAndRole(Long userId, Long groupId, MembershipRole role);
}