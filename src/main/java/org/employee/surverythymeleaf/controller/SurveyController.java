package org.employee.surverythymeleaf.controller;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

import static org.employee.surverythymeleaf.controller.ApplicationController.getAllApplication;
import static org.employee.surverythymeleaf.controller.ApplicationController.getString;

@Controller
@RequestMapping("/sale")
public class SurveyController {

    private final SurveyService surveyService;
    private final UserService userService;
    private final ApplicationService applicationService;

    public SurveyController(SurveyService surveyService, UserService userService, ApplicationRepository applicationRepository, ApplicationService applicationService) {
        this.surveyService = surveyService;
        this.userService = userService;
        this.applicationService = applicationService;
    }

    @GetMapping("/survey/form")
    public String addSurvey(Model model, Principal principal) {
        String email = principal.getName();
        Survey survey = new Survey();
        User currentUser = userService.findByEmail(email);
        survey.setSalePerson(currentUser);
        model.addAttribute("survey", survey);
        return "survey/createSurvey";
    }

    @PostMapping("/survey/add")
    public String addSurvey(@ModelAttribute("survey") Survey survey) {
        survey.setRequestDate(LocalDate.now());
        Survey latestSurvey = surveyService.searchLatestSurvey();
        int newNumber = 2131;
        if(latestSurvey != null && latestSurvey.getId() != null) {
            String latestSurveyId = latestSurvey.getGeneratedSurveyId();
            String numberString = latestSurveyId.replace("SUR-","");
            newNumber = Integer.parseInt(numberString) + 1;
        }

        String newSurveyId = String.format("SUR-%05d", newNumber);
        survey.setGeneratedSurveyId(newSurveyId);

        survey.setStatus(SurveyStatus.PENDING);
        surveyService.addNewSurvey(survey);
        return "redirect:/sale/survey/allSurvey";
    }

    @GetMapping("/survey/allSurvey")
    public String getAllSurveys(Model model,
                                @RequestParam(required = false) String query,
                                @RequestParam(required = false,defaultValue = "0") int page,
                                @RequestParam(required = false,defaultValue = "13") int size,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate)
    {
        return getAllSurveysDouble(model, query, page, size, status, fromDate, toDate, surveyService);
    }

    static String getAllSurveysDouble(Model model, @RequestParam(required = false) String query, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "13") int size, @RequestParam(required = false) String status, @DateTimeFormat(pattern = "MM/dd/yyyy") @RequestParam(required = false) LocalDate fromDate, @DateTimeFormat(pattern = "MM/dd/yyyy") @RequestParam(required = false) LocalDate toDate, SurveyService surveyService) {
        Page<Survey> surveyPage;
        SurveyStatus surveyStatus = null;

        if( status != null && !status.trim().isEmpty() ){
            surveyStatus = SurveyStatus.valueOf(status);
        }

        if((query != null && !query.isEmpty()) || status != null || (fromDate != null && toDate != null)){

            surveyPage = surveyService.searchSurveyWithQuery(query,page,size, surveyStatus,fromDate,toDate);
        }else{
            surveyPage = surveyService.getAllSuvey(page,size);
        }


        model.addAttribute("surveyWithNonePendingStatus", SurveyStatus.getNonPendingSurveyStatus());
        model.addAttribute("surveyWithPendingStatus", SurveyStatus.values());
        return getString(model, query, page, size, surveyPage,surveyStatus,fromDate,toDate);
    }

    @GetMapping("/application/allApplications")
    public String getAllApplications(Model model, @RequestParam(required = false) String query,
                                     @RequestParam(required = false,defaultValue = "0") int page,
                                     @RequestParam(required = false,defaultValue = "13") int size,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate){
        Page<Application> applicationPage;
        ApplicationStatus applicationStatus = null;
        if( status != null && !status.trim().isEmpty() ){
            applicationStatus = ApplicationStatus.valueOf(status);
        }
        if(query != null && !query.isEmpty() || (fromDate != null && toDate != null) || status != null){
            applicationPage = applicationService.searchApplicationWithQuery(query,page,size,applicationStatus,fromDate,toDate);
        }else{
            applicationPage = applicationService.getAllApplicationPaginated(page,size);
        }

        model.addAttribute("applicationWithNonePendingStatus", ApplicationStatus.getNonPendingApplicationStatus());
        model.addAttribute("applicationWithPendingStatus",ApplicationStatus.values());
        return getAllApplication(model, query, page, size, applicationPage,applicationStatus,fromDate,toDate);
    }

    @GetMapping("/application/create/{id}")
    public String getApplicationForm(Model model, @PathVariable String id) {
        Application application = new Application();
        Survey findSurvey = surveyService.findSurveyByGeneratedSurveyId(id);
        model.addAttribute("surveyId", findSurvey.getId());
        model.addAttribute("applications", application);
        return "application/applicationForm";
    }

    @PostMapping("/application/add")
    public String addApplication(@ModelAttribute("application") Application application) {
        application.setApplicationDate(LocalDate.now());

        Application latestApplication = applicationService.searchLatestApplication();
        int newNumber = 4321;
        if(latestApplication != null && latestApplication.getId() != null) {
            String latestApplicationId = latestApplication.getGeneratedApplicationId();
            String numberString = latestApplicationId.replace("APP-","");
            newNumber = Integer.parseInt(numberString) + 1;
        }

        String newApplicationId = String.format("APP-%05d", newNumber);
        application.setGeneratedApplicationId(newApplicationId);
        applicationService.addNewApplication(application);
        return "redirect:/sale/survey/allSurvey";
    }

}
