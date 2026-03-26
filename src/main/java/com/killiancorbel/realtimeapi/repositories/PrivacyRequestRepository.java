package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.PrivacyRequest;
import com.killiancorbel.realtimeapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivacyRequestRepository extends JpaRepository<PrivacyRequest, Long> {
    List<PrivacyRequest> findByUser(User user);
    List<PrivacyRequest> findByStatus(String status);
    long countByStatus(String status);
}
