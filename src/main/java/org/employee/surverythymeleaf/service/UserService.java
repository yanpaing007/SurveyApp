package org.employee.surverythymeleaf.service;
import jakarta.persistence.criteria.Predicate;
import org.employee.surverythymeleaf.model.User;
import org.employee.surverythymeleaf.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
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

    public Page<User> getAllUserPagniated(int page, int size, Sort sort) {
        Pageable pageable =PageRequest.of(page, size,sort);
        Page<User> userList = userRepo.findAll(pageable);
        System.out.println("userList Count: " + userList.stream().count());
        System.out.println("userList Name: " + userList.stream().map(User::getFullName).toList());
        return userList;
    }

    public List<User> getAllUsers(){
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
    }

    public boolean updateUser(User user) {
        if (userRepo.existsById(user.getId())) {
            userRepo.save(user);
            return true;
        } else {
            return false;
        }
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
        return userRepo.findByEmail(email)
               .orElseThrow(
                       () -> new UsernameNotFoundException("Email not found")
               );
    }

    public Page<User> searchUser(String query, int page, int size, String role, Boolean status, Sort sort) {
        Pageable pageable = PageRequest.of(page, size,sort);
        return userRepo.searchUser(query,role,status,pageable);
    }

    public List<User> filterUsers(String query, String role, Boolean status, Sort sort) {
        // This reuses the same logic as paginated, but without pagination
        return userRepo.findAll((root, queryObj, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isEmpty()) {
                String likeQuery = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), likeQuery),
                        cb.like(cb.lower(root.get("email")), likeQuery)
                ));
            }

            if (role != null && !role.isEmpty()) {
                predicates.add(cb.equal(root.get("role").get("roleName"), role));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, sort);
    }

}
