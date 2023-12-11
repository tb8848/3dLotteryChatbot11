package com.beans;

import lombok.Data;

import java.util.Date;

@Data
public class WechatIpadToken {

    private String id;

    private String ipadToken;

    private String botUserId;

    private Integer useFlag;

    private Date loginTime;

    private Date validTime;

    private String wxId;

    private String wxName;

    private String wxAvatar;

    private Integer wxStatus = 0;

    private Integer ipadLoginFlag = 0;

    private Integer pcLoginFlag = 0;


}
