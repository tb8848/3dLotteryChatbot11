package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 聊天室消息
 */
@Data
public class ChatRoomMsg {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 房间ID
     */
    private String roomId;

    /**
     * 发送人ID
     */
    private String fromUserId;


    /**
     * 发送用户类型，0：玩家，1：机器人
     */
    private Integer fromUserType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 消息类型: 0：文本，1：图片
     */
    private Integer msgType;

    /**
     * 快选规则，json格式字符串
     */
    private String kuaixuanRule;

    /**
     * 发送人昵称
     */
    private String fromUserNick;

    /**
     * 发送人头像
     */
    private String fromUserImg;


    private String botUserId;


    private String toUserNick;

    private String toUserId;

    /**
     * 发送用户类型，0：玩家，1：所有人，2：机器人
     */
    private Integer toUserType = 1;

    /**
     * 操作类型
     * 0:通用
     * 1:快选下注
     * 2:定投
     * 3:下注+退码
     * -1:开关盘
     * 4:退码
     * 10:成功订单
     * 11:中奖情况
     * 90:快捷下组
     */
    private Integer optType;

    private String playerBuyId;

    /**
     * 是否显示
     */
    private Integer isShow = 1;

    /**
     * 消息来源，0:(PC),1(微信),2(机器人)
     */
    private Integer source = 0;


}
