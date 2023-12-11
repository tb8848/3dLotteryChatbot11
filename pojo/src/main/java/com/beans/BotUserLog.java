package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 机器人操作日志
 */
@Data
public class BotUserLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 机器人账号ID
     */
    private String userId;

    /**
     * 操作内容
     */
    private String contents;

    /**
     * 操作时间
     */
    private Date optTime;

}
