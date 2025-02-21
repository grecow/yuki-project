package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.LessonDone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonDoneRepository extends JpaRepository<LessonDone, Integer> {
}
