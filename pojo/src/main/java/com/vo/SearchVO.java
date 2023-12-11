package com.vo;

import lombok.Data;

@Data
public class SearchVO {

    Integer pageNo=1;

    Integer pageSize=30;

    String drawId;

    Integer deleteFlag;

    String vipId;

    String buyCodes;

}
