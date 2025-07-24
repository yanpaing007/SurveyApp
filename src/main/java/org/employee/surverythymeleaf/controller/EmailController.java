package org.employee.surverythymeleaf.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class EmailController {
    private final UserRepository userRepository;

    public EmailController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@Valid @RequestBody EmailCheckRequest request) {
        boolean isUnique = !userRepository.existsByEmail(request.getEmail());
        return ResponseEntity.ok(Collections.singletonMap("isUnique", isUnique));
    }

    // Request DTO for better structure
    @Setter
    @Getter
    public static class EmailCheckRequest {
        // getters and setters
        @NotBlank
        @Email
        private String email;

    }
}