package org.employee.surverythymeleaf.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.employee.surverythymeleaf.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
        private long id;
        private String fullName;
        private String email;
        private Role role;
        private String profilePictureUrl;
}
