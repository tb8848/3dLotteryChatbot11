package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LotteryMethod implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 所属彩票ID
     */
    private String lotteryTypeId;

    /**
     * 下注方式
     */
    private String bettingMethod;

    /**
     * 赔率与回水值的比例值
     */
    private String baseline;


    @TableField(exist = false)
    private List<LotterySetting> lotterySettingList;

    @TableField(exist = false)
    private List<BotUserOdds> botUserOddsList;

    /**
     * 负值
     */
    @TableField(exist = false)
    private BigDecimal fuzhi = BigDecimal.ZERO;

    private String bettingMethodTh;

    /**
     * 排序
     */
    private Integer shortNo;
}
