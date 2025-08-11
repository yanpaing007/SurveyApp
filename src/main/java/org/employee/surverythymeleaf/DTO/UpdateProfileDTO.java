package org.employee.surverythymeleaf.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.employee.surverythymeleaf.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

    private String fullName;
    private String phoneNumber;
    private String email;

    private Role role;
    private boolean status;
    private String profilePictureUrl;

    private String oldPassword;
    private String newPassword;
}
