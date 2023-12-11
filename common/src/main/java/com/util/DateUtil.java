package com.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {

    /**
     * 获取最近几个月数据
     * @param month
     * @return
     */
    public static List<String> getLastMonth(Integer month) {
        List<String> monthList = new ArrayList<>();
        for (int i = 0; i < month; i ++) {
            LocalDate end = LocalDate.now();
            LocalDate start =  end.minusMonths(i);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy年MM月");
            String yearMonth = start.format(fmt);
            monthList.add(yearMonth);
        }
        return monthList;
    }
}
