package com.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.beans.ResponseBean;
import com.vo.BuyRecord3DVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报网相关接口
 */
@Service
public class ReportToPanService {

    private Logger logger = LoggerFactory.getLogger(ReportToPanService.class);

    //发送消息
    public ResponseBean buy(String reportNetUrl,List<BuyRecord3DVO> buyRecord3DVOList, String token){
        String url = reportNetUrl+"/vip/draw/bot/buy3d";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(buyRecord3DVOList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info("=============>"+url+"返回："+result);
        return JSON.parseObject(result,ResponseBean.class);

    }

    //发送消息
    public ResponseBean buyHs(String reportNetUrl,List<BuyRecord3DVO> buyRecord3DVOList, String token){
        String url = reportNetUrl+"/vip/draw/bot/buy3d/hs";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(buyRecord3DVOList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info("=============>"+url+"返回："+result);
        return JSON.parseObject(result,ResponseBean.class);

    }

    //快捷下注
    public ResponseBean buyFast(String reportNetUrl,List<BuyRecord3DVO> buyRecord3DVOList, String token){
        String url = reportNetUrl+"/vip/draw/bot/buy3d/fast";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(buyRecord3DVOList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info("=============>"+url+"返回："+result);
        return JSON.parseObject(result,ResponseBean.class);

    }


    public ResponseBean tuima(String reportNetUrl,List<String> playerBuyIdList, String token){
        String url = reportNetUrl+"/vip/draw/bot/tuima";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(playerBuyIdList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info("=============>"+url+"返回："+result);
        return JSON.parseObject(result,ResponseBean.class);

    }


}
