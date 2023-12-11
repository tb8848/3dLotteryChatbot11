package com.vo;

import lombok.Data;

import java.util.Map;

@Data
public class WechatApiMsgVo {

    private Long MsgId;

    private Map<String,String> FromUserName;

    private Map<String,String> ToUserName;

    private Integer MsgType;

    private Map<String,String> Content;

    private Integer Status;



}
