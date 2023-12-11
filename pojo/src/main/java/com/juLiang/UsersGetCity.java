package com.juLiang;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取对应省份下的可用代理城市
 */
@Data
@Accessors(chain = true)
public class UsersGetCity {
    /**
     * key 用户秘钥
     */
    private String key;
    /**
     * 用户Id
     */
    private String user_id;
    /**
     * 城市名称
     */
    private String province;
}
