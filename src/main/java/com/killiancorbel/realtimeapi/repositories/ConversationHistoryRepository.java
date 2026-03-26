package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.ConversationHistory;
import com.killiancorbel.realtimeapi.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Integer> {
    List<ConversationHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    long countByUser(User user);
}
