package org.employee.surverythymeleaf.service;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public void createuser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
    }

    public Page<User> getAllUserPagniated(int page, int size) {
        Pageable pageable =PageRequest.of(page, size);
        Page<User> userList = userRepo.findAll(pageable);
        System.out.println("userList Count: " + userList.stream().map(User::getId).count());
        System.out.println("userList Name: " + userList.stream().map(User::getFullName).toList());
        return userList;
    }

    public List<User> getAllUsers(){
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public void updateUser(User user) {
        userRepo.save(user);
    }

    public void deleteUserById(Long id) {
        userRepo.deleteById(id);
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true);
        user.setRole(roleService.findByName("Member"));
        userRepo.save(user);
    }

    public User findByEmail(String email) {
         User user = userRepo.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Email not found")
                );
        System.out.println("user found: " + user);
        return user;
    }

    public Page<User> searchUser(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepo.searchUser(query,pageable);
    }


    public List<User> searchAllUser(String query) {
        return userRepo.searchAllUser(query);
    }
}
