package com.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static boolean isNull(String str) {
        if (str == null || str.trim().length() == 0 || "undefined".equals(str) || "null".equals(str)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNotNull(String str) {
        return !isNull(str);
    }



    public static boolean checkCodeFormat(String codeDesc){
        String reg1 = "[0-9]{1,2}拖[0-9]{2,9}"; //脱码
        String reg2 = "([0-9]([.,，-])?){1,}";
        String reg3 = "(百[0-9]{1,10})?(十[0-9]{1,10})?(个[0-9]{1,10})?";
        String reg5 = "([0-9](\\.|,|，)?){1,}";
        Pattern pattern = Pattern.compile(reg1);
        Matcher ma = pattern.matcher(codeDesc);
        if(ma.matches()){
//            System.out.println(String.format("%s>>满足规则1>>>%s",codeDesc,reg1));
            return true;
        }
        pattern = Pattern.compile(reg2);
        ma = pattern.matcher(codeDesc);
        if(ma.matches()){
//            System.out.println(String.format("%s>>满足规则2>>>%s",codeDesc,reg2));
            return true;
        }
        pattern = Pattern.compile(reg3);
        ma = pattern.matcher(codeDesc);
        if(ma.matches()){
//            System.out.println(String.format("%s>>满足规则3>>>%s",codeDesc,reg3));
            return true;
        }

        pattern = Pattern.compile(reg5);
        ma = pattern.matcher(codeDesc);
        if(ma.matches()){
//            System.out.println(String.format("%s>>满足规则3>>>%s",codeDesc,reg3));
            return true;
        }

        return false;
    }


    public static String changeDigitToChinese(int dig,String unit) {
        switch (dig) {
            case 1:
                return "一" + unit;
            case 2:
                return "两" + unit;
            case 3:
                return "三" + unit;
            case 4:
                return "四" + unit;
            case 5:
                return "五" + unit;
            case 6:
                return "六" + unit;
            case 7:
                return "七" + unit;
            case 8:
                return "八" + unit;
            case 9:
                return "九" + unit;
            case 0:
                return "零" + unit;
            case 10:
                return "全包";
            default:
                return "";
        }
    }

    //判断字符串是否有相同字符（true：有相同字符）
    public static boolean hasDuplicateChar(String str) {
        Set<Character> charSet = new HashSet<>();
        for (char c : str.toCharArray()) {
            if (!charSet.add(c)) {
                return true;
            }
        }
        return false;
    }



}
