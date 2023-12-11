package com.juLiang;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UsersGetBalance {


    /**
     * key 用户秘钥
     */
    private String key;
    /**
     * 用户Id
     */
    private String user_id;

}
