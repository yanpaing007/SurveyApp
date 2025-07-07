package org.employee.surverythymeleaf.util;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DateCalculator {
    public static Map<String,Long> getCurrentMonthAndYearCounts(BiFunction<Integer,Integer,Long> countByMonthAndYearFn){
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        LocalDate lastMonthDate = today.minusMonths(1);
        int lastYear = lastMonthDate.getYear();
        int lastMonth = lastMonthDate.getMonthValue();

        Long currentMonthSurveyCount = countByMonthAndYearFn.apply(currentMonth,currentYear);
        Long lastMonthSurveyCount = countByMonthAndYearFn.apply(lastMonth,lastYear);

        Map<String,Long> data = new HashMap<>();
        data.put("currentMonth",currentMonthSurveyCount);
        data.put("lastMonth",lastMonthSurveyCount);

        return data;
    }
}
