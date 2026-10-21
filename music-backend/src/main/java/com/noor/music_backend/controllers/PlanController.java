package com.noor.music_backend.controllers;

import com.noor.music_backend.models.Plan;
import com.noor.music_backend.models.User;
import com.noor.music_backend.repositories.PlanRepository;
import com.noor.music_backend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public PlanController(PlanRepository planRepository, UserRepository userRepository) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public Plan createPlan(@RequestBody Plan plan) {
        return planRepository.save(plan);
    }

    @GetMapping
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    // ✅ 2. تعديل الاشتراك لارجاع JSON
    @PostMapping("/subscribe/{planId}")
    public ResponseEntity<?> subscribe(@PathVariable Long planId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("the plan is not found"));

        user.setCurrentPlan(plan); // ✅ استخدمنا setCurrentPlan مالتك
        userRepository.save(user);

        // ارجاع JSON مرتب
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("message", "subscribed to plan: " + plan.getName());
        response.put("plan", plan); // نرجع الخطة الجديدة حتى الفرونت يحدث نفسه

        return ResponseEntity.ok(response);
    }
}