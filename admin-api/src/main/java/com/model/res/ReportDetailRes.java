package com.model.res;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportDetailRes {
    private String userId;

    private String userName;

    /**
     * 上分
     */
    private BigDecimal upScore = BigDecimal.ZERO;

    /**
     * 下分
     */
    private BigDecimal downScore = BigDecimal.ZERO;

    /**
     * 总投
     */
    private BigDecimal totalMoney = BigDecimal.ZERO;

    /**
     * 回水
     */
    private BigDecimal totalHs = BigDecimal.ZERO;

    /**
     * 总投笔数
     */
    private Integer totalCount = 0;

    /**
     * 中奖
     */
    private BigDecimal drawMoney = BigDecimal.ZERO;

    /**
     * 盈亏
     */
    private BigDecimal profitLossMoney = BigDecimal.ZERO;

}
