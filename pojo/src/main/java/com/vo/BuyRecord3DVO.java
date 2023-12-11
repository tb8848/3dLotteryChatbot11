package com.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购买明细VO
 * 福彩3D 购买记录
 */
@Data
public class BuyRecord3DVO extends SearchVO{

    private String huizongName; //名称，如直选复式，直选和值等

    private String bai;

    private String shi;

    private String ge;

    private String value;

    private String lmId;

    private String lsTypeId;

    private List<String> codeList;

    private String lotteryMethodId;

    //下注金额
    private BigDecimal buyMoney;

    private String drawId;

    //下注数量
    private Integer buyAmount;

    /**
     * 号码来源(购买路径): 1 app端 快打，2 app端 快选，3网页端 快打,4网页端 快选，5：网页端 导入,6:二字定
     */
    private Integer codesFrom;

    private String buyCode;

    private String lotterySettingId;

    private String ip;

    private List<String> hzList;

    /**
     * 号码的简称
     * 仅适用于大,小,奇,偶,拖拉机,三同号，分别对应:da,xiao,ji,ou,tlj,c3t
     */
    @TableField(exist = false)
    private String buyCodeShortName;


    @TableField(exist = false)
    private String playerBuyId;

    @TableField(exist = false)
    private String buyDesc;

    @TableField(exist = false)
    private Integer typeFlag;
}
