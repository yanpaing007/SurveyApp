package org.employee.surverythymeleaf.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChartDatasetDTO {
    private String label;
    private List<Integer> data;

    public ChartDatasetDTO(String label, List<Integer> data) {
        this.label = label;
        this.data = data;
    }
}
