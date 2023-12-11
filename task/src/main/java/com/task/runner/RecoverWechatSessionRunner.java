package com.task.runner;

import cn.hutool.core.date.DateUtil;
import com.beans.BotUser;
import com.beans.PlayerFixedBuy;
import com.service.BotUserService;
import com.service.PlayerFixedBuyService;
import com.service.WechatApiService;
import com.service.kotlin.DingTouTaskService;
import com.service.kotlin.DummyBuyService;
import com.service.kotlin.SyncWechatMsgService;
import com.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Component
public class RecoverWechatSessionRunner{

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SyncWechatMsgService syncWechatMsgService;

    @Autowired
    private DummyBuyService dummyBuyService;

    @Autowired
    private DingTouTaskService dingTouTaskService;

    @Autowired
    private PlayerFixedBuyService playerFixedBuyService;


    private Logger logger = LoggerFactory.getLogger(RecoverWechatSessionRunner.class);


    @PostConstruct
    public void run() throws Exception {
        new Thread(()->{
            initData();
        }).start();
    }

    public void initData(){
        logger.info("=========程序重启，初始化kotlin协程执行");
        //System.out.println("=========程序重启，初始化kotlin协程执行");
        List<BotUser> botUserList = botUserService.listBy();
        if(null!=botUserList && botUserList.size()>0){
            List<BotUser> wxList = botUserList.stream().filter(item-> StringUtil.isNotNull(item.getWxId())).collect(Collectors.toList());
            if(null!=wxList && wxList.size()>0){
                logger.info(">>>>>>>>>>微信消息监听启动："+wxList.size());
                //System.out.println(DateUtil.now()+">>>>>>>>>>微信消息监听启动："+wxList.size());
                for(BotUser botUser : wxList){
                    if(botUser.getWxStatus()==1){
                        syncWechatMsgService.getMsg(botUser);
                    }
                }
            }


            List<BotUser> onlineList = botUserList.stream().filter(item-> null!=item.getOnlineStatus() && item.getOnlineStatus()==1).collect(Collectors.toList());
            if(null!=onlineList && onlineList.size()>0){
                logger.info(">>>>>>>>>>在线机器人假人下注启动："+onlineList.size());
                //System.out.println(DateUtil.now()+">>>>>>>>>>在线机器人假人下注启动："+onlineList.size());
                for(BotUser botUser : onlineList){
                    dummyBuyService.dummyBuy(botUser);
                }
            }

        }

        //启动未开始和运行中两种状态的定投任务
        List<PlayerFixedBuy> runningList = playerFixedBuyService.getRunningList();
        if(null!=runningList && runningList.size()>0){
            logger.info(">>>>>>>>>>玩家定投任务启动："+runningList.size());
            //System.out.println(DateUtil.now()+">>>>>>>>>>玩家定投任务启动："+runningList.size());
            for(PlayerFixedBuy dtTask : runningList){
                dingTouTaskService.startDingTou(dtTask);
            }
        }


    }

}
