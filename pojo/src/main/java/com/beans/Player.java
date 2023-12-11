package com.beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家信息
 */
@Data
public class Player {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String headimg;

    /**
     * 可用积分
     */
    private BigDecimal points;

    /**
     * openid
     */
    private String openid;


    private String chaturl;


    private BigDecimal hsvalue = BigDecimal.ZERO;

    /**
     * 回水类型，0：通用，1：单独设置
     */
    private Integer hsType = 0;

    /**
     * 是否假托，0：否，1：是
     */
    private Integer  pretexting = 0;

    /**
     * 是否报网，0：否，1：是
     */
    private Integer  reportNet = 1;

    /**
     * 是否吃奖，0：否，1：是
     */
    private Integer  eatPrize = 0;

    /**
     * 状态，1：正常；2：黑名单；-1：删除
     */
    private Integer status;

    /**
     * 玩家类型，0：假人，1：会员，2：微信好友
     */
    private Integer userType;

    /**
     * 微信好友ID
     */
    private String wxFriendId;

    /**
     * 微信好友昵称
     */
    private String wxFriendNick;

    /**
     * 微信好友头像
     */
    private String wxFriendImg;

    /**
     * 所属机器人账号
     */
    private String botUserId;


    /**
     * 当日总投
     */
    @TableField(exist = false)
    private BigDecimal dayTotalBuy = BigDecimal.ZERO;

    /**
     * 当日盈亏
     */
    @TableField(exist = false)
    private BigDecimal dayTotalEarn = BigDecimal.ZERO;

    /**
     * 彩种，1:3D，2：排列三
     */
    private Integer lotteryType;


}
