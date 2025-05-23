package org.employee.surverythymeleaf.service;

import org.employee.surverythymeleaf.model.Role;
import org.employee.surverythymeleaf.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findByName(String name) {
        return roleRepository.findByRoleName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }
}
