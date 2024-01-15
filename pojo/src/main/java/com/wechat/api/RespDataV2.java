package com.wechat.api;

import lombok.Data;

import java.util.Map;

@Data
public class RespDataV2 {

    Integer Code;

    Boolean Success;

    String Message;

    Object data;
}
