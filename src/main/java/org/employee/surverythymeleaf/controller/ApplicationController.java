package org.employee.surverythymeleaf.controller;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.employee.surverythymeleaf.model.*;
import org.employee.surverythymeleaf.model.Enum.ActivityType;
import org.employee.surverythymeleaf.model.Enum.ApplicationStatus;
import org.employee.surverythymeleaf.model.Enum.SurveyStatus;
import org.employee.surverythymeleaf.repository.SurveyRepository;
import org.employee.surverythymeleaf.service.ApplicationService;
import org.employee.surverythymeleaf.service.SurveyService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.ActivityHelper;
import org.employee.surverythymeleaf.util.SortUtils;
import org.employee.surverythymeleaf.util.StatusValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final SurveyRepository surveyRepository;

    public ApplicationController(ApplicationService applicationService, SurveyService surveyService , UserService userService, ActivityHelper activityHelper, SurveyRepository surveyRepository) {
        this.applicationService = applicationService;
        this.surveyService = surveyService;
        this.userService = userService;
        this.activityHelper = activityHelper;
        this.surveyRepository = surveyRepository;
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
            validStatusMap.put(application.getId(), StatusValidator.getNextValidApplicationStatuses(application.getApplicationStatus()));
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
    public String getDetailSurveyForm(Model model, @PathVariable String id,RedirectAttributes redirectAttributes) {
        try {
            Survey survey=surveyService.findSurveyByGeneratedSurveyId(id);
            List<SurveyStatus> statuses = StatusValidator.getNextValidSurveyStatuses(survey.getStatus());
            model.addAttribute("survey", survey);
            model.addAttribute("statuses", statuses);
            return "survey/surveyDetailsCopy";
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("message",e.getMessage());
            redirectAttributes.addFlashAttribute("messageType","warning");
            return "redirect:/technical/survey/allSurvey";
        }
    }

    @PostMapping("/survey/edit/{generatedSurveyId}")
    public String editSurveyForm(Model model, @PathVariable String generatedSurveyId,RedirectAttributes redirectAttributes,@ModelAttribute Survey survey,Principal principal) {
        try {
            Survey findSurvey=surveyService.findSurveyByGeneratedSurveyId(generatedSurveyId);
            User currentUser = userService.findByEmail(principal.getName());
            if(findSurvey!=null){
                findSurvey.setGeneratedSurveyId(survey.getGeneratedSurveyId());
                findSurvey.setCustomerName(survey.getCustomerName());
                findSurvey.setState(survey.getState());
                findSurvey.setTownShip(survey.getTownShip());
                findSurvey.setLongitude(survey.getLongitude());
                findSurvey.setLatitude(survey.getLatitude());
                findSurvey.setPhoneNumber(survey.getPhoneNumber());
                findSurvey.setStatus(survey.getStatus());
                findSurvey.setTechnicalPerson(currentUser);
                findSurvey.setUpdatedAt(LocalDateTime.now());

                surveyRepository.save(findSurvey);

                redirectAttributes.addFlashAttribute("messageType","success");
                redirectAttributes.addFlashAttribute("message","Survey updated successfully");
            }
            else{
                redirectAttributes.addFlashAttribute("message","Survey not found");
                redirectAttributes.addFlashAttribute("messageType","warning");
            }
            return "redirect:/technical/survey/allSurvey";
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("message",e.getMessage());
            redirectAttributes.addFlashAttribute("messageType","warning");
            return "redirect:/technical/survey/allSurvey";
        }
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
            redirectAttributes.addFlashAttribute("message", "Invalid transition:couldn't update from current state to " + status);
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/technical/survey/allSurvey";
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

        applicationExport(response, query, status, fromDate, toDate, sortField, sortDir, type, principal, applicationService, activityHelper);
    }

    static void applicationExport(HttpServletResponse response, @RequestParam(required = false) String query, @RequestParam(required = false) String status, @DateTimeFormat(pattern = "MM/dd/yyyy") @RequestParam(required = false) LocalDate fromDate, @DateTimeFormat(pattern = "MM/dd/yyyy") @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false, defaultValue = "id") String sortField, @RequestParam(required = false, defaultValue = "desc") String sortDir, @RequestParam String type, Principal principal, ApplicationService applicationService, ActivityHelper activityHelper) throws IOException {
        ApplicationStatus applicationStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            applicationStatus = ApplicationStatus.valueOf(status);
        }

        Sort sort = SortUtils.sortFunction(sortField, sortDir);
        List<Application> applications = applicationService.filterApplications(query, applicationStatus, fromDate, toDate, sort);

        if (Objects.equals(type, "csv")) {
            response.setContentType("text/csv");
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setHeader("Content-Disposition", "attachment; filename=applications_" + now + ".csv");

            PrintWriter writer = response.getWriter();
            writer.println("Exported on:," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("Application ID,Survey ID,Customer Name,Company Name,Phone Number,Contact Email,Address,Application Date,Status,Submitted By,Latitude,Longitude,Comment");

            for (Application app : applications) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        app.getGeneratedApplicationId(),
                        app.getSurvey() != null ? app.getSurvey().getGeneratedSurveyId() : "",
                        app.getCustomerName(),
                        app.getCompanyName() != null ? app.getCompanyName() : "",
                        app.getPhoneNumber(),
                        app.getContactEmail() != null ? app.getContactEmail() : "",
                        app.getAddress() != null ? app.getAddress() : "",
                        app.getApplicationDate() != null ? app.getApplicationDate() : "",
                        app.getApplicationStatus().getStatus(),
                        app.getSubmittedBy() != null ? app.getSubmittedBy().getFullName() : "",
                        app.getLatitude() != null ? app.getLatitude() : "",
                        app.getLongitude() != null ? app.getLongitude() : "",
                        app.getComment() != null ? app.getComment().replace("\n", " ").replace(",", ";") : "");
            }
            activityHelper.saveActivity(ActivityType.EXPORT_APPLICATION, principal);
            writer.flush();
            writer.close();
        } else if (Objects.equals(type, "excel")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setHeader("Content-Disposition", "attachment; filename=applications_" + now + ".xlsx");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Applications");

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
            String[] columns = {"Application ID", "Survey ID", "Customer Name", "Company Name", "Phone Number", "Contact Email", "Address", "Application Date", "Status", "Submitted By", "Latitude", "Longitude", "Comment"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Application app : applications) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(app.getGeneratedApplicationId());
                row.createCell(1).setCellValue(app.getSurvey() != null ? app.getSurvey().getGeneratedSurveyId() : "");
                row.createCell(2).setCellValue(app.getCustomerName());
                row.createCell(3).setCellValue(app.getCompanyName() != null ? app.getCompanyName() : "");
                row.createCell(4).setCellValue(app.getPhoneNumber());
                row.createCell(5).setCellValue(app.getContactEmail() != null ? app.getContactEmail() : "");
                row.createCell(6).setCellValue(app.getAddress() != null ? app.getAddress() : "");
                row.createCell(7).setCellValue(app.getApplicationDate() != null ? app.getApplicationDate().toString() : "");
                row.createCell(8).setCellValue(app.getApplicationStatus().getStatus());
                row.createCell(9).setCellValue(app.getSubmittedBy() != null ? app.getSubmittedBy().getFullName() : "");
                row.createCell(10).setCellValue(app.getLatitude() != null ? app.getLatitude().toString() : "");
                row.createCell(11).setCellValue(app.getLongitude() != null ? app.getLongitude().toString() : "");
                row.createCell(12).setCellValue(app.getComment() != null ? app.getComment() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            activityHelper.saveActivity(ActivityType.EXPORT_APPLICATION, principal);
            workbook.write(response.getOutputStream());
            workbook.close();
        }
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

        SurveyController.surveyExport(response, query, status, fromDate, toDate, sortField, sortDir, type, principal, surveyService, activityHelper);
    }

}
