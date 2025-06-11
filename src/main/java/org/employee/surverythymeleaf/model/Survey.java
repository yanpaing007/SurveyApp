package org.employee.surverythymeleaf.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor

@ToString(exclude = "application")
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate requestDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status = SurveyStatus.PENDING;

    private String state;
    private String townShip;

    @Column(unique = true)
    private String generatedSurveyId;

    @ManyToOne
    @JoinColumn(name = "sales_user_id")
    private User salePerson;

    @OneToOne(mappedBy = "survey", cascade = CascadeType.ALL)
    private Application application;


    @ManyToOne
    @JoinColumn(name = "technical_person")
    private User technicalPerson;

    private Double longitude;
    private Double latitude;


    private boolean hasApplications(){
        return application != null;
    }
}
