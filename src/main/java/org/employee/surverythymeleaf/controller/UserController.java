package org.employee.surverythymeleaf.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.service.RoleService;
import org.employee.surverythymeleaf.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


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
        return "redirect:/";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model,@RequestParam(required = false) String query,
                              @RequestParam(required = false,defaultValue = "0") int page,
                              @RequestParam(required = false,defaultValue = "6") int size){
        Page<User> userPage;
        if(query != null && !query.isEmpty()){
            userPage = userService.searchUser(query,page,size);
        }else{
            userPage = userService.getAllUserPagniated(page,size);
        }
        model.addAttribute("users",userPage);
        model.addAttribute("query",query);
        model.addAttribute("totalItems",userPage.getTotalElements());
        model.addAttribute("size",size);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",userPage.getTotalPages());
        return "user/allUsers";
    }

    @GetMapping("/edit/{id}")
    public String getEditUserForm(@PathVariable("id") Long id , Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user",user);
        model.addAttribute("role", roleService.findAll());
        return "user/editUser";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes){
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("message", "User updated successfully");
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes){
        userService.deleteUserById(id);
        redirectAttributes.addFlashAttribute("message", "User with id ->"+ id +" was deleted successfully");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/export")
    public void exportUsers(HttpServletResponse response,@RequestParam(required = false) String query) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        List<User> users = userService.searchAllUser(query);

        PrintWriter writer = response.getWriter();
        writer.println("ID,Full Name,Email Address,Phone Number,Role,Status");

        for(User user : users){
            assert user.getRole() != null;
            writer.printf("%d,%s,%s,%s,%s,%s%n",
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole().getRoleName(),
                    user.isEnabled() ? "Active" : "NotActive" );
        }
        writer.flush();
        writer.close();
        }

}



