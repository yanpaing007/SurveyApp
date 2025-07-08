package org.employee.surverythymeleaf.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applications", "surveysSalePerson", "surveysTechnicalPerson","activities"})
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false,name = "full_name")
    private String fullName;

    @Column(nullable = false,name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,unique = true)
    private String email;

    private LocalDateTime updatedAt;
//    @Enumerated(EnumType.STRING) For using with Enum, need this Annotation
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private boolean status=false;

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
