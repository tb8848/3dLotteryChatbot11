package com.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

import java.net.URLEncoder;
import java.util.*;

/**
 * 字符串处理方法
 */
public class StrKit {

    /**
     * 计算签名
     *
     * @param object 对象
     * @param appKey 秘钥
     * @return 请求参数
     * @throws IllegalAccessException
     */
    public static Map<String, Object> getParams(Object object, String appKey) throws IllegalAccessException {
        Map<String, Object> params = ObjectKit.getObjectToMap(object);
        Map<String, Object> cleanParams = StrKit.cleanParams(params);
        String value = StrKit.formatUrlMap(cleanParams, false, false);
        value = value + "&key=" + appKey;
        String sign = SecureUtil.md5(value);
        cleanParams.put("sign", sign);
        return cleanParams;
    }

    /**
     * 格式化参数，去掉为空的参数
     *
     * @param kv 所有参数
     * @return
     */
    public static Map<String, Object> cleanParams(Map<String, Object> kv) {
        Map<String, Object> params = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : kv.entrySet()) {
            String value = String.valueOf(entry.getValue());
            if (StrUtil.isBlank(value)) {
                //如果不存在值则直接清理掉
                continue;
            }
            params.put(entry.getKey(), entry.getValue());
        }
        return params;
    }

    /**
     * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br>
     *
     * @param paraMap    要排序的Map对象
     * @param urlEncode  是否需要URLENCODE
     * @param keyToLower 是否需要将Key转换为全小写 true:key转化成小写，false:不转化
     * @return
     */
    public static String formatUrlMap(Map<String, Object> paraMap, boolean urlEncode, boolean keyToLower) {
        String buff = "";
        Map<String, Object> tmpMap = paraMap;
        try {
            List<Map.Entry<String, Object>> infoIds = new ArrayList<Map.Entry<String, Object>>(tmpMap.entrySet());
            Collections.sort(infoIds, new Comparator<Map.Entry<String, Object>>() {
                @Override
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return (o1.getKey()).toString().compareTo(o2.getKey());
                }
            });
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, Object> item : infoIds) {
                String key = item.getKey();
                String val = String.valueOf (item.getValue());
                if (urlEncode) {
                    val = URLEncoder.encode(val, "utf-8");
                }
                if (keyToLower) {
                    buf.append(key.toLowerCase() + "=" + val);
                } else {
                    buf.append(key + "=" + val);
                }
                buf.append("&");
            }
            buff = buf.toString();
            if (buff.isEmpty() == false) {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e) {
            return null;
        }
        return buff;
    }
}
