package org.employee.surverythymeleaf.controller;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.SortUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

import static org.employee.surverythymeleaf.controller.ApplicationController.getAllApplication;
import static org.employee.surverythymeleaf.util.SortUtils.getAllSurveysDouble;

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
        return "survey/a";
    }

    @PostMapping("/survey/add")
    public String addSurvey(@ModelAttribute("survey") Survey survey, Model model, RedirectAttributes redirectAttributes) {
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
        boolean success =surveyService.addNewSurvey(survey);
        if(success) {
            redirectAttributes.addFlashAttribute("message", "Survey added successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        else {
            redirectAttributes.addFlashAttribute("message", "Error adding new survey");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/sale/survey/allSurvey";
    }

    @GetMapping("/survey/allSurvey")
    public String getAllSurveys(Model model,Principal principal,
                                @RequestParam(required = false) String query,
                                @RequestParam(required = false,defaultValue = "0") int page,
                                @RequestParam(required = false,defaultValue = "13") int size,
                                @RequestParam(required = false, defaultValue = "id") String sortField,
                                @RequestParam(required = false, defaultValue = "desc") String sortDir,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate)
    {
        String email = principal.getName();
        Survey survey = new Survey();
        Application app = new Application();

        model.addAttribute("app", new Application());
        User currentUser = userService.findByEmail(email);
        survey.setSalePerson(currentUser);

        Sort sort = SortUtils.sortFunction(sortField, sortDir);

        return getAllSurveysDouble(model, query, page, size, status, fromDate, toDate, surveyService,survey, app,sort,sortField,sortDir);
    }



    @GetMapping("/survey/details/{id}")
    public String getDetailSurveyForm(Model model, @PathVariable String id) {
        Survey survey=surveyService.findSurveyByGeneratedSurveyId(id);
        model.addAttribute("survey", survey);
        return "survey/surveyDetails";
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

    @PostMapping("/application/add")
    public String addApplication(@ModelAttribute("application") Application application,@ModelAttribute("surveyId") String surveyId) {
        application.setApplicationDate(LocalDate.now());

        Application latestApplication = applicationService.searchLatestApplication();
        int newNumber = 4321;
        if(latestApplication != null && latestApplication.getId() != null) {
            String latestApplicationId = latestApplication.getGeneratedApplicationId();
            String numberString = latestApplicationId.replace("APP-","");
            newNumber = Integer.parseInt(numberString) + 1;
        }

        Survey findSurveyId = surveyService.findSurveyByGeneratedSurveyId(surveyId);

        String newApplicationId = String.format("APP-%05d", newNumber);
        application.setGeneratedApplicationId(newApplicationId);
        application.setApplicationDate(LocalDate.now());
        application.setSurvey(findSurveyId);
        applicationService.addNewApplication(application);
        return "redirect:/sale/survey/allSurvey";
    }

}
