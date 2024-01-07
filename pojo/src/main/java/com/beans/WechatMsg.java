package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class WechatMsg {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String msgId;

    private String fromUser;

    private String fromId;

    private String toId;

    private String toUser;

    private String content;

    private Date receiveTime;
}
