package com.auth;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthInfo implements Serializable {

    private String ip;

    private String device;

    private String uri;

    /**ip所属城市*/
    private String city;

    /**用户id*/
    private String userId;

    private String userName;

    private String userRoleId;

    private String language;

    private String userRoleName;

}
