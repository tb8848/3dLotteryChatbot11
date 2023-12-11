package com.utils;

public class TestCodeCreateUtils {


    public static void main(String[] args){

//        Set<String> codesSet = CodeCreateUtils.createDoubleRepeatCodes(4,10);
//        for(String c :codesSet){
//            System.out.println(c);
//        }

        String txtContent = "    1234,3456,7890         8899".trim();
        String[] codesArr = txtContent.split(",|\\s+ ");
        for(String c : codesArr){
            System.out.println(c);
        }

    }

}
