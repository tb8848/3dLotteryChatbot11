package com.juLiang;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 动态代理--检查代理IP可用性
 */
@Data
@Accessors(chain = true)
public class DynamicCheck {



    /**
     * key 业务秘钥
     */
    private String key;
    /**
     * 业务号
     */
    private String trade_no;
    /**
     * 代理列表
     */
    private String proxy;


}
