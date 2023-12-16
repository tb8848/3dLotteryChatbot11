package com.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beans.BotUser;
import com.beans.Dictionary;
import com.beans.ResponseBean;
import com.dao.BotUserDAO;
import com.juLiang.DynamicGetIps;
import com.util.JuLiangUtil;
import com.vo.WechatPushMsgVo;
import com.wechat.api.RespData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 聚量服务接口
 */
@Service
public class JuLiangService {

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    @Autowired
    private BotUserDAO botUserDAO;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DictionaryService dictionaryService;


    //
    public Map<String,Object> getDynamicIp(String area,String city){

        Dictionary tradeNoDic = dictionaryService.getDicByCode("system","Jltrade_no");
        String tradeNo = tradeNoDic.getValue();
        Dictionary apiKeyDic = dictionaryService.getDicByCode("system","JlApiAccessKey");
        String apiKey = apiKeyDic.getValue();
        Dictionary userNameDic = dictionaryService.getDicByCode("system","JlProxyUsername");
        String userName = userNameDic.getValue();
        Dictionary pwdDic = dictionaryService.getDicByCode("system","JlProxyPassword");
        String pwd = pwdDic.getValue();

        DynamicGetIps dynamicGetIps = new DynamicGetIps();
        dynamicGetIps.setArea(area);
        dynamicGetIps.setNum(1);
        dynamicGetIps.setPt(2);
        //dynamicGetIps.setCity_name(1);
        dynamicGetIps.setResult_type("json");
        dynamicGetIps.setTrade_no(tradeNo);
        dynamicGetIps.setKey(apiKey);
        try {

            String res = JuLiangUtil.dynamicGetIps(dynamicGetIps);
//            System.out.println("========================res ----"+res);
            ResponseBean responseBean = JSONObject.parseObject(res,ResponseBean.class);
            if(responseBean.getCode()==200){
                JSONObject datas = (JSONObject)responseBean.getData();
                JSONArray proxy_list = (JSONArray)datas.get("proxy_list");
                Map<String, Object> returnData = new HashMap<>();
                returnData.put("uname",userName);
                returnData.put("pwd",pwd);
                returnData.put("ip",proxy_list.get(0));
                return returnData;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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
//        System.out.println(DateUtil.now()+">>>>>>Msg/SendTxt>>>>>>"+result);
        RespData respData = JSONObject.parseObject(result, RespData.class);

    }
}




