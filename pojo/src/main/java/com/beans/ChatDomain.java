package com.beans;

import lombok.Data;

import java.util.Date;

@Data
public class ChatDomain {
    private String id;

    /**
     * 域名地址
     */
    private String url;

    /**
     * 状态：0禁用，1正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}
