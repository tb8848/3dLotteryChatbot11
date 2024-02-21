package com.vo;

import lombok.Data;

import java.util.Map;

@Data
public class WechatApiMsgVo {

    private Long msgId;

    private String fromUserName;

    private String toUserName;

    private Integer msgType;

    private String content;

    private Integer status;



}
