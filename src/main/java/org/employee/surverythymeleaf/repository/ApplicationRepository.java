package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("""
            select a
            from Application a
            where(
            (:query is null or :query = '')
            or lower(a.customerName) like lower(trim(concat('%',:query,'%')))
            or lower(a.phoneNumber) like lower(trim(concat('%',:query,'%')))
            or cast(a.generatedApplicationId as string) like trim(concat('%',:query,'%'))
            or lower(a.survey.generatedSurveyId) like lower(trim(concat('%',:query,'%')))
            or lower(a.companyName) like lower(trim(concat('%',:query,'%')))
            or lower(a.submittedBy.fullName) like lower(trim(concat('%',:query,'%'))))
            and (:applicationStatus is null or a.applicationStatus=:applicationStatus)
            and (:fromDate is null or :toDate is null or a.applicationDate between :fromDate and :toDate)
""")
    Page<Application> searchApplication(@Param("query") String query, Pageable pageable,@Param("applicationStatus") ApplicationStatus applicationStatus,@Param("fromDate") LocalDate fromDate,@Param("toDate") LocalDate toDate);

    @Query("SELECT a from Application a ORDER BY a.id DESC LIMIT 1")
    Application findLatestApplication();


   Optional<Application> findApplicationByGeneratedApplicationId(String generatedApplicationId);

    Optional<Application> findByGeneratedApplicationId(String generatedApplicationId);

}
