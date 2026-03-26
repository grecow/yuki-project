package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.DailyLesson;
import com.killiancorbel.realtimeapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyLessonRepository extends JpaRepository<DailyLesson, Integer> {
    List<DailyLesson> findByUserAndLessonDate(User user, LocalDate date);
    List<DailyLesson> findByUserAndCompletedFalseOrderByLessonDateDesc(User user);
    List<DailyLesson> findByUserOrderByLessonDateDesc(User user);
    long countByUserAndLessonDate(User user, LocalDate date);
}
