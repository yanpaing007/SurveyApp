package org.employee.surverythymeleaf.model.Enum;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ApplicationStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String status;
    ApplicationStatus(String status) {
        this.status = status;
    }

    public static List<ApplicationStatus> getNonPendingApplicationStatus() {
        return Arrays.stream(values())
                .filter(status -> status != PENDING)
                .collect(Collectors.toList());
    }
}
