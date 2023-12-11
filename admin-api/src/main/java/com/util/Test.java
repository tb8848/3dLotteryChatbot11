package com.util;

import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
class TA {
    private String name;

    public TA(String name) {
        this.name = name;
    }
}

public class Test {

    public static void main(String[] args) {
        List<String> list = Lists.newArrayList();
        list.add("hh01");
        list.add("tt02");
        list.add("h03");
        list.add("t03");
        list.add("h02");
        list.add("t01");


        list.stream().sorted().forEach(System.out::println);

        List<TA> tas = Arrays.asList(new TA("hh01"), new TA("tt02"),
                new TA("h03"), new TA("t03"), new TA("h02"), new TA("t01"));
        List<TA> tas1 = tas.stream().sorted(Comparator.comparing(TA::getName)).collect(Collectors.toList());
        for (TA a : tas1) {
            System.out.println(a);
        }
    }

//    public static void main(String[] args) {
//        //get1DDraw("761X", "761543", "口口XX");
//
//        String s = "761543";
//
//        int count = 0;
//        String s2 = "153";
//        char[] c = s2.toCharArray();
//        for (char c2 : c) {
//            if (s.contains(String.valueOf(c2))) {
//                count ++;
//            }
//        }
//        System.out.println(c.length);
//        System.out.println(count);
//    }
//
//    public static Integer get1DDraw(String buyNumber, String drawNumber, String bettingRule) {
//        int index = 0;
//        List<Integer> siteList = Lists.newArrayList();
//        while ((index = bettingRule.indexOf("口", index)) != -1) {
//            siteList.add(index);
//            index += "口".length();
//        }
//
//        int c = 0;
//        for (int i : siteList) {
//            System.out.println(i);
//            if (String.valueOf(buyNumber.charAt(i)).equals(String.valueOf(drawNumber.charAt(i)))) {
//                c ++;
//            }
//        }
//
//        System.out.println(c);
//
//        return 0;
//    }


    /*public static void main(String[] args) throws IOException, ParseException {
        //get1DDraw("761X", "761543", "口口XX");
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        System.out.println(dayOfWeek.getValue());

        LocalDate parse1 = LocalDate.parse("2022-12-01");
        DayOfWeek day = parse1.getDayOfWeek();
        System.out.println(Int2ChineseNumUtil.int2chineseNum(day.getValue()));

        *//*String path = "D:\\test\\test.txt";
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path,true)));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 22; i ++) {
            String s = "24X" + i;
            if (i % 10 == 0) {
                out.newLine();
            }
            out.write(s+" ");
        }

        out.close();*//*


        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now2 = LocalDateTime.now();
        String dateTime = dateTimeFormatter.format(now2);
        System.out.println(dateTime);

        *//*SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
        String date = "2022-12-10";
        Date d = sdf.parse(date);
        System.out.println(sdf.format(d));*//*

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse("2022-12-10", fmt);
        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("yyyy年MM月");
        String s = localDate.format(fmt2);
        System.out.println(s);


        String s2 = "2022-12-12 20:00:00";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = df.parse(s2);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        calendar.add(Calendar.HOUR_OF_DAY, 8);
        calendar.add(Calendar.MINUTE, 20);
        System.out.println("时间：" + df.format(calendar.getTime()));
    }*/

        public static Integer get1DDraw(String buyNumber, String drawNumber, String bettingRule) {
            int index = 0;
            List<Integer> siteList = Lists.newArrayList();
            while ((index = bettingRule.indexOf("口", index)) != -1) {
                siteList.add(index);
                index += "口".length();
            }

            int c = 0;
            for (int i : siteList) {
                System.out.println(i);
                if (String.valueOf(buyNumber.charAt(i)).equals(String.valueOf(drawNumber.charAt(i)))) {
                    c ++;
                }
            }

            System.out.println(c);

            return 0;
        }
}
