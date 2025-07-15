package org.employee.surverythymeleaf.service;

import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.util.CalculateDashboard;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.employee.surverythymeleaf.util.DateCalculator.
        getCurrentMonthAndYearCounts;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final ApplicationRepository applicationRepository;

    public SurveyService(SurveyRepository surveyRepository, ApplicationRepository applicationRepository) {
        this.surveyRepository = surveyRepository;
        this.applicationRepository = applicationRepository;
    }

    public boolean addNewSurvey(Survey survey) {
        Survey savedSurvey = surveyRepository.save(survey);
        return savedSurvey.getId() != null;
    }

    public Page<Survey> searchSurveyWithQuery(String query, int page, int size, SurveyStatus status, LocalDate fromDate, LocalDate toDate, Sort sort) {
        return surveyRepository.searchSurvey(query,PageRequest.of(page,size,sort),status,fromDate,toDate);
    }

    public Page<Survey> getAllSurvey(int page, int size, Sort sort) {
        return surveyRepository.findAll(PageRequest.of(page, size,sort));
    }

    public Survey findSurveyById(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey with "+id+ " was not found on server")
        );
    }

    public boolean updateStatus(Long id, SurveyStatus status, User techPerson) {
        Optional<Survey> optionalSurvey = surveyRepository.findById(id);
        if (optionalSurvey.isPresent()) {
            Survey survey = optionalSurvey.get();
            survey.setStatus(status);
            survey.setTechnicalPerson(techPerson);
            survey.setUpdatedAt(LocalDateTime.now());
            surveyRepository.save(survey);
            return true;
        }
        return false;
    }

    public Survey searchLatestSurvey() {
        return surveyRepository.findLatestSurvey();
    }

    public Survey findSurveyByGeneratedSurveyId(String id) {
        return surveyRepository.findSurveyByGeneratedSurveyId(id)
                .orElseThrow(() -> new RuntimeException("Survey with "+id+ " was not found on server"));
    }


    public Long surveyCompareToLastMonth() {
        Map<String,Long> data = getCurrentMonthAndYearCounts(surveyRepository::countSurveyByMonthAndYear);
        Long lastMonthSurveyCount = data.get("lastMonth");
        Long currentMonthSurveyCount = data.get("currentMonth");
        return CalculateDashboard.calculatePercentage(currentMonthSurveyCount,lastMonthSurveyCount);
    }

    public Long pendingSurveyCompareToLastMonth() {
        Map<String,Long> data = getCurrentMonthAndYearCounts(surveyRepository::countPendingSurveyByMonthAndYear);
        Long lastMonthSurveyCount = data.get("lastMonth");
        Long currentMonthSurveyCount = data.get("currentMonth");
        return CalculateDashboard.calculatePercentage(currentMonthSurveyCount,lastMonthSurveyCount);
    }

    public List<Survey> getRecentSurvey() {
        Pageable pageable = PageRequest.of(0,5);
        List<Survey> latestSurvey = surveyRepository.findAllByOrderByIdDesc(pageable);
        return latestSurvey;
    }


    /**
     * Finds the top survey creator
     * @return Optional containing the result array if found, empty Optional otherwise
     * @throws DataAccessException if there's a database access problem
     */

    public Optional<Object[]> findTopSurveyCreator() {
        Pageable pageable1 = PageRequest.of(0,1);
        return surveyRepository.findTopSurveyCreator(pageable1).stream().findFirst();
    }

}
