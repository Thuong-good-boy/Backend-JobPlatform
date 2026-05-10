package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification>
    findByRecipientIdOrderByCreatedAtDesc(
            Long recipientId,
            Pageable pageable
    );
    @Query("select count(n.id) from Notification n where n.recipientId =:userId")
    int countUnread(@Param("userId") Long userId);
    @Query("update Notification  set isRead = true where id = :id")
    void markAsRead(@Param("id") Long id);

    @Query("update Notification set isRead = true where recipientId =:userId")
    void markAllAsRead(@Param("usetId") Long userId);


}