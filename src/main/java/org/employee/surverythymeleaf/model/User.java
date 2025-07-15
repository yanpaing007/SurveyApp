package org.employee.surverythymeleaf.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applications", "surveysSalePerson", "surveysTechnicalPerson","activities"})
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    @NotBlank(message="*Full name can't be empty")
//    @Size(min = 4,max = 25, message = "*Full name should have at least 4 characters and maximum 25")
    private String fullName;

    @NotBlank(message = "*Phone number can't be empty")
    @Column(name = "phone_number")
    private String phoneNumber;


    @NotBlank(message = "*Password can't be empty")
//    @Size(min = 6, message = "*Password must at least 6 characters")
    private String password;

    @NotBlank(message = "*Email can't be empty")
    @Email(message = "*Invalid Email Format")
    @Column(unique = true)
    private String email;

    private LocalDateTime updatedAt;
//    @Enumerated(EnumType.STRING) For using with Enum, need this Annotation
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private boolean status=true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "submittedBy",cascade = CascadeType.ALL)
    private List<Application> applications;

    @OneToMany(mappedBy = "salePerson",cascade = CascadeType.ALL)
    private List<Survey> surveysSalePerson;

    @OneToMany(mappedBy = "technicalPerson",cascade = CascadeType.ALL)
    private List<Survey> surveysTechnicalPerson;

    @OneToMany(mappedBy = "actor",cascade = CascadeType.ALL)
    private List<ActivityLog> activities = new ArrayList<>();
}
