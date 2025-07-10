package org.employee.surverythymeleaf.controller;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.ActivityHelper;
import org.employee.surverythymeleaf.util.AppStatusValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.employee.surverythymeleaf.util.SortUtils.getAllSurveysDouble;
import static org.employee.surverythymeleaf.util.SortUtils.sortFunction;

@Controller
@RequestMapping("/technical")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final SurveyService surveyService;
    private final UserService userService;
    private final ActivityHelper activityHelper;

    public ApplicationController(ApplicationService applicationService, SurveyService surveyService , UserService userService, ActivityHelper activityHelper) {
        this.applicationService = applicationService;
        this.surveyService = surveyService;
        this.userService = userService;
        this.activityHelper = activityHelper;
    }

    @GetMapping("/application/allApplications")
    public String getAllApplications(Model model, @RequestParam(required = false) String query,
                                     @RequestParam(required = false,defaultValue = "0") int page,
                                     @RequestParam(required = false,defaultValue = "13") int size,
                                     @RequestParam(required = false,defaultValue = "id") String sortField,
                                     @RequestParam(required = false,defaultValue = "desc") String sortDir,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate){
        Page<Application> applicationPage;
        Sort sort = sortFunction(sortField, sortDir);
        ApplicationStatus applicationStatus = null;
        if( status != null && !status.trim().isEmpty() ){
            applicationStatus = ApplicationStatus.valueOf(status);
        }
        if(query != null && !query.isEmpty() || (fromDate != null && toDate != null) || status != null){
            applicationPage = applicationService.searchApplicationWithQuery(query,page,size,applicationStatus,fromDate,toDate,sort);
        }else{
            applicationPage = applicationService.getAllApplicationPaginated(page,size,sort);
        }

        Map<Long, List<ApplicationStatus>> validStatusMap = new HashMap<>();
        for(Application application : applicationPage.getContent()){
            validStatusMap.put(application.getId(), AppStatusValidator.getNextValidStatuses(application.getApplicationStatus()));
        }

        model.addAttribute("validStatusMap", validStatusMap);
        ApplicationStatus[] allApplicationStatus = ApplicationStatus.values();
        return getAllApplication(model, query, page, size, applicationPage,applicationStatus,fromDate,toDate,sortField,sortDir,allApplicationStatus);
    }

    static String getAllApplication(Model model,
                                    @RequestParam(required = false) String query,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "5") int size, Page<Application> applicationPage,
                                    @RequestParam(required = false) ApplicationStatus applicationStatus,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate,
                                    String sortField,
                                    String sortDir, ApplicationStatus[] allApplicationStatus){
        model.addAttribute("applications",applicationPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",applicationPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("currentStatus",applicationStatus);
        model.addAttribute("fromDate",fromDate);
        model.addAttribute("toDate",toDate);
        model.addAttribute("totalPages",applicationPage.getTotalPages());
        model.addAttribute("sortField",sortField);
        model.addAttribute("sortDir",sortDir);
        model.addAttribute("allApplicationStatus",allApplicationStatus);
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
        SurveyStatus[] allSurveyStatus = SurveyStatus.values();

        return getAllSurveysDouble(model, query, page, size, status, fromDate, toDate, surveyService,survey,app,sort,sortField,sortDir,allSurveyStatus);
    }


    @GetMapping("/survey/details/{id}")
    public String getDetailSurveyForm(Model model, @PathVariable String id) {
        Survey survey=surveyService.findSurveyByGeneratedSurveyId(id);
        model.addAttribute("survey", survey);
        return "survey/surveyDetails";
    }

    @PostMapping("application/updateStatus")
    public String updateApplicationStatus(@RequestParam ApplicationStatus status, @RequestParam Long id, RedirectAttributes redirectAttributes, Principal principal) {
       boolean updated= applicationService.updateStatus(id,status);
       if(updated){
           redirectAttributes.addFlashAttribute("message", "Application status updated to " + status);
           redirectAttributes.addFlashAttribute("messageType", "success");

           ActivityType typeOfActivity = Objects.equals(status.getStatus(), "Processing") ? ActivityType.PROCESS_APPLICATION  : (Objects.equals(status.getStatus(),"Completed") ? ActivityType.COMPLETE_APPLICATION : ActivityType.CANCEL_APPLICATION);

           activityHelper.saveActivity(typeOfActivity,principal);
       }
       else{
           redirectAttributes.addFlashAttribute("message", "Invalid transition:couldn't update from current state to " + status);
           redirectAttributes.addFlashAttribute("messageType", "error");
       }
       return "redirect:/technical/application/allApplications";
    }



    @PostMapping("survey/updateStatus")
    public String updateSurveyStatus(@RequestParam SurveyStatus status, @RequestParam Long id, RedirectAttributes redirectAttributes, @RequestParam User TechPerson, Principal principal) {
        boolean survey = surveyService.updateStatus(id,status,TechPerson);
        if(survey){
            redirectAttributes.addFlashAttribute("message", "Survey status updated to " + status);
            redirectAttributes.addFlashAttribute("messageType", "success");
            ActivityType type = status.getStatus().equals("Succeeded") ? ActivityType.SUCCEEDED_SURVEY : ActivityType.FAILED_SURVEY;
            activityHelper.saveActivity(type,principal);
        }
        else{
            redirectAttributes.addFlashAttribute("message", "Survey status not updated to " + status);
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/technical/survey/allSurvey";
    }

}
