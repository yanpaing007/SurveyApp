package org.employee.surverythymeleaf.repository;

import org.employee.surverythymeleaf.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByIdDesc(Pageable pageable);


    @Query("""
          select a.actor,count(a) from ActivityLog a group by a.actor order by count(a) desc
""")
    List<Object[]> findTopActivityUser(Pageable pageable);


}
