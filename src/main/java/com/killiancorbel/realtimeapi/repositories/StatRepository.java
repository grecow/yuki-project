package com.killiancorbel.realtimeapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.killiancorbel.realtimeapi.models.Stat;

public interface StatRepository extends JpaRepository<Stat, Integer>{
    List<Stat> findAllByType(String type);
}
