package com.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseBody {
    String id;
    String title = "未命名";
    List children;
    boolean spread = true;
    boolean end = true;
}
