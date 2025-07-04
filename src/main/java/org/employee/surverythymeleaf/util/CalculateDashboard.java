package org.employee.surverythymeleaf.util;

public class CalculateDashboard {
    public static Long calculateSuccessRate(Long totalApplication, Long pendingApplication){
        if(totalApplication == 0){
            return 0L;
        }
        double successRate = (double) pendingApplication / totalApplication * 100;
        return Math.round(successRate);
    }
}
