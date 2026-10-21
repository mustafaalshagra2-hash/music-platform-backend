package com.noor.music_backend.controllers;

import com.noor.music_backend.models.User;
import com.noor.music_backend.repositories.UserRepository;
import com.noor.music_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("username is already taken!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.ok("successfully registered!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", loginRequest.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("failed to login: the username or password is incorrect.");
        }
    }

    // ✅ 2. دالة ME الجديدة (حسب طلب أبو الفرونت)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        
        // نجيب اليوزر من الداتابيس
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("the user is not found"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("role", user.getRole());

        // ✅ تجهيز بيانات الخطة والليميت
        Map<String, Object> planData = new HashMap<>();
        if (user.getCurrentPlan() != null) { // انتبه: استخدمت getCurrentPlan مالتك
            planData.put("id", user.getCurrentPlan().getId());
            planData.put("name", user.getCurrentPlan().getName());
            
            // تحديد اللمت: اذا VIP نعطي 100، غيرها 5
            int limit = "VIP".equalsIgnoreCase(user.getCurrentPlan().getName()) ? 100 : 5;
            planData.put("uploadLimit", limit);
        } else {
            planData.put("name", "Free");
            planData.put("uploadLimit", 5);
        }
        userData.put("plan", planData); // سميناها plan لان الفرونت يريدها هيج

        // ✅ حساب عدد الاغاني (اذا عندك علاقة songs باليوزر)
        // اذا ما مسوي علاقة بعدك، خليها ترجع 0 مؤقتاً
        // int uploadsCount = (user.getSongs() != null) ? user.getSongs().size() : 0;
        userData.put("uploadsCount", 0); // غيرها لاحقاً

        return ResponseEntity.ok(userData);
    }
}