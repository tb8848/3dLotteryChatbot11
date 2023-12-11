package com.model.req;

import lombok.Data;

@Data
public class AgentNumberRuleReq {
    private String id;

    private Long maxBettingCount;

    private Long maxNumberTypeCount;
}
