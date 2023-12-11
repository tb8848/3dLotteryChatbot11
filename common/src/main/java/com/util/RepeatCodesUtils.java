package com.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 号码生成工具
 */
public class RepeatCodesUtils {

    /**
     * 生成二重、三重、四重的号码
     * @param len
     * @param maxInt
     * @return
     */
    public static Set<String> createRepeatCodes(int len,int maxInt){
        Set<String> finalNums = new HashSet<String>();
        for(int j=0;j<maxInt;j++){
            String values = "";
            for(int i=0;i<len;i++){
                values = values +j;
            }
            finalNums.add(values);
        }
//        for(String a : finalNums){
//            System.out.print(a+"---");
//        }
        //System.out.println();
        return finalNums;
    }



    /**
     * 生成双双重的号码
     * @param len
     * @param maxInt
     * @return
     */
    public static Set<String> createRepeat2Codes(int len,int maxInt){
        Set<String> finalNums = new HashSet<String>();
        if(len<4){
            return finalNums;
        }
        for(int j=0;j<maxInt;j++){
            String values = j+""+j;
            for(int i=0;i<maxInt;i++){
                String cc = values +i+""+i;
                finalNums.add(cc);
            }
        }
//        Set<String> excludes = createRepeatCodes(4,10);
//        finalNums.removeAll(excludes);
//        for(String a : finalNums){
//            System.out.print(a+"---");
//        }
        return finalNums;
    }

    /**
     * 检查购买的号码中是否有重复的数
     * @param repeatType 重数，2，3，4，22（双双重)
     */
    public static boolean checkRepeatCode(String buyCodes,int repeatType){
        Set<String> repeatCodes = null;
        if(repeatType==22){
            repeatCodes = createRepeat2Codes(4,10);
        }else{
            repeatCodes = createRepeatCodes(repeatType,10);
        }
        if(null==repeatCodes || repeatCodes.size()<1){
            return false;
        }
        for(String rc : repeatCodes){
            if(buyCodes.contains(rc)){
                return true;
            }
        }
        return false;
    }


    /**
     * 返回重数分隔后的号码
     * @param repeatType 重数，2，3，4，22（双双重)
     */
    public static String getRepeatCodeFrom(String buyCodes,int repeatType){
        Set<String> repeatCodes = null;
        String finalResult = null;
        if(repeatType==22){
            repeatCodes = createRepeat2Codes(4,10);
        }else{
            repeatCodes = createRepeatCodes(repeatType,10);
        }
        if(null==repeatCodes || repeatCodes.size()<1){
            return null;
        }
        for(String rc : repeatCodes){
            if(buyCodes.contains(rc)){
                finalResult = rc;
                break;
            }
        }
        if(null!=finalResult){
            if(repeatType==22){
                return buyCodes.substring(0,2)+","+buyCodes.substring(2,4);
            }else{

                int idx = buyCodes.indexOf(finalResult);
                if(idx==0){
                    return buyCodes.substring(0,finalResult.length())+","
                            +Arrays.stream(buyCodes.substring(finalResult.length(),buyCodes.length()).split("")).collect(Collectors.joining(","));
                }else{
                    int endIdx = idx+finalResult.length();
                    if(endIdx>=buyCodes.length()){
                        //逗号分隔的第一个数为重数
                        return buyCodes.substring(idx,buyCodes.length())+","
                                +Arrays.stream(buyCodes.substring(0,idx).split("")).collect(Collectors.joining(","));
                    }else{
                        //逗号分隔的第一个数为重数
                        return buyCodes.substring(idx,endIdx) +","
                                +Arrays.stream(buyCodes.substring(0,idx).split("")).collect(Collectors.joining(","))+","
                                +Arrays.stream(buyCodes.substring(endIdx).split("")).collect(Collectors.joining(","));
                    }
                }
            }
        }

        return null;
    }



    public static void main(String[] args){
       //getRepeatCodeFrom("1333",3);
        String buyCodes = "3312";
        String finalResult = "33";
        String result = "";
        int idx = buyCodes.indexOf(finalResult);
        if(idx==0){
            result = buyCodes.substring(0,finalResult.length())+","
                    +Arrays.stream(buyCodes.substring(finalResult.length(),buyCodes.length()).split("")).collect(Collectors.joining(","));
        }else{
            int endIdx = idx+finalResult.length();
            if(endIdx>=buyCodes.length()){
                result = buyCodes.substring(idx,buyCodes.length())+","
                        +Arrays.stream(buyCodes.substring(0,idx).split("")).collect(Collectors.joining(","));
            }else{
                result = buyCodes.substring(idx,endIdx) +","
                        +Arrays.stream(buyCodes.substring(0,idx).split("")).collect(Collectors.joining(","))+","
                        +Arrays.stream(buyCodes.substring(endIdx).split("")).collect(Collectors.joining(","));
            }

        }
        //System.out.println(result);

    }

}
