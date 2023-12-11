package com.task;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.beans.Dictionary;
import com.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SyncSuperAdminTask {
    @Autowired
    private DictionaryService dictionaryService;

    @Value("${superAdmin.url}")
    private String adminUrl;

    /**
     * 每分钟执行同步机器人最大数量
     */
    @Scheduled(cron = "0 * * * * ?")
    @Async
    public void task () {
        Dictionary dictionary = dictionaryService.getDicByCode("system", "domain");
        if (null != dictionary) {
            // 请求总控接口，判断接口是否到期
            String result = HttpUtil.get(adminUrl + "getRobotMaxCount?url=" + dictionary.getValue());
            JSONObject jsonObject = JSONObject.parseObject(result);
            // 已到期
            if (jsonObject.getInteger("code") == 200) {
                Integer maxCount = jsonObject.getInteger("data");
                Dictionary dic = dictionaryService.getDicByCode("system", "dueTime");
                if (null == dic) {
                    dic = new Dictionary();
                    dic.setCode("MaxRobotCount");
                    dic.setName("机器人最大数量");
                    dic.setRemark("机器人最大数量");
                    dic.setType("system");
                    dic.setValue(String.valueOf(maxCount));
                    dictionaryService.save(dic);
                }else {
                    dic.setValue(String.valueOf(maxCount));
                    dictionaryService.updateById(dic);
                }
            }
        }
    }
}
