package org.employee.surverythymeleaf.service;
import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.ApplicationStatus;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.util.CalculateDashboard;
import org.employee.surverythymeleaf.util.DateCalculator;
import org.employee.surverythymeleaf.util.StatusValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final SurveyRepository surveyRepository;

    public ApplicationService(ApplicationRepository applicationRepository, SurveyRepository surveyRepository) {
        this.applicationRepository = applicationRepository;
        this.surveyRepository = surveyRepository;
    }

    public void addNewApplication(Application application) {
        applicationRepository.save(application);
    }

    public Page<Application> getAllApplicationPaginated(int page, int size, Sort sort) {
        return applicationRepository.findAll(PageRequest.of(page,size,sort));
        
    }

    public Page<Application> searchApplicationWithQuery(String query, int page, int size, ApplicationStatus applicationStatus, LocalDate fromDate, LocalDate toDate, Sort sort) {
        Pageable pageable = PageRequest.of(page,size,sort);
        return applicationRepository.searchApplication(query,pageable,applicationStatus,fromDate,toDate);
    }

    public Application findApplicationById(Long id) {
        return applicationRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("Application with id " + id + " not found")
        );
    }

    public Application findApplicationByGeneratedApplicationId(String generatedApplicationId) {
        return applicationRepository.findApplicationByGeneratedApplicationId(generatedApplicationId)
                .orElseThrow(() -> new UsernameNotFoundException("Application with id " + generatedApplicationId + " not found"));
    }

    public boolean updateStatus(Long id, ApplicationStatus status) {
           Application application= applicationRepository.findById(id).orElseThrow(
                        () -> new UsernameNotFoundException("Application was not found on server"));
            ApplicationStatus currentStatus = application.getApplicationStatus();
            if(!StatusValidator.validateAppStatus(currentStatus, status)) {
                return false;
            }
            application.setApplicationStatus(status);
            applicationRepository.save(application);
            return true;
    }

    public Application searchLatestApplication() {
       return applicationRepository.findLatestApplication();
    }

    public boolean updateApplication(String id, Application application) {
        Optional<Application> applicationOpt= applicationRepository.findByGeneratedApplicationId((id));
        if(applicationOpt.isPresent()) {
            Application app = applicationOpt.get();
            ApplicationStatus currentStatus = app.getApplicationStatus();
            ApplicationStatus nextStatus = application.getApplicationStatus();
            if(!StatusValidator.validateAppStatus(currentStatus, nextStatus)) {
                return false;
            }
            app.setCompanyName(application.getCompanyName());
            app.setCustomerName(application.getCustomerName());
            app.setContactEmail(application.getContactEmail());
            app.setPhoneNumber(application.getPhoneNumber());
            app.setAddress(application.getAddress());
            app.setLongitude(application.getLongitude());
            app.setLatitude(application.getLatitude());
            app.setApplicationStatus(application.getApplicationStatus());
            app.setSubmittedBy(application.getSubmittedBy());
            app.setComment(application.getComment());
            app.setApplicationDate(application.getApplicationDate());
            applicationRepository.save(app);
        }
        else{
            throw new UsernameNotFoundException("Application with id " + id + " not found");
        }
        return true;
    }

    public Long applicationCompareToLastMonth(){
       Map<String,Long> data =  DateCalculator.getCurrentMonthAndYearCounts(applicationRepository::countApplicationByMonthAndYear);
       Long currentMonth = data.get("currentMonth");
       Long lastMonth = data.get("lastMonth");
       return CalculateDashboard.calculatePercentage(currentMonth,lastMonth);
    }

    public Long successRateCompareToLastMonth(){
        Map<String,Long> data =  DateCalculator.getCurrentMonthAndYearCounts((month,year) ->
                applicationRepository.countApplicationByMonthYearAndStatus(month,year,ApplicationStatus.COMPLETED));
        Long currentMonth = data.get("currentMonth");
        Long lastMonth = data.get("lastMonth");
        return CalculateDashboard.calculatePercentage(currentMonth,lastMonth);
    }

    public Optional<Object[]> findTopApplicationCreator(){
        Pageable pageable = PageRequest.of(0, 1);
        return applicationRepository.findTopApplicationCreator(pageable).stream().findFirst();
    }

    public List<Application> filterApplications(String query, ApplicationStatus status, LocalDate fromDate, LocalDate toDate, Sort sort) {
        if((query != null && !query.isEmpty()) || status != null || (fromDate != null && toDate != null)){
            return applicationRepository.searchApplication(query, Pageable.unpaged(), status, fromDate, toDate).getContent();
        } else {
            return applicationRepository.findAll(sort);
        }
    }



}
