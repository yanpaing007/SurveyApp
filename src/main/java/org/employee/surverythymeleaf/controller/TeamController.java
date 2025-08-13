package org.employee.surverythymeleaf.controller;


import org.employee.surverythymeleaf.model.Team;
import org.employee.surverythymeleaf.service.TeamService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

}
