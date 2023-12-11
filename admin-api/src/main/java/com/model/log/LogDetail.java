package com.model.log;

import lombok.Data;

import java.io.Serializable;

@Data
public class LogDetail implements Serializable {

    private String typeName;    //类别
    private String detail;      //赚水or福利

    private String detailOne;
    private String detailTwo;
}
