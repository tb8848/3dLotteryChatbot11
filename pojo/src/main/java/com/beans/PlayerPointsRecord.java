package com.beans;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 玩家上下分记录
 */
@Data
public class PlayerPointsRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 玩家ID
     */
    private String playerId;

    /**
     * 审核时间
     */
    private Date authTime;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 金额，单位(元),如果上分，值为正数，如果下分，值为负数
     */
    private BigDecimal points = BigDecimal.ZERO;

    /**
     * 操作类型，0：上分，1：下分
     */
    private Integer optType;

    /**
     * 审核状态，0：待审核，1：同意，2：拒绝，-1：删除
     */
    private Integer authStatus;

    /**
     * 玩家所属机器人ID
     */
    private String botUserId;


    @TableField(exist = false)
    private Player player;

    /**
     * 玩家名称
     */
    @TableField(exist = false)
    private String playerName;

    @TableField(exist = false)
    private String playerHeadimg;

    /**
     * 玩家所属机器人
     */
    @TableField(exist = false)
    private String botName;

    /**
     * 是否机器人操作：0玩家上下分（需审核）、1机器人上下分（不用审核）
     */
    @TableField(exist = false)
    private Integer type;

}
