package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUserOdds;
import com.dao.BotUserOddsDAO;
import com.google.common.collect.Maps;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BotUserOddsService extends ServiceImpl<BotUserOddsDAO, BotUserOdds> {

    @Autowired
    private BotUserOddsDAO botUserOddsDAO;

    /**
     * 根据用户ID查询用户赔率设置列表
     * @param userId
     * @return
     */
    public List<BotUserOdds> getListByUserId (String userId, String lmId) {
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BotUserOdds::getBotUserId, userId);
        queryWrapper.eq(BotUserOdds::getLotteryMethodId, lmId);
        queryWrapper.notIn(BotUserOdds::getLotterySettingId, excludeIds);
        queryWrapper.orderByAsc(BotUserOdds::getShortNo);
        List<BotUserOdds> botUserOddsList = botUserOddsDAO.selectList(queryWrapper);
        return botUserOddsList;
    }

    public Map<String, Object> updateOdds (BotUserOdds botUserOdds, String uId, List<BotUserOdds> lsList, String lang) {
        BotUserOdds ls = botUserOddsDAO.selectById(botUserOdds.getId());
        Map<String, Object> resultMap = Maps.newLinkedHashMap();
        if(ls == null){
            //return "未找到设置";
            resultMap.put("error", "dp.batchUpdateDingPan.unFindSetting");
            return resultMap;
        }
        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");

        if(null != botUserOdds.getMinBuy()){
            if (botUserOdds.getMinBuy().compareTo(BigDecimal.ZERO) < 1){
                resultMap.put("error", "botUserOdds.changeOdds.minBuy");
                //return "最小下注需大于0";
                return resultMap;
            }
            Matcher isNum = pattern.matcher(botUserOdds.getMinBuy().toString());
            if (!isNum.matches()) {
                resultMap.put("error", "botUserOdds.changeOdds.minBuyNumber");
                //return "最小下注必须是数字或小数";
                return resultMap;
            }
        }
        Pattern pattern2 = Pattern.compile("[0-9]*");
        if(null != botUserOdds.getMaxBuy()){
            Matcher isNum2 = pattern2.matcher(botUserOdds.getMaxBuy().toString());
            if (!isNum2.matches()) {
                resultMap.put("error", "botUserOdds.changeOdds.maxBuyNumber");
                //return "单注上限必须是正整数";
                return resultMap;
            }
            if (new BigDecimal(botUserOdds.getMaxBuy()).compareTo(BigDecimal.ZERO) < 1){
                resultMap.put("error", "botUserOdds.changeOdds.maxBuy");
                //return "单注上限需大于0";
                return resultMap;
            }
        }

        if(StringUtil.isNotNull(botUserOdds.getOdds())) {
            String s = botUserOdds.getOdds();
            if (s.contains("/")) {
                String[] strs = s.split("/");
                for (String str : strs){
                    Matcher isNum4 = pattern.matcher(str);
                    if (!isNum4.matches()) {
                        resultMap.put("error", "db.batchUpdateDingPan.oddsNumberValidator");
                        //return "赔率必须是数字或小数";
                        return resultMap;
                    }
                    if(new BigDecimal(str).compareTo(BigDecimal.ZERO) < 1){
                        resultMap.put("error", "db.batchUpdateDingPan.oddsUpperNumberValidator");
                        //return "赔率上限需大于0";
                        return resultMap;
                    }
                }
            }else {
                if(null != botUserOdds.getOdds() && new BigDecimal(botUserOdds.getOdds()).compareTo(BigDecimal.ZERO) < 1){
                    resultMap.put("error", "db.batchUpdateDingPan.oddsUpperNumberValidator");
                    //return "赔率上限需大于0";
                    return resultMap;
                }
            }
        }

        lsList.add(botUserOdds);
        return resultMap;
    }
}
