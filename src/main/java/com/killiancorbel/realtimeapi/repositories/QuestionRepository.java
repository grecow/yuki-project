package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
