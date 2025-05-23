package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.role.roleName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR CAST(u.status AS string) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUser(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.role.roleName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR CAST(u.status AS string) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchAllUser(@Param("query") String query);


}
