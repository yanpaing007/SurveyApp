package org.employee.surverythymeleaf.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.employee.surverythymeleaf.model.Role;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

    @Size(min = 3,max = 25, message = "*Full name should have at least 3 characters and maximum 25")
    private String fullName;

    @Pattern(regexp = "^[0-9]{7,15}$", message = "*Phone number must be between 7 to 15 digits")
    private String phoneNumber;

    private String email;

    private Role role;
    private boolean status;
    private MultipartFile profilePictureUrl;
    private String profilePicturePath;


    private String oldPassword;
    private String newPassword;
}
