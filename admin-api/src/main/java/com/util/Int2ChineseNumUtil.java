package com.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Int2ChineseNumUtil {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        System.out.print("请输入FinalShell的离线机器码：");
        @SuppressWarnings("resource")
        //Scanner reader = new Scanner(System.in);
                String machineCode = "fantasyovo@d7e066abdd87dba0"; // 在此处放置机器码
        generateKey(machineCode);
    }

    public static void generateKey(String hardwareId) throws NoSuchAlgorithmException {
        String proKey = transform(61305 + hardwareId + 8552);
        String pfKey = transform(2356 + hardwareId + 13593);
//        System.out.println("请将此行复制到离线激活中：" + proKey);
    }

    public static String transform(String str) throws NoSuchAlgorithmException {

        @SuppressWarnings("unused")
        String md5 = hashMD5(str);

        return hashMD5(str).substring(8, 24);
    }

    public static String hashMD5(String str) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hashed = digest.digest(str.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) {
            int len = b & 0xFF;
            if (len < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(len));
        }
        return sb.toString();
    }

    public static String int2chineseNum(int src) {
        final String num[] = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        final String unit[] = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千"};
        String dst = "";
        int count = 0;
        while(src > 0) {
            dst = (num[src % 10] + unit[count]) + dst;
            src = src / 10;
            count++;
        }
        return dst.replaceAll("零[千百十]", "零").replaceAll("零+万", "万")
                .replaceAll("零+亿", "亿").replaceAll("亿万", "亿零")
                .replaceAll("零+", "零").replaceAll("零$", "");
    }
}
