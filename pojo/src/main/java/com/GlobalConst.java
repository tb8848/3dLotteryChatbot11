package com;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GlobalConst {


    public static ConcurrentMap<String,String> msgBuffer = new ConcurrentHashMap<>();

    //下注指令
    public static String[] keywords = {"单选","直选普通","直选","直选和值","通选","组三普通","组三","组三胆拖","组三和值","组六普通","组六","组六胆拖","包选三","包选六",
            "组六和值","和数","1D","猜1D","2D","猜2D","大小","大","小","奇偶","奇","偶","拖拉机","三同号","猜三同","跨度","3D","P3","双飞组三","双飞组六","复式"};


    public static String[] keywords1 = {"单选","直选普通","直选和值","直选","通选","分笔组三","分笔组六","分笔复式","组三和值","组三","双飞组三","组六和值","组六","双飞组六","包选三","包选六",
           "和数","1D","猜1D","2D","猜2D","大小","大","小","奇偶","奇","偶","拖拉机","三同号","猜三同","跨度","3D","P3","复式","独胆"};


    public static String[] keywords2 = {"福","体","单选","直选","分笔组三","分笔组六","分笔复式","组三","双飞组三","组六","双飞组六",
            "和数","1D","2D","大小","大","小","奇偶","奇","偶","拖拉机","三同号","猜三同","跨度","3D","P3","复式","独胆"};



    //通用指令
    public static String[] commonCmd = {"上分","下分","作业格式","流水","盛鱼","返","查","盈亏"};
}
