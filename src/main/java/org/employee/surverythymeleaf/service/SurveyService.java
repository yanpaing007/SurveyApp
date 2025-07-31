package org.employee.surverythymeleaf.service;

import org.employee.surverythymeleaf.DTO.ChartDatasetDTO;
import org.employee.surverythymeleaf.DTO.ChartResponseDTO;
import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.util.CalculateDashboard;
import org.employee.surverythymeleaf.util.StatusValidator;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        SurveyStatus currentStatus = optionalSurvey.map(Survey::getStatus).orElse(null);
        if (optionalSurvey.isPresent()) {
            if(!StatusValidator.validateSurveyStatus(currentStatus, status)) {
                return false;
            }
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

    public List<Survey> filterSurveys(String query, SurveyStatus status, LocalDate fromDate, LocalDate toDate, Sort sort) {
        if((query != null && !query.isEmpty()) || status != null || (fromDate != null && toDate != null)){
            return surveyRepository.searchSurvey(query, Pageable.unpaged(), status, fromDate, toDate).getContent();
        } else {
            return surveyRepository.findAll(sort);
        }
    }

    public ChartResponseDTO getRealSurveyData(String range,List<String> labels){
        List<Object[]> result;
        Map<String,List<Integer>> dataMap = new LinkedHashMap<>();

        List<String> statuses = Arrays.stream(SurveyStatus.values()).map(SurveyStatus::name).toList();
        for(String status : statuses){
            dataMap.put(status,new ArrayList<>(Collections.nCopies(labels.size(),0)));
        }

        if("monthly".equals(range)){
            LocalDateTime start = LocalDate.now().minusMonths(12).atStartOfDay();
            result = surveyRepository.countSurveysByMonth(start);

            Map<Integer,Integer> monthIndexMap = new HashMap<>();
            for(int i = 0 ; i < labels.size() ; i++){
                monthIndexMap.put(i+1,i);
            }

            for(Object[] row : result){
                int month = ((Number) row[0]).intValue();
                String status = ((SurveyStatus) row[1]).name();
                int count = ((Number) row[2]).intValue();

                Integer index = monthIndexMap.get(month);
                if(index != null){
                    dataMap.get(status).set(index, count);
                }
            }
        }else{
            int days = Integer.parseInt(range);
            LocalDateTime start = LocalDate.now().minusDays(days - 1).atStartOfDay();
            result = surveyRepository.countSurveysByDay(start);

            Map<LocalDateTime, Integer> dateIndexMap = new HashMap<>();
            for (int i = 0; i < labels.size(); i++) {
                dateIndexMap.put(start.plusDays(i), i);
            }
            for (Object[] row : result) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                LocalDateTime dateTimeKey = date.atStartOfDay();

                String status = ((SurveyStatus) row[1]).name();
                int count = ((Number) row[2]).intValue();

                Integer idx = dateIndexMap.get(dateTimeKey);
                if (idx != null && dataMap.containsKey(status)) {
                    dataMap.get(status).set(idx, count);
                }
            }
        }

        List<ChartDatasetDTO> datasets = new ArrayList<>();
        for(String status : statuses){
            datasets.add(new ChartDatasetDTO(status, dataMap.get(status)));
        }
        return new ChartResponseDTO(labels,datasets);
    }


}
