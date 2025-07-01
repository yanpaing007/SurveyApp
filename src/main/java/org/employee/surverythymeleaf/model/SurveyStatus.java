package org.employee.surverythymeleaf.model;

import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SurveyStatus {
    PENDING("Pending"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    ;

    private final String status;

    SurveyStatus(String status) {
        this.status = status;
    }

    public static List<SurveyStatus> getNonPendingSurveyStatus() {
        return Arrays.stream(values())
                .filter(status -> status != PENDING )
                .collect(Collectors.toList());
    }

}
