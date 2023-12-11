package com.beans;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class Proxy {
    private String id;

    /**
     * ip
     */
    private String ip;

    /**
     * 状态:0禁用，1启用，2已到期
     */
    private Integer status;

    /**
     * 到期时间
     */
    private Date dueTime;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    @TableField(exist = false)
    private String area;
}
