package com.model.res;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账单返回值对象
 */
@Data
public class BillsRes {
    /**
     * 机器人ID
     */
    private String botUserId;

    /**
     * 机器人总上分
     */
    private BigDecimal botUpScore = BigDecimal.ZERO;

    /**
     * 机器人总下分
     */
    private BigDecimal botDownScore = BigDecimal.ZERO;

    /**
     * 机器人总投
     */
    private BigDecimal botTotalMoney = BigDecimal.ZERO;

    /**
     * 机器人总回水
     */
    private BigDecimal botTotalHs = BigDecimal.ZERO;

    /**
     * 账单明细列表
     */
    private List<BillsDetailRes> billsDetailResList;
}
