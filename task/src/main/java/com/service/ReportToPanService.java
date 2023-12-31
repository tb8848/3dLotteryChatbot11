package com.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.beans.ResponseBean;
import com.vo.BuyRecord3DVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 报网相关接口
 */
@Service
public class ReportToPanService {


    //发送消息
    public ResponseBean buyForText(String reportNetUrl,List<BuyRecord3DVO> buyRecord3DVOList, String token){
        String url = reportNetUrl+"/vip/draw/bot/buy3d/text";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(buyRecord3DVOList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
        return JSON.parseObject(result,ResponseBean.class);

    }

    //发送消息
    public ResponseBean buy(String reportNetUrl,List<BuyRecord3DVO> buyRecord3DVOList, String token){
        String url = reportNetUrl+"/vip/draw/bot/buy3d";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(buyRecord3DVOList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
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
        return JSON.parseObject(result,ResponseBean.class);

    }

    //发送消息
    public ResponseBean buyFast(String reportNetUrl,List<BuyRecord3DVO> buyRecord3DVOList, String token){
        String url = reportNetUrl+"/vip/draw/bot/buy3d/fast";
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(buyRecord3DVOList));
        httpRequest.header("token",token);
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
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
        return JSON.parseObject(result,ResponseBean.class);

    }


}
