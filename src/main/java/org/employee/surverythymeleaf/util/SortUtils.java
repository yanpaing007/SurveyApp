package org.employee.surverythymeleaf.util;

import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.service.SurveyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SortUtils {
    public static Sort sortFunction(String sortField, String sortDir){

        if(sortDir.isEmpty()){
            sortField="id";
        }
        return Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
    }


    public static String getAllSurveysDouble(Model model,
                                             String query,
                                             int page,
                                             int size,
                                             String status,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             SurveyService surveyService,
                                             Survey survey,
                                             Application app,
                                             Sort sort,
                                             String sortField,
                                             String sortDir, SurveyStatus[] allSurveyStatus) {
        Page<Survey> surveyPage;
        SurveyStatus surveyStatus = null;

        if( status != null && !status.trim().isEmpty() ){
            surveyStatus = SurveyStatus.valueOf(status);
        }

        if((query != null && !query.isEmpty()) || status != null || (fromDate != null && toDate != null)){
            surveyPage = surveyService.searchSurveyWithQuery(query,page,size, surveyStatus,fromDate,toDate,sort);
        }else{
            surveyPage = surveyService.getAllSurvey(page,size,sort);
        }
        Map<Long, List<SurveyStatus>> validStatusMap = new HashMap<>();

        for(Survey surv : surveyPage.getContent()){
            SurveyStatus currentStatus = surv.getStatus();
            List<SurveyStatus> validStatuses =switch (currentStatus){
                case PENDING -> List.of(SurveyStatus.PENDING,SurveyStatus.SUCCEEDED,SurveyStatus.FAILED );
                case SUCCEEDED -> List.of(SurveyStatus.SUCCEEDED);
                case FAILED -> List.of(SurveyStatus.FAILED);
            };
            validStatusMap.put(surv.getId(),validStatuses);
        }

        model.addAttribute("surveys",surveyPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",surveyPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("currentStatus",status);
        model.addAttribute("totalPages",surveyPage.getTotalPages());
        model.addAttribute("fromDate",fromDate);
        model.addAttribute("toDate",toDate);
        model.addAttribute("app",app);
        model.addAttribute("survey",survey);
        model.addAttribute("validStatusMap",validStatusMap);
        model.addAttribute("sortField",sortField);
        model.addAttribute("sortDir",sortDir);
        model.addAttribute("allSurveyStatus",allSurveyStatus);
        return "survey/allSurveys";
    }

}


