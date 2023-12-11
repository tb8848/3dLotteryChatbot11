package com.action;

import com.beans.ResponseBean;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网盘接口
 */
@RestController
@RequestMapping(value = "/bot/networkDisk")
public class NetworkDiskAction {

    /**
     * 网盘登录
     * @param token
     * @return
     */
    @RequestMapping(value = "login")
    public ResponseBean login (@RequestHeader(value = "token")String token) {
        return ResponseBean.OK;
    }
}
