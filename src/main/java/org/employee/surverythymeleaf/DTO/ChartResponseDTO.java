package org.employee.surverythymeleaf.DTO;

import lombok.*;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponseDTO {
    private List<String> labels;
    private List<ChartDatasetDTO> datasets;
}