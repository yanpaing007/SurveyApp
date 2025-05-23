package org.employee.surverythymeleaf.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@ToString(exclude = "survey")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "survey_id",unique = true)
    private Survey survey;

    private LocalDate applicationDate;

    @ManyToOne
    @JoinColumn(name = "submittedBy_id")
    private User submittedBy;

    private String customerName;

    private String companyName;
    private String address;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus= ApplicationStatus.PENDING;

    @Column(unique = true)
    private String generatedApplicationId;

    private String phoneNumber;
    private String contactEmail;
    private Double latitude;
    private Double longitude;
    private String comment;
}
