package com.util;

import cn.hutool.core.date.DateUtil;

import java.security.SecureRandom;

public class PrintUtils {


    public static String createPrintNo(String uid){
        SecureRandom random = new SecureRandom();
        String times = DateUtil.format(DateUtil.date(),"yyMMddHHmmss");
//        System.out.println("times="+times);
        String prefix = String.valueOf(random.nextInt(999));
        if(prefix.length()<3){
            for(int i=0,len=3-prefix.length();i<len;i++){
                prefix = prefix+"0";
            }
        }
//        System.out.println("prefix="+prefix);
        String subfix = String.valueOf(random.nextInt(99999));
//        System.out.println("subfix="+subfix);
        if(subfix.length()<5){
            for(int i=0,len=5-subfix.length();i<len;i++){
                subfix = "0"+subfix;
            }
        }
        return prefix+times+subfix;
    }


    public static String createPrintNo(){
        SecureRandom random = new SecureRandom();
        String times = DateUtil.format(DateUtil.date(),"yyMMdd");
//        System.out.println("times="+times);
        String subfix = String.valueOf(random.nextInt(9999));
//        System.out.println("subfix="+subfix);
        return times+subfix;
    }


    public static String createPrintNo(Integer maxPrintCount){
        if(null == maxPrintCount){
            maxPrintCount=1;
        }else{
            maxPrintCount++;
        }
        if(maxPrintCount>99999){
            maxPrintCount = 1;
        }
        String str = String.valueOf(maxPrintCount);
        if(str.length()<5){
            for(int i=0,len=5-str.length();i<len;i++){
                str = "0"+str;
            }
        }
        String times = DateUtil.format(DateUtil.date(),"yyMMdd");
        return times+str;
    }


    public static String createPrintNo(Integer drawNo,Integer maxPrintCount){
        if(null == maxPrintCount){
            maxPrintCount=1;
        }else{
            maxPrintCount++;
        }
        if(maxPrintCount>9999999){
            maxPrintCount = 1;
        }
        String str = String.valueOf(maxPrintCount);
        if(str.length()<7){
            for(int i=0,len=7-str.length();i<len;i++){
                str = "0"+str;
            }
        }
        return drawNo+str;
    }
}
