package org.employee.surverythymeleaf.util;


import lombok.RequiredArgsConstructor;
import org.employee.surverythymeleaf.Configuration.GlobalControllerAdvice;
import org.employee.surverythymeleaf.DTO.UserDTO;
import org.employee.surverythymeleaf.model.Enum.ActivityType;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.ActivityLogService;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class ActivityHelper {

    private final UserService userService;
    private final GlobalControllerAdvice globalControllerAdvice;
    private final ActivityLogService activityLogService;

    public void saveActivity(ActivityType activityType, Principal principal) {
        UserDTO current_user = globalControllerAdvice.getCurrentUser(principal);
        User found_user = userService.findByEmail(current_user.getEmail());
        activityLogService.logActivity(activityType, found_user);
    }

}
