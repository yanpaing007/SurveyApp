package org.employee.surverythymeleaf.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.employee.surverythymeleaf.DTO.ChartResponseDTO;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class EmailController {
    private final UserRepository userRepository;
    private final SurveyService surveyService;
    private final ApplicationService applicationService;

    public EmailController(UserRepository userRepository, SurveyService surveyService, ApplicationService applicationService) {
        this.userRepository = userRepository;
        this.surveyService = surveyService;
        this.applicationService = applicationService;
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

    @GetMapping("/chart-data")
    public ChartResponseDTO getChartData(@RequestParam String type, @RequestParam String range) {
        List<String> labels;

        if ("monthly".equals(range)) {
            labels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul");
        } else {
            int days = Integer.parseInt(range);
            LocalDate start = LocalDate.now().minusDays(days - 1);
            labels = new ArrayList<>();
            for (int i = 0; i < days; i++) {
                labels.add(start.plusDays(i).getDayOfWeek().toString().substring(0, 3)); // Mon, Tue...
            }
        }

        return "survey".equalsIgnoreCase(type) ? surveyService.getRealSurveyData(range, labels) : applicationService.getRealApplicationData(range,labels);
    }
}