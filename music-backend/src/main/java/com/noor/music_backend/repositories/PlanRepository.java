package com.noor.music_backend.repositories;

import com.noor.music_backend.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}