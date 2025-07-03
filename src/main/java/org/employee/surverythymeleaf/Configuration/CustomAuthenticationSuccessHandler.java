package org.employee.surverythymeleaf.Configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if(roles.contains("Admin")) {
            response.sendRedirect("/");
        } else if (roles.contains("Technical")) {
            response.sendRedirect("/technical/survey/allSurvey");
        } else if (roles.contains("Sale")){
            response.sendRedirect("/sale/survey/allSurvey");
        } else{
            response.sendRedirect("/access-denied");
        }
    }
}
