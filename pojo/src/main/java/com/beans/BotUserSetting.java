package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 机器人账号配置信息
 */
@Data
public class BotUserSetting {

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
     * 假人数量
     */
    private Integer dummyAmount;

    /**
     * 玩家统一回水值
     */
    private BigDecimal hsvalue;

    /**
     * 是否停盘后自动返水，1：是，0：否
     */
    private Integer hsAutoBack = 0;

    /**
     * 是否玩家自助返水，1：是，0：否
     */
    private Integer hsHelpBack = 0;

    /**
     * 是否私聊下注，1：是，0：否
     */
    private Integer wxChatBuy = 1;

    /**
     * 是否开放房间，1：是，0：否
     */
    private Integer roomOpen = 1;

    /**
     * 是否开启快选，1：是，0：否
     */
    private Integer kuaixuanEnable = 1;

    /**
     * 是否开启退码，1：是，0：否
     */
    private Integer tuimaEnable = 1;

    /**
     * 是否开启微信群发图片，1：是，0：否
     */
    private Integer wxGroupSend = 1;

    /**
     * 是否私聊发送图片，1：是，0：否
     */
    private Integer wxChatSend = 1;

}
