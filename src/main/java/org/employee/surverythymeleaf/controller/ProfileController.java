package org.employee.surverythymeleaf.controller;

import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.employee.surverythymeleaf.service.ProfileService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ProfileController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProfileService profileService;

    @GetMapping("/user/profile")
    public String getUserProfile(Principal principal, Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUser().getId();
        if (principal == null) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }
        User user = userRepository.findUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long UserSurveyCount = profileService.UserSurveyCount(userId);
        Long UserApplicationCount = profileService.UserApplicationCount(userId);
        Long UserTotalActivityCount = profileService.UserTotalActivityCount(userId);
        Long DaysSinceRegistration = userService.calculateDaysSinceRegistration(user.getCreatedAt());
        String lastLoginTime = user.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String accountCreatedAt = user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        model.addAttribute("user", user);
        model.addAttribute("userSurveyCount", UserSurveyCount);
        model.addAttribute("userApplicationCount", UserApplicationCount);
        model.addAttribute("userTotalActivityCount", UserTotalActivityCount);
        model.addAttribute("daysSinceRegistration", DaysSinceRegistration);
        model.addAttribute("lastLoginTime", lastLoginTime);
        model.addAttribute("accountCreatedAt", accountCreatedAt);

        return "user/profile";
    }
}
