package org.employee.surverythymeleaf.service;

import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    public void addNewSurvey(Survey survey) {
        surveyRepository.save(survey);
    }

    public Page<Survey> searchSurveyWithQuery(String query, int page, int size, SurveyStatus status, LocalDate fromDate, LocalDate toDate) {
        return surveyRepository.searchSurvey(query,PageRequest.of(page,size),status,fromDate,toDate);
    }

    public Page<Survey> getAllSuvey(int page, int size) {
        return surveyRepository.findAll(PageRequest.of(page, size));
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

}
