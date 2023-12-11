package com.service.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class XuPanRunner implements ApplicationRunner {
//
//    @Autowired
//    private XuPanCodesServiceV2 xuPanCodesServiceV2;
//
//    @Autowired
//    private XuPanCodes3DService xuPanCodes3DService;
//
//    @Autowired
//    private ShiPanCodes3DService shiPanCodes3DService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        new Thread(()->{
//            //xuPanCodesServiceV2.createXuPanCodesV2();
//            shiPanCodes3DService.createShiPanCodesV2();
//            xuPanCodes3DService.createXuPanCodesV2();
//        }).start();


//        Draw draw = drawService.getLastDrawInfo();
//        String key = draw.getDrawId()+"-xupan";
//        System.out.println("========================检测【"+draw.getDrawId()+"】期虚盘数据是否写入redis");
//        if(!redisTemplate.hasKey(key)){ //redis中没有虚盘的数据
//            new Thread(()->{
//                //xuPanCodesService.xupanCodesParam();
//                xuPanCodesServiceV2.createXuPanCodes();
//            }).start();
//        }else{
//            System.out.println("========================第"+draw.getDrawId()+"期虚盘数据已存在redis");
//        }
        //xuPanCodesServiceV2.createXuPanCodes();

//        new Thread(()->{
//            xuPanCodesServiceV2.createXuPanCodes();
//        }).start();
    }




}
