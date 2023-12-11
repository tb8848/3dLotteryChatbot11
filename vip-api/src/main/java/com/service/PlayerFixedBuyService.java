package com.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.dao.PlayerFixedBuyDAO;
import com.google.common.collect.Maps;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PlayerFixedBuyService extends ServiceImpl<PlayerFixedBuyDAO, PlayerFixedBuy> {


    @Autowired
    private PlayerFixedBuyDAO dataDao;

    @Autowired
    private PlayerService playerService;


    @Autowired
    private BotUserService botUserService;

    @Autowired
    private KuaidaBuyMsgServiceV2 kuaidaBuyMsgServiceV2;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DrawService drawService;

    public PlayerFixedBuy getRunningOne(String playerId){
        LambdaQueryWrapper<PlayerFixedBuy> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayerFixedBuy::getPlayerId,playerId);
        qw.in(PlayerFixedBuy::getTaskStatus,Lists.newArrayList(0,1));
        qw.last("limit 1");
        return dataDao.selectOne(qw);
    }


    public List<PlayerFixedBuy> getList(String playerId){
        LambdaQueryWrapper<PlayerFixedBuy> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayerFixedBuy::getPlayerId,playerId);
        return dataDao.selectList(qw);
    }


    public List<PlayerFixedBuy> getList(String playerId,Integer lotteryType){
        LambdaQueryWrapper<PlayerFixedBuy> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayerFixedBuy::getPlayerId,playerId);
        qw.eq(PlayerFixedBuy::getLotteryType,lotteryType);
        return dataDao.selectList(qw);
    }

    /**
     * 新增或修改定投
     * @param fixedBuy
     * @return
     */

    public String editPlan(PlayerFixedBuy fixedBuy){
        int lotteryType = fixedBuy.getLotteryType();
        try {
            Draw draw = drawService.getLastDrawInfo();
            String playerId = fixedBuy.getPlayerId();
            String botUserId = fixedBuy.getUserId();
            String taskId = fixedBuy.getId();
            BotUser botUser = botUserService.getById(botUserId);
            if (null == botUser) {
                return "参数错误";
            }

            Player player = playerService.getById(playerId);
            if (null == player) {
                return "参数错误";
            }

            if (!player.getBotUserId().equals(botUserId)) {
                return "参数错误";
            }

            if(null == player.getLotteryType()){
                return "请先开通排列三或福彩3D服务";
            }

            if(player.getLotteryType()!=3){
                if(player.getLotteryType()!=lotteryType){
                    return "未开通"+(lotteryType==2?"排列三":"福彩3D")+"服务";
                }
            }

            PlayerFixedBuy existOne = null;
            if (StringUtil.isNotNull(taskId)) {//定投计划ID不为空，判断是否存在
                existOne = dataDao.selectById(taskId);
                if (null == existOne) {
                    return "参数错误";
                }
                if(existOne.getTaskStatus()!=-1){
                    return "请先停止计划再启动";
                }
            }

            if (null == existOne) {
                //新增计划，判断是否超过3个任务
                long createCount = dataDao.selectCount(new LambdaQueryWrapper<PlayerFixedBuy>().eq(PlayerFixedBuy::getPlayerId, playerId).eq(PlayerFixedBuy::getLotteryType,lotteryType));
                if (createCount >= 3) {
                    return "超出数量上限：3";
                }
            }

            String buyPoints = fixedBuy.getBuyPoints();
            String[] pointsArr = buyPoints.split("-");
            boolean moneyErr = false;
            if (pointsArr.length > 0) {
                moneyErr = Arrays.stream(pointsArr).anyMatch(item -> new BigDecimal(item).compareTo(BigDecimal.ZERO) <= 0);
            }
            if (moneyErr) {
                return "购买金额必须大于0";
            }
            String buyDesc = fixedBuy.getBuyDesc().trim().replace("\r\n","").toUpperCase();
            if (StringUtil.isNull(fixedBuy.getKuaixuanRule())) {
                //内容为快译内容，校验快译内容格式
                if(buyDesc.startsWith("P3") || buyDesc.startsWith("3D")){
                    String desc = buyDesc.substring(2);
                    if (!desc.contains("各")) {
                        desc = desc + "各1";
                    }
                    Map<String, Object> result = kuaidaBuyMsgServiceV2.kuaidaBuy(desc, botUser, player);
                    Integer errcode = (Integer) result.get("errcode");
                    if (-1 == errcode) {
                        String errmsg = (String) result.get("errmsg");
                        return errmsg;
                    }
                    List<BuyRecord3DVO> buyList = (List<BuyRecord3DVO>) result.get("buyList");
                    fixedBuy.setKuaixuanRule(JSON.toJSONString(buyList));

                }else{
                    return "作业格式错误";
                }

            }
            int result = 0;
            fixedBuy.setBuyDesc(buyDesc);
            Date startTime = DateUtil.offsetMinute(new Date(),5);
            int taskStatus = draw.getOpenStatus()==1?1:0;
            if (null == existOne) {
                existOne = fixedBuy;
                existOne.setCreateTime(new Date());
                existOne.setUpdateTime(new Date());
                existOne.setStartTime(startTime); //启动后5分钟后执行
                existOne.setTaskStatus(taskStatus);
                existOne.setUserId(botUserId);
                result = dataDao.insert(existOne);
            } else {
                existOne.setBuyDesc(fixedBuy.getBuyDesc());
                existOne.setBuyPoints(fixedBuy.getBuyPoints());
                existOne.setFollowDraws(fixedBuy.getFollowDraws());
                existOne.setCircleType(fixedBuy.getCircleType());
                existOne.setKuaixuanRule(fixedBuy.getKuaixuanRule());
                existOne.setTaskStatus(taskStatus);
                existOne.setUpdateTime(new Date());
                existOne.setStartTime(startTime); //启动后5分钟后执行
                existOne.setEndTime(null);
                existOne.setStopReason("");
                result = dataDao.updateById(existOne);
            }
            if (result > 0) { //启动成功，提交到mq，通知定时任务定期执行
                Map<String, Object> dataMap = Maps.newHashMap();
                dataMap.put("userId", botUser.getId());
                dataMap.put("type", "dingtou");
                dataMap.put("value", 1);
                dataMap.put("taskId", existOne.getId());
                rabbitTemplate.convertAndSend("exchange_botUserTopic_3d", "botUserMsg", JSON.toJSONString(dataMap));
                return null;
            } else {
                return "启动失败";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "系统繁忙";
        }

    }

    /**
     * 分页查询机器人跟码列表
     * @param botUserId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public IPage getBotUserFixedBuy(String botUserId, int pageNo, int pageSize){
        Page p = new Page(pageNo,pageSize);
        QueryWrapper<PlayerFixedBuy> q = new QueryWrapper<>();
        q.eq("userId",botUserId);
        q.eq("taskStatus",1);
        IPage iPage = dataDao.selectPage(p,q);
        return iPage;
    }

}
