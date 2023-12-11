package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Draw implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 开奖期号
     */
    private Integer drawId;

    /**
     * 最后1位中奖号码
     */
    private String drawResult1T;

    /**
     * 最后2位中奖号码
     */
    private String drawResult2T;

    /**
     * 最后3位中奖号码
     */
    private String drawResult3T;

    /**
     * 最后4位中奖号码
     */
    private String drawResult4T;

    /**
     * 最后5位中奖号码
     */
    private String drawResult5T;

    /**
     * 中奖号码
     */
    private String drawResult;

    /**
     * 完整开奖号码
     */
    private String drawResultFull;

    /**
     * 开奖状态：0未开奖、1已开奖、2一键结账、3前台显示
     */
    private Integer drawStatus;

    /**
     * 开奖日期
     */
    private String drawDate;

    /**
     * 开奖时间
     */
    private Date drawTime;

    /**
     * 停止下注时间
     */
    private Date drawCloseTime;


    private String lotteryTypeId;

    /**
     * 开盘时间
     */
    private Date openDateTime;

    /**
     * 停盘时间
     */
    private Date closeDateTime;

    /**
     * 开盘状态：0已停盘、1已开盘、2未开盘、3正在开盘中
     */
    private Integer openStatus;


    /**
     * 会员下注数量
     */
    private Integer vipCount;

    /**
     * 利润率‰
     */
    private BigDecimal liRunLv;

    /**
     * 是否使用：0否、1是
     */
    private Integer isUse;

    @TableField(exist = false)
    private Integer bishu;          //笔数
    @TableField(exist = false)
    private BigDecimal totalMoney;  //金额

    @TableField(exist = false)
    private String password; //密码

    /**
     * 是否可以删除：0否，1是
     */
    @TableField(exist = false)
    private Integer isDelete;

    /**
     * 期号所属年月
     */
    @TableField(exist = false)
    private String parentDate;

    /**
     * 字符串格式期号
     */
    @TableField(exist = false)
    private String drawIdStr;
}
