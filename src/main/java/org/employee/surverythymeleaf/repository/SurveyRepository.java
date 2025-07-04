package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("""
                SELECT s FROM Survey s WHERE
                            (:query IS NULL OR :query = '' OR
                            LOWER(s.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                            LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR
                            CAST(s.generatedSurveyId AS string) LIKE CONCAT('%', :query, '%'))
                            AND (:status IS NULL OR s.status = :status)
                            AND (:fromDate IS NULL OR :toDate IS NULL OR s.requestDate between :fromDate and :toDate)
""")
    Page<Survey> searchSurvey(@Param("query") String query, Pageable pageable, @Param("status") SurveyStatus status, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query("SELECT s from Survey s ORDER BY s.id DESC LIMIT 1")
    Survey findLatestSurvey();

    Optional<Survey> findSurveyByGeneratedSurveyId(String generatedSurveyId);

    Long countByStatus(SurveyStatus status);
}
