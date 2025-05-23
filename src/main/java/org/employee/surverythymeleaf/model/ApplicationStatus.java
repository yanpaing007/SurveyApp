package org.employee.surverythymeleaf.model;


import lombok.Getter;

@Getter
public enum ApplicationStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    ;

    private String status;

    ApplicationStatus(String status) {
        this.status = status;
    }
}
