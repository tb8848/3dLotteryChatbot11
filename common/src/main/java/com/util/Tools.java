package com.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {


    public static String substractDigit(String text){
        //String str = "love234csdn3423java";
        String regEx = "[^0-9.]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        String result = m.replaceAll("").trim();
        return result;
    }
}
