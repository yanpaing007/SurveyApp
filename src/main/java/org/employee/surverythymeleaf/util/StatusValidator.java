package org.employee.surverythymeleaf.util;

import org.employee.surverythymeleaf.model.ApplicationStatus;
import org.employee.surverythymeleaf.model.SurveyStatus;

import java.util.List;

public class StatusValidator {
    public static boolean validateAppStatus(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == ApplicationStatus.PENDING || newStatus == ApplicationStatus.PROCESSING;
            case PROCESSING -> newStatus == ApplicationStatus.PROCESSING || newStatus == ApplicationStatus.COMPLETED || newStatus == ApplicationStatus.CANCELLED;
            case COMPLETED -> newStatus == ApplicationStatus.COMPLETED;
            case CANCELLED -> newStatus == ApplicationStatus.CANCELLED;
        };
    }

    public static List<ApplicationStatus> getNextValidApplicationStatuses(ApplicationStatus currentStatus){
        return switch (currentStatus){

            case PENDING -> List.of(ApplicationStatus.PENDING,ApplicationStatus.PROCESSING);
            case PROCESSING -> List.of(ApplicationStatus.PROCESSING, ApplicationStatus.COMPLETED, ApplicationStatus.CANCELLED);
            case COMPLETED -> List.of(ApplicationStatus.COMPLETED);
            case CANCELLED -> List.of(ApplicationStatus.CANCELLED);
        };
    }

    public static boolean validateSurveyStatus(SurveyStatus currentStatus, SurveyStatus newStatus) {
        return switch (currentStatus){
            case PENDING ->newStatus == SurveyStatus.PENDING || newStatus == SurveyStatus.SUCCEEDED || newStatus == SurveyStatus.FAILED;
            case SUCCEEDED -> newStatus == SurveyStatus.SUCCEEDED;
            case FAILED -> newStatus == SurveyStatus.FAILED;
        };
    }

    public static List<SurveyStatus> getNextValidSurveyStatuses(SurveyStatus currentStatus){
        return switch (currentStatus){
            case PENDING -> List.of(SurveyStatus.PENDING,SurveyStatus.SUCCEEDED,SurveyStatus.FAILED);
            case SUCCEEDED -> List.of(SurveyStatus.SUCCEEDED);
            case FAILED -> List.of(SurveyStatus.FAILED);
        };
    }
}
