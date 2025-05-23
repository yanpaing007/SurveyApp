package org.employee.surverythymeleaf.controller;
import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

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
    public String getAllSurveys(Model model,@RequestParam(required = false) String query,
                              @RequestParam(required = false,defaultValue = "0") int page,
                              @RequestParam(required = false,defaultValue = "5") int size){
        Page<Survey> surveyPage;
        if(query != null && !query.isEmpty()){
            surveyPage = surveyService.searchSurveyWithQuery(query,page,size);
        }else{
            surveyPage = surveyService.getAllSuvey(page,size);
        }
        model.addAttribute("surveyStatus", SurveyStatus.values());
        model.addAttribute("surveys",surveyPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",surveyPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",surveyPage.getTotalPages());
        return "survey/allSurveys";
    }

    @GetMapping("/application/allApplications")
    public String getAllApplications(Model model, @RequestParam(required = false) String query,
                                     @RequestParam(required = false,defaultValue = "0") int page,
                                     @RequestParam(required = false,defaultValue = "5") int size){
        Page<Application> applicationPage;
        if(query != null && !query.isEmpty()){
            applicationPage = applicationService.searchApplicationWithQuery(query,page,size);
        }else{
            applicationPage = applicationService.getAllApplicationPaginated(page,size);
        }
        model.addAttribute("applications",applicationPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",applicationPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",applicationPage.getTotalPages());
        return "application/allApplication";
    }

    @GetMapping("/application/create/{id}")
    public String getApplicationForm(Model model, Principal principal, @PathVariable String id) {
        Application application = new Application();
        model.addAttribute("surveyId", id);
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
        return "redirect:/sale/survey/allSurvey";
    }

}
