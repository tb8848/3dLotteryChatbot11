package com.util;

import java.util.Calendar;
import java.util.Date;

public class TTTT {

    public static void main(String[] args) {
        int c = CodeUtils.countSameStr("244", "144");
        System.out.println(c);

        int c2 = CodeUtils.getCountByCode("121");
        System.out.println(c2);

        int c3 = CodeUtils.countSameStr("444", "4");
        System.out.println(c3);
    }
}
