package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {
    Optional<User> findUserByEmail(String email);

    @Query("""
SELECT u FROM User u
WHERE (
    (:query IS NULL OR :query = '' OR
        LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%'))
    )
)
AND (:role IS NULL OR :role = '' OR u.role.roleName = :role)
AND (:status IS NULL OR u.status = :status)
""")
    Page<User> searchUser(@Param("query") String query,
                          @Param("role") String role,
                          @Param("status") Boolean status,
                          Pageable pageable);

    boolean existsByEmail(String email);

    @Query("""
        SELECT count(s)
        FROM Survey s
        where s.salePerson.id = :userId
""")
    Long UserSurveyCount(@Param("userId") Long name );

    @Query("""
        SELECT count(a)
        FROM Application a
        where a.submittedBy.id = :userId
""")
    Long UserApplicationCount(Long userId);
}
