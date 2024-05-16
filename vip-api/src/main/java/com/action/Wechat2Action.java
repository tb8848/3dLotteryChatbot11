package com.action;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth.OpLogIpUtil;
import com.beans.*;
import com.service.*;
import com.util.IpUtil;
import com.util.JwtUtil;
import com.util.StringUtil;
import com.wechat.api.RespData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

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
    private ThreadPoolExecutor threadPool;

//    @Value("${wechat.api.url}")
//    private String wechatApiUrl;

    @Autowired
    private DictionaryService dictionaryService;

    @Value("${wechat.proxy.default.ip}")
    private String defaultProxyIp;

    @Value("${wechat.proxy.default.user}")
    private String defaultProxyUser;

    @Value("${wechat.proxy.default.pwd}")
    private String defaultProxyPwd;

    @Value("${wechat.fixed.proxy}")
    private String defaultFixedProxy;

    @Resource
    OpLogIpUtil opLogIpUtil;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private ProxyService proxyService;


    private Logger logger = LoggerFactory.getLogger(Wechat2Action.class);

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

            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("userId",uid);
            resultMap.put("wxId",botUser.getWxId());
            resultMap.put("wxNick",botUser.getWxNick());
            resultMap.put("wxHeadimg",botUser.getWxHeadimg());
            resultMap.put("wxStatus",botUser.getWxStatus());
            return new ResponseBean(0, 0, "", resultMap,true);
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
        String userId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(userId)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(userId);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            if(StringUtil.isNotNull(botUser.getWxId())){
                //清除代理
                wechatApiService.clearProxyIp(botUser.getWxId());
            }

            Map<String,Object> proxy = new HashMap<>();
            if(null!=defaultFixedProxy && "1".equals(defaultFixedProxy)){
                proxy.put("ProxyIp",defaultProxyIp);
                proxy.put("ProxyUser",defaultProxyUser);
                proxy.put("ProxyPassword",defaultProxyPwd);
            }else{
                Proxy proxyInfo = null;
                Map<String,Object> addr = opLogIpUtil.getCityInfoV2(IpUtil.getIpAddr(request));
                if(null!=addr){
                    String prov = null;
                    String city = null;
                    //如果ip解析返回省份，则使用省份
                    if(addr.containsKey("province")){
                        prov = (String)addr.get("province");
                    }
                    //如果省份为空，但返回国家，则将国家视为省份
                    if(StringUtil.isNull(prov)){
                        if(addr.containsKey("country")){
                            prov = (String)addr.get("country");
                        }
                    }
                    logger.info("["+botUser.getLoginName()+"]扫码登录省市："+String.format("ip：%s,省份：%s,城市:%s",IpUtil.getIpAddr(request), prov,city));
                    proxyInfo = proxyService.getProxyByArea(prov,city);
                    if(null == proxyInfo){
                        proxyInfo = proxyService.getUnuseProxy();
                    }
                }else{
                    proxyInfo = proxyService.getUnuseProxy();
                }


                if(null == proxyInfo){
                    proxy.put("ProxyIp",defaultProxyIp);
                    proxy.put("ProxyUser",defaultProxyUser);
                    proxy.put("ProxyPassword",defaultProxyPwd);
                    logger.info("["+botUser.getLoginName()+"]使用临时代理1："+JSON.toJSONString(proxy));
                }else{
                    logger.info("["+botUser.getLoginName()+"]使用动态代理2："+JSON.toJSONString(proxyInfo));
                    proxy.put("ProxyIp",proxyInfo.getIp());
                    proxy.put("ProxyUser",proxyInfo.getUsername());
                    proxy.put("ProxyPassword",proxyInfo.getPassword());
                }
            }

//            proxy.put("ProxyIp","");
//            proxy.put("ProxyUser","");
//            proxy.put("ProxyPassword","");
//            System.out.println("-------------------");
//            System.out.println(proxy);

            Map<String,Object> reqData = new HashMap<>();
            reqData.put("DeviceID","");
            reqData.put("DeviceName","");
            reqData.put("OSModel","");
            reqData.put("Proxy",proxy);

            String wechatApiUrl = "";
            Dictionary dic = dictionaryService.getDicByCode("system","wxApi");
            if (dic != null){
                wechatApiUrl = dic.getValue();
            }
//            System.out.println("=========="+wechatApiUrl);
            String url = wechatApiUrl+"Login/GetQR";
//            System.out.println("获取二维码接口："+url);
            HttpRequest httpRequest = HttpUtil.createPost(url);
            httpRequest.body(JSON.toJSONString(reqData));
            httpRequest.contentType("application/json");
            HttpResponse httpResponse = httpRequest.execute();
            String result = httpResponse.body();
            logger.info("["+botUser.getLoginName()+"]获取二维码结果："+result);
            //System.out.println("result>>>>>>"+result);
            RespData respData = JSONObject.parseObject(result, RespData.class);
            if(respData.getCode()==1){
//                System.out.println("===========二维码获取成功");
                Map<String,Object> datas = respData.getData();
                String qrUrl = (String)datas.get("QrUrl");
                String Uuid = (String)datas.get("Uuid");
                String qrBase64 = (String)datas.get("QrBase64");
                botUser.setQrUUid(Uuid);
                botUserService.updateById(botUser);
                threadPool.execute(()->{
                    wechatApiService.checkQrcodeScan(userId,Uuid);
                });
                return new ResponseBean(0, 0, "", qrBase64);
            }

            return new ResponseBean(-1, 0, "获取失败", result,true);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }



    /**
     * 退出
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/logout")
    public ResponseBean logout(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String userId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(userId)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(userId);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            if(StringUtil.isNotNull(botUser.getWxId())){
                String wechatApiUrl = "";
                Dictionary dic = dictionaryService.getDicByCode("system","wxApi");
                if (dic != null){
                    wechatApiUrl = dic.getValue();
                }
                String url = wechatApiUrl+"Login/LogOut?wxid="+botUser.getWxId();
                HttpRequest httpRequest = HttpUtil.createPost(url);
                httpRequest.contentType("application/json");
                HttpResponse httpResponse = httpRequest.execute();
                String respResult = httpResponse.body();
//                logger.info(">>>>>>Login/LogOut>>>>>>"+respResult);
                //System.out.println(DateUtil.now()+">>>>>>Login/LogOut>>>>>>"+respResult);
                RespData respData = JSONObject.parseObject(respResult, RespData.class);
                if(respData.getCode()==0 || respData.getCode()==-13){
                    botUser.setWxId("");
                    botUser.setWxHeadimg("");
                    botUser.setWxStatus(0);
                    botUser.setWxNick("");
                    botUser.setQrUUid("");
                    botUser.setWxLoginTime(null);
                    botUserService.updateById(botUser);
                    return new ResponseBean(0, 0, "", 0);
                }
            }
            return new ResponseBean(-1, 0, "操作失败", null,true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }



    /**
     * 唤醒
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/wakeup")
    public ResponseBean wakeup(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String userId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(userId)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(userId);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            if(StringUtil.isNotNull(botUser.getWxId()) && botUser.getWxStatus()==2){
                String wechatApiUrl = "";
                Dictionary dic = dictionaryService.getDicByCode("system","wxApi");
                if (dic != null){
                    wechatApiUrl = dic.getValue();
                }
                String url = wechatApiUrl+"Login/Awaken?wxid="+botUser.getWxId();
                Map<String,Object> reqData = new HashMap<>();
                reqData.put("Wxid",botUser.getWxId());
                reqData.put("OSModel","");
                HttpRequest httpRequest = HttpUtil.createPost(url);
                httpRequest.contentType("application/json");
                httpRequest.body(JSON.toJSONString(reqData));
                HttpResponse httpResponse = httpRequest.execute();
                String respResult = httpResponse.body();
//                logger.info(">>>>>>Login/Awaken>>>>>>"+respResult);
                RespData respData = JSONObject.parseObject(respResult, RespData.class);
                if(respData.getCode()==0){
                    Map<String,Object> datas = respData.getData();
                    if(datas.containsKey("Uuid")){
                        String Uuid = (String)datas.get("Uuid");
                        botUser.setQrUUid(Uuid);
                        botUser.setWxStatus(-1);
                        botUserService.updateById(botUser);
                        threadPool.execute(()->{
                            wechatApiService.checkQrcodeScan(userId,Uuid);
                        });
                    }else{
                        botUser.setWxId("");
                        botUser.setWxHeadimg("");
                        botUser.setWxStatus(0);
                        botUser.setWxNick("");
                        botUser.setQrUUid("");
                        botUser.setWxLoginTime(null);
                        botUserService.updateById(botUser);
                        wechatApiService.notifyBotUserRelogin(botUser.getId());
                    }
                    return new ResponseBean(0, 0, "", 0);
                }
            }
            return new ResponseBean(-1, 0, "操作失败", null,true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }


    @GetMapping(value = "/del")
    public ResponseBean del(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String userId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(userId)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(userId);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            if(StringUtil.isNotNull(botUser.getWxId())){
                boolean canDel = true;
                if(botUser.getWxStatus()==1){
                    String wechatApiUrl = "";
                    Dictionary dic = dictionaryService.getDicByCode("system","wxApi");
                    if (dic != null){
                        wechatApiUrl = dic.getValue();
                    }
                    String url = wechatApiUrl+"Login/LogOut?wxid="+botUser.getWxId();
                    Map<String,Object> reqData = new HashMap<>();
                    HttpRequest httpRequest = HttpUtil.createPost(url);
                    httpRequest.contentType("application/json");
                    httpRequest.body(JSON.toJSONString(reqData));
                    HttpResponse httpResponse = httpRequest.execute();
                    String respResult = httpResponse.body();
//                    logger.info(">>>>>>Login/LogOut>>>>>>"+respResult);
                    RespData respData = JSONObject.parseObject(respResult, RespData.class);
                    if(respData.getCode()==0){
                        canDel = true;
                    }
                }
                if(canDel){
                    botUser.setWxId("");
                    botUser.setWxHeadimg("");
                    botUser.setWxStatus(0);
                    botUser.setWxNick("");
                    botUser.setQrUUid("");
                    botUser.setWxLoginTime(null);
                    botUserService.updateById(botUser);
                    return new ResponseBean(0, 0, "", 0);
                }else{
                    return new ResponseBean(-1, 0, "操作失败", null,true);
                }
            }
            return new ResponseBean(0, 0, "", null,true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }



}
