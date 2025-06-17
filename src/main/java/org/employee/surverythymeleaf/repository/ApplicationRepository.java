package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a WHERE " +
            "LOWER(a.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "CAST(a.generatedApplicationId AS string) LIKE CONCAT('%', :query, '%')")
    Page<Application> searchApplication(@Param("query") String query, Pageable pageable);


    @Query("SELECT a from Application a ORDER BY a.id DESC LIMIT 1")
    Application findLatestApplication();


    Application findApplicationByGeneratedApplicationId(String generatedApplicationId);

    Optional<Application> findByGeneratedApplicationId(String generatedApplicationId);

    boolean deleteByGeneratedApplicationId(String id);
}
