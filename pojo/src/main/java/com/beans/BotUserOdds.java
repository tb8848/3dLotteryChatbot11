package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 机器人账号赔率
 */
@Data
public class BotUserOdds {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 机器人账号ID
     */
    private String botUserId;

    /**
     * 赔率
     */
    private String odds;

    /**
     * 最小限额
     */
    private BigDecimal minBuy;

    /**
     * 最大限额
     */
    private Long maxBuy;

    /**
     * 小分类ID
     */
    private String lotterySettingId;

    /**
     * 大分类ID
     */
    private String lotteryMethodId;

    /**
     * 父级ID
     */
    private String parentId;

    private Integer shortNo;

    private String bettingRule;

    /**
     * 彩种：1福彩3D，2排列三
     */
    private Integer lotteryType;

}
