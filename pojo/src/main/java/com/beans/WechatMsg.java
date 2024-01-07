package com.beans;

import lombok.Data;

import java.util.Date;

@Data
public class WechatMsg {

    private String id;

    private String msgId;

    private String fromUser;

    private String fromId;

    private String toId;

    private String toUser;

    private String content;

    private Date receiveTime;
}
