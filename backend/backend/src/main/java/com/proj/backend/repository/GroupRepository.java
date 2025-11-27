package com.proj.backend.repository;

import com.proj.backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByCreatedByUserId(Long userId); // все группы, где юзер — создатель
}