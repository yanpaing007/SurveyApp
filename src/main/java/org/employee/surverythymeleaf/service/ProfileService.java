package org.employee.surverythymeleaf.service;

import org.employee.surverythymeleaf.DTO.UpdateProfileDTO;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.ActivityLogRepository;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ProfileService(UserRepository userRepository, ActivityLogRepository activityLogRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Long UserSurveyCount(Long userId) {
        return userRepository.UserSurveyCount(userId);
    }

    public Long UserApplicationCount(Long userId) {
        return userRepository.UserApplicationCount(userId);
    }

    public Long UserTotalActivityCount(Long userId) {
        return activityLogRepository.UserTotalActivityCount(userId);
    }

    public User updateProfile(User user, UpdateProfileDTO updateProfileDTO) {
        user.setFullName(updateProfileDTO.getFullName());


        if(!updateProfileDTO.getNewPassword().isEmpty()) {
            String encodedPassword = bCryptPasswordEncoder.encode(updateProfileDTO.getNewPassword());
            user.setPassword(encodedPassword);
        } else {
            user.setPassword(user.getPassword());
        }

        user.setPhoneNumber(updateProfileDTO.getPhoneNumber());
        if (updateProfileDTO.getProfilePicturePath() != null && !updateProfileDTO.getProfilePicturePath().isBlank()) {
            user.setProfilePictureUrl(updateProfileDTO.getProfilePicturePath());
        }

        return userRepository.save(user);
    }
}
