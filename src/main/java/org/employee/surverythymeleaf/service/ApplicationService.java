package org.employee.surverythymeleaf.service;


import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.ApplicationStatus;
import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

    public Page<Application> searchApplicationWithQuery(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return applicationRepository.searchApplication(query,pageable);
    }

    public Application findApplicationById(Long id) {
        return applicationRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("Application with id " + id + " not found")
        );
    }

    public Application findApplicationByGeneratedApplicationId(String generatedApplicationId) {
        return applicationRepository.findApplicationByGeneratedApplicationId(generatedApplicationId);
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

//    public void updateApplicationStatus(Long id, ApplicationStatus status) {
//
//    }

    public Application searchLatestApplication() {
       return applicationRepository.findLatestApplication();
    }
}
