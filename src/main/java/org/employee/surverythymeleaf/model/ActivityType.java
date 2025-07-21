package org.employee.surverythymeleaf.model;

import lombok.Getter;

@Getter
public enum ActivityType {
    CREATE_SURVEY("created new survey"),
    UPDATE_SURVEY("update an survey"),
    EXPORT_USER("exported user list"),
    EXPORT_APPLICATION("exported application"),
    EXPORT_SURVEY("exported survey"),
    SUCCEEDED_SURVEY("update survey status to succeeded"),
    FAILED_SURVEY("update survey status to succeeded"),
    CREATE_USER("created new user"),
    UPDATE_USER("updated an exiting user's record"),
    DELETE_USER("deleted a user"),
    CREATE_APPLICATION("created new application"),
    UPDATE_APPLICATION("updated an application"),
    PROCESS_APPLICATION("updated the application status to processing"),
    COMPLETE_APPLICATION("updated the application status to completed"),
    CANCEL_APPLICATION("updated the application status to cancelled");

    private final String description;
    ActivityType(String description) {
        this.description = description;
    }

}
