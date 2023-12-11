package com.model.res;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单明细
 */
@Data
public class BillsDetailRes {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String headImg;

    /**
     * 用户总上分
     */
    private BigDecimal upScore = BigDecimal.ZERO;

    /**
     * 用户总下分
     */
    private BigDecimal downScore = BigDecimal.ZERO;

    /**
     * 用户总投
     */
    private BigDecimal totalMoney = BigDecimal.ZERO;

    /**
     * 用户总回水
     */
    private BigDecimal totalHs = BigDecimal.ZERO;
}
