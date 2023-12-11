package com.config;


import com.beans.ResponseBean;
import com.util.StringUtil;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Aspect
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "lang", name = "open", havingValue = "true")
public class LanguageAspect {
    @Autowired
    I18nUtils i18nUtils;

    @Pointcut("execution(* com.action.*.*(..)))")
    public void annotationLangCut() {
    }

    /**
     * 拦截controller层返回的结果，修改msg字段
     *
     * @param point
     * @param obj
     */
    @AfterReturning(pointcut = "annotationLangCut()", returning = "obj")
    public void around(JoinPoint point, Object obj) {
        Object resultObject = obj;
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            //从获取RequestAttributes中获取HttpServletRequest的信息
            HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            String langFlag = request.getHeader("lang");
            if (StringUtil.isNull(langFlag)) {
                langFlag = "zh";
            }
            if (StringUtil.isNotNull(langFlag)) {
                ResponseBean r = (ResponseBean) obj;
                if (r == null || StringUtil.isNull(r.getMsg())) {
                    return;
                }
                if(!r.isNoTrans()){//翻译开关打开
                    String msg = r.getMsg().trim();
                    Object[] objects = null;
                    if (r.getData() instanceof Object[]) {
                        objects = (Object[]) r.getData();
                    }
                    if (msg.startsWith("查询") ||msg.startsWith("成功") || msg.startsWith("已登录")) {
                        return;
                    }
                    if(r.isNoTrans()){
                        return;
                    }
                    if ("zh".equals(langFlag)) {
                        Locale locale = Locale.CHINA;
                        if (null != objects && objects.length > 0) {
                            msg = i18nUtils.getKey(msg, objects,locale);
                        }else {
                            msg = i18nUtils.getKey(msg, locale);
                        }
                    } else if ("th".equals(langFlag)) {
                        Locale locale = new Locale("th");
                        if (null != objects && objects.length > 0) {
                            msg = i18nUtils.getKey(msg, objects,locale);
                        }else {
                            msg = i18nUtils.getKey(msg, locale);
                        }
                    }else {
                        msg = i18nUtils.getKey(msg);
                    }
                    r.setMsg(msg);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            //返回原值
            obj = resultObject;
        }
    }
}
