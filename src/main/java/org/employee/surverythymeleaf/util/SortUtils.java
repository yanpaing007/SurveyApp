package org.employee.surverythymeleaf.util;

import org.employee.surverythymeleaf.model.Application;
import org.employee.surverythymeleaf.model.Survey;
import org.employee.surverythymeleaf.model.SurveyStatus;
import org.employee.surverythymeleaf.service.SurveyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;


public class SortUtils {
    public static Sort sortFunction(String sortField, String sortDir){
//        List<String> allowedList= List.of("id","fullName","email","phoneNumber");
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
                                             String sortDir) {
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
        List<SurveyStatus> surveyWithoutPending = SurveyStatus.getNonPendingSurveyStatus();

        return getString(model, query, page, size, surveyPage,surveyStatus,fromDate,toDate,app,survey,SurveyStatus.values(),surveyWithoutPending,sortField,sortDir);
    }

    static String getString(Model model,
                            String query,
                            int page,
                            int size,
                            Page<Survey> surveyPage,
                            SurveyStatus status,
                            LocalDate fromDate,
                            LocalDate toDate,
                            Application app,
                            Survey survey,
                            SurveyStatus[] allSurveyStatus,
                            List<SurveyStatus> surveyWithoutPending,
                            String sortField,
                            String sortDir

                            ) {
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
        model.addAttribute("surveyWithNonePendingStatus",surveyWithoutPending);
        model.addAttribute("surveyWithPendingStatus",allSurveyStatus);
        model.addAttribute("sortField",sortField);
        model.addAttribute("sortDir",sortDir);
        return "survey/allSurveys";
    }

}


