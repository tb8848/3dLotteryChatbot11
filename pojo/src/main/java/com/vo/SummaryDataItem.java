package com.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SummaryDataItem {

    /**
     * 名称
     */
    private String text;

    /**
     * 玩家积分
     */
    private BigDecimal value;

    public SummaryDataItem(){}

    public SummaryDataItem(String text,BigDecimal value){
        this.text = text;
        this.value = value;
    }

    public SummaryDataItem(String text){
        this.text = text;
        this.value = BigDecimal.ZERO;
    }

}
