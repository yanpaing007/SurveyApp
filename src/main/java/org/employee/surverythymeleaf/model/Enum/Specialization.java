package org.employee.surverythymeleaf.model.Enum;


import lombok.Getter;

@Getter
public enum Specialization {
    NETWORKING("Networking"),
    COMPUTING("Computing"),
    IOT("IoT"),
    GENERAL("General"),
    SECURITY("Security"),
    COMMUNICATION("Communication");

    private final String label;

    Specialization(String label) {
        this.label = label;
    }
}
