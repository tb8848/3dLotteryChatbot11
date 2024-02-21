package com.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beans.BotUser;
import com.beans.BotUserSetting;
import com.beans.Player;

import com.vo.WechatApiMsgVo;
import com.wechat.api.RespData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * 账号服务类
 */
@Service
public class WechatApiService {

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private BotUserService botUserService;

    private Logger logger = LoggerFactory.getLogger(WechatApiService.class);

    //发送消息

    /**
     * 向微信好友发送文本消息
     * @param toWxId 接收人微信ID
     * @param wxId 发送人微信ID
     * @param text 发送内容
     */
    public void sendMsg(String toWxId, String wxId, String text){
        String url = wechatApiUrl+"msg/WXSendText";
        Map<String,Object> reqData = new HashMap<>();
        /*reqData.put("Content",text);
        reqData.put("ToWxid",toWxId);
        reqData.put("Type",1);
        reqData.put("Wxid",wxId);*/
        reqData.put("toUserName",toWxId);
        reqData.put("content",text);
        reqData.put("accountId",wxId);

        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info(">>>>>>Msg/SendTxt>>>>>>"+result);

    }

    //向微信好友发送图片
    public void sendImage(String wxFriendId, String wxId, String msg) {
        int maxTry = 3;
        while(maxTry>0){
            String url = wechatApiUrl+"msg/WXSendImage";
            try{
                String base64 = netImageToBase64(msg);
                //base64 = "data:image/jpg;base64,"+base64;
                Map<String,Object> reqData = new HashMap<>();
                reqData.put("Base64",base64);
                reqData.put("toUserName",wxFriendId);
                reqData.put("accountId",wxId);

                HttpRequest httpRequest = HttpUtil.createPost(url);
                httpRequest.contentType("application/json");
                httpRequest.body(JSON.toJSONString(reqData));
                HttpResponse httpResponse = httpRequest.execute();
                String result = httpResponse.body();
//                logger.info(">>>>>>Msg/UploadImg>>>>>>"+result);
                if(result.startsWith("{") && result.endsWith("}")){
                    RespData respData = JSON.parseObject(result,RespData.class);
                    if(respData.getCode()==0){
                        break;
                    }
                }
                maxTry--;
                Thread.sleep(5000);

            }catch (Exception e){
                e.printStackTrace();
            }


        }

    }





    public RespData clearProxyIp(String wxId) {
        RespData respData = null;
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
            respData = JSON.parseObject(result,RespData.class);

        }catch (Exception e){
            e.printStackTrace();
        }
        return respData;
    }



    public String imageToBase64(String imgFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        byte[] data = null;
        try(InputStream in = new FileInputStream(imgFile)){
            data = new byte[in.available()];
            in.read(data);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(data));
    }


    //网络图片转base64
    public String netImageToBase64(String imgFile) throws IOException {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        String encoder = "data:image/png;base64,"; //定义图片类型，方便前端直接使用
        ByteArrayOutputStream dataSream = new ByteArrayOutputStream();
        URL url = new URL(imgFile);//picUrl为图片地址
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream is = connection.getInputStream();
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = is.read(bytes)) != -1){
            dataSream.write(bytes,0,len);
        }
        is.close();
        BASE64Encoder base64Encoder = new BASE64Encoder();
        encoder = encoder +  base64Encoder.encode(dataSream.toByteArray()).replace("\r\n","").trim();//这里去掉结果里面的"\r\n"，也可以不去，但是不去的话需要使用的时候还是要去掉，所以为了方便就先去掉再存储
        return encoder;
    }


}
