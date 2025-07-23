package org.employee.surverythymeleaf.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.employee.surverythymeleaf.Configuration.GlobalControllerAdvice;
import org.employee.surverythymeleaf.model.ActivityType;
import org.employee.surverythymeleaf.model.Role;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.ActivityLogService;
import org.employee.surverythymeleaf.service.RoleService;
import org.employee.surverythymeleaf.service.UserService;
import org.employee.surverythymeleaf.util.ActivityHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import static org.employee.surverythymeleaf.util.SortUtils.sortFunction;


@Controller
@RequestMapping("/admin")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    private final GlobalControllerAdvice globalControllerAdvice;
    private final ActivityLogService activityLogService;
    private final ActivityHelper activityHelper;

    public UserController(UserService userService, RoleService roleService, GlobalControllerAdvice globalControllerAdvice, ActivityLogService activityLogService, ActivityHelper activityHelper) {
        this.userService = userService;
        this.roleService = roleService;
        this.globalControllerAdvice = globalControllerAdvice;
        this.activityLogService = activityLogService;
        this.activityHelper = activityHelper;
    }

//    @GetMapping("/create")
//    public String getUserForm(Model model){
//        model.addAttribute("new_user_obj",new User());
//        model.addAttribute("role", roleService.findAll());
//        return "user/allUsers";
//    }
    
    @PostMapping("/create")
    public String createuser(@Valid @ModelAttribute("new_user_obj") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             @ModelAttribute("roles") List<Role> roles,
                             Model model) {
        if(result.hasErrors()) {
            model.addAttribute("roles", roles);
            return "fragments/modal/addUser :: newUser";
        }
        userService.createuser(user);
        redirectAttributes.addFlashAttribute("message", "User created successfully");
        activityHelper.saveActivity(ActivityType.CREATE_USER, principal);
        return "user/allUsers";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model,@RequestParam(required = false) String query,
                              @RequestParam(required = false,defaultValue = "0") int page,
                              @RequestParam(required = false,defaultValue = "9") int size,
                              @RequestParam(required = false) String role,
                              @RequestParam(required = false, defaultValue = "id") String sortField,
                              @RequestParam(required = false, defaultValue = "desc") String sortDir,
                              @RequestParam(required = false) Boolean status
                              ){
       Sort sort = sortFunction(sortField, sortDir);
        Page<User> userPage;
        if((query != null && !query.isEmpty()) || (role != null && !role.isEmpty()) || (status != null)){
            userPage = userService.searchUser(query,page,size,role,status,sort);
        }else{
            userPage = userService.getAllUserPagniated(page,size,sort);
        }

        model.addAttribute("users",userPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",userPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",userPage.getTotalPages());
        model.addAttribute("role", roleService.findAll());
        model.addAttribute("selectedRole",role);
        model.addAttribute("selectedStatus",status);
        model.addAttribute("sortField",sortField);
        model.addAttribute("sortDir",sortDir);
        model.addAttribute("new_user_obj",new User());
        model.addAttribute("reverseSortDir",sortDir.equals("asc") ? "desc" : "asc");
        return "user/allUsers";
    }


    @GetMapping("/user/edit/{id}")
    public String getEditUserForm(@PathVariable("id") Long id , Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user",user);
        model.addAttribute("role", roleService.findAll());
        return "user/editUser";
    }

    @PostMapping("/user/edit/{id}")
    public String updateUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes, Principal principal){

        String rawPassword = user.getPassword();
        if(rawPassword != null && !rawPassword.isEmpty()){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(rawPassword);
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
        }else {
            User exitingUser = userService.getUserById(user.getId());
            user.setPassword(exitingUser.getPassword());
            user.setUpdatedAt(LocalDateTime.now());
        }

        boolean check = userService.updateUser(user);
        if(check){
            redirectAttributes.addFlashAttribute("message", "User updated successfully");
            redirectAttributes.addFlashAttribute("messageType", "success");
            activityHelper.saveActivity(ActivityType.UPDATE_USER,principal);
        }
        return "redirect:/admin/user/edit/" + user.getId();
    }

    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal){
        User exitingUser = userService.getUserById(id);
        String fullName = exitingUser.getFullName();
        userService.deleteUserById(id);
        redirectAttributes.addFlashAttribute("message", "User["+ fullName +"] was deleted successfully");
        redirectAttributes.addFlashAttribute("messageType", "success");
        activityHelper.saveActivity(ActivityType.DELETE_USER,principal);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/export")
    public void exportUsers(HttpServletResponse response,
                            @RequestParam(required = false) String query,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false, defaultValue = "id") String sortField,
                            @RequestParam(required = false, defaultValue = "desc") String sortDir,
                            @RequestParam(required = false) Boolean status,
                            @RequestParam() String type,
                            Principal principal) throws IOException {

        if(Objects.equals(type, "csv")){
            response.setContentType("text/csv");
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setHeader("Content-Disposition", "attachment; filename=users_" + now + ".csv");

            Sort sort = sortFunction(sortField, sortDir);
            activityHelper.saveActivity(ActivityType.EXPORT_USER,principal);

            List<User> users = userService.filterUsers(query,role,status,sort);

            PrintWriter writer = response.getWriter();
            writer.println("Exported on:," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("ID,Full Name,Email Address,Phone Number,Role,Status");

            for(User user : users){
                assert user.getRole() != null;
                writer.printf("%d,%s,%s,%s,%s,%s%n",
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole().getRoleName(),
                        user.isStatus() ? "Active" : "Not Active" );
            }
            writer.flush();
            writer.close();
        }
        else if(Objects.equals(type, "excel")){

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setHeader("Content-Disposition", "attachment; filename=users_" + now + ".xlsx");
            activityHelper.saveActivity(ActivityType.EXPORT_USER,principal);

            Sort sort = sortFunction(sortField, sortDir);
            List<User> users = userService.filterUsers(query, role, status, sort);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Users");

            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowNum = 0;

            Row metaRow = sheet.createRow(rowNum++);
            metaRow.createCell(0).setCellValue("Exported on:");
            metaRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Row header = sheet.createRow(rowNum++);
            String[] columns = {"ID", "Full Name", "Email Address", "Phone Number", "Role", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFullName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getPhoneNumber());
                row.createCell(4).setCellValue(user.getRole().getRoleName());
                row.createCell(5).setCellValue(user.isStatus() ? "Active" : "Not Active");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
            workbook.close();
        }
    }
}



