package com.model.res;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportRes {
    private String playerId;

    /**
     * 用户上分
     */
    private BigDecimal upScore = BigDecimal.ZERO;

    /**
     * 用户下分
     */
    private BigDecimal downScore = BigDecimal.ZERO;

    /**
     * 用户总投
     */
    private BigDecimal totalMoney = BigDecimal.ZERO;

    /**
     * 用户回水
     */
    private BigDecimal totalHs = BigDecimal.ZERO;

    /**
     * 用户总投笔数
     */
    private Integer totalCount = 0;

    /**
     * 用户中奖
     */
    private BigDecimal drawMoney = BigDecimal.ZERO;

    /**
     * 用户盈亏
     */
    private BigDecimal profitLossMoney = BigDecimal.ZERO;
}
