package org.employee.surverythymeleaf.controller;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.repository.ApplicationRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.ActivityHelper;
import org.employee.surverythymeleaf.util.SortUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static org.employee.surverythymeleaf.controller.ApplicationController.getAllApplication;
import static org.employee.surverythymeleaf.util.SortUtils.getAllSurveysDouble;

@Controller
@RequestMapping("/sale")
public class SurveyController {

    private final SurveyService surveyService;
    private final UserService userService;
    private final ApplicationService applicationService;
    private final ActivityHelper activityHelper;

    public SurveyController(SurveyService surveyService, UserService userService, ApplicationRepository applicationRepository, ApplicationService applicationService, ActivityHelper activityHelper) {
        this.surveyService = surveyService;
        this.userService = userService;
        this.applicationService = applicationService;
        this.activityHelper = activityHelper;
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
    public String addSurvey(@Valid @ModelAttribute("survey") Survey survey, BindingResult result, Model model, RedirectAttributes redirectAttributes, Principal principal) {
        survey.setRequestDate(LocalDate.now());
        Survey latestSurvey = surveyService.searchLatestSurvey();
        int newNumber = 2131;
        if(result.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Failed to add new survey, please fill all fields");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/sale/survey/allSurvey";
        }
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
            activityHelper.saveActivity(ActivityType.CREATE_SURVEY,principal);
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
        SurveyStatus[] allSurveyStatus = SurveyStatus.values();
        return getAllSurveysDouble(model, query, page, size, status, fromDate, toDate, surveyService,survey, app,sort,sortField,sortDir, allSurveyStatus);
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
                                     @RequestParam(required = false,defaultValue = "id") String sortField,
                                     @RequestParam(required = false,defaultValue = "desc") String sortDir,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate){
        Page<Application> applicationPage;
        ApplicationStatus applicationStatus = null;
        Sort sort = SortUtils.sortFunction(sortField, sortDir);
        if( status != null && !status.trim().isEmpty() ){
            applicationStatus = ApplicationStatus.valueOf(status);
        }
        if(query != null && !query.isEmpty() || (fromDate != null && toDate != null) || status != null){
            applicationPage = applicationService.searchApplicationWithQuery(query,page,size,applicationStatus,fromDate,toDate, sort);
        }else{
            applicationPage = applicationService.getAllApplicationPaginated(page,size, sort);
        }

        model.addAttribute("applicationWithNonePendingStatus", ApplicationStatus.getNonPendingApplicationStatus());
        model.addAttribute("applicationWithPendingStatus",ApplicationStatus.values());
        ApplicationStatus[] allApplicationStatus = ApplicationStatus.values();
        return getAllApplication(model, query, page, size, applicationPage,applicationStatus,fromDate,toDate,sortField,sortDir, allApplicationStatus);
    }

    @PostMapping("/application/add")
    public String addApplication(@ModelAttribute("application") Application application, @ModelAttribute("surveyId") String surveyId, Principal principal) {
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
        activityHelper.saveActivity(ActivityType.CREATE_APPLICATION,principal);
        return "redirect:/sale/survey/allSurvey";
    }


    @GetMapping("/application/export")
    public void exportApplications(HttpServletResponse response,
                                   @RequestParam(required = false) String query,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate,
                                   @RequestParam(required = false, defaultValue = "id") String sortField,
                                   @RequestParam(required = false, defaultValue = "desc") String sortDir,
                                   @RequestParam() String type,
                                   Principal principal) throws IOException {

        ApplicationController.applicationExport(response, query, status, fromDate, toDate, sortField, sortDir, type, principal, applicationService, activityHelper);
    }

    @GetMapping("/survey/export")
    public void exportSurveys(HttpServletResponse response,
                              @RequestParam(required = false) String query,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate,
                              @RequestParam(required = false, defaultValue = "id") String sortField,
                              @RequestParam(required = false, defaultValue = "desc") String sortDir,
                              @RequestParam() String type,
                              Principal principal) throws IOException {

        surveyExport(response, query, status, fromDate, toDate, sortField, sortDir, type, principal, surveyService, activityHelper);
    }

    static void surveyExport(HttpServletResponse response, @RequestParam(required = false) String query, @RequestParam(required = false) String status, @DateTimeFormat(pattern = "MM/dd/yyyy") @RequestParam(required = false) LocalDate fromDate, @DateTimeFormat(pattern = "MM/dd/yyyy") @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false, defaultValue = "id") String sortField, @RequestParam(required = false, defaultValue = "desc") String sortDir, @RequestParam String type, Principal principal, SurveyService surveyService, ActivityHelper activityHelper) throws IOException {
        SurveyStatus surveyStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            surveyStatus = SurveyStatus.valueOf(status);
        }

        Sort sort = SortUtils.sortFunction(sortField, sortDir);
        List<Survey> surveys = surveyService.filterSurveys(query, surveyStatus, fromDate, toDate, sort);

        if (Objects.equals(type, "csv")) {
            response.setContentType("text/csv");
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setHeader("Content-Disposition", "attachment; filename=surveys_" + now + ".csv");

            PrintWriter writer = response.getWriter();
            writer.println("Exported on:," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("Survey ID,Customer Name,Phone Number,Request Date,Status,State,Township,Sale Person,Technical Person,Latitude,Longitude,Application ID");

            for (Survey survey : surveys) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        survey.getGeneratedSurveyId(),
                        survey.getCustomerName(),
                        survey.getPhoneNumber(),
                        survey.getRequestDate(),
                        survey.getStatus().getStatus(),
                        survey.getState() != null ? survey.getState() : "",
                        survey.getTownShip() != null ? survey.getTownShip() : "",
                        survey.getSalePerson() != null ? survey.getSalePerson().getFullName() : "",
                        survey.getTechnicalPerson() != null ? survey.getTechnicalPerson().getFullName() : "",
                        survey.getLatitude() != null ? survey.getLatitude() : "",
                        survey.getLongitude() != null ? survey.getLongitude() : "",
                        survey.getApplication() != null ? survey.getApplication().getGeneratedApplicationId() : "No Application");
            }
            activityHelper.saveActivity(ActivityType.EXPORT_SURVEY, principal);
            writer.flush();
            writer.close();
        } else if (Objects.equals(type, "excel")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setHeader("Content-Disposition", "attachment; filename=surveys_" + now + ".xlsx");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Surveys");

            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowNum = 0;

            Row metaRow = sheet.createRow(rowNum++);
            metaRow.createCell(0).setCellValue("Exported on:");
            metaRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            rowNum++;

            Row header = sheet.createRow(rowNum++);
            String[] columns = {"Survey ID", "Customer Name", "Phone Number", "Request Date", "Status", "State", "Township", "Sale Person", "Technical Person", "Latitude", "Longitude", "Application ID"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Survey survey : surveys) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(survey.getGeneratedSurveyId());
                row.createCell(1).setCellValue(survey.getCustomerName());
                row.createCell(2).setCellValue(survey.getPhoneNumber());
                row.createCell(3).setCellValue(survey.getRequestDate().toString());
                row.createCell(4).setCellValue(survey.getStatus().getStatus());
                row.createCell(5).setCellValue(survey.getState() != null ? survey.getState() : "");
                row.createCell(6).setCellValue(survey.getTownShip() != null ? survey.getTownShip() : "");
                row.createCell(7).setCellValue(survey.getSalePerson() != null ? survey.getSalePerson().getFullName() : "");
                row.createCell(8).setCellValue(survey.getTechnicalPerson() != null ? survey.getTechnicalPerson().getFullName() : "");
                row.createCell(9).setCellValue(survey.getLatitude() != null ? survey.getLatitude().toString() : "");
                row.createCell(10).setCellValue(survey.getLongitude() != null ? survey.getLongitude().toString() : "");
                row.createCell(11).setCellValue(survey.getApplication() != null ? survey.getApplication().getGeneratedApplicationId() : "No Application");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            activityHelper.saveActivity(ActivityType.EXPORT_SURVEY, principal);
            workbook.write(response.getOutputStream());
            workbook.close();
        }
    }
}
