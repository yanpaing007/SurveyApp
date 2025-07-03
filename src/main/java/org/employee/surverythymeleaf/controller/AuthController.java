package org.employee.surverythymeleaf.controller;

import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.ApplicationStatus;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.UserService;
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

@Controller
public class AuthController {

    private final UserService userService;
    private final ApplicationService applicationService;

    public AuthController(UserService userService, DaoAuthenticationProvider authProvider, ApplicationService applicationService) {
        this.userService = userService;
        this.applicationService = applicationService;
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
        return "dashboard";
    }

    @GetMapping("/application/details/{id}")
    public String getApplicationDetails(Model model, @PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("Admin"));
        boolean isTechnical = authentication.getAuthorities().contains(new SimpleGrantedAuthority("Technical"));

        boolean isEditable = isAdmin || isTechnical;
        model.addAttribute("isEditable", isEditable);
        Application application = applicationService.findApplicationByGeneratedApplicationId(id);
        model.addAttribute("applications", application);
        model.addAttribute("applicationWithNonePendingStatus", ApplicationStatus.getNonPendingApplicationStatus());
        model.addAttribute("applicationWithPendingStatus",ApplicationStatus.values());
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
