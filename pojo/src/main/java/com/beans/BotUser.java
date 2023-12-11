package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 机器人账号信息
 */
@Data
public class BotUser {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 登录密码
     */
    private String loginPwd;

    /**
     * 到期时间
     */
    private Date dueDate;

    /**
     * 最后一次登录时间
     */
    private Date lastLoginTime;

    /**
     * 登录IP
     */
    private String lastLoginIp;

    /**
     * 状态，1：正常，0：删除；2：已过期
     */
    private Integer status;

    /**
     * 福彩3d会员登录网址
     */
    private String lottery3dUrl;

    /**
     * 福彩3d会员登录账号
     */
    private String lottery3dAccount;

    /**
     * 福彩3d会员登录密码
     */
    private String lottery3dPwd;

    /**
     * 登录token
     */
    private String login3dToken;


    @TableField(exist = false)
    private Integer useMonth;

    /**
     * 安全密码
     */
    private String safePwd;

    /**
     * 回水值
     */
    private BigDecimal hsValue;

    /**
     * 假人数量
     */
    private Integer botCount;

    /**
     * 拦货占成
     */
    private Integer lhzc;

    private String wxId;

    private String wxNick;

    private String wxHeadimg;

    private Integer wxStatus;

    private String qrUUid;

    private Date wxLoginTime;

    /**
     * 剩余有效天数
     */
    @TableField(exist = false)
    private long validDays;


    private Integer onlineStatus;

    @TableField(exist = false)
    private Integer useType;

    /**
     * 归属彩票类型，1:3D，2：P3,3:3D+P3
     */
    private Integer lotteryType;
}
