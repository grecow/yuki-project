package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    User findByPushId(String pushId);
    User findByOriginalAppUserId(String originalAppUserId);
    List<User> findAllByOriginalAppUserId(String originalAppUserId);
    User findByOriginalAppUserIdAndEmail(String originalAppUserId, String email);
    User findByUid(String uid);
}
