package com.util;

import org.assertj.core.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeUtils {

    /**
     * 根据号码获取赔率-2字现、3字现、4字现
     * @param buyCode
     * @param peiRate
     * @return
     */
    public static String getPeiRateByCode (String buyCode, String peiRate) {
        Integer count = getCountByCode(buyCode);
        String[] prs = peiRate.split("/");
        String pr = "";
        if (count == 1) {
            pr = prs[0];
        }else if (count == 2) {
            pr = prs[1];
        }else if (count == 3) {
            pr = prs[2];
        }else if (count == 4) {
            pr = prs[3];
        }

        return pr;
    }

    /**
     * 获取相同字符串个数
     * @param buyCode
     * @return
     */
    public static Integer getCountByCode(String buyCode) {
        List list = new ArrayList();
        char c[] = buyCode.toCharArray();
        list.add(c[0]);
        for(int i = 0;i < c.length;i++){
            for(int j = 0;j < list.size();j++){
                if((char)list.get(j) == c[i]){
                    break;
                }else{
                    if(j == list.size()-1){
                        list.add(c[i]);
                    }
                }
            }
        }
        List<Integer> list2 = Lists.newArrayList();
        for(int j = 0;j < list.size();j++){  //拿出集合中每一个跟数组中元素比，如果一样则m++
            int m=0;
            for(int i = 0;i < c.length;i++){  //遍历数组中的每一个字符
                if(list.get(j).equals(c[i])){
                    m++;
                }
            }
            list2.add(m);
        }

        return list2.stream().max(Integer::compareTo).get();
    }

    public static List<String> get1DCodes (String drawCode) {
        List<String> list = Lists.newArrayList();
        StringBuffer sb = new StringBuffer(drawCode);
        StringBuffer oxxx = sb.replace(1,4, "XX");
        list.add(oxxx.toString());

        StringBuffer sb2 = new StringBuffer(drawCode);
        StringBuffer xoxx = sb2.replace(0,1, "X").replace(2,3, "X");
        list.add(xoxx.toString());

        StringBuffer sb3 = new StringBuffer(drawCode);
        StringBuffer xxox = sb3.replace(0,2, "XX");
        list.add(xxox.toString());

        return list;
    }

    public static List<String> get2DCodes (String drawCode) {
        List<String> list = Lists.newArrayList();
        StringBuffer sb = new StringBuffer(drawCode);
        StringBuffer ooxx = sb.replace(1,2, "X");
        list.add(ooxx.toString());

        StringBuffer sb2 = new StringBuffer(drawCode);
        StringBuffer xoox = sb2.replace(2,3, "X");
        list.add(xoox.toString());

        StringBuffer sb6 = new StringBuffer(drawCode);
        StringBuffer oxox = sb6.replace(0,1, "X");
        list.add(oxox.toString());

        return list;
    }

    public static List<String> get3DCodes (String drawCode) {
        List<String> list = Lists.newArrayList();
        StringBuffer sb = new StringBuffer(drawCode);
        StringBuffer ooox = sb.replace(3,4, "X");
        list.add(ooox.toString());

        StringBuffer sb2 = new StringBuffer(drawCode);
        StringBuffer ooxo = sb2.replace(2,3, "X");
        list.add(ooxo.toString());

        StringBuffer sb3 = new StringBuffer(drawCode);
        StringBuffer oxoo = sb3.replace(1,2, "X");
        list.add(oxoo.toString());

        StringBuffer sb4 = new StringBuffer(drawCode);
        StringBuffer xooo = sb4.replace(0,1, "X");
        list.add(xooo.toString());

        return list;
    }

    public static List<String> get2XCodes (String drawCode) {
        List<String> list = Lists.newArrayList();
        String s1 = drawCode.substring(0,2);
        list.add(s1);
        String s2 = drawCode.substring(1,3);
        list.add(s2);
        String s4 = drawCode.substring(0,1) + drawCode.substring(2,3);
        list.add(s4);
        return list;
    }

    public static List<String> get3XCodes (String drawCode) {
        List<String> list = Lists.newArrayList();
        String[] ss = drawCode.split("");
        List<String> strings = Lists.newArrayList(ss);
        String code = strings.stream().sorted().collect(Collectors.joining(""));
        String s1 = code.substring(0,3);
        list.add(s1);
        String s2 = code.substring(1,4);
        list.add(s2);
        String s3 = code.substring(0,2) + code.substring(3);
        list.add(s3);
        String s4 = code.substring(0,1) + code.substring(2);
        list.add(s4);
        return list;
    }

    public static List<String> get4XCodes (String drawCode) {
        List<String> list = Lists.newArrayList();
        String[] ss = drawCode.split("");
        List<String> strings = Lists.newArrayList(ss);
        String code = strings.stream().sorted().collect(Collectors.joining(""));
        list.add(code);
        return list;
    }

    public static void main(String[] args) {
        // 直选和值：号码相同，位置相同如：和值2,6注（002,020,200,101,110,011），开002， 则只中一注002，其他不中
        // 组3和组6和值：号码相同，任意位置如：和值4，3注（112,004,022），开400或004，则都中奖一注
        // 和值号码
        String hzCode = "9";
        String b = "2";
        String s = "1";
        String g = "0";
        Integer hz = Integer.valueOf(b) + Integer.valueOf(s) + Integer.valueOf(g);
        // 和值-开奖号码三数和加起来等于和值则中奖
        if (hzCode.equals(hz.toString())) {
//            System.out.println("和值中奖");
        }

        String code = b + s + g;
        // 通选号码-匹配任意两位则中奖
        int txCount = countSameStr(code, "145");
        if (txCount > 1) {
//            System.out.println("通选中奖");
        }

        // 判断号码是组3(有重复号码组合)还是组6(没有重复号码组合)
        int groupCount = getCountByCode(code);
        if (groupCount > 1) {
//            System.out.println("组3号码");
            int z3Count = countSameStr(code, "144");
            // 组3-任意位置3个号码相同则中奖
            if (z3Count >= 2) {
//                System.out.println("组3中奖");
            }
        }else {
//            System.out.println("组6号码");
            int z3Count = countSameStr(code, "514");
            // 组6-任意位置3个号码相同则中奖
            if (z3Count >= 2) {
//                System.out.println("组6中奖");
            }
        }

        // 1D中奖-百十个位某一个位置数字正确且位置相同则中奖
        String d1 = "4";
        if (s.equals(d1)) {
//            System.out.println("1D中奖");
        }

        // 猜1D中奖-根据购买的1个号码和开奖号码判断中奖号码个数
        if (s.contains(d1)) {
            int count = sameCount(code, d1);
//            System.out.println("猜1D中奖号码个数：" + count);
        }

        // 2D中奖-百十个位某2个位置数字正确且位置相同则中奖
        String d2 = "45";
        String d = s + g;
        if (d.equals(d2)) {
//            System.out.println("2D中奖");
        }

        // 猜2D中奖-根据购买的2个号码和开奖号码判断中奖号码个数
        if (d.contains(d2)) {
            int d2Count = getCountByCode(d2);
            int count = countSameStr(code, d2);
            if (d2Count > 1) {
//                System.out.println("2同号中奖：" + count);
            }else {
//                System.out.println("2不同号中奖：" + count);
            }
        }

        // 包选中奖
        // 1. 包选三全中
        if (code.equals("144")) {
//            System.out.println("包选3全中");
        }
        // 2. 包选三组中
        int bGroupCount = getCountByCode(code);
        if (bGroupCount > 1) {
            int bz3Count = countSameStr(code, "144");
            // 组3-任意位置3个号码相同则中奖
            if (bz3Count == 3) {
//                System.out.println("包选三组中");
            }
        }else {
            int bz3Count = countSameStr(code, "514");
            // 组6-任意位置3个号码相同则中奖
            if (bz3Count == 3) {
//                System.out.println("包选六组中");
            }
        }

        if (hz >= 0 && hz <= 8) {
//            System.out.println("猜大小：小");
        }else if (hz >= 19 && hz <= 27){
//            System.out.println("猜大小：大");
        }

        Integer bai = Integer.valueOf(b);
        Integer shi = Integer.valueOf(s);
        Integer ge = Integer.valueOf(g);
        if (bai % 2 == 0 && shi % 2 == 0 && ge % 2 == 0) {
//            System.out.println("猜奇偶：偶");
        }else if (bai % 2 != 0 && shi % 2 != 0 && ge % 2 != 0) {
//            System.out.println("猜奇偶：奇");
        }else {
//            System.out.println("不是奇偶");
        }

        if (bai == shi && bai == ge) {
//            System.out.println("三同号码");
        }

        int[] arrays = {bai, shi, ge};
        boolean flag = true;
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0) {
                int num = arrays[i];
                int num2 = arrays[i - 1] + 1;
                if (num != num2) {
                    flag = false;
                    break;
                }
            }
        }

        boolean flag2 = true;
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0) {
                int num = arrays[i];
                int num2 = arrays[i - 1] - 1;
                if (num != num2) {
                    flag2 = false;
                    break;
                }
            }
        }

        if (flag || flag2) {
//            System.out.println("是拖拉机");
        }
    }

    /**
     * 是否拖拉机号码
     * @param bai
     * @param shi
     * @param ge
     * @return true:是 false:否
     */
    public static boolean isTlj(Integer bai, Integer shi, Integer ge) {
        int[] arrays = {bai, shi, ge};
        boolean flag = true;
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0) {
                int num = arrays[i];
                int num2 = arrays[i - 1] + 1;
                if (num != num2) {
                    flag = false;
                    break;
                }
            }
        }

        boolean flag2 = true;
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0) {
                int num = arrays[i];
                int num2 = arrays[i - 1] - 1;
                if (num != num2) {
                    flag2 = false;
                    break;
                }
            }
        }
        if (flag || flag2) {
            return true;
        }
        return false;
    }

    public static int sameCount (String s1, String s2) {
        int codeLength = s1.length();
        int code1D = s2.length();
        int count = 0;
        int index = 0;
        if (codeLength >= code1D) {
            while ((index = s1.indexOf(s2, index)) != -1) {
                count++;
                index += code1D;
            }
        }
        return count;
    }

    public static int countSameStr (String s1, String s2) {
        int count = 0;
        for (int i = 0; i < s1.length(); i ++) {
            for (int j = 0; j < s2.length(); j ++) {
                if (s1.charAt(i) == s2.charAt(j)) {
                    count ++;
                    break;
                }
            }
        }
        return count;
    }

    /**
     * 通选中2的中奖号码
     * @return
     */
    public static List<String> tx2DrawCode (String drawCode){
        // 904-90
        String code1 = drawCode.substring(0, 2);
        // 904-04
        String code2 = drawCode.substring(1, 3);
        // 904-9
        String c1 = drawCode.substring(0, 1);
        // 904-4
        String c2 = drawCode.substring(2, 3);
        // 94
        String code3 = c1 + c2;
        List<String> list = Lists.newArrayList();
        list.add(code1);
        list.add(code2);
        list.add(code3);
        return list;
    }

    /**
     * 字符串去重
     * @param s
     * @return
     */
    public static String removeRepeatChar(String s) {
        if (s == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = s.length();
        while (i < len) {
            char c = s.charAt(i);
            sb.append(c);
            i++;
            while (i < len && s.charAt(i) == c) {//这个是如果这两个值相等，就让i+1取下一个元素
                i++;
            }
        }
        return sb.toString();
    }

    public static void z3Combine(char[] str, int start, int end, List<String> list){
        if (start == end){
            list.add(Arrays.toString(str).replaceAll(", ", "").replaceAll("\\[", "").replaceAll("]", ""));
        }else {
            for(int j=start;j<end;j++){
                if (str[start] == str[j] && start!=j) continue;  //不同位置的相同字符不交换，避免重复
                swap(str,start,j);
                z3Combine(str,start+1,end, list);//递归
                swap(str,start,j);  //还原字符串，为下一for循环准备
            }
        }

    }

    public static void swap(char[] str, int i, int j){
        char tmp = str[i];
        str[i] = str[j];
        str[j] = tmp;
    }

}
