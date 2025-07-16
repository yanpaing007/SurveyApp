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

}
