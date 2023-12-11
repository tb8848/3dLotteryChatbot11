package com.utils;

import cn.hutool.core.date.DateUtil;

import java.security.SecureRandom;

public class PrintUtils {


    public static String createPrintNo(String uid){
        SecureRandom random = new SecureRandom();
        String times = DateUtil.format(DateUtil.date(),"yyMMddHHss");
        //System.out.println("times="+times);
        String prefix = uid.substring(uid.length()-4,uid.length());
        //System.out.println("prefix="+prefix);
        String subfix = String.valueOf(10000 + random.nextInt(9999));
        //System.out.println("subfix="+subfix);
        return prefix+times+subfix;
    }


    public static String createPrintNo(){
        SecureRandom random = new SecureRandom();
        String times = DateUtil.format(DateUtil.date(),"yyMMdd");
        //System.out.println("times="+times);
        String subfix = String.valueOf(10000 + random.nextInt(9999));
        //System.out.println("subfix="+subfix);
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


    public static String createPrintNo(Integer drawNo,Long maxPrintCount){
        long max = 99999999L;
        int len = 8;
        if(null == maxPrintCount){
            maxPrintCount=0L;
        }else{
            maxPrintCount++;
        }
        if(maxPrintCount>max){
            maxPrintCount = 0L;
        }
        String drawEnd = String.valueOf(drawNo).substring(1);
        String str = String.valueOf(maxPrintCount);
        if(str.length()<len){
            for(int i=0,leftlen=len-str.length();i<leftlen;i++){
                str = "0"+str;
            }
        }
        return drawEnd+str;
    }


    public static String createPrePrintNo(Integer drawNo,Long maxPrintCount){
        long max = 9999999L;
        int len = 7;
        if(null == maxPrintCount){
            maxPrintCount=0L;
        }else{
            maxPrintCount++;
        }
        if(maxPrintCount>max){
            maxPrintCount = 0L;
        }
        String drawEnd = String.valueOf(drawNo).substring(1);
        String str = String.valueOf(maxPrintCount);
        if(str.length()<len){
            for(int i=0,leftlen=len-str.length();i<leftlen;i++){
                str = "0"+str;
            }
        }
        return drawEnd+"Y"+str;
    }
}
