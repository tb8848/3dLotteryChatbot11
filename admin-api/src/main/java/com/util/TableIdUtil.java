package com.util;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.Snowflake;

public class TableIdUtil {

    public static String getTableId() {
        Snowflake snowflake = Singleton.get(Snowflake.class, 2L, 1L, true);
        return snowflake.nextIdStr();
    }
}
