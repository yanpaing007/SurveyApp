package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.Enum.SurveyStatus;
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


    @Query("SELECT s " +
            "from Survey s " +
            "ORDER BY s.id DESC LIMIT 1")
    Survey findLatestSurvey();

    Optional<Survey> findSurveyByGeneratedSurveyId(String generatedSurveyId);



    Long countByStatus(SurveyStatus status);

    @Query("""
   SELECT count(s) 
   FROM Survey s 
   WHERE month(s.createdAt)=:month and year(s.createdAt)=:year
""")
    Long countSurveyByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);


    @Query("""
   SELECT count(s)
   FROM Survey s 
   WHERE month(s.createdAt)=:month and year(s.createdAt)=:year and s.status = "PENDING"
""")
    Long countPendingSurveyByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    List<Survey> findAllByOrderByIdDesc(Pageable pageable);

    @Query("""
        select s.salePerson , count(s) 
        from Survey s 
        group by s.salePerson 
        order by count(s) desc
""")
    List<Object[]> findTopSurveyCreator(Pageable pageable);

    @Query("SELECT COUNT(a) > 0 " +
            "FROM Application a " +
            "WHERE a.survey.id = :surveyId")
    boolean existsBySurveyId(@Param("surveyId") Long surveyId);

    @Query("""
           select date(s.createdAt), s.status, count(s)
           from Survey s
           where s.createdAt >= :startDate
           group by date(s.createdAt), s.status
           order by date(s.createdAt)
""")
    List<Object[]> countSurveysByDay(@Param("startDate") LocalDateTime startDate);

    @Query("""
           select function('MONTH', s.createdAt) ,s.status, count(s)
           from Survey s
           where s.createdAt >= :startDate
           group by function('MONTH', s.createdAt) ,s.status
           order by function('MONTH', s.createdAt)
           
""")
    List<Object[]> countSurveysByMonth(@Param("startDate") LocalDateTime startDate);


}
