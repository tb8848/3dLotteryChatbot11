package com.websocket;
/**
 * 消息
 */

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    String from; //从哪里发送的
    private String to;  //发送给谁
    private String message; //发送的内容
    private Date   sendTime; //发送时间
    private Source source; //用户来源
}
