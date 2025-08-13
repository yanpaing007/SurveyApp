package org.employee.surverythymeleaf.model.Enum;

import lombok.Getter;

@Getter
public enum Priority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4);

    private final String displayName;
    private final int level;

    Priority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
}
