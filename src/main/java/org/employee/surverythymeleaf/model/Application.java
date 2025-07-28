package org.employee.surverythymeleaf.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @NotNull(message = "The person who submit the application can't be empty")
    @JoinColumn(name = "submittedBy_id")
    private User submittedBy;

    @NotBlank(message = "Customer Name can't be empty")
    private String customerName;

    @Nullable
    private String companyName;

    @NotBlank(message = "Address can't be empty")
    private String address;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus= ApplicationStatus.PENDING;

    @Column(unique = true)
    private String generatedApplicationId;

    @NotBlank(message = "Phone Number can't be empty")
    private String phoneNumber;

    @NotBlank(message = "Contact email can't be empty")
    private String contactEmail;

    @NotNull(message = "Latitude can't be empty")
    private Double latitude;

    @NotNull(message = "Longitude can't be empty")
    private Double longitude;

    @Lob
    @Column(columnDefinition = "TEXT")
    @Nullable
    private String comment;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;
}
