package org.employee.surverythymeleaf.service;


import org.employee.surverythymeleaf.model.ActivityLog;
import org.employee.surverythymeleaf.model.Enum.ActivityType;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.ActivityLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Object[]> getMostActiveUser(){
        Pageable pageable = PageRequest.of(0, 1);
        return activityLogRepository.findTopActivityUser(pageable).stream().findFirst();
    }

    public List<ActivityLog> getRelatedUserActivityLog(Long userId) {
        Pageable pageable = PageRequest.of(0, 5);
        return activityLogRepository.findByActorIdOrderByTimestampDesc(userId, pageable);
    }
}
