package org.employee.surverythymeleaf.util;

import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.ApplicationStatus;

import java.util.Arrays;
import java.util.List;

public class AppStatusValidator {
    public static boolean validateAppStatus(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == ApplicationStatus.PROCESSING;
            case PROCESSING -> newStatus == ApplicationStatus.COMPLETED || newStatus == ApplicationStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public static List<ApplicationStatus> getNextValidStatuses(ApplicationStatus currentStatus){
        return switch (currentStatus){

            case PENDING -> List.of(ApplicationStatus.PENDING,ApplicationStatus.PROCESSING);
            case PROCESSING -> List.of(ApplicationStatus.PROCESSING, ApplicationStatus.COMPLETED, ApplicationStatus.CANCELLED);
            case COMPLETED -> List.of(ApplicationStatus.COMPLETED);
            case CANCELLED -> List.of(ApplicationStatus.CANCELLED);
        };
    }
}
