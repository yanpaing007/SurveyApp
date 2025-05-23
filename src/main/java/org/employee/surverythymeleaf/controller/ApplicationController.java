package org.employee.surverythymeleaf.controller;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/technical")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final SurveyService surveyService;

    public ApplicationController(ApplicationService applicationService, SurveyService surveyService) {
        this.applicationService = applicationService;
        this.surveyService = surveyService;
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

        model.addAttribute("applicationStatus", ApplicationStatus.values());
        model.addAttribute("applications",applicationPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",applicationPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",applicationPage.getTotalPages());
        return "application/allApplication";
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

    @GetMapping("survey/edit/{id}")
    public String getEditSurveyForm(Model model, @PathVariable Long id) {
        Survey survey=surveyService.findSurveyById(id);
        model.addAttribute("survey", survey);
        return "survey/editSurveyForm";
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
}
