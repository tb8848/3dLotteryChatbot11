package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class LocaleMessage {
    @Autowired
    private MessageSource messageSource;
    public String getMessage(String code){
        return this.getMessage(code,new Object[]{});
    }
    public String getMessage(String code,String defaultMessage){
        return this.getMessage(code,null,defaultMessage);
    }
    public String getMessage(String code, String defaultMessage, Locale locale){ return this.getMessage(code,null,defaultMessage,locale); }
    public String getMessage(String code,Locale locale){
        return this.getMessage(code,null,"",locale);
    }
    public String getMessage(String code,Object[] args){ return this.getMessage(code,args,""); }
    public String getMessage(String code,Object[] args,Locale locale){
        return this.getMessage(code,args,"",locale);
    }
    public String getMessage(String code,Object[] args,String defaultMessage){ return this.getMessage(code,args, defaultMessage, LocaleContextHolder.getLocale()); }
    public String getMessage(String code,Object[] args,String defaultMessage,Locale locale){
        //return messageSource.getMessage(code,args, defaultMessage,locale);
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(-1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setBasenames("/i18n/messages");

        String message = "";
        try {
            message = messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}
