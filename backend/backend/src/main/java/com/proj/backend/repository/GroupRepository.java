package com.proj.backend.repository;

import com.proj.backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // Старий метод (можеш залишити, якщо десь використовується)
    List<Group> findByCreatedByUserId(Long userId);

    // ✅ НОВИЙ МЕТОД: Знайти всі групи, де користувач є учасником
    // Ми дивимося в таблицю Membership і витягуємо звідти групи для конкретного юзера
    @Query("SELECT m.group FROM Membership m WHERE m.user.userId = :userId")
    List<Group> findAllByUserId(@Param("userId") Long userId);
}