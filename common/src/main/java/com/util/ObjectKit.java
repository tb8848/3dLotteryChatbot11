package com.util;

import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;


public class ObjectKit {

    /**
     * 对象转map
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> getObjectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (StrUtil.equals(fieldName, "key")) {
                continue;
            }
            Object value = field.get(obj);
            if (value == null) {
                value = "";
            }
            map.put(fieldName, value);
        }
        return map;
    }

}
