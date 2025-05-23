package org.employee.surverythymeleaf.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applications", "surveysSalePerson", "surveysTechnicalPerson"})
public class User implements UserDetails {
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
//    @Enumerated(EnumType.STRING) For using with Enum, need this Annotation
    @ManyToOne
    @Nullable
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        assert role != null;
        return List.of(new SimpleGrantedAuthority(role.getRoleName()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status;
    }
}
