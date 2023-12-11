package com.model.req;

import lombok.Data;

@Data
public class BpGroupPeiRateReq {

    /**
     * 第一批分批条数
     */
    private Integer firstGroupCount;

    /**
     * 第一批第一条截止金额放
     */
    private Integer firstEndAmount;

    /**
     * 第一批截止金额循环递增
     */
    private Integer firstEndAmountLoopAdd;

    /**
     * 第一批赔率上限循环递减
     */
    private Integer firstPeiRateUpperLimitLoopSubtract;

    /**
     * 第一批截止金额为
     */
    private Integer firstEndAmount2;

    /**
     * 第一批赔率降到
     */
    private Integer firstPeiRateLower;

    /**
     * 第二批分批条数
     */
    private Integer secondGroupCount;

    /**
     * 第二批第一条截止金额放
     */
    private Integer secondEndAmount;

    /**
     * 第二批截止金额循环递增
     */
    private Integer secondEndAmountLoopAdd;

    /**
     * 第二批赔率上限循环递减
     */
    private Integer secondPeiRateUpperLimitLoopSubtract;

    /**
     * 第二批截止金额为
     */
    private Integer secondEndAmount2;

    /**
     * 第二批赔率降到
     */
    private Integer secondPeiRateLower;

    /**
     * 第三批分批条数
     */
    private Integer thirdGroupCount;

    /**
     * 第三批第一条截止金额放
     */
    private Integer thirdEndAmount;

    /**
     * 第三批截止金额循环递增
     */
    private Integer thirdEndAmountLoopAdd;

    /**
     * 第三批赔率上限循环递减
     */
    private Integer thirdPeiRateUpperLimitLoopSubtract;

    /**
     * 第三批截止金额为
     */
    private Integer thirdEndAmount2;

    /**
     * 第三批赔率降到
     */
    private Integer thirdPeiRateLower;

    /**
     * 号码类型ID
     */
    private String lotterySettingId;
}
