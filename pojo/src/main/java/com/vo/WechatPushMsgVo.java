package com.vo;

import com.beans.ResponseBean;
import lombok.Data;

import java.util.Date;

@Data
public class WechatPushMsgVo {

    private String botUserId;

    private ResponseBean responseBean;

}
