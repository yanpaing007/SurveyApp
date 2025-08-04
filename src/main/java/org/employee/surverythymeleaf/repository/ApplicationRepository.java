package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.Enum.ApplicationStatus;
import org.employee.surverythymeleaf.model.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    Long countByApplicationStatus(ApplicationStatus applicationStatus);

    @Query("""
        select count(a) from Application a where month(a.createdAt)=:month and year(a.createdAt)=:year
""")
    Long countApplicationByMonthAndYear(@Param("month") Integer month,@Param("year") Integer year);

    @Query("""
        select count(a) from Application a where month(a.createdAt)=:month and year(a.createdAt)=:year and a.applicationStatus=:status
""")
    Long countApplicationByMonthYearAndStatus(@Param("month") Integer month,@Param("year") Integer year,@Param("status") ApplicationStatus status);


    @Query("""
        select a.submittedBy , count(a) from Application a group by a.submittedBy order by count(a) desc
""")
    List<Object[]> findTopApplicationCreator(Pageable pageable);

    List<Application> survey(Survey survey);

//    Application findApplicationByGeneratedSurveyId(String surveyId);

    @Query("SELECT DATE(a.createdAt), a.applicationStatus, COUNT(a) " +
            "FROM Application a WHERE a.createdAt >= :startDate " +
            "GROUP BY DATE(a.createdAt), a.applicationStatus " +
            "ORDER BY DATE(a.createdAt)")
    List<Object[]> countApplicationsByDay(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('MONTH', a.createdAt), a.applicationStatus, COUNT(a) " +
            "FROM Application a WHERE a.createdAt >= :startDate " +
            "GROUP BY FUNCTION('MONTH', a.createdAt), a.applicationStatus " +
            "ORDER BY FUNCTION('MONTH', a.createdAt)")
    List<Object[]> countApplicationsByMonth(@Param("startDate") LocalDateTime startDate);

}
