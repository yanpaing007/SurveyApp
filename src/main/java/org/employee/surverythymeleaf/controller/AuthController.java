package org.employee.surverythymeleaf.controller;


import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AuthController {

    private final UserService userService;
    private final DaoAuthenticationProvider authProvider;

    public AuthController(UserService userService, DaoAuthenticationProvider authProvider) {
        this.userService = userService;
        this.authProvider = authProvider;
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
//        String email = principal.getName();
//        User userDetails = userService.findByEmail(email);
//        model.addAttribute("user", userDetails);
        return "dashboard";
    }
}
