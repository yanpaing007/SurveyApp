package org.employee.surverythymeleaf.Configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.employee.surverythymeleaf.DTO.UserDTO;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;


@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;
    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("servletPath")
    public String getServletPath(HttpServletRequest request) {
        return request.getServletPath();
    }

    @ModelAttribute("currentUser")
    public UserDTO getCurrentUser(Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            return new UserDTO(user.getId(), user.getFullName(), user.getEmail());
        }
        return null;
    }
}
