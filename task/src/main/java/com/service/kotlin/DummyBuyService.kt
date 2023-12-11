package com.service.kotlin

import com.beans.*
import com.service.*
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service


@Service
open class DummyBuyService {

    @Autowired
    private lateinit var botUserService: BotUserService

    @Autowired
    private lateinit var lotteryMethodService: LotteryMethodService

    @Autowired
    private lateinit var lotterySettingService: LotterySettingService

    @Autowired
    private lateinit var dummyServiceV2: DummyServiceV2

//    @Autowired
//    private lateinit var stringRedisTemplate: StringRedisTemplate

    open fun dummyBuy(botUser: BotUser) {
        run {
            CoroutineScope(Dispatchers.IO).launch {
                var user = botUser;
                var userId = user.id;
                val lmList: List<LotteryMethod> = lotteryMethodService.list()
                val lsList: List<LotterySetting> = lotterySettingService.getLotterySettingList()
                while (user.onlineStatus==1) {
                    try {
                        dummyServiceV2.dummyBuy(botUser, lmList, lsList);
                        delay(180000) //休眠5分钟
                        user = botUserService.getById(userId);
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}