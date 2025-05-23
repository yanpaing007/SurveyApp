package org.employee.surverythymeleaf.model;

import lombok.Getter;

@Getter
public enum SurveyStatus {
    PENDING("Pending"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    ;

    private String status;

    SurveyStatus(String status) {
        this.status = status;
    }

}
