package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 机器人盘口信息
 */
@Data
public class BotUserPan {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;


    /**
     * 盘口网址
     */
    private String lottery3dUrl;

    /**
     * 盘口登录账号
     */
    private String lottery3dAccount;

    /**
     * 盘口登录密码
     */
    private String lottery3dPwd;

    /**
     * 登录token
     */
    private String login3dToken;

    /**
     * 归属机器人账号ID
     */
    private String botUserId;

    /**
     * 激活状态，0：未激活；1：已激活
     */
    private Integer activeStatus;

    /**
     * 彩种类型，1：福彩3D，2：排列三
     */
    private Integer lotteryType;

}
