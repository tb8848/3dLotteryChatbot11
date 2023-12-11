package com.beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家返水记录
 */
@Data
public class PlayerReturnPoints {

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
     * 期号
     */
    private Integer drawNo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 返还时间
     */
    private Date returnTime;

    /**
     * 返还回水金额
     */
    private BigDecimal returnPoints = BigDecimal.ZERO;

    /**
     * 状态，0：未处理，1：已处理
     */
    private Integer status;

    /**
     * 审核状态，0：后台处理；1：玩家自助；2：自动返还
     */
    private Integer returnType;

    /**
     * 玩家所属机器人ID
     */
    private String botUserId;

    /**
     * 返点值
     */
    private BigDecimal hsPercent;


    /**
     * 有效流水
     */
    private BigDecimal validBuyPoints;


    @TableField(exist = false)
    private Player player;

    /**
     * 玩家名称
     */
    @TableField(exist = false)
    private String playerName;


    @TableField(exist = false)
    private String playerHeadimg;

    /**
     * 玩家所属机器人
     */
    @TableField(exist = false)
    private String botName;


    private Integer lotteryType;


}
