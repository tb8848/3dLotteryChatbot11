package com.vo;

import lombok.Data;

/**
 * 明细查询条件vo
 */
@Data
public class DrawBuyRecordSearchVO extends SearchVO {

    //查询号码
    String buyCodes;

    /**
     * 范围查询最小值
     */
    Double minValue;

    /**
     * 范围查询最大值
     */
    Double maxValue ;

    /**
     * 数据类型
     * 0 全部,1 退码，2快打，3快选，4导入，5二定，6汇总表 -1 号码规则大类，-2号码规则小类
     */
    Integer dataType ;

    /**
     * 号码规则小类ID,当dataType=-2时有效
     */
    String lotterySettingsId ;

    /**
     * 号码规则大类,当dataType=-1时有效
     */
    String lotteryMethodId;

    /**
     * 范围查询类型 0，赔率，1：金额，2：退码
     */
    Integer colType ;

    /**
     * 明细类型 0：明细；1：中奖
     */
    Integer codeWinType; //1：中奖

    Integer isXian;

    Integer buyType;

    String printCacheNo;

    String baopaiId; //包牌汇总记录ID

}
