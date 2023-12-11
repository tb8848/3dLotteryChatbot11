package com.juLiang;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 动态代理--提取代理IP
 */
@Data
@Accessors(chain = true)
public class DynamicGetIps {

    /**
     * key 业务秘钥
     */
    private String key;
    /**
     * 业务编号
     */
    private String trade_no;
    /**
     * 提取数量
     */
    private Integer num;
    /**
     * 代理类型:1 http/https、2 socks4/socks5
     */
    private Integer pt;
    /**
     * 返回类型：json, text,xml
     */
    private String result_type;
    /**
     * 结果分隔符
     */
    private Integer split;
    /**
     * 地区名称
     */
    private Integer city_name;
    /**
     * 邮政编码
     */
    private Integer city_code;
    /**
     * 剩余可用时长
     */
    private Integer ip_remain;
    /**
     * 筛选地区
     */
    private String area;
    /**
     * 排除地区
     */
    private String no_area;
    /**
     * 筛选ip段
     */
    private String ip_seg;
    /**
     * 排除IP段
     */
    private String no_ip_seg;
    /**
     * 运营商筛选
     */
    private String isp;
    /**
     * IP去重
     */
    private Integer filter;



}
