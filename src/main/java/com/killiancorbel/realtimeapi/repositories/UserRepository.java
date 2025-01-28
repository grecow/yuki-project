package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    User findByPushId(String pushId);
    User findByOriginalAppUserId(String originalAppUserId);
    User findTopByOriginalAppUserIdOrderByIdDesc(String originalAppUserId);
    User findByOriginalAppUserIdAndEmail(String originalAppUserId, String email);
    User findByUid(String uid);
}
