package com.wechat.api;

import lombok.Data;

import java.util.Map;

@Data
public class RespData {

    Integer Code;

    Boolean Success;

    String Message;

    Map<String,Object> data;
}
