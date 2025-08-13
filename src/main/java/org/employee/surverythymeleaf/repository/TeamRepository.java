package org.employee.surverythymeleaf.repository;


import org.employee.surverythymeleaf.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {


    @Query("""
select t from Team t left join fetch t.members where t.active=true""")
    public List<Team> findByActiveIsTrue();

    public Team findByName(String name);

    @Query("SELECT t FROM Team t WHERE t.active = true AND " +
            "(:specialization IS NULL OR t.specialization = :specialization)")
    List<Team> findBySpecialization(@Param("specialization") String specialization);

    @Query("SELECT t FROM Team t WHERE t.active = true AND " +
            "SIZE(t.applications) < t.maxCapacity")
    List<Team> findAvailableTeams();

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Team t " +
            "LEFT JOIN FETCH t.members " +
            "WHERE t.active = true")
    Page<Team> findAllWithMembers(Pageable pageable);
}
