package com.action;


import cn.hutool.core.date.DateUtil;
import com.beans.*;
import com.config.NoLogin;
import com.service.*;
import com.util.JwtUtil;
import com.util.PasswordUtil;
import com.util.StringUtil;
import com.vo.BotUserCountData;
import com.vo.SummaryDataItem;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * wechat接口
 * 基于自建的wechaty接口
 */

@RestController
@RequestMapping(value = "/bot/wechat")
public class WechatAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private WechatIpadTokenService wechatIpadTokenService;


    @GetMapping(value = "/getToken")
    public ResponseBean getToken(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            WechatIpadToken bindUser = wechatIpadTokenService.getByUserId(uid);
            if(null!=bindUser){
                return new ResponseBean(0, 0, "", bindUser);
            }else{
                WechatIpadToken unuseOne = wechatIpadTokenService.getUnuseOne();
                if(null==unuseOne){
                    return new ResponseBean(-1, 0, "无可用token", null,true);
                }
                unuseOne.setBotUserId(uid);
                unuseOne.setUseFlag(1);
                wechatIpadTokenService.updateById(unuseOne);
                return new ResponseBean(0, 0, "", unuseOne);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }


    /**
     * 获取登录信息
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/getLogStatus")
    public ResponseBean getLogStatus(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            WechatIpadToken bindUser = wechatIpadTokenService.getByUserId(uid);
            if(null!=bindUser){
                return new ResponseBean(0, 0, "", bindUser);
            }else{
                WechatIpadToken unuseOne = wechatIpadTokenService.getUnuseOne();
                if(null==unuseOne){
                    return new ResponseBean(-1, 0, "无可用token", null,true);
                }
                unuseOne.setBotUserId(uid);
                unuseOne.setUseFlag(1);
                wechatIpadTokenService.updateById(unuseOne);
                return new ResponseBean(0, 0, "", unuseOne);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }

    /**
     * 获取登录二维码
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/getQrcode")
    public ResponseBean getQrcode(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            WechatIpadToken bindUser = wechatIpadTokenService.getByUserId(uid);
            if(null!=bindUser){
                return new ResponseBean(0, 0, "", bindUser);
            }else{
                WechatIpadToken unuseOne = wechatIpadTokenService.getUnuseOne();
                if(null==unuseOne){
                    return new ResponseBean(-1, 0, "无可用token", null,true);
                }
                unuseOne.setBotUserId(uid);
                unuseOne.setUseFlag(1);
                wechatIpadTokenService.updateById(unuseOne);
                return new ResponseBean(0, 0, "", unuseOne);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }





}
