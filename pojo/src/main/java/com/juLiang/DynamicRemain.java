package com.juLiang;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 动态代理--获取代理剩余可用时长
 */
@Data
@Accessors(chain = true)
public class DynamicRemain {

    /**
     * key 业务秘钥
     */
    private String key;
    /**
     * 业务编号
     */
    private String trade_no;
    /**
     * 代理列表
     */
    private String proxy;
}
