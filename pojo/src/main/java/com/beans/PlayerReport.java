package com.beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家日报表信息
 */
@Data
public class PlayerReport {

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
     * 统计时间
     */
    private Date countDate;

    /**
     * 统计时间
     */
    private Date createTime;

    /**
     * 当日上分
     */
    private BigDecimal upPoints = BigDecimal.ZERO;

    /**
     * 当日下分
     */
    private BigDecimal downPoints = BigDecimal.ZERO;

    /**
     * 当日总投
     */
    private BigDecimal totalBuyPoints = BigDecimal.ZERO;


    /**
     * 当日回水
     */
    private BigDecimal huishui = BigDecimal.ZERO;

    /**
     * 当日下分
     */
    private Integer totalBuyAmount = 0;

    /**
     * 当日中奖
     */
    private BigDecimal totalDrawPoints = BigDecimal.ZERO;


    /**
     * 当日盈亏
     */
    private BigDecimal totalEarnPoints = BigDecimal.ZERO;



}
