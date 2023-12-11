package com.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BotUserCountData {

    private String botUserId;

    /**
     * 玩家积分
     */
    private BigDecimal playerTotalPoints = BigDecimal.ZERO;

    /**
     * 当日总投
     */
    private BigDecimal dayBuyMoney = BigDecimal.ZERO;

    /**
     * 当日盈亏
     */
    private BigDecimal dayEarnMoney = BigDecimal.ZERO;

    /**
     * 当日上分
     */
    private BigDecimal dayUpPoints = BigDecimal.ZERO;

    /**
     * 当日下分
     */
    private BigDecimal dayDownPoints = BigDecimal.ZERO;

    /**
     * 玩家回水
     */
    private BigDecimal playerTotalHs = BigDecimal.ZERO; //玩家回水

    /**
     * 网盘余额
     */
    private BigDecimal netBalance = BigDecimal.ZERO;

    /**
     * 当期报网
     */
    private BigDecimal netBuyMoney = BigDecimal.ZERO;

    /**
     * 网盘回水
     */
    private BigDecimal netHsMoney = BigDecimal.ZERO;
}
