package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 聊天室
 */
@Data
public class ChatRoom {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 登录账号
     */
    private String userId;

    /**
     * 登录密码
     */
    private String roomName;

    /**
     * 创建时间
     */
    private Date createTime;


}
