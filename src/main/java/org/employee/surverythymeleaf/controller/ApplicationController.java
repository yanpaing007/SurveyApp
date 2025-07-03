package org.employee.surverythymeleaf.controller;
import org.employee.surverythymeleaf.model.*;
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

import static org.employee.surverythymeleaf.util.SortUtils.getAllSurveysDouble;
import static org.employee.surverythymeleaf.util.SortUtils.sortFunction;

import java.time.format.DateTimeFormatter;


@Controller
@RequestMapping("/technical")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final SurveyService surveyService;
    private final UserService userService;

    public ApplicationController(ApplicationService applicationService, SurveyService surveyService , UserService userService) {
        this.applicationService = applicationService;
        this.surveyService = surveyService;
        this.userService = userService;
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

    static String getAllApplication(Model model,
                                    @RequestParam(required = false) String query,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "5") int size, Page<Application> applicationPage,
                                    @RequestParam(required = false) ApplicationStatus applicationStatus,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate) {
        model.addAttribute("applications",applicationPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",applicationPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("currentStatus",applicationStatus);
        model.addAttribute("fromDate",fromDate);
        model.addAttribute("toDate",toDate);
        model.addAttribute("totalPages",applicationPage.getTotalPages());
        return "application/allApplication";
    }

    @GetMapping("/survey/allSurvey")
    public String getAllSurveys(Model model, Principal principal,
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
        User currentUser = userService.findByEmail(email);
        survey.setSalePerson(currentUser);
        Sort sort = sortFunction(sortField, sortDir);

        return getAllSurveysDouble(model, query, page, size, status, fromDate, toDate, surveyService,survey,app,sort,sortField,sortDir);
    }


//    @GetMapping("survey/edit/{id}")
//    public String getEditSurveyForm(Model model, @PathVariable Long id) {
//        Survey survey=surveyService.findSurveyById(id);
//        model.addAttribute("survey", survey);
//        return "survey/editSurveyForm";
//    }

    @GetMapping("/survey/details/{id}")
    public String getDetailSurveyForm(Model model, @PathVariable String id) {
        Survey survey=surveyService.findSurveyByGeneratedSurveyId(id);
        model.addAttribute("survey", survey);
        return "survey/surveyDetails";
    }

    @PostMapping("application/updateStatus")
    public String updateApplicationStatus(@RequestParam ApplicationStatus status, @RequestParam Long id, RedirectAttributes redirectAttributes) {
       boolean application= applicationService.updateStatus(id,status);
       if(application){
           redirectAttributes.addFlashAttribute("message", "Application status updated to " + status);
           redirectAttributes.addFlashAttribute("messageType", "success");
       }
       else{
           redirectAttributes.addFlashAttribute("message", "Application status not updated to " + status);
           redirectAttributes.addFlashAttribute("messageType", "error");
       }
       return "redirect:/technical/application/allApplications";
    }



    @PostMapping("survey/updateStatus")
    public String updateSurveyStatus(@RequestParam SurveyStatus status, @RequestParam Long id, RedirectAttributes redirectAttributes,@RequestParam User TechPerson) {
        boolean survey = surveyService.updateStatus(id,status,TechPerson);
        if(survey){
            redirectAttributes.addFlashAttribute("message", "Survey status updated to " + status);
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        else{
            redirectAttributes.addFlashAttribute("message", "Survey status not updated to " + status);
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/technical/survey/allSurvey";
    }

//    @PostMapping("/application/details/{id}")
//    public String updateApplication(@PathVariable String id, @ModelAttribute Application application, RedirectAttributes redirectAttributes) {
//        boolean app= applicationService.updateApplication(id,application);
//        if(app){
//            redirectAttributes.addFlashAttribute("message", "Application status updated");
//            redirectAttributes.addFlashAttribute("messageType", "success");
//        }
//        else{
//            redirectAttributes.addFlashAttribute("message", "Application status not updated");
//            redirectAttributes.addFlashAttribute("messageType", "error");
//        }
//        return "redirect:/technical/application/allApplications";
//    }

//    @GetMapping("application/details/{id}")
//    public String getApplicationDetails(Model model, @PathVariable String id) {
//        Application application = applicationService.findApplicationByGeneratedApplicationId(id);
//        model.addAttribute("applications", application);
//        return "application/applicationDetails";
//    }
}
