package org.employee.surverythymeleaf.controller;

import jakarta.validation.Valid;
import org.employee.surverythymeleaf.DTO.UpdateProfileDTO;
import org.employee.surverythymeleaf.model.ActivityLog;
import org.employee.surverythymeleaf.model.Team;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.employee.surverythymeleaf.service.ActivityLogService;
import org.employee.surverythymeleaf.service.ProfileService;
import org.employee.surverythymeleaf.service.TeamService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    private final UserService userService;

    private final ProfileService profileService;

    private final TeamService teamService;

    @Value("${app.upload.dir:${user.home}/uploads/images/profile}")
    private String uploadDir;

    @Autowired
    private ActivityLogService activityLogService;

    public ProfileController(UserRepository userRepository, UserService userService, TeamService teamService, ProfileService profileService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.teamService = teamService;
        this.profileService = profileService;
    }

    @GetMapping("/user/profile")
    public String getUserProfile(Principal principal, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUser().getId();
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long UserSurveyCount = profileService.UserSurveyCount(userId);
        Long UserApplicationCount = profileService.UserApplicationCount(userId);
        Long UserTotalActivityCount = profileService.UserTotalActivityCount(userId);
        Long DaysSinceRegistration = userService.calculateDaysSinceRegistration(user.getCreatedAt());
        String lastLoginTime = user.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String accountCreatedAt = user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ActivityLog> activityLog = activityLogService.getRelatedUserActivityLog(userId);




        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.setFullName(user.getFullName());
        updateProfileDTO.setEmail(user.getEmail());
        updateProfileDTO.setPhoneNumber(user.getPhoneNumber());
        updateProfileDTO.setRole(user.getRole());
        updateProfileDTO.setStatus(user.isStatus());
        updateProfileDTO.setProfilePicturePath(user.getProfilePictureUrl());

        model.addAttribute("user", updateProfileDTO);
        model.addAttribute("userSurveyCount", UserSurveyCount);
        model.addAttribute("userApplicationCount", UserApplicationCount);
        model.addAttribute("userTotalActivityCount", UserTotalActivityCount);
        model.addAttribute("daysSinceRegistration", DaysSinceRegistration);
        model.addAttribute("lastLoginTime", lastLoginTime);
        model.addAttribute("accountCreatedAt", accountCreatedAt);
        model.addAttribute("activityLog", activityLog);

        return "user/profile";
    }

    @PostMapping("/user/profile")
    public String updateUserProfile(@Valid @ModelAttribute("user") UpdateProfileDTO updateProfileDTO,
                                    RedirectAttributes redirectAttributes,
                                    BindingResult bindingResult,
                                    Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Failed to update user profile due to validation errors");
            model.addAttribute("user", updateProfileDTO);
            return "redirect:/user/profile";
        }

        MultipartFile profileImage = updateProfileDTO.getProfilePictureUrl();
        if (profileImage != null && !profileImage.isEmpty()) {
            try{
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(profileImage.getOriginalFilename()));
                Path staticPath = Paths.get(uploadDir);
                if(!Files.exists(staticPath)) {
                    Files.createDirectories(staticPath);
                }

                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                Path targetPath = staticPath.resolve(uniqueFileName);

                Files.copy(profileImage.getInputStream(), targetPath);
                updateProfileDTO.setProfilePicturePath("/images/profile/" + uniqueFileName);
            }
            catch (IOException e) {
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Failed to upload profile image: " + e.getMessage());
                return "redirect:/user/profile";
            }
        }

        User updatedUser = profileService.updateProfile(user, updateProfileDTO);
        if (updatedUser != null) {
            redirectAttributes.addFlashAttribute("message", "User profile updated successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/user/profile";
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to update user profile");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/user/profile";
        }
    }
}
