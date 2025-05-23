package org.employee.surverythymeleaf.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
        private long id;
        private String fullName;
        private String email;
}
