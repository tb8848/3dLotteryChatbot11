package com.wechat.api;

import lombok.Data;

import java.util.Map;

@Data
public class RespData {

    Integer code;

    Boolean success;

    String MSG;

    Map<String,Object> data;
}
