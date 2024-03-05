package com.action;

import com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * wechat接口
 * 基于第三方接口(Java版)
 */

@RestController
@RequestMapping(value = "/bot/wechat/api")
public class Wechat2Action {

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

//    @Value("${wechat.api.url}")
//    private String wechatApiUrl;


    /**
     * 获取登录二维码
     * @param userId
     * @param request
     * @return
     */
//    @GetMapping(value = "/getQrcode")
//    public ResponseBean getQrcode(String userId,HttpServletRequest request){
//
//        if(StringUtil.isNull(userId)){
//            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
//        }
//
//        try{
//            BotUser botUser = botUserService.getById(userId);
//            if(null == botUser) {
//                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
//            }
//
//            Map<String,Object> proxy = new HashMap<>();
//            proxy.put("ProxyIp","175.6.126.228:2021");
//            proxy.put("ProxyUser","tp65");
//            proxy.put("ProxyPassword","6605");
//
//            Map<String,Object> data = new HashMap<>();
//            data.put("DeviceID","");
//            data.put("DeviceName","");
//            data.put("OSModel","");
//            data.put("Proxy",proxy);
//
//            String url = wechatApiUrl+"/Login/GetQR";
//            String result = HttpUtil.post(url, JSON.toJSONString(data));
//            System.out.println("result>>>>>>"+result);
//            return new ResponseBean(0, 0, "", result);
//
//        }catch (Exception e){
//            e.printStackTrace();
//            return new ResponseBean(500, 0, "系统繁忙", null,true);
//        }
//
//    }





}
