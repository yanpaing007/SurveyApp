package org.employee.surverythymeleaf.controller;

import jakarta.validation.Valid;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.service.ActivityLogService;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.ActivityHelper;
import org.employee.surverythymeleaf.util.CalculateDashboard;
import org.employee.surverythymeleaf.util.StatusValidator;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;
import java.util.Optional;




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
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        Optional<User> findUser = userService.userExists(user.getEmail());
        if(findUser.isPresent()) {
            result.rejectValue("email","401", "*Email already exists");
            return "auth/register";
        }

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

        Long totalSurvey = surveyRepository.count();
        Long totalApplication = applicationRepository.count();

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
        Optional<Object[]> topSurvey = surveyService.findTopSurveyCreator();

        User topSurveyCreator = null;
        Long topSurveyCount = 0L;
        if(topSurvey.isPresent()) {
            Object[] survey = topSurvey.get();
            topSurveyCreator = (User) survey[0];
            topSurveyCount = (Long) survey[1];
        }

        User topApplicationCreator = null;
        Long topApplicationCount = 0L;
        Optional<Object[]> topApplication = applicationService.findTopApplicationCreator();
        if(topApplication.isPresent()) {
            Object[] application = topApplication.get();
            topApplicationCreator = (User) application[0];
            topApplicationCount = (Long) application[1];
        }

        User mostActiveUser = null;
        Long mostActiveUserCount = 0L;
        Optional<Object[]> getMostActiveUser = activityLogService.getMostActiveUser();
        if(getMostActiveUser.isPresent()) {
            mostActiveUserCount = (Long) getMostActiveUser.get()[1];
            mostActiveUser = (User) getMostActiveUser.get()[0];
        }

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
        model.addAttribute("mostActiveUser", mostActiveUser);
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
        List<ApplicationStatus> status = StatusValidator.getNextValidApplicationStatuses(application.getApplicationStatus());
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
