package org.employee.surverythymeleaf.util;

public class CalculateDashboard {
    public static Long calculateSuccessRate(Long totalApplication, Long completedApplication) {
        if(totalApplication == 0){
            return 0L;
        }
        double successRate = (double) completedApplication / totalApplication * 100;
        return Math.round(successRate);
    }

    public static Long calculatePercentage(Long currentMonthCount, Long lastMonthCount) {
        if(lastMonthCount == 0){
            return 100L;
        }
        else {
            double percentage =  ((double)(currentMonthCount - lastMonthCount)/lastMonthCount) * 100;
            return Math.round(percentage);
        }


    }
}
