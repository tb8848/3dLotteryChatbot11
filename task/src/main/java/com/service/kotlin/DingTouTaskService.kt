package com.service.kotlin

import cn.hutool.core.date.DateTime
import cn.hutool.core.date.DateUtil
import com.beans.BotUser
import com.beans.Draw
import com.beans.PlayerFixedBuy
import com.service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
open class DingTouTaskService{

//    @Autowired
//    private lateinit var stringRedisTemplate:StringRedisTemplate

    @Autowired
    private lateinit var botUserService: BotUserService;

    @Autowired
    private lateinit var wechatApiService: WechatApiService

    @Autowired
    private lateinit var playerFixedBuyService: PlayerFixedBuyService;

    @Autowired
    private lateinit var drawService: DrawService;

    @Autowired
    private lateinit var p3DrawService: P3DrawService;

    open fun startDingTou(dtPlan: PlayerFixedBuy)
    {
        run {
            CoroutineScope(Dispatchers.IO).launch {
                var dtTask = dtPlan;
                var taskId = dtPlan.id;
                while (dtTask.taskStatus!=-1) {
                    var draw: Draw? = null;
                    if(dtTask.lotteryType==2){
                        draw = p3DrawService.lastDrawInfo
                    }else{
                        draw = drawService.lastDrawInfo;
                    }
                    try {
                        var date = DateTime.now();
                        if(draw.openStatus==1 && dtTask.startTime.before(date)){
                            if(dtTask.taskStatus==0){
                                dtTask.taskStatus=1;
                                playerFixedBuyService.updateById(dtTask);
                            }
                            playerFixedBuyService.startDingTou(dtTask)
                        }
                        delay(10000)
                        dtTask = playerFixedBuyService.getById(taskId);
                        if(null == dtTask){
                            break;
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}