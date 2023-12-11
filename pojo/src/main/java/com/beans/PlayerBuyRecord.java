package com.beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家下注信息
 * 类似汇总记录，具体号码明细写入分表
 */
@Data
public class PlayerBuyRecord {

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
     * 下注金额
     */
    private BigDecimal buyPoints;

    /**
     * 下注数量
     */
    private Integer buyAmount;

    /**
     * 下注时间
     */
    private Date buyTime;

    /**
     * 结算时间
     */
    private Date settlementTime;


    /**
     * 回水金额
     */
    private BigDecimal hsPoints = BigDecimal.ZERO;

    /**
     * 中奖金额
     */
    private BigDecimal drawPoints = BigDecimal.ZERO;


    /**
     * 盈亏金额
     */
    private BigDecimal earnPoints = BigDecimal.ZERO;

    /**
     * 下注说明
     */
    private String buyDesc;

    /**
     * 下注类别（0：报网，1：吃奖，2：假托）
     */
    private Integer buyType;

    /**
     * 来源，1：定投，0：手动下注,-1:假人下注
     */
    private Integer  buyFrom=0;

    /**
     * 快选规则，json格式字符串
     */
    private String kuaixuanRule;


    /**
     * 结算状态，0：未结算，1：已结算，-1：已退码，-2：失败
     */
    private Integer buyStatus;

    /**
     * 所属机器人ID
     */
    private String botUserId;

    /**
     * 定投状态,0:失败，1：成功
     */
    private Integer dtStatus;

    /**
     * 定投状态描述
     */
    private String dtDesc;


    @TableField(exist = false)
    private Player player;

    /**
     * 定投任务ID
     */
    private String dtTaskId;


    private Integer lotteryType;


}
