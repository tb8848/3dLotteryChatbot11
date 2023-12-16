package com.action;


import com.alibaba.fastjson.JSON;
import com.beans.*;
import com.service.*;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * wechat接口
 */

@RestController
@RequestMapping(value = "/bot/wechat")
public class WechatAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ThreadPoolExecutor threadPool;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private WechatIpadTokenService wechatIpadTokenService;

    @SubscribeMapping("/topic/wechat/{userId}")
    public String subscribeWechatMsg(@DestinationVariable(value = "userId")String userId, Principal principal) throws Exception {
        //Thread.sleep(1000); // simulated delay
        //System.out.println("topic/wechat>>>>>有人订阅了:"+userId);
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("flag","subscribe");
        resultMap.put("userId",userId);
        ResponseBean responseBean = new ResponseBean(0,0,"",resultMap);
        String content = JSON.toJSONString(responseBean);
        return content;
    }



    //接受登录二维码
    @PostMapping(value = "/recvQrCode")
    public ResponseBean recvQrCode(@RequestBody Map<String,Object> params,HttpServletRequest request){
        String uid = (String)params.get("userId");
        String qrcodeUrl = (String)params.get("qrcodeUrl");

        if(StringUtil.isNull(uid) || StringUtil.isNull(qrcodeUrl)){
            ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
            simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
            return responseBean;
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
                return responseBean;
            }

            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("qrcodeUrl",qrcodeUrl);
            resultMap.put("userId",botUser.getId());
            resultMap.put("flag","scan");

            ResponseBean responseBean = new ResponseBean(0, 0, "", resultMap);
            simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid,JSON.toJSONString(responseBean));
            return responseBean;

        }catch (Exception e){
            e.printStackTrace();
            ResponseBean responseBean = new ResponseBean(500, 0, "系统繁忙", null,true);
            simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid,JSON.toJSONString(responseBean));
            return responseBean;
        }

    }


    //更新登录信息
    @PostMapping(value = "/saveLogin")
    public ResponseBean saveLogin(@RequestBody Map<String,Object> params,HttpServletRequest request){
        String uid = (String)params.get("userId");
        String wxId = (String)params.get("wxId");
        String wxName = (String)params.get("wxName");
        String wxAvatar = (String)params.get("wxAvatar");

        if(StringUtil.isNull(uid)){
            ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
            simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
            return responseBean;
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
                return responseBean;
            }

            WechatIpadToken wechatIpadToken = wechatIpadTokenService.getByUserId(uid);
            if(null == wechatIpadToken){
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
                return responseBean;
            }

            wechatIpadToken.setLoginTime(new Date());
            wechatIpadToken.setWxId(wxId);
            wechatIpadToken.setWxName(wxName);
            wechatIpadToken.setWxAvatar(wxAvatar);
            wechatIpadToken.setWxStatus(1);
            wechatIpadToken.setPcLoginFlag(1);
            wechatIpadToken.setIpadLoginFlag(1);
            wechatIpadTokenService.updateById(wechatIpadToken);

            params.put("wxStatus",1);
            params.put("flag","login");

            ResponseBean responseBean = new ResponseBean(0, 0, "", params);
            simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid,JSON.toJSONString(responseBean));
            return responseBean;

        }catch (Exception e){
            e.printStackTrace();
            ResponseBean responseBean = new ResponseBean(500, 0, "系统繁忙", null,true);
            simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid,JSON.toJSONString(responseBean));
            return responseBean;
        }

    }


    //添加信息
    @PostMapping(value = "/addPlayer")
    public ResponseBean addPlayer(@RequestBody Map<String,Object> params,HttpServletRequest request){
        String uid = (String)params.get("userId");
        String wxId = (String)params.get("wxId");
        String wxName = (String)params.get("wxName");
        String wxAvatar = (String)params.get("wxAvatar");

        if(StringUtil.isNull(uid)){
            ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
            return responseBean;
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                return responseBean;
            }

            BotUserSetting botUserSetting = botUserSettingService.getByUserId(uid);
            if(null == botUserSetting) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getOneBy(uid,wxId);
            if(null!=player){
                String chatUrl = player.getChaturl();
                //玩家已存在
                return new ResponseBean(1,0,"已存在",chatUrl,true);
            }else{

                player = new Player();
                String openId = UUID.randomUUID().toString().replace("-","").toUpperCase();
                player.setOpenid(openId);
                player.setNickname(wxName);
                player.setHeadimg(wxAvatar);
                player.setWxFriendId(wxId);
                player.setBotUserId(uid);
                player.setChaturl("http://zzsunjob.vicp.cc/?openId="+openId);
                player.setHsvalue(botUserSetting.getHsvalue());
                player.setHsType(0);
                player.setUserType(2);
                playerService.save(player);
            }

            ResponseBean responseBean = new ResponseBean(0, 0, "", player.getChaturl());
            return responseBean;

        }catch (Exception e){
            e.printStackTrace();
            ResponseBean responseBean = new ResponseBean(500, 0, "系统繁忙", null,true);
            return responseBean;
        }

    }


    //退出登录
    @PostMapping(value = "/logout")
    public ResponseBean logout(@RequestBody Map<String,Object> params,HttpServletRequest request){
        String uid = (String)params.get("userId");
        String from = (String)params.get("from");
//        System.out.println("===============退出登录>>>>> userId:"+uid+",from:"+from);
        if(StringUtil.isNull(uid)){
            ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
            return responseBean;
        }
        if(StringUtil.isNull(from)){
            from = "wx";
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                return responseBean;
            }

            WechatIpadToken wechatIpadToken = wechatIpadTokenService.getByUserId(uid);
            if(null == wechatIpadToken){
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                return responseBean;
            }

            if("wx".equals(from)){
                //退出请求来自微信客户端，设置账号为离线
                wechatIpadToken.setIpadLoginFlag(1);
                wechatIpadToken.setPcLoginFlag(0);
                wechatIpadToken.setWxStatus(2);
                wechatIpadTokenService.updateById(wechatIpadToken);
                params.put("wxStatus",2);
                params.put("flag","offline");
                ResponseBean responseBean = new ResponseBean(0, 0, "",params);
                simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
                return responseBean;

            }else if("pc".equals(from)){
                //退出请求来自网页，对账号进行解绑
                wechatIpadToken.setWxStatus(0);
                wechatIpadToken.setWxId("");
                wechatIpadToken.setWxAvatar("");
                wechatIpadToken.setWxName("");
                wechatIpadToken.setBotUserId(null);
                wechatIpadToken.setUseFlag(0);
                wechatIpadToken.setIpadLoginFlag(0);
                wechatIpadToken.setPcLoginFlag(0);
                wechatIpadTokenService.updateById(wechatIpadToken);
                params.put("wxStatus",0);
                params.put("flag","logout");
                ResponseBean responseBean = new ResponseBean(0, 0, "",params);
                simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
                return responseBean;
            }

            ResponseBean responseBean = new ResponseBean(0, 0, "",null);

            return responseBean;

        }catch (Exception e){
            e.printStackTrace();
            ResponseBean responseBean = new ResponseBean(500, 0, "系统繁忙", null,true);
            return responseBean;
        }

    }



    //登录状态更新
    @PostMapping(value = "/logstatus")
    public ResponseBean logstatus(@RequestBody Map<String,Object> params,HttpServletRequest request){
        String uid = (String)params.get("userId");
        Integer isLogin = (Integer)params.get("isLogin");
//        System.out.println("===============更新登录状态>>>>> userId:"+uid+"====login:"+isLogin);
        if(StringUtil.isNull(uid)){
            ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
            return responseBean;
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                return responseBean;
            }

            WechatIpadToken wechatIpadToken = wechatIpadTokenService.getByUserId(uid);
            if(null == wechatIpadToken){
                ResponseBean responseBean = new ResponseBean(-1,0,"参数错误",null,true);
                simpMessagingTemplate.convertAndSend("/topic/wechat/"+uid, JSON.toJSONString(responseBean));
                return responseBean;
            }

            wechatIpadTokenService.updateWxStatus(uid,isLogin);

            ResponseBean responseBean = new ResponseBean(0, 0, "",null);
            return responseBean;

        }catch (Exception e){
            e.printStackTrace();
            ResponseBean responseBean = new ResponseBean(500, 0, "系统繁忙", null,true);
            return responseBean;
        }

    }


}
