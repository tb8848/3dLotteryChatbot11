package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18nUtils {

    @Autowired
    private LocaleMessage localeMessage;

    /**
     * 获取key
     *
     * @param key
     * @return
     */
    public  String getKey(String key) {
        String name = localeMessage.getMessage(key);
        return name;
    }

    /**
     * 获取指定哪个配置文件下的key
     *
     * @param key
     * @param local
     * @return
     */
    public String getKey(String key, Locale local) {
        String name = localeMessage.getMessage(key, local);
        return name;
    }

    /**
     * 占位符方式获取key
     * @param key
     * @param args
     * @param local
     * @return
     */
    public String getKey (String key,Object[] args,Locale local ) {
        String name = localeMessage.getMessage(key, args, local);
        return name;
    }
}
