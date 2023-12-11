package com.model.res;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReportRes {

    /**
     * 总上分
     */
    private BigDecimal totalUpScore = BigDecimal.ZERO;

    /**
     * 下分
     */
    private BigDecimal totalDownScore = BigDecimal.ZERO;

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
     * 总中奖
     */
    private BigDecimal totalDrawMoney = BigDecimal.ZERO;

    /**
     * 总盈亏
     */
    private BigDecimal totalProfitLossMoney = BigDecimal.ZERO;

    private List<ReportDetailRes> reportDetailResList;

}
