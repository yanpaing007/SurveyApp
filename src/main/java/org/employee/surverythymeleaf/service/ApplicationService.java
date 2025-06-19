package org.employee.surverythymeleaf.service;
import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.ApplicationStatus;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;


@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void addNewApplication(Application application) {
        applicationRepository.save(application);
    }

    public Page<Application> getAllApplicationPaginated(int page, int size) {
        return applicationRepository.findAll(PageRequest.of(page,size));
        
    }

    public Page<Application> searchApplicationWithQuery(String query, int page, int size, ApplicationStatus applicationStatus, LocalDate fromDate, LocalDate toDate) {
        Pageable pageable = PageRequest.of(page,size);
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
        Optional<Application> applicationOpt= applicationRepository.findById(id);
        if(applicationOpt.isPresent()) {
            Application application = applicationOpt.get();
            application.setApplicationStatus(status);
            applicationRepository.save(application);
            return true;
        }
        else{
            throw new UsernameNotFoundException("Application with id " + id + " not found");
        }
    }

    public Application searchLatestApplication() {
       return applicationRepository.findLatestApplication();
    }

    public boolean updateApplication(String id, Application application) {
        Optional<Application> applicationOpt= applicationRepository.findByGeneratedApplicationId((id));
        if(applicationOpt.isPresent()) {
            Application app = applicationOpt.get();
//            BeanUtils.copyProperties(application,app,"id","generatedApplicationId","generatedApplicationId");
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

}
