package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Integer> {
}
