package com.beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家定投设置
 */
@Data
public class PlayerFixedBuy {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 玩家ID
     */
    private String playerId;

    /**
     * 下注金额
     */
    private String buyPoints;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *  更新时间
     */
    private Date updateTime;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 下注描述
     */
    private String buyDesc;

    /**
     * 剩余期数
     */
    private Integer leftDraws;

    /**
     * 定投期数
     */
    private Integer followDraws;


    /**
     * 任务执行周期(0:中奖后停止，1:继续跟码，2:从头开始)
     */
    private Integer circleType = 1;

    /**
     * 任务状态，0：未运行，1：运行中，-1：已结束
     */
    private Integer taskStatus = 0;

    /**
     * 快选规则，json格式字符串
     */
    private String kuaixuanRule;

    /**
     * 是否为快捷下注，0:否，1:是
     */
    private Integer fastBuyFlag = 0;

    /**
     * 下注
     */
    private String stopReason;

    /*
       编号
     */
    private Integer planNo;

    /**
     * 机器人ID
     */
    private String userId;

    /**
     * 玩家名称
     */
    @TableField(exist = false)
    private String playerName;

    /**
     * 玩家头像
     */
    @TableField(exist = false)
    private String playerHeadImg;


    private Integer lotteryType;

}
