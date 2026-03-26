package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.UserLearningProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLearningProfileRepository extends JpaRepository<UserLearningProfile, Integer> {
    UserLearningProfile findByUser(User user);
}
