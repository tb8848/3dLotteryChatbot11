package com.service.runner;

import com.alibaba.fastjson.JSON;
import com.beans.Draw;
import com.service.BuyRecord3DService;
import com.service.DrawService;
import com.util.Cell;
import com.websocket.DrawOpenStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送停盘剩余时间任务
 * 用while循环实现每秒执行效果
 * 在spring boot 启动完成后执行
 */
@Component
@Lazy
public class copyTable implements ApplicationRunner {

    public static Logger logger = LoggerFactory.getLogger(copyTable.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BuyRecord3DService buyRecord3DService;

    @Autowired
    private DrawService drawService;

    @Value("${upload.dir}")
    private String uploadDir;

//    @Autowired
//    private BasicSettingService basicSettingService;

    private void init() {
        Draw draw = drawService.getLastDrawInfo();
        buyRecord3DService.copyTable(draw.getDrawId());

        List<Draw> last15List = drawService.listN(15);
        String filePath = uploadDir+"images/";
        String fileName = draw.getDrawId()+"-result.jpg";

        Cell.style0("3D",last15List,filePath,fileName);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(()->{
            //init();
        }).start();
    }
}
