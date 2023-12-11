package com.websocket;
/**
 * 消息
 */

import lombok.Data;

@Data
public class DrawOpenStatusMessage {
    private String id; //期号ID
    private Integer drawNo; //期号
    private Integer openStatus; //状态
    private long leftMillSeconds;  //开盘/关盘时间戳
    private String drawResult; //中奖号码
    //开奖状态：0未开奖、1已开奖、2一键结账、3前台显示
    private Integer drawStatus;

    /**
     *  二字定栏目禁止标识，0：禁止，1：开放
     */

    private Integer erzidingCloseFlag=1;

    /**
     * 快选、txt导入栏目禁止标识，0：禁止，1：开放
     */

    private Integer quickOrTxtCloseFlag=1;
}
