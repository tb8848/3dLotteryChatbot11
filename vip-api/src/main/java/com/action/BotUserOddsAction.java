package com.action;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.*;
import com.google.common.collect.Maps;
import com.service.*;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/5/31 14:47
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/bot/odds")
public class BotUserOddsAction {

    @Autowired
    private BotUserOddsService botUserOddsService;

    @Autowired
    private LotteryMethodService lotteryMethodService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private LotterySettingService lotterySettingService;

    /**
     * 赔率设置列表
     * @return
     */
    @GetMapping("/oddsList")
    public ResponseBean listPage(@RequestHeader(value = "token")String token, Integer lotteryType) {
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }
        BotUser botUser = botUserService.getById(uid);
        if (null == botUser) {
            return new ResponseBean(500,0,"账号异常",null,true);//用户名或密码为空
        }
        if (null == lotteryType) {
            lotteryType = botUser.getLotteryType();
        }
        try {
            Admin admin = adminService.getOne(new LambdaQueryWrapper<Admin>().last("limit 1"));
            if (admin.getLotteryType() != botUser.getLotteryType()) {
                if (admin.getLotteryType() == 3) {
                    LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BotUserOdds::getBotUserId, uid);
                    queryWrapper.in(BotUserOdds::getLotteryType, Arrays.asList(1,2));
                    List<BotUserOdds> botUserOddsList = botUserOddsService.list(queryWrapper);
                    if (!botUserOddsList.isEmpty()) {
                        Map<Integer, List<BotUserOdds>> map = botUserOddsList.stream().collect(Collectors.groupingBy(BotUserOdds::getLotteryType));
                        if (map.size() == 1) {
                            Integer ltId = map.keySet().stream().findFirst().get();
                            List<LotterySetting> lotterySettingList = lotterySettingService.list(new QueryWrapper<LotterySetting>().eq("isShow",1));
                            if (!lotterySettingList.isEmpty()) {
                                for (LotterySetting lotterySetting : lotterySettingList) {
                                    BotUserOdds botUserOdds = new BotUserOdds();
                                    botUserOdds.setBotUserId(botUser.getId());
                                    botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                    botUserOdds.setLotterySettingId(lotterySetting.getId());
                                    botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                    botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                    botUserOdds.setOdds(lotterySetting.getPeiRate());
                                    botUserOdds.setParentId(lotterySetting.getParentId());
                                    botUserOdds.setShortNo(lotterySetting.getShortNo());
                                    botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                    if (ltId == 1) {
                                        botUserOdds.setLotteryType(2);
                                    }else {
                                        botUserOdds.setLotteryType(1);
                                    }
                                    botUserOddsService.save(botUserOdds);
                                }
                            }
                        }else if (map.size() == 0) {
                            List<LotterySetting> lotterySettingList = lotterySettingService.list(new QueryWrapper<LotterySetting>().eq("isShow",1));
                            if (!lotterySettingList.isEmpty()) {
                                for (LotterySetting lotterySetting : lotterySettingList) {
                                    BotUserOdds botUserOdds = new BotUserOdds();
                                    botUserOdds.setBotUserId(botUser.getId());
                                    botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                    botUserOdds.setLotterySettingId(lotterySetting.getId());
                                    botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                    botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                    botUserOdds.setOdds(lotterySetting.getPeiRate());
                                    botUserOdds.setParentId(lotterySetting.getParentId());
                                    botUserOdds.setShortNo(lotterySetting.getShortNo());
                                    botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                    botUserOdds.setLotteryType(1);
                                    botUserOddsService.save(botUserOdds);

                                    BotUserOdds botUserOdds2 = new BotUserOdds();
                                    botUserOdds2.setBotUserId(botUser.getId());
                                    botUserOdds2.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                    botUserOdds2.setLotterySettingId(lotterySetting.getId());
                                    botUserOdds2.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                    botUserOdds2.setMinBuy(lotterySetting.getMinBettingPrice());
                                    botUserOdds2.setOdds(lotterySetting.getPeiRate());
                                    botUserOdds2.setParentId(lotterySetting.getParentId());
                                    botUserOdds2.setShortNo(lotterySetting.getShortNo());
                                    botUserOdds2.setBettingRule(lotterySetting.getBettingRule());
                                    botUserOdds2.setLotteryType(2);
                                    botUserOddsService.save(botUserOdds2);
                                }
                            }
                        }
                    }
                }else if (admin.getLotteryType() == 1) {
                    LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BotUserOdds::getBotUserId, uid);
                    queryWrapper.in(BotUserOdds::getLotteryType, Arrays.asList(1,2));
                    List<BotUserOdds> botUserOddsList = botUserOddsService.list(queryWrapper);
                    if (!botUserOddsList.isEmpty()) {
                        Map<Integer, List<BotUserOdds>> map = botUserOddsList.stream().collect(Collectors.groupingBy(BotUserOdds::getLotteryType));
                        if (map.size() == 1) {
                            Integer ltId = map.keySet().stream().findFirst().get();
                            if (ltId != 1) {
                                List<LotterySetting> lotterySettingList = lotterySettingService.list(new QueryWrapper<LotterySetting>().eq("isShow",1));
                                if (!lotterySettingList.isEmpty()) {
                                    for (LotterySetting lotterySetting : lotterySettingList) {
                                        BotUserOdds botUserOdds = new BotUserOdds();
                                        botUserOdds.setBotUserId(botUser.getId());
                                        botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                        botUserOdds.setLotterySettingId(lotterySetting.getId());
                                        botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                        botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                        botUserOdds.setOdds(lotterySetting.getPeiRate());
                                        botUserOdds.setParentId(lotterySetting.getParentId());
                                        botUserOdds.setShortNo(lotterySetting.getShortNo());
                                        botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                        botUserOdds.setLotteryType(1);
                                        botUserOddsService.save(botUserOdds);
                                    }
                                }

                                List<String> ids = botUserOddsList.stream().filter(botUserOdds -> botUserOdds.getLotteryType() == 2).map(BotUserOdds::getId).collect(Collectors.toList());
                                if (!ids.isEmpty()) {
                                    botUserOddsService.removeBatchByIds(ids);
                                }
                            }

                        }else if (map.size() == 0) {
                            List<LotterySetting> lotterySettingList = lotterySettingService.list(new QueryWrapper<LotterySetting>().eq("isShow",1));
                            if (!lotterySettingList.isEmpty()) {
                                for (LotterySetting lotterySetting : lotterySettingList) {
                                    BotUserOdds botUserOdds = new BotUserOdds();
                                    botUserOdds.setBotUserId(botUser.getId());
                                    botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                    botUserOdds.setLotterySettingId(lotterySetting.getId());
                                    botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                    botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                    botUserOdds.setOdds(lotterySetting.getPeiRate());
                                    botUserOdds.setParentId(lotterySetting.getParentId());
                                    botUserOdds.setShortNo(lotterySetting.getShortNo());
                                    botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                    botUserOdds.setLotteryType(1);
                                    botUserOddsService.save(botUserOdds);
                                }
                            }
                        }else if (map.size() == 2) {
                            List<String> ids = botUserOddsList.stream().filter(botUserOdds -> botUserOdds.getLotteryType() == 2).map(BotUserOdds::getId).collect(Collectors.toList());
                            if (!ids.isEmpty()) {
                                botUserOddsService.removeBatchByIds(ids);
                            }
                        }
                    }
                }else if (admin.getLotteryType() == 2) {
                    LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BotUserOdds::getBotUserId, uid);
                    queryWrapper.in(BotUserOdds::getLotteryType, Arrays.asList(1,2));
                    List<BotUserOdds> botUserOddsList = botUserOddsService.list(queryWrapper);
                    if (!botUserOddsList.isEmpty()) {
                        Map<Integer, List<BotUserOdds>> map = botUserOddsList.stream().collect(Collectors.groupingBy(BotUserOdds::getLotteryType));
                        if (map.size() == 1) {
                            Integer ltId = map.keySet().stream().findFirst().get();
                            if (ltId != 2) {
                                List<LotterySetting> lotterySettingList = lotterySettingService.list(new QueryWrapper<LotterySetting>().eq("isShow",1));
                                if (!lotterySettingList.isEmpty()) {
                                    for (LotterySetting lotterySetting : lotterySettingList) {
                                        BotUserOdds botUserOdds = new BotUserOdds();
                                        botUserOdds.setBotUserId(botUser.getId());
                                        botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                        botUserOdds.setLotterySettingId(lotterySetting.getId());
                                        botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                        botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                        botUserOdds.setOdds(lotterySetting.getPeiRate());
                                        botUserOdds.setParentId(lotterySetting.getParentId());
                                        botUserOdds.setShortNo(lotterySetting.getShortNo());
                                        botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                        botUserOdds.setLotteryType(2);
                                        botUserOddsService.save(botUserOdds);
                                    }
                                }

                                List<String> ids = botUserOddsList.stream().filter(botUserOdds -> botUserOdds.getLotteryType() == 1).map(BotUserOdds::getId).collect(Collectors.toList());
                                if (!ids.isEmpty()) {
                                    botUserOddsService.removeBatchByIds(ids);
                                }
                            }
                        }else if (map.size() == 0) {
                            List<LotterySetting> lotterySettingList = lotterySettingService.list(new QueryWrapper<LotterySetting>().eq("isShow",1));
                            if (!lotterySettingList.isEmpty()) {
                                for (LotterySetting lotterySetting : lotterySettingList) {
                                    BotUserOdds botUserOdds = new BotUserOdds();
                                    botUserOdds.setBotUserId(botUser.getId());
                                    botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                    botUserOdds.setLotterySettingId(lotterySetting.getId());
                                    botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                    botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                    botUserOdds.setOdds(lotterySetting.getPeiRate());
                                    botUserOdds.setParentId(lotterySetting.getParentId());
                                    botUserOdds.setShortNo(lotterySetting.getShortNo());
                                    botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                    botUserOdds.setLotteryType(2);
                                    botUserOddsService.save(botUserOdds);
                                }
                            }
                        }else if (map.size() == 2) {
                            List<String> ids = botUserOddsList.stream().filter(botUserOdds -> botUserOdds.getLotteryType() == 1).map(BotUserOdds::getId).collect(Collectors.toList());
                            if (!ids.isEmpty()) {
                                botUserOddsService.removeBatchByIds(ids);
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        List<BotUserOdds> oddsList = botUserOddsService.getListByUserId(uid,lotteryType);
        Map<Integer,List<BotUserOdds>> lotteryTypeGroup = oddsList.stream().collect(Collectors.groupingBy(BotUserOdds::getLotteryType));
        List<Map<String,Object>> resultData = Lists.newArrayList();
        lotteryTypeGroup.forEach((lottype,dlist)->{
            List<LotteryMethod> list = lotteryMethodService.list();
            for(LotteryMethod lm : list){
                List<BotUserOdds> lmOddsList = dlist.stream().filter(item->item.getLotteryType()==lottype && item.getLotteryMethodId().equals(lm.getId())).collect(Collectors.toList());
                lm.setBotUserOddsList(lmOddsList);
            }
            Map<String,Object> map = Maps.newHashMap();
            map.put("lottype",lottype);
            map.put("data",list);
            resultData.add(map);
        });
        //List<LotteryMethod> list = lotteryMethodService.list();
        return new ResponseBean(0,1,"",resultData,true);
    }

    /**
     * 批量用户赔率设置数据
     * @param botUserOddsList
     * @return
     */
    @RequestMapping("/batchUpdateOdds")
    public ResponseBean batchUpdateOdds (@RequestBody List<BotUserOdds> botUserOddsList, @RequestHeader(value = "token") String token) {
        if(botUserOddsList.size()<1){
            return ResponseBean.ok("操作成功");
        }
        String uid = JwtUtil.getUsername(token);
        List<BotUserOdds> lsList = Lists.newArrayList();
        Map<String, Object> resultMap = Maps.newLinkedHashMap();
        for(BotUserOdds ls : botUserOddsList){
            resultMap = botUserOddsService.updateDingPan(ls, uid, lsList);
            if (resultMap.containsKey("param")) {
                if (resultMap.get("param") instanceof String[]) {
                    String[] sts = (String[]) resultMap.get("param");
                    return new ResponseBean(500, resultMap.get("error").toString(), new Object[]{sts[0], sts[1]});
                }else {
                    return new ResponseBean(500, resultMap.get("error").toString(), new Object[]{resultMap.get("param")});
                }
            }
        }
        if (lsList.size() == botUserOddsList.size()) {
            botUserOddsService.updateBatchById(lsList);
            return ResponseBean.ok("");
        }
        return ResponseBean.error(resultMap.get("error").toString());
    }
}
