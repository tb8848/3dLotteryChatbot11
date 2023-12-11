package com.util;

import cn.hutool.http.HttpUtil;
import com.juLiang.*;
import com.juLiang.enums.URL;

import java.util.Map;

/**
 * 巨量ip接口工具类
 */
public class JuLiangUtil {

    /**
     * 获取账户余额
     *
     * @param usersGetBalance 获取账户余额请求对象
     * @return
     * @throws IllegalAccessException
     */
    public static String usersGetBalance(UsersGetBalance usersGetBalance) throws IllegalAccessException {
        Map<String, Object> params = StrKit.getParams(usersGetBalance, usersGetBalance.getKey());
        return HttpUtil.post(URL.USERS_GETBALANCE.getValue(), params);
    }


    /**
     * 获取对应省份下的可用代理城市
     * @param usersGetCity
     * @return
     * @throws IllegalAccessException
     */
    public static String getCity(UsersGetCity usersGetCity) throws IllegalAccessException {
        Map<String, Object> params = StrKit.getParams(usersGetCity, usersGetCity.getKey());
        return HttpUtil.post(URL.USERS_GETCITY.getValue(),params);
    }

    /**
     * 提取动态代理
     * <br/> 不需要的参数不传即可
     *
     * @param dynamicGetIps 动态代理参数
     * @return
     * @throws IllegalAccessException
     */
    public static String dynamicGetIps(DynamicGetIps dynamicGetIps) throws IllegalAccessException {
        Map<String, Object> params = StrKit.getParams(dynamicGetIps, dynamicGetIps.getKey());
        return HttpUtil.post(URL.DYNAMIC_GETIPS.getValue(), params);
    }

    /**
     * 动态代理 -- 校验IP可用性
     *
     * @param dynamicCheck 校验IP请求对象
     * @return
     * @throws IllegalAccessException
     */
    public static String dynamicCheck(DynamicCheck dynamicCheck) throws IllegalAccessException {
        Map<String, Object> params = StrKit.getParams(dynamicCheck, dynamicCheck.getKey());
        return HttpUtil.post(URL.DYNAMIC_CHECK.getValue(), params);
    }

    /**
     * 动态代理 -- 获取代理剩余可用时长
     *
     * @param dynamicRemain 获取代理剩余可用时长请求对象
     * @return
     * @throws IllegalAccessException
     */
    public static String dynamicRemain(DynamicRemain dynamicRemain) throws IllegalAccessException {
        Map<String, Object> params = StrKit.getParams(dynamicRemain, dynamicRemain.getKey());
        return HttpUtil.post(URL.DYNAMIC_REMAIN.getValue(), params);
    }
}
