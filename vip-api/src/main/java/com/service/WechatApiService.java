package com.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.beans.*;
import com.dao.BotUserDAO;

import com.google.common.collect.Maps;

import com.vo.WechatPushMsgVo;
import com.wechat.api.RespData;
import com.wechat.api.RespDataV2;

import net.bytebuddy.utility.JavaConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WechatApiService{

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    @Autowired
    private BotUserDAO botUserDAO;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Logger logger = LoggerFactory.getLogger(WechatApiService.class);


    public RespDataV2 clearProxyIp(String wxId) {
        RespDataV2 respData = null;
        String url = wechatApiUrl+"Tools/setproxy";
        try{
            Map<String,Object> reqData = new HashMap<>();

            Map<String,Object> proxy = new HashMap<>();
            proxy.put("ProxyIp","");
            proxy.put("ProxyPassword","");
            proxy.put("ProxyUser",wxId);

            reqData.put("Proxy",proxy);
            reqData.put("Wxid",wxId);

            HttpRequest httpRequest = HttpUtil.createPost(url);
            httpRequest.contentType("application/json");
            httpRequest.body(JSON.toJSONString(reqData));
            HttpResponse httpResponse = httpRequest.execute();
            String result = httpResponse.body();
//            logger.info(">>>>>>Tools/setproxy>>>>>>"+result);
            respData = JSON.parseObject(result,RespDataV2.class);

        }catch (Exception e){
            e.printStackTrace();
        }
        return respData;
    }



    //检测二维码的扫码状态
    public void checkQrcodeScan(String botUserId,String uuid){
        BotUser botUser = botUserDAO.selectById(botUserId);
        String headImgUrl = "";
        String nickName = "";
        long maxWait = 5*60; //单位：秒
        boolean scanSucc = false;
        String url = wechatApiUrl+"Login/CheckQR?uuid="+uuid;
//        logger.info(">>>>>>【校验二维码链接】>>>>>>"+url);
        while(maxWait>0){
            HttpRequest httpRequest = HttpUtil.createPost(url);
            httpRequest.contentType("application/json");
            HttpResponse httpResponse = httpRequest.execute();
            String result = httpResponse.body();
            logger.info(">>>>>>【"+botUser.getLoginName()+"】Login/CheckQR>>>>>>"+result);
            RespData respData = null;
            try{
                respData = JSONObject.parseObject(result, RespData.class);
                if(respData.getCode()==0 && respData.getSuccess()){
                    Map<String, Object> datas = respData.getData();
                    if(datas.containsKey("status")){
                        Integer status = (Integer)datas.get("status");
                        if(status==1){

                            headImgUrl = (String)datas.get("headImgUrl");
                            nickName = (String)datas.get("nickName");
                            Map<String,Object> info = new HashMap<>();
                            info.put("wxId",botUser.getWxId());
                            info.put("wxNick",nickName);
                            info.put("wxHeadimg",headImgUrl);
                            info.put("wxStatus",-1);
                            info.put("flag","login");

                            ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
                            WechatPushMsgVo vo = new WechatPushMsgVo();
                            vo.setBotUserId(botUserId);
                            vo.setResponseBean(responseBean);
                            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
                        }else if(status==4){
                            Map<String,Object> info = new HashMap<>();
                            info.put("wxId",botUser.getWxId());
                            info.put("wxNick",botUser.getWxNick());
                            info.put("wxHeadimg",botUser.getWxHeadimg());
                            info.put("wxStatus",-2); //取消登录
                            info.put("flag","login");
                            ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
                            WechatPushMsgVo vo = new WechatPushMsgVo();
                            vo.setBotUserId(botUserId);
                            vo.setResponseBean(responseBean);
                            //stringRedisTemplate.boundValueOps("3d:chatbot:wxStatus:"+botUserId).set("-2");
                            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
                            break;
                        }
                    }
                    if(datas.containsKey("HeadUrl")){
                        String wxId = (String) datas.get("WxId"); //微信ID
                        String imgUrl = (String) datas.get("HeadUrl"); //微信头像
                        String wxNick = (String) datas.get("NickName"); //微信昵称
                        botUser.setWxHeadimg(imgUrl);
                        botUser.setWxId(wxId);
                        botUser.setWxNick(wxNick);
                        botUser.setWxStatus(1);
                        botUser.setWxLoginTime(new Date());
                        botUserDAO.updateWxInfo(botUser);
                        scanSucc = true;
                        break;
                    }
//                    if(datas.containsKey("acctSectResp")){
//                        JSONObject object = (JSONObject)datas.get("acctSectResp");
//                        String wxId = object.getString("userName"); //微信ID
//                        botUser.setWxHeadimg(headImgUrl);
//                        botUser.setWxId(wxId);
//                        botUser.setWxNick(nickName);
//                        botUser.setWxStatus(1);
//                        botUser.setWxLoginTime(new Date());
//                        botUserDAO.updateWxInfo(botUser);
//                        scanSucc = true;
//                        break;
//                    }
                }else{
//                    Map<String,Object> info = new HashMap<>();
//                    info.put("wxStatus",-2); //取消登录
//                    info.put("flag","login");
//                    ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
//                    WechatPushMsgVo vo = new WechatPushMsgVo();
//                    vo.setBotUserId(botUserId);
//                    vo.setResponseBean(responseBean);
//                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
//                    break;
                }
                maxWait--;
                Thread.sleep(1000);

            }catch (Exception e){
                e.printStackTrace();
                Map<String,Object> info = new HashMap<>();
                info.put("wxStatus",-2); //取消登录
                info.put("flag","login");
                ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
                WechatPushMsgVo vo = new WechatPushMsgVo();
                vo.setBotUserId(botUserId);
                vo.setResponseBean(responseBean);
                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
                break;
            }
            //System.out.println(DateUtil.now()+">>>>>>Login/CheckQR>>>>>>"+result);
//            if(result.startsWith("{") && result.endsWith("}")){
//                try {
//                    RespData respData = JSONObject.parseObject(result, RespData.class);
//                    if(respData.getCode()==0 && respData.getSuccess()){
//                        Map<String,Object> datas = respData.getData();
//                        if(datas.containsKey("status")){
//                            Integer status = (Integer)datas.get("status");
//                            if(status==1){
//
//                                headImgUrl = (String)datas.get("headImgUrl");
//                                nickName = (String)datas.get("nickName");
//                                Map<String,Object> info = new HashMap<>();
//                                info.put("wxId",botUser.getWxId());
//                                info.put("wxNick",nickName);
//                                info.put("wxHeadimg",headImgUrl);
//                                info.put("wxStatus",-1);
//                                info.put("flag","login");
//
//                                ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
//                                WechatPushMsgVo vo = new WechatPushMsgVo();
//                                vo.setBotUserId(botUserId);
//                                vo.setResponseBean(responseBean);
//                                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
//                            }else if(status==4){
//                                Map<String,Object> info = new HashMap<>();
//                                info.put("wxId",botUser.getWxId());
//                                info.put("wxNick",botUser.getWxNick());
//                                info.put("wxHeadimg",botUser.getWxHeadimg());
//                                info.put("wxStatus",-2); //取消登录
//                                info.put("flag","login");
//                                ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
//                                WechatPushMsgVo vo = new WechatPushMsgVo();
//                                vo.setBotUserId(botUserId);
//                                vo.setResponseBean(responseBean);
//                                //stringRedisTemplate.boundValueOps("3d:chatbot:wxStatus:"+botUserId).set("-2");
//                                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
//                                break;
//                            }
//                        }
//                        if(datas.containsKey("acctSectResp")){
//                            JSONObject object = (JSONObject)datas.get("acctSectResp");
//                            String wxId = object.getString("userName"); //微信ID
//                            botUser.setWxHeadimg(headImgUrl);
//                            botUser.setWxId(wxId);
//                            botUser.setWxNick(nickName);
//                            botUser.setWxStatus(1);
//                            botUser.setWxLoginTime(new Date());
//                            botUserDAO.updateWxInfo(botUser);
//                            scanSucc = true;
//                            break;
//                        }
//                    }else{
//                        Map<String,Object> info = new HashMap<>();
//                        info.put("wxStatus",-2); //取消登录
//                        info.put("flag","login");
//                        ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
//                        WechatPushMsgVo vo = new WechatPushMsgVo();
//                        vo.setBotUserId(botUserId);
//                        vo.setResponseBean(responseBean);
//                        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
//                        break;
//                    }
//                    maxWait--;
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    Map<String,Object> info = new HashMap<>();
//                    info.put("wxStatus",-2); //取消登录
//                    info.put("flag","login");
//                    ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
//                    WechatPushMsgVo vo = new WechatPushMsgVo();
//                    vo.setBotUserId(botUserId);
//                    vo.setResponseBean(responseBean);
//                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
//                    break;
//                }
//            }else{
//                break;
//            }

        }
        if(scanSucc){//扫码成功
            //string
            Map<String,Object> info = new HashMap<>();
            info.put("wxId",botUser.getWxId());
            info.put("wxNick",botUser.getWxNick());
            info.put("wxHeadimg",botUser.getWxHeadimg());
            info.put("wxStatus",botUser.getWxStatus());
            info.put("flag","login");
            ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
            WechatPushMsgVo vo = new WechatPushMsgVo();
            vo.setBotUserId(botUserId);
            vo.setResponseBean(responseBean);
            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
//            logger.info(">>>>>>【"+botUser.getLoginName()+"】二维码检测结束");
            //System.out.println("===========二维码检测结束");
            //stringRedisTemplate.boundValueOps("3d:chatbot:wxStatus:"+botUserId).set("1");
            //stringRedisTemplate.boundSetOps("3d:chatbot:newWxLogin").add(botUserId);
            //发送机器人账号微信登录状态至mq : botUserMsg队列
            Map<String,Object> dataMap = Maps.newHashMap();
            dataMap.put("userId",botUser.getId());
            dataMap.put("type","wxOnline");
            dataMap.put("value",1);
            rabbitTemplate.convertAndSend("exchange_botUserTopic_3d","botUserMsg", JSON.toJSONString(dataMap));

        }else if(maxWait==0){
            ResponseBean responseBean = new ResponseBean(500, 0, "", null,true);
            WechatPushMsgVo vo = new WechatPushMsgVo();
            vo.setBotUserId(botUserId);
            vo.setResponseBean(responseBean);
            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
        }
    }


    //发送消息
    public void sendMsg(String toWxId, String wxId, String text){
        String url = wechatApiUrl+"Msg/SendTxt";
        Map<String,Object> reqData = new HashMap<>();
        reqData.put("Content",text);
        reqData.put("ToWxid",toWxId);
        reqData.put("Type",1);
        reqData.put("Wxid",wxId);

        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info(">>>>>>Msg/SendTxt>>>>>>"+result);
        //System.out.println(DateUtil.now()+">>>>>>Msg/SendTxt>>>>>>"+result);

    }



    public void notifyBotUserRelogin(String botUserId){
        Map<String,Object> info = new HashMap<>();
        info.put("wxStatus",0);
        info.put("flag","rescan");
        ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
        WechatPushMsgVo vo = new WechatPushMsgVo();
        vo.setBotUserId(botUserId);
        vo.setResponseBean(responseBean);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
    }





}




