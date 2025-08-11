package org.employee.surverythymeleaf.Configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        String email = authentication.getName();
        userRepository.findUserByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });

        if(roles.contains("Admin")) {
            response.sendRedirect("/");
        } else if (roles.contains("Technical")) {
            response.sendRedirect("/technical/survey/allSurvey");
        } else if (roles.contains("Sale")){
            response.sendRedirect("/sale/survey/allSurvey");
        } else if (roles.contains("Member")){
            response.sendRedirect("/pending");
        } else{
            response.sendRedirect("/access-denied");
        }
    }
}
