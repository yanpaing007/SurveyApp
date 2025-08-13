package org.employee.surverythymeleaf.service;


import org.employee.surverythymeleaf.model.Team;
import org.employee.surverythymeleaf.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }
    public List<Team> getAllAvailableTeams() {
        return teamRepository.findByActiveIsTrue();
    }
}
