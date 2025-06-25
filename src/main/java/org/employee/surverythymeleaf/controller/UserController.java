package org.employee.surverythymeleaf.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.RoleService;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;


@Controller
@RequestMapping("/admin")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/create")
    public String getUserForm(Model model){
        model.addAttribute("user",new User());
        model.addAttribute("role", roleService.findAll());
        return "user/createUser";
    }
    
    @PostMapping("/create")
    public String createuser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes){
        userService.createuser(user);
        redirectAttributes.addFlashAttribute("message", "User created successfully");
        return "redirect:/admin/users";
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
        List<String> allowedList= List.of("id","fullName","email","phoneNumber");
        if(!allowedList.contains(sortField) || sortDir.isEmpty()){
            sortField="id";
        }
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
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
    public String updateUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes){

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
        }
        return "redirect:/admin/user/edit/" + user.getId();
    }

    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes){
        User exitingUser = userService.getUserById(id);
        String fullName = exitingUser.getFullName();
        userService.deleteUserById(id);
        redirectAttributes.addFlashAttribute("message", "User["+ fullName +"] was deleted successfully");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/export")
    public void exportUsers(HttpServletResponse response,
                            @RequestParam(required = false) String query,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false, defaultValue = "id") String sortField,
                            @RequestParam(required = false, defaultValue = "desc") String sortDir,
                            @RequestParam(required = false) Boolean status,
                            @RequestParam(required = true) String type
    ) throws IOException {
       if(Objects.equals(type, "csv")){
           response.setContentType("text/csv");
           String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
           response.setHeader("Content-Disposition", "attachment; filename=users_" + now + ".csv");

           List<String> allowedList= List.of("id","fullName","email","phoneNumber");
           if(!allowedList.contains(sortField) || sortDir.isEmpty()){
               sortField="id";
           }

           Sort sort=Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,sortField);

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
                       user.isEnabled() ? "Active" : "Not Active" );
           }
           writer.flush();
           writer.close();
       }
       else if(Objects.equals(type, "excel")){

           response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
           String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
           response.setHeader("Content-Disposition", "attachment; filename=users_" + now + ".xlsx");


           List<String> allowedList = List.of("id", "fullName", "email", "phoneNumber");
           if (!allowedList.contains(sortField) || sortDir == null || sortDir.isEmpty()) {
               sortField = "id";
           }

           Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
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
               row.createCell(5).setCellValue(user.isEnabled() ? "Active" : "Not Active");
           }

           for (int i = 0; i < columns.length; i++) {
               sheet.autoSizeColumn(i);
           }

           workbook.write(response.getOutputStream());
           workbook.close();
       }

        }

}



