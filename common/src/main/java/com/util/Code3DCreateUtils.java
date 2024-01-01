package com.util;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 号码生成工具
 * 福彩3D
 */
public class Code3DCreateUtils {

    public static String Ding_Holder="口";

    public static String Random_Holder="X";


    public static String getCodePrintName(Integer lmId,String splitChar,String buyCodes){
        if(StringUtil.isNull(splitChar)){
            splitChar = "-";
        }
        String lmShortName = null;
        switch(lmId){
            case 1:
                lmShortName = "ZX";
                break;
            case 2:
                lmShortName = "TX";
                break;
            case 3:
                lmShortName = "Z3";
                break;
            case 4:
                lmShortName = "Z6";
                break;
            case 5:
                lmShortName = "HS";
                break;
            case 13:
                lmShortName = "KD";
                break;
            case 6:
                lmShortName = "1D";
                break;
            case 7:
                lmShortName = "2D";
                break;
            case 8:
                lmShortName = "BX";
                break;
            case 14:
                lmShortName = "DD";
            case 100:
                lmShortName = "Z3N";
                break;
            case 200:
                lmShortName = "Z6N";
                break;
            case 300:
                lmShortName = "FSN";
                break;
            default:
                break;
        }
        if(null == lmShortName){
            return buyCodes;
        }
        return lmShortName + splitChar + buyCodes;
    }


    public static String getCodeShortName(String buyCodes){
        String codeShortName = buyCodes;
        switch(buyCodes){
            case "大":
                codeShortName = "da";
                break;
            case "小":
                codeShortName = "xiao";
                break;
            case "奇":
                codeShortName = "ji";
                break;
            case "偶":
                codeShortName = "ou";
                break;
            case "拖拉机":
                codeShortName = "tlj";
                break;
            case "猜三同":
            case "三同号":
                codeShortName = "c3t";
                break;
            default:
                break;
        }
        return codeShortName;
    }




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

    //判断字符串中是否有重复的字符
    public static boolean checkSameCompCodes(String nums,Set<String> srcNums){

        for(String src : srcNums){

            char[] srcArr = src.toCharArray();
            char[] numsArr = nums.toCharArray();
            Arrays.sort(srcArr);
            Arrays.sort(numsArr);
            String srcStr = new String(srcArr);
            String numStr = new String(numsArr);
            if(srcStr.equals(numStr)){
                return true;
            }
        }

        return false;

    }



    //判断字符串中是否有重复的字符
    public static String checkAndGetSameCompCodes(String nums,Set<String> srcNums){
        char[] numsArr = nums.toCharArray();
        Arrays.sort(numsArr);
        String numStr = new String(numsArr);
        for(String src : srcNums){
            char[] srcArr = src.toCharArray();
            Arrays.sort(srcArr);
            String srcStr = new String(srcArr);
            if(srcStr.equals(numStr)){
                return null;
            }
        }

        return numStr;

    }

    //替换特殊符号
    public static String replaceDingHolder(String buyCodes){

        int len = buyCodes.length();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            String subStr = buyCodes.substring(i,i+1);
            if(subStr.equals(Random_Holder)){
                sb.append(subStr);
            }else{
                sb.append(Ding_Holder);
            }
        }
        return sb.toString();
    }

    /**
     * 检查购买的号码中是否有重复的数
     */
    public static boolean checkRepeatCode(String buyCodes,int repeatType){
        Set<String> repeatCodes = null;
        repeatCodes = createRepeatCodes(repeatType,10);
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
     * 创建指定长度的号码,不含任何定位符号
     * @param count
     * @param maxInt
     * @return
     */
    public static Set<String> createCodes(int count,int maxInt){
        Set<String> holderNums = new HashSet<String>();
        Set<String> nums = new HashSet<String>();
        String[] holders = new String[count];
        String formatStr = "";
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<count;i++) {
            holders[i] = "-";
            formatStr = formatStr +"%s";
        }
        for(int i=0;i<count;i++){
            if(i==0){
                for(int j=0;j<maxInt;j++){
                    String[] h = holders;
                    h[i]=j+"";
                    String ncode = String.format(formatStr,h);
                    holderNums.add(ncode);
                    nums.add(ncode);
                }
            }else{
                for(String nu : holderNums){
                    if(nu.indexOf("-")>-1){
                        int loc = nu.indexOf("-");
                        for(int j=0;j<maxInt;j++){
                            sb.append(nu);
                            sb.replace(loc,loc+1,j+"");
                            String ncode = sb.toString();
                            //holderNums.add(ncode);
                            nums.add(ncode);
                            sb.setLength(0);
                        }
                    }
                }
                holderNums.addAll(nums);
            }
        }
        Set<String> finalNums = new HashSet();
        //删除包含占位符的号码
        for(String item : nums){
            if(item.indexOf("-")<0){
                finalNums.add(item);
            }
        }
        nums = null;
        holderNums = null;
        return finalNums;
    }



    /**
     * 创建指定长度的号码,不区分位置
     * 删除相同组合
     * @param count
     * @param maxInt
     * @return
     */
    public static Set<String> createCodesForXianV2(int count,int maxInt){
        List<String> codesList = new ArrayList<>();
        switch(count){
            case 2:
                for(int i=0;i<10;i++){
                    for(int j=0;j<10;j++){
                        codesList.add(i+""+j);
                    }
                }
                break;
            case 3:
                for(int i=0;i<10;i++){
                    for(int j=0;j<10;j++){
                        for(int k=0;k<10;k++) {
                            codesList.add(i + "" + j+""+k);
                        }
                    }
                }
                break;
            case 4:
                for(int i=0;i<10;i++){
                    for(int j=0;j<10;j++){
                        for(int k=0;k<10;k++) {
                            for(int m=0;m<10;m++) {
                                codesList.add(i + "" + j + "" + k+""+m);
                            }
                        }
                    }
                }
                break;
        }

        //删除内容一样的号码，如011，110，101，都是一样的。
        Set<String> finalSet = new HashSet();
        for(String item : codesList){
            if(finalSet.size()<1){
                finalSet.add(item);
            }else{
                boolean isSameComp = checkSameCompCodes(item,finalSet);
                if(!isSameComp){
                    finalSet.add(item);
                }
            }
        }
        return finalSet;
    }



    /**
     * 创建指定长度的号码,不区分位置
     * @param count
     * @param maxInt
     * @return
     */
    public static Set<String> createCodesForXian(int count,int maxInt){
        Set<String> holderNums = new HashSet<String>();
        Set<String> nums = new HashSet<String>();
        String[] holders = new String[count];
        String formatStr = "";
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<count;i++) {
            holders[i] = "-";
            formatStr = formatStr +"%s";
        }
        for(int i=0;i<count;i++){
            if(i==0){
                for(int j=0;j<maxInt;j++){
                    String[] h = holders;
                    h[i]=j+"";
                    String ncode = String.format(formatStr,h);
                    holderNums.add(ncode);
                    nums.add(ncode);
                }
            }else{
                for(String nu : holderNums){
                    if(nu.indexOf("-")>-1){
                        int loc = nu.indexOf("-");
                        for(int j=0;j<maxInt;j++){
                            sb.append(nu);
                            sb.replace(loc,loc+1,j+"");
                            String ncode = sb.toString();
                            //holderNums.add(ncode);
                            nums.add(ncode);
                            sb.setLength(0);
                        }
                    }
                }
                holderNums.addAll(nums);
            }
        }
        Set<String> finalNums = new HashSet();
        //删除包含占位符的号码
        for(String item : nums){
            if(item.indexOf("-")<0){
                finalNums.add(item);
            }
        }

//        //删除内容一样的号码，如011，110，101，都是一样的。
//        Set<String> finalSet = new HashSet();
//        for(String item : finalNums){
//            if(finalSet.size()<1){
//                finalSet.add(item);
//            }else{
//                boolean isSameComp = checkSameCompCodes(item,finalSet);
//                if(!isSameComp){
//                    finalSet.add(item);
//                }
//            }
//        }

        nums = null;
        holderNums = null;
        return finalNums;
    }





    /**
     * 创建指定长度的号码,区分位置
     * @param count
     * @param maxInt
     * @return
     */
    public static Set<String> createCodesForDing(int count,int maxInt){
        Set<String> holderNums = new HashSet<String>();
        Set<String> nums = new HashSet<String>();
        String[] holders = new String[count];
        String formatStr = "";
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<count;i++) {
            holders[i] = "-";
            formatStr = formatStr +"%s";
        }
        for(int i=0;i<count;i++){
            if(i==0){
                for(int j=0;j<maxInt;j++){
                    String[] h = holders;
                    h[i]=j+"";
                    String ncode = String.format(formatStr,h);
                    holderNums.add(ncode);
                    nums.add(ncode);
                }
            }else{
                for(String nu : holderNums){
                    if(nu.indexOf("-")>-1){
                        int loc = nu.indexOf("-");
                        for(int j=0;j<maxInt;j++){
                            sb.append(nu);
                            sb.replace(loc,loc+1,j+"");
                            String ncode = sb.toString();
                            //holderNums.add(ncode);
                            nums.add(ncode);
                            sb.setLength(0);
                        }
                    }
                }
                holderNums.addAll(nums);
            }
        }
        Set<String> finalNums = new HashSet();
        //删除包含占位符的号码
        for(String item : nums){
            if(item.indexOf("-")<0){
                finalNums.add(item);
            }
        }
        nums = null;
        holderNums = null;
        return finalNums;
    }


    public static Set<String> createFixFormatCodes(int count,String ruleName){
        Set<String> finalSet = new HashSet<String>();
        Set<String> nums = createCodes(count,10);
        for(String nu:nums){
            String rn = ruleName;
            char[] dings = rn.toCharArray();
            char[] chars = nu.toCharArray();
            for(char c :chars){
                int idx =  rn.indexOf(Ding_Holder);
                if(idx>-1){
                    dings[idx]=c;
                }
                rn = new String(dings);
            }
            finalSet.add(new String(dings));
        }
        nums = null;
        return finalSet;
    }



    public static Set<String> createFixFormatCodes(Set<String> nums,String ruleName){
        Set<String> finalSet = new HashSet<String>();
        for(String nu:nums){
            String rn = ruleName;
            char[] dings = rn.toCharArray();
            char[] chars = nu.toCharArray();
            for(char c :chars){
                int idx =  rn.indexOf(Ding_Holder);
                if(idx>-1){
                    dings[idx]=c;
                }
                rn = new String(dings);
            }
            finalSet.add(new String(dings));
        }
        nums = null;
        return finalSet;
    }


    public static List<String> createFixFormatCodes(List<String> nums,String ruleName){
        List<String> finalSet = new ArrayList<String>();
        for(String nu:nums){
            String rn = ruleName;
            char[] dings = rn.toCharArray();
            char[] chars = nu.toCharArray();
            for(char c :chars){
                int idx =  rn.indexOf(Ding_Holder);
                if(idx>-1){
                    dings[idx]=c;
                }
                rn = new String(dings);
            }
            finalSet.add(new String(dings));
        }
        nums = null;
        return finalSet;
    }


    /**
     * 生成重数
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
        return finalNums;
    }


    /**
     * 生成双双重
     * @param len
     * @param maxInt
     * @return
     */
    public static Set<String> createDoubleRepeatCodes(int len,int maxInt){
        Set<String> finalNums = new HashSet<String>();
        if(len!=4){
            return finalNums;
        }

        Set<String> tmp1 = new HashSet<String>();
        for(int j=0;j<maxInt;j++){
            String values = "";
            for(int i=0;i<len/2;i++){
                values = values +j;
            }
            tmp1.add(values);
        }
        for(String aa : tmp1){
            for(int j=0;j<maxInt;j++){
                String values = "";
                for(int i=0;i<len/2;i++){
                    values = values +j;
                }
                finalNums.add(aa+values);
            }
        }
        return finalNums;
    }


    public static Set<String> createPairsCodes(int maxInt){
        Set<String> pairCodes = new HashSet<String>();
        pairCodes.add("05");
        pairCodes.add("50");
        pairCodes.add("16");
        pairCodes.add("61");
        pairCodes.add("72");
        pairCodes.add("27");
        pairCodes.add("83");
        pairCodes.add("38");
        pairCodes.add("49");
        pairCodes.add("94");
        return pairCodes;
    }

    /**
     * 生成兄弟号码
     * @param len
     * @param maxInt
     * @return
     */
    public static Set<String> createBrotherCodes(int len,int maxInt){
        Set<String> finalNums = new HashSet<String>();
        int nextBrother = 0;
        for(int j=0;j<maxInt;j++){
            List<Integer> nums = new ArrayList<>();
            nums.add(j);
            String values = j+"";
            nextBrother = j;
            if(j>7){
                break;
            }
            for(int i=1;i<len;i++){
                nextBrother = nextBrother+1;
                nums.add(nextBrother);
                values = values + nextBrother;
            }
            String[] arr = values.split("");
            values = Arrays.stream(arr).sorted().collect(Collectors.joining());
            finalNums.add(values);
            Collections.reverse(nums);
            values = nums.stream().map(cc->String.valueOf(cc)).collect(Collectors.joining());
            finalNums.add(values);
        }
        return finalNums;
    }


    private static Set<String> findCodeByLoc(Set<String> oriNums,int locIndex,String locValue,boolean isInclude){
        Set<String> finalNums = new HashSet<String>();
        for(String a : oriNums){

            String subStr = a.substring(locIndex,locIndex+1);
            if(isInclude){
                //取
                if(locValue.contains(subStr)){
                    finalNums.add(a);
                }
            }else {
                //除
                if (!locValue.contains(subStr)) {
                    finalNums.add(a);
                }
            }
        }
        return finalNums;
    }

    private static Set<String> matchCodesByLoc(Set<String> oriNums,int locIndex,String locValue,boolean isInclude){
        Set<String> finalNums = new HashSet<String>();
        StringBuffer sb = new StringBuffer();
        for(String a : oriNums){
            sb.append(a);
            String subStr = a.substring(locIndex,locIndex+1);
            if(isInclude){
                //取
                if(locValue.contains(subStr)){
                    finalNums.add(a);
                }
            }else{
                //除
                if(!locValue.contains(subStr)){
                    finalNums.add(a);
                }
            }
            sb.setLength(0);
        }
        return finalNums;
    }

    private static Set<String> findBySum2(Set<String> oriNums,Integer sumValue){
        Set<String> finalNums = new HashSet<String>();
        StringBuffer sb = new StringBuffer();
        for(String a : oriNums){
            Integer firstV = Integer.parseInt(a.substring(0,0+1));
            Integer lastV = Integer.parseInt(a.substring(a.length()-1));
            int sums = firstV + lastV;
            String sumStr = String.valueOf(sums);
            lastV = Integer.parseInt(sumStr.substring(sumStr.length()-1));
            if(lastV == sumValue){
                finalNums.add(a);
            }
        }
        return finalNums;
    }


    private static Set<String> findBySum2(Set<String> oriNums,String sumValue){
        Set<String> finalNums = new HashSet<String>();
        for(String a : oriNums){
            Integer firstV = Integer.parseInt(a.substring(0,0+1));
            Integer lastV = Integer.parseInt(a.substring(a.length()-1));
            int sums = firstV + lastV;
            String sumStr = String.valueOf(sums);
            String lastNum = sumStr.substring(sumStr.length()-1);
            if(sumValue.contains(lastNum)){
                finalNums.add(a);
            }
        }
        return finalNums;
    }

    private static Set<String> findCodeByRepeat(Set<String> oriNums,int repeatsCount,boolean isInclude){
        Set<String> oriRepeatSet = new HashSet<String>();

        Set<String> repeatCodeSet = null;
        if(repeatsCount==22){
            repeatCodeSet= RepeatCodesUtils.createRepeat2Codes(4,10);
        }else{
            repeatCodeSet= RepeatCodesUtils.createRepeatCodes(repeatsCount,10);
        }
        for(String a : oriNums){ //找出原始数据中包含重数的号码集合
            String[] arr = a.split(""); //对号码排序
            String a0 = Arrays.stream(arr).sorted().collect(Collectors.joining());
            for(String rcode : repeatCodeSet){
                if(a0.contains(rcode)){
                    oriRepeatSet.add(a);
                }
            }
        }

        if(isInclude){
            //取
            return oriRepeatSet;
        }else{
            oriNums.removeAll(oriRepeatSet);
            repeatCodeSet = null;
            return oriNums;
        }
    }


    private static Set<String> findCodeByBrother(Set<String> oriNums,int brotherCount,boolean isInclude){
        Set<String> oriBrotherSet = new HashSet<String>();
        Set<String> brotherCodeSet = createBrotherCodes(brotherCount,10);
        for(String a : oriNums){ //找出原始数据中包含兄弟的号码集合
            String[] arr = a.split(""); //原始号码进行排序
            String a0 = Arrays.stream(arr).sorted().collect(Collectors.joining());
            for(String rcode : brotherCodeSet){
                if(a0.contains(rcode)){
                    oriBrotherSet.add(a);
                }
            }
        }
        brotherCodeSet = null;
        if(isInclude){
            //取
            return oriBrotherSet;
        }else{
            oriNums.removeAll(oriBrotherSet);
            oriBrotherSet = null;
            return oriNums;
        }
    }

    private static Set<String> findCodeByPairs(Set<String> oriNums,boolean isInclude,Set<String> targetPairs){
        Set<String> oriPairsSet = new HashSet<String>();
        Set<String> pairsCodeSet = createPairsCodes(10);
        if(null!=targetPairs && targetPairs.size()>0){
            pairsCodeSet = targetPairs;
        }

        for(String a : oriNums) { //找出原始数据中包含对数的号码集合
            for (String rcode : pairsCodeSet) {
                String[] arr1 = rcode.split("");
                boolean matchResult = Arrays.stream(arr1).allMatch(item->{
                    return a.contains(item);
                });
                if (matchResult) {
                    oriPairsSet.add(a);
                }
            }
        }
        if(isInclude){
            //取
            return oriPairsSet;
        }else{
            oriNums.removeAll(oriPairsSet);
            oriPairsSet = null;
            return oriNums;
        }
    }


    /**
     * 根据大数、小数、单数、双数等规则找符合条件的号码
     * @param oriNums 号码集合
     * @param locIndex 位置,0到5之间的一个整数
     * @param isInclude true(取）,false(除)
     * @param numType 1(单数),2(双数),4(大数),3(小数)
     * @return
     */
    private static Set<String> findCodeBy(Set<String> oriNums,int locIndex,boolean isInclude,int numType){
        Set<String> finalNums = new HashSet<String>();
        for(String a : oriNums){
            String subStr = a.substring(locIndex,locIndex+1);
            if(subStr.contains(Random_Holder)){
                continue;
            }
            int locValue = Integer.valueOf(subStr);
            boolean isOk = false; //标记是否满足条件
            switch(numType){
                case 1: //单数
                    isOk = locValue%2==1;
                    break;
                case 2: //双数
                    isOk = locValue%2==0;
                    break;
                case 3: //小数
                    isOk = locValue<5;
                    break;
                case 4: //大数
                    isOk = locValue>4;
                    break;
                default:
                    continue;
            }

            if(isInclude){
                //取
                if(isOk){
                    finalNums.add(a);
                }
            }else{
                //除
                if(!isOk){
                    finalNums.add(a);
                }
            }
        }
        return finalNums;
    }

    public static Set<String> oneCodeRuleMatch(Set<String> oriNums,List<Integer> locArr,
                                               boolean isInclude,int numType){
        for(int i=0;i<locArr.size();i++){
            if(locArr.get(i)>0){
                oriNums = findCodeBy(oriNums,i,isInclude,numType);
            }
        }
        return oriNums;
    }




    /**
     * 1D号码
     * 按1定位规则生成
     * @param bai 百位数字字符串
     * @param shi 十位数字字符串
     * @param ge 个位数字字符串
     * @return
     */
    public static List<String> ding1Code(String bai,String shi,String ge){
        Set<String> codeSet = new HashSet<>();
        if(StringUtil.isNotNull(bai) && bai.indexOf("-")<0){
            String[] arr = bai.split("");
            for(String bi : arr){
                codeSet.add(bi+"XX");
            }
        }

        if(StringUtil.isNotNull(shi) && shi.indexOf("-")<0){
            String[] arr = shi.split("");
            for(String bi : arr){
                codeSet.add("X"+bi+"X");
            }
        }

        if(StringUtil.isNotNull(ge) && ge.indexOf("-")<0){
            String[] arr = ge.split("");
            for(String bi : arr){
                codeSet.add("XX"+bi);
            }
        }

        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    /**
     * 2D号码
     * 按2定位规则生成
     * @param bai 百位数字字符串
     * @param shi 十位数字字符串
     * @param ge 个位数字字符串
     * @return
     */
    public static List<String> ding2Code(String bai,String shi,String ge){
        Set<String> codeSet = new HashSet<>();
        if(StringUtil.isNotNull(bai) && StringUtil.isNotNull(shi) && StringUtil.isNotNull(ge) && bai.indexOf("-")<0 && shi.indexOf("-")<0 && ge.indexOf("-")<0){
            String[] arr1 = bai.split("");
            String[] arr2 = shi.split("");
            String[] arr3 = ge.split("");
            for(String bi : arr1){
                for(String si : arr2) {
                    for (String gi : arr3) {
                        String c1 = bi+si+"X";
                        String c2 = bi+"X"+gi;
                        String c3 = "X"+si+gi;
                        codeSet.add(c1);
                        codeSet.add(c2);
                        codeSet.add(c3);
                    }
                }
            }
        }else if(StringUtil.isNotNull(bai) && StringUtil.isNotNull(shi) && bai.indexOf("-")<0 && shi.indexOf("-")<0){
            String[] arr1 = bai.split("");
            String[] arr2 = shi.split("");
            for(String bi : arr1){
                for(String si : arr2) {
                    codeSet.add(bi +si+"X");
                }
            }
        }else if(StringUtil.isNotNull(bai) && StringUtil.isNotNull(ge) && bai.indexOf("-")<0 && ge.indexOf("-")<0){
            String[] arr1 = bai.split("");
            String[] arr2 = ge.split("");
            for(String bi : arr1){
                for(String si : arr2) {
                    codeSet.add(bi+"X"+si);
                }
            }
        }else if(StringUtil.isNotNull(shi) && StringUtil.isNotNull(ge) && shi.indexOf("-")<0 && ge.indexOf("-")<0){
            String[] arr1 = shi.split("");
            String[] arr2 = ge.split("");
            for(String bi : arr1){
                for(String si : arr2) {
                    codeSet.add("X"+bi+si);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    /**
     * 3D号码
     * @param bai
     * @param shi
     * @param ge
     * @return
     */
    public static List<String> ding3Code(String bai,String shi,String ge){
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = bai.split("");
        String[] shiArr = shi.split("");
        String[] geArr = ge.split("");
        for(String bi : baiArr){
            for(String si : shiArr){
                for(String g : geArr){
                    String code = bi+si+g;
                    codeSet.add(code);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    /**
     * 直选复式号码
     * @param bai
     * @param shi
     * @param ge
     * @return
     */
    public static List<String> zxFushi(String bai,String shi,String ge){
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = bai.split("");
        String[] shiArr = shi.split("");
        String[] geArr = ge.split("");
        for(String bi : baiArr){
            for(String si : shiArr){
                for(String g : geArr){
                    String code = bi+si+g;
                    codeSet.add(code);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    /**
     * 和值
     * @param sums 和值列表
     * @return
     */
    public static List<String> hezhi(List<String> sums){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    String code = String.format("%s%s%s",i,j,k);
                    int he = i+j+k;
                    if(sums.contains(String.valueOf(he))){
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    /**
     * 组三 和值
     * 按三字现规则生成号码
     * 存在两个相同的数字
     * 排除3重
     * @param sums 和值列表
     * @return
     */
    public static List<String> z3hezhi(List<String> sums){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    if(i==j && j==k){
                        continue;
                    }
                    int[] numArr = {i,j,k};
                    Arrays.sort(numArr);
                    if(numArr[0]==numArr[1] || numArr[1]==numArr[2]){
                        String code = String.format("%s%s%s",numArr[0],numArr[1],numArr[2]);
                        int he = i+j+k;
                        if(sums.contains(String.valueOf(he))){
                            codeSet.add(code);
                        }
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    /**
     * 组三 号码
     * 按三字现规则生成号码
     * 存在两个相同的数字
     * 排除3重
     * @return
     */
    public static List<String> z3Code(){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    if(i==j && j==k){
                        continue;
                    }
                    int[] numArr = {i,j,k};
                    Arrays.sort(numArr);
                    if(numArr[0]==numArr[1] || numArr[1]==numArr[2]){
                        String code = String.format("%s%s%s",numArr[0],numArr[1],numArr[2]);
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    public static List<String> z3Code(String numStr){
        Set<String> codeSet = new HashSet<>();
        String[] numArr = numStr.split("");
        Arrays.sort(numArr);
        for(int i=0;i<numArr.length-1;i++){
            for(int j=i+1;j<numArr.length;j++){
                String[] sortArr = {numArr[i],numArr[i],numArr[j]};
                Arrays.sort(sortArr);
                String code1 = Arrays.stream(sortArr).collect(Collectors.joining());

                String[] sortArr1 = {numArr[i],numArr[j],numArr[j]};
                Arrays.sort(sortArr1);
                String code2 = Arrays.stream(sortArr1).collect(Collectors.joining());;
                codeSet.add(code1);
                codeSet.add(code2);
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    public static List<String> z3SFCode(String nums) {
        Set<String> codeSet = new HashSet<>();
        String[] codeArr = nums.split("");
        Arrays.sort(codeArr);
        String code1 = codeArr[0]+codeArr[0]+codeArr[0];
        String code2 = codeArr[0]+codeArr[0]+codeArr[1];
        String code3 = codeArr[0]+codeArr[1]+codeArr[1];
        String code4 = codeArr[1]+codeArr[1]+codeArr[1];
        codeSet.add(code1);
        codeSet.add(code2);
        codeSet.add(code3);
        codeSet.add(code4);
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    //组三胆拖
    public static List<String> z3DtCode(String dm,String tm) { //胆码
        Set<String> codeSet = new HashSet<>();
        if(dm.length()>0){
            String[] tmArr = tm.split("");
            Arrays.sort(tmArr);
            for(int i=0;i<tmArr.length;i++){

                String[] sortArr = {dm,dm,tmArr[i]};
                Arrays.sort(sortArr);
                String code1 = Arrays.stream(sortArr).collect(Collectors.joining());

                String[] sortArr1 = {dm,tmArr[i],tmArr[i]};
                Arrays.sort(sortArr1);
                String code2 = Arrays.stream(sortArr1).collect(Collectors.joining());;
                codeSet.add(code1);
                codeSet.add(code2);
            }
        }else{
            String[] tmArr = tm.split("");
            Arrays.sort(tmArr);
            for(int i=0;i<tmArr.length-1;i++){
                for(int j=i+1;j<tmArr.length;j++){

                    String[] sortArr = {tmArr[i],tmArr[j],tmArr[j]};
                    Arrays.sort(sortArr);
                    String code1 = Arrays.stream(sortArr).collect(Collectors.joining());

                    String[] sortArr1 = {tmArr[i],tmArr[i],tmArr[j]};
                    Arrays.sort(sortArr1);
                    String code2 = Arrays.stream(sortArr1).collect(Collectors.joining());;
                    codeSet.add(code1);
                    codeSet.add(code2);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    public static List<String> z6CodeBy(String nums) { //重号
        Set<String> codeSet = new HashSet<>();
        String[] arr = nums.split("");
        for(int i=0;i<arr.length-2;i++){
            for(int j=i+1;j<arr.length-1;j++){
                for(int k=j+1;k<arr.length;k++) {
                    String[] sortArr = {arr[i],arr[j],arr[k]};
                    Arrays.sort(sortArr);
                    String code1 = Arrays.stream(sortArr).collect(Collectors.joining());
                    codeSet.add(code1);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    //组六胆拖
    public static List<String> z6DtCode(String dm,String tm) { //胆码
        Set<String> codeSet = new HashSet<>();
        if(dm.length()==1){
            String[] arr = tm.split("");
            for(int i=0;i<arr.length-1;i++){
                for(int j=i+1;j<arr.length;j++) {
                    String[] sortArr = {dm,arr[i],arr[j]};
                    Arrays.sort(sortArr);
                    String code1 = Arrays.stream(sortArr).collect(Collectors.joining());
                    //String code1 = dm + "" + arr[i] + "" + arr[j];
                    codeSet.add(code1);
                }
            }
        }else if(dm.length()==2){
            String[] dmArr = dm.split("");
            String[] arr = tm.split("");
            for(int i=0;i<arr.length;i++){
                String[] sortArr = {dmArr[0],dmArr[1],arr[i]};
                Arrays.sort(sortArr);
                String code1 = Arrays.stream(sortArr).collect(Collectors.joining());
                //String code1 = dmArr[0] + "" + dmArr[1] + "" + arr[i];
                codeSet.add(code1);
            }
        }else if(dm.length()==0){
            String[] arr = tm.split("");
            for(int i=0;i<arr.length-2;i++) {
                for (int j = i + 1; j < arr.length - 1; j++) {
                    for (int k = j + 1; k < arr.length; k++) {
                        String[] sortArr = {arr[i],arr[j],arr[k]};
                        Arrays.sort(sortArr);
                        String code1 = Arrays.stream(sortArr).collect(Collectors.joining());
                        //String code1 = dmArr[0] + "" + dmArr[1] + "" + arr[i];
                        codeSet.add(code1);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }



    /**
     * 包选三 号码
     * 按三字定规则生成号码
     * 存在两个相同的数字
     * 排除3重号码
     * @return
     */
    public static List<String> b3Code(){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    if(i==j && j==k){
                        continue;
                    }
                    String code = String.format("%s%s%s",i,j,k);
                    if(i==j || j==k || i==k){
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    public static List<String> b3Code(String bai, String shi, String ge) {
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = bai.split("");
        String[] shiArr = shi.split("");
        String[] geArr = ge.split("");
        for(int i=0;i<baiArr.length;i++){
            for(int j=0;j<shiArr.length;j++){
                for(int k=0;k<geArr.length;k++){
                    String b = baiArr[i];
                    String s = shiArr[j];
                    String g = geArr[k];
                    if(b.equals(s) && s.equals(g)){
                        continue;
                    }
                    String code = String.format("%s%s%s",b,s,g);
                    if(b.equals(s) || s.equals(g) || b.equals(g)){
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    public static List<String> b6Code(String bai, String shi, String ge) {
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = bai.split("");
        String[] shiArr = shi.split("");
        String[] geArr = ge.split("");
        for(int i=0;i<baiArr.length;i++){
            for(int j=0;j<shiArr.length;j++){
                for(int k=0;k<geArr.length;k++){
                    String b = baiArr[i];
                    String s = shiArr[j];
                    String g = geArr[k];
                    if(b.equals(s) && s.equals(g)){
                        continue;
                    }
                    String code = String.format("%s%s%s",b,s,g);
                    if(!b.equals(s) && !s.equals(g)){
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    public static List<String> createB3Code(String bai,String shi,String ge){
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = bai.split("");
        String[] shiArr = shi.split("");
        String[] geArr = ge.split("");
        if(baiArr.length>0 && shiArr.length>0 && geArr.length>0){
            for(int i=0;i<baiArr.length;i++){
                for(int j=0;j<shiArr.length;j++){
                    for(int k=0;k<geArr.length;k++){
                        String bv = baiArr[i];
                        String sv = shiArr[j];
                        String gv = geArr[k];
                        if(bv.equals(sv) || sv.equals(gv) || bv.equals(gv)){
                            codeSet.add(bv+sv+gv);
                        }
                    }
                }
            }
            List<String> codeList = codeSet.stream().collect(Collectors.toList());
            Collections.sort(codeList);
            return codeList;
        }
        return Lists.newArrayList();

    }


    public static List<String> createB6Code(String bai,String shi,String ge){
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = bai.split("");
        String[] shiArr = shi.split("");
        String[] geArr = ge.split("");
        if(baiArr.length>0 && shiArr.length>0 && geArr.length>0){
            for(int i=0;i<baiArr.length;i++){
                for(int j=0;j<shiArr.length;j++){
                    for(int k=0;k<geArr.length;k++){
                        String bv = baiArr[i];
                        String sv = shiArr[j];
                        String gv = geArr[k];
                        if(!bv.equals(sv) && !sv.equals(gv)){
                            codeSet.add(bv+sv+gv);
                        }
                    }
                }
            }
            List<String> codeList = codeSet.stream().collect(Collectors.toList());
            Collections.sort(codeList);
            return codeList;
        }
        return Lists.newArrayList();
    }


    /**
     * 包选六 号码
     * 按三字定规则生成号码
     * 排除2重和3重号码
     * @return
     */
    public static List<String> b6Code(){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    if(i==j && j==k){
                        continue;
                    }
                    String code = String.format("%s%s%s",i,j,k);
                    if(i!=j && j!=k ){
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }



    /**
     * 组六 和值
     * 按三字现规则生成号码
     * 三个数字各不相同
     * @param sums 和值列表
     * @return
     */
    public static List<String> z6hezhi(List<String> sums){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    int[] numArr = {i,j,k};
                    Arrays.sort(numArr);
                    String code = String.format("%s%s%s",numArr[0],numArr[1],numArr[2]);
                    int he = i+j+k;
                    if(numArr[0]!=numArr[1] && numArr[1]!=numArr[2]){
                        if(sums.contains(String.valueOf(he))){
                            codeSet.add(code);
                        }
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }



    /**
     * 组六 号码
     * 按三字现规则生成号码
     * 三个数字各不相同
     * @return
     */
    public static List<String> z6Code(){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                for(int k=0;k<10;k++){
                    int[] numArr = {i,j,k};
                    Arrays.sort(numArr);
                    if(numArr[0]!=numArr[1] && numArr[1]!=numArr[2]){
                        String code = String.format("%s%s%s",numArr[0],numArr[1],numArr[2]);
                        codeSet.add(code);
                    }
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    public static List<String> z6Code(String nums) {
        Set<String> codeSet = new HashSet<>();
        String[] codeArr = nums.split("");
        Arrays.sort(codeArr);
        int len = nums.length();
        for(int i=0;i<len-2;i++){
            for(int j=i+1;j<len-1;j++){
                for(int k=j+1;k<len;k++){
                    String code = String.format("%s%s%s",codeArr[i],codeArr[j],codeArr[k]);
                    codeSet.add(code);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }

    /**
     * 猜2D 号码
     * 按二字现规则生成号码
     * @return
     */
    public static List<String> c2dCode(){
        Set<String> codeSet = new HashSet<>();
        for(int i=0;i<10;i++){
        for(int j=0;j<10;j++){
                int[] numArr = {i,j};
                Arrays.sort(numArr);
                String code = String.format("%s%s",numArr[0],numArr[1]);
                codeSet.add(code);
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }



    /**
     * 猜2D 号码
     * 按二字现规则生成号码
     * @return
     */
    public static List<String> c2dCode(String nums){
        Set<String> codeSet = new HashSet<>();
        String[] baiArr = nums.split("");
        if(baiArr.length>0){
            for(int i=0;i<baiArr.length;i++){
                for(int j=0;j<baiArr.length;j++){
                    String[] arr = {baiArr[i],baiArr[j]};
                    Arrays.sort(arr);
                    String code = Arrays.stream(arr).collect(Collectors.joining());
                    codeSet.add(code);
                }
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }


    /**
     * 拖拉机 号码
     * @return
     */
    public static List<String> tljCode(){
        List<String> codeList = new ArrayList<>();
        codeList.add("012");
        codeList.add("123");
        codeList.add("234");
        codeList.add("345");
        codeList.add("456");
        codeList.add("567");
        codeList.add("678");
        codeList.add("789");
        codeList.add("210");
        codeList.add("321");
        codeList.add("432");
        codeList.add("543");
        codeList.add("654");
        codeList.add("765");
        codeList.add("876");
        codeList.add("987");
        Collections.sort(codeList);
        return codeList;
    }


    public static List<String> z6SFCode(String nums) {
        Set<String> codeSet = new HashSet<>();
        String[] codeArr = nums.split("");
        Arrays.sort(codeArr);
        for(int i=0;i<10;i++){
            if(nums.indexOf(i+"")<0){
                String[] arr = {codeArr[0],codeArr[1],i+""};
                String code1 = Arrays.stream(arr).sorted().collect(Collectors.joining());
                codeSet.add(code1);
            }
        }
        List<String> codeList = codeSet.stream().collect(Collectors.toList());
        Collections.sort(codeList);
        return codeList;
    }





}
