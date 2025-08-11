package org.employee.surverythymeleaf.service;

import org.employee.surverythymeleaf.model.ActivityLog;
import org.employee.surverythymeleaf.repository.ActivityLogRepository;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    public ProfileService(UserRepository userRepository,ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
    }

    public Long UserSurveyCount(Long userId) {
        return userRepository.UserSurveyCount(userId);
    }

    public Long UserApplicationCount(Long userId) {
        return userRepository.UserApplicationCount(userId);
    }

    public Long UserTotalActivityCount(Long userId) {
        return activityLogRepository.UserTotalActivityCount(userId);
    }
}
