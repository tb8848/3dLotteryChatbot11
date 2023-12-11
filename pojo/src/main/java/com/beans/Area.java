package com.beans;

import lombok.Data;

@Data
public class Area {
    private String id;

    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 上级代码
     */
    private String parentCode;

    /**
     * 级别
     */
    private String level;
}
