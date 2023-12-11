package com.service;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.beans.DrawBuyRecord;
import com.dao.ChatDomainDAO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下注服务类
 * 下注方法加入锁机制
 */
@Service
@Deprecated
public class DrawBuyRecordService{

//    @Autowired
//    private Snowflake snowflake;
//
//    @Autowired
//    private DrawBuyRecordDAO drawBuyRecordDAO;
//
//    @Autowired
//    private PlayerService playerService;
//
//    @Autowired
//    private PlayerBuyRecordService playerBuyRecordService;
//
//    @Autowired
//    private BotUserOddsService botUserOddsService;
//
//    @Autowired
//    private LotterySettingService lotterySettingService;
//
//    @Autowired
//    private LotteryMethodService lotteryMethodService;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private LockTemplate lockTemplate;
//
//
//    /**
//     * 退码
//     * @return
//     */
//    public Map<String,Object> tuima(Player player, String playerBuyId,Draw draw) {
//        Map<String, Object> resultMap = new HashMap<>();
//        PlayerBuyRecord playerBuyRecord = playerBuyRecordService.getById(playerBuyId);
//        String drawNo = String.valueOf(draw.getDrawId());
//        if (null == playerBuyRecord) {
//            resultMap.put("errcode", -1);
//            resultMap.put("errmsg", "退码失败，没有记录");
//            return resultMap;
//        }
//        if (playerBuyRecord.getBuyStatus() != 0) {
//            resultMap.put("errcode", -1);
//            resultMap.put("errmsg", "不允许退码");
//            return resultMap;
//        }
//
//        List<DrawBuyRecord> getBackCodeList = getBpCodesList(drawNo, playerBuyId);
//        if (getBackCodeList.size() ==0){
//            resultMap.put("errcode", -1);
//            resultMap.put("errmsg", "退码失败，没有记录");
//            return resultMap;
//        }
//
//        getBackCodeList.forEach(cc -> {
//            cc.setBackCodeFlag(1);
//            cc.setBackCodeTime(new Date());
//        });
//        List<String> recordIdList = getBackCodeList.stream().map(item -> item.getId()).collect(Collectors.toList());
//        updateBackCodeFlag(drawNo, recordIdList);
//
//        playerBuyRecord.setBuyStatus(-1);
//        playerBuyRecordService.updateById(playerBuyRecord);
//
//        BigDecimal backTotalMoney = getBackCodeList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
//        //更新余额
//        playerService.updatePoint(player.getId(), backTotalMoney, true);
//
//        resultMap.put("errcode",0);
//        resultMap.put("errmsg","退码成功");
//        resultMap.put("backTotalMoney",backTotalMoney);
//        return resultMap;
//
//    }
//
//    /**
//     * 获取包牌号码
//     * @param drawId
//     * @param baopaiId
//     * @return
//     */
//    public List<DrawBuyRecord> getBpCodesList(String drawId, String baopaiId){
//        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper<>();
//        uw.eq(DrawBuyRecord::getDrawId,drawId);
//        uw.eq(DrawBuyRecord::getBaopaiId,baopaiId);
//        return drawBuyRecordDAO.selectList(uw);
//    }
//
//
//    /**
//     * 更新退码状态和时间
//     * @param drawId
//     * @param recordIdList 退码记录ID列表
//     * @return
//     */
//    public int updateBackCodeFlag(String drawId,List<String> recordIdList){
//        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper<>();
//        uw.eq(DrawBuyRecord::getDrawId,drawId);
//        uw.in(DrawBuyRecord::getId,recordIdList);
//        uw.set(DrawBuyRecord::getBackCodeFlag,1);
//        uw.set(DrawBuyRecord::getBackCodeTime,new Date());
//        return drawBuyRecordDAO.update(null,uw);
//    }
//
//
//    public void copyTable (int drawNo) {
//        drawBuyRecordDAO.copyTable(String.valueOf(drawNo));
//    }
//

}
