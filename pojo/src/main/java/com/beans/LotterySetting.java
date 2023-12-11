package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class LotterySetting implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;


    /**
     * 下注规则
     */
    private String bettingRule;

    /**
     * 下注方式ID
     */
    private String lotteryMethodId;

    /**
     * 最小下注金额
     */
    private BigDecimal minBettingPrice;

    /**
     * 单注上限
     */
    private Long maxBettingCount;

    /**
     * 单项上限
     */
    private Long maxNumberTypeCount;

    /**
     * 拦货上限
     */
    private Integer lanHuoUpperLimit;

    /**
     * 父级ID
     */
    private String parentId;

    /**
     * 赔率上限
     */
    private String peiRate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 赔率下限
     */
    private String peiRateLowerLimit;


    /**
     * 会员ID
     */
    @TableField(exist = false)
    private String vipId;

    /**
     * 回水
     */
    @TableField(exist = false)
    private String huiShui;

    /**
     * 会员的赔率
     */
    @TableField(exist = false)
    private String vipPeiRate;

    @TableField(exist = false)
    private boolean open = true;

    /**
     * 上级赔率
     */
    @TableField(exist = false)
    private String parentPeiRate;

    /**
     * 负值
     */
    @TableField(exist = false)
    private BigDecimal fuzhi = BigDecimal.ZERO;

    @TableField(exist = false)
    private Integer codesAmount;

    /**
     * 拦货金额
     */
    @TableField(exist = false)
    private Integer lanHuoAmount;

    /**
     * 泰语
     */
    private String bettingRuleTh;

    /**
     * 赔率类型：是几重赔率
     */
    private Integer peiRateType;

    /**
     * 号码类型子ID
     */
    private Integer typeId;

    /**
     * 排序字段
     */
    private Integer shortNo;

    /**
     * 玩法提示
     */
    private String waysTips;

    /**
     * 显示和隐藏： 1：显示，0：隐藏
     */
    private Integer isShow;
    /**
     * 彩种：1福彩3D，2排列三
     */
//    private Integer lotteryType;
}
