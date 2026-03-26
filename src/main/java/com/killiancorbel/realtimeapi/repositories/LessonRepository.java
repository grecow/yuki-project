package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findAllByLanguageAndPublished(String language, boolean published);
    Page<Lesson> findAllByLanguageAndPublished(String language, boolean published, Pageable pageable);
}
