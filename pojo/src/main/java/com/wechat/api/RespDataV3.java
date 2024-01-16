package com.wechat.api;

import lombok.Data;

import java.util.Map;

@Data
public class RespDataV3 {
    Integer code;

    Boolean Success;

    String MSG;

    Map<String,Object> data;
}
