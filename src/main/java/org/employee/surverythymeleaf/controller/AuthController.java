package org.employee.surverythymeleaf.controller;

import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.AppStatusValidator;
import org.employee.surverythymeleaf.util.CalculateDashboard;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final ApplicationService applicationService;
    private final SurveyService surveyService;
    private final SurveyRepository surveyRepository;
    private final ApplicationRepository applicationRepository;

    public AuthController(UserService userService, DaoAuthenticationProvider authProvider, ApplicationService applicationService, SurveyService surveyService, SurveyRepository surveyRepository, ApplicationRepository applicationRepository) {
        this.userService = userService;
        this.applicationService = applicationService;
        this.surveyService =surveyService;
        this.surveyRepository = surveyRepository;
        this.applicationRepository = applicationRepository;
    }

    public boolean check_authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }


    @GetMapping("/login")
    public String login(RedirectAttributes redirectAttributes) {
      if (check_authentication()) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "You are already logged in");
            return "redirect:/";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model, RedirectAttributes redirectAttributes) {
        if (check_authentication()) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "You are already registered");
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        userService.registerUser(user);
        redirectAttributes.addFlashAttribute("message", "User registered successfully");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        String email = principal.getName();
        User userDetails = userService.findByEmail(email);
        model.addAttribute("user", userDetails);

        Long totalSurvey = (long) surveyRepository.findAll().size();
        Long totalApplication = (long) applicationRepository.findAll().size();

        Long pendingSurvey = surveyRepository.countByStatus(SurveyStatus.PENDING);
        Long succeededSurvey = surveyRepository.countByStatus(SurveyStatus.SUCCEEDED);
        Long failedSurvey = surveyRepository.countByStatus(SurveyStatus.FAILED);

        Long pendingApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.PENDING);
        Long processingApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.PROCESSING);
        Long completedApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.COMPLETED);
        Long cancelledApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.CANCELLED);
        Long successRate = CalculateDashboard.calculateSuccessRate(totalApplication, pendingApplication);

        model.addAttribute("totalSurvey", totalSurvey);
        model.addAttribute("totalApplication", totalApplication);
        model.addAttribute("pendingSurvey", pendingSurvey);
        model.addAttribute("succeededSurvey", succeededSurvey);
        model.addAttribute("failedSurvey", failedSurvey);
        model.addAttribute("pendingApplication", pendingApplication);
        model.addAttribute("processingApplication", processingApplication);
        model.addAttribute("completedApplication", completedApplication);
        model.addAttribute("cancelledApplication", cancelledApplication);
        model.addAttribute("successRate", successRate);

        return "dashboard";
    }



    @GetMapping("/pending")
    public String pending(Model model, Principal principal) {
        String email = principal.getName();
        User userDetails = userService.findByEmail(email);
        model.addAttribute("user", userDetails);
        return "pending";
    }

    @GetMapping("/application/details/{id}")
    public String getApplicationDetails(Model model, @PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("Admin"));
        boolean isTechnical = authentication.getAuthorities().contains(new SimpleGrantedAuthority("Technical"));

        boolean isEditable = isAdmin || isTechnical;
        model.addAttribute("isEditable", isEditable);
        Application application = applicationService.findApplicationByGeneratedApplicationId(id);
        List<ApplicationStatus> status = AppStatusValidator.getNextValidStatuses(application.getApplicationStatus());
        model.addAttribute("applications", application);
        model.addAttribute("status", status);
        return "application/applicationDetails";
    }

    @PostMapping("/application/details/{id}")
    public String updateApplication(@PathVariable String id, @ModelAttribute Application application, RedirectAttributes redirectAttributes) {
        boolean app= applicationService.updateApplication(id,application);
        if(app){
            redirectAttributes.addFlashAttribute("message", "Application updated successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        else{
            redirectAttributes.addFlashAttribute("message", "Application wasn't updated!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/application/details/{id}";
    }
}
