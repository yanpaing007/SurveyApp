package org.employee.surverythymeleaf.service;


import org.employee.surverythymeleaf.model.ActivityLog;
import org.employee.surverythymeleaf.model.ActivityType;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.ActivityLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(ActivityType activityType, User actor) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setActivityType(activityType);
        activityLog.setActor(actor);
        activityLogRepository.save(activityLog);
    }


    public List<ActivityLog> getRecentActivity() {
        Pageable pageable = PageRequest.of(0, 5);
        return activityLogRepository.findAllByOrderByIdDesc(pageable);
    }
}
