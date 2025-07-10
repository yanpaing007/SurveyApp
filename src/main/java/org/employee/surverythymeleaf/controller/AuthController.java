package org.employee.surverythymeleaf.controller;

import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.service.ActivityLogService;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.ActivityHelper;
import org.employee.surverythymeleaf.util.AppStatusValidator;
import org.employee.surverythymeleaf.util.CalculateDashboard;
import org.springframework.data.domain.Page;
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
import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;
    private final ApplicationService applicationService;
    private final SurveyService surveyService;
    private final SurveyRepository surveyRepository;
    private final ApplicationRepository applicationRepository;
    private final ActivityHelper activityHelper;
    private final ActivityLogService activityLogService;

    public AuthController(UserService userService, DaoAuthenticationProvider authProvider, ApplicationService applicationService, SurveyService surveyService, SurveyRepository surveyRepository, ApplicationRepository applicationRepository, ActivityHelper activityHelper, ActivityLogService activityLogService) {
        this.userService = userService;
        this.applicationService = applicationService;
        this.surveyService =surveyService;
        this.surveyRepository = surveyRepository;
        this.applicationRepository = applicationRepository;
        this.activityHelper = activityHelper;
        this.activityLogService = activityLogService;
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

        Long totalSurvey = (long) surveyRepository.count();
        Long totalApplication = (long) applicationRepository.count();

        Long pendingSurvey = surveyRepository.countByStatus(SurveyStatus.PENDING);
        Long succeededSurvey = surveyRepository.countByStatus(SurveyStatus.SUCCEEDED);
        Long failedSurvey = surveyRepository.countByStatus(SurveyStatus.FAILED);

        Long pendingApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.PENDING);
        Long processingApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.PROCESSING);
        Long completedApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.COMPLETED);
        Long cancelledApplication = applicationRepository.countByApplicationStatus(ApplicationStatus.CANCELLED);
        Long successRate = CalculateDashboard.calculateSuccessRate(totalApplication, completedApplication);

        Long surveyPercentage = surveyService.surveyCompareToLastMonth();
        Long pendingSurveyPercentage = surveyService.pendingSurveyCompareToLastMonth();
        Long applicationPercentage = applicationService.applicationCompareToLastMonth();
        Long successRatePercentage = applicationService.successRateCompareToLastMonth();

        List<Survey> getRecentSurvey = surveyService.getRecentSurvey();
        List<ActivityLog> recentActivity = activityLogService.getRecentActivity();
        Object[] topSurvey = surveyService.findTopSurveyCreator();
        User topSurveyCreator = (User) topSurvey[0];
        Long topSurveyCount = (Long) topSurvey[1];

        Object[] topApplication = applicationService.findTopApplicationCreator();
        User topApplicationCreator = (User) topApplication[0];
        Long topApplicationCount = (Long) topApplication[1];

        Object[] getmostActiveUser = activityLogService.getMostActiveUser();
        User mostActiveUser = (User) getmostActiveUser[0];
        Long mostActiveUserCount = (Long) getmostActiveUser[1];

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
        model.addAttribute("surveyPercentage", surveyPercentage);
        model.addAttribute("pendingSurveyPercentage", pendingSurveyPercentage);
        model.addAttribute("applicationPercentage", applicationPercentage);
        model.addAttribute("successRatePercentage", successRatePercentage);
        model.addAttribute("recentSurvey", getRecentSurvey);
        model.addAttribute("topSurveyCreator", topSurveyCreator);
        model.addAttribute("topSurveyCreatorCount", topSurveyCount);
        model.addAttribute("topApplicationCreator", topApplicationCreator);
        model.addAttribute("topApplicationCreatorCount", topApplicationCount);
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("mostActiveUser", mostActiveUser.getFullName());
        model.addAttribute("mostActiveUserCount", mostActiveUserCount);

        System.out.println(getRecentSurvey);

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
    public String updateApplication(@PathVariable String id, @ModelAttribute Application application, RedirectAttributes redirectAttributes, Principal principal) {
        boolean app= applicationService.updateApplication(id,application);
        if(app){
            redirectAttributes.addFlashAttribute("message", "Application updated successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            activityHelper.saveActivity(ActivityType.UPDATE_APPLICATION,principal);
        }
        else{
            redirectAttributes.addFlashAttribute("message", "Application wasn't updated!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/application/details/{id}";
    }
}
