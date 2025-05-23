package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("SELECT s FROM Survey s WHERE " +
            "LOWER(s.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "CAST(s.generatedSurveyId AS string) LIKE CONCAT('%', :query, '%')")
    Page<Survey> searchSurvey(@Param("query") String query, Pageable pageable);


    @Query("SELECT s from Survey s ORDER BY s.id DESC LIMIT 1")
    Survey findLatestSurvey();
}
