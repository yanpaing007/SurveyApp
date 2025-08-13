package org.employee.surverythymeleaf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.employee.surverythymeleaf.model.Enum.Specialization;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"members", "applications"})
public class Team {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Team name cannot be empty")
    private String name;

    @NotBlank(message = "Team description cannot be empty")
    private String description;

    @NotNull(message = "Total members cannot be null")
    @Size(min = 1, max = 10, message = "Total members must be between 1 and 10")
    private Integer totalMembers;

    @NotBlank(message = "Team leader cannot be empty")
    @Size(max = 50, message = "Team leader name cannot exceed 50 characters")
    private String teamLeader;

    @NotBlank(message = "Contact number cannot be empty")
    @Size(min = 10, max = 15, message = "Contact number must be between 10 and 15 digits")
    @jakarta.validation.constraints.Pattern(regexp = "^[0-9]+$", message =" Contact number must contain only digits")
    private String contactNumber;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @NotNull(message = "Max capacity cannot be null")
    @Size(min = 4, max = 20, message = "Max capacity must be between 4 and 20 characters")
    private Integer maxCapacity = 8;

    @CreationTimestamp
    @jakarta.persistence.Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members;

    @OneToMany(mappedBy = "assignedTeam", fetch = FetchType.LAZY)
    private List<Application> applications;

}
