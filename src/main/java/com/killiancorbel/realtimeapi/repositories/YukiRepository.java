package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.YukiData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YukiRepository extends JpaRepository<YukiData, Integer> {
    YukiData findByUser(User u);
}
