package com.service.kotlin

import com.beans.BotUser
import com.service.BotUserService
import com.service.WechatApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
open class SyncWechatMsgService{

    @Autowired
    private lateinit var botUserService: BotUserService;

    @Autowired
    private lateinit var wechatApiService: WechatApiService

    open fun getMsg(botUser: BotUser)
    {
        run {
            CoroutineScope(Dispatchers.IO).launch {
                var user = botUser;
                var userId = user.id;
                while (user.wxStatus==1) {
                    try {
                        wechatApiService.receiveMsg(user)
                        delay(5000)
                        user = botUserService.getById(userId);

                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        delay(5000)
                    }
                }
            }
        }
    }

}