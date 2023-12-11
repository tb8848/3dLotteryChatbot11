package com.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GlobalConst {


    public static ConcurrentMap<String,String> msgBuffer = new ConcurrentHashMap<>();
}
