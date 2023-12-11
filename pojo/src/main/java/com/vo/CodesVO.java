package com.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CodesVO {

    //private String id;
    private String buyCode;         //号码
    private String lotterySettingId;    //二级规则ID
    private Integer lsType;
    private String lotteryMethodId;//一级规则ID
    private Integer buyAmount;
    private String peiRate;    //赔率
    private Integer isXian;    //是否为现字玩法
    private BigDecimal buyMoney = BigDecimal.ZERO; //购买金额

    private String printPeiRate; //打印赔率

    private String value;

    private String hzname;

    private List<String> codeList;


    //private Integer codesFrom;

}
