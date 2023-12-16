package com.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.dao.PlayerFixedBuyDAO;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class PlayerFixedBuyService extends ServiceImpl<PlayerFixedBuyDAO, PlayerFixedBuy> {


    @Autowired
    private PlayerFixedBuyDAO dataDao;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;


    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private PlayerService playerService;


    @Autowired
    private KuaidaBuyMsgService kuaidaBuyMsgService;


    @Autowired
    private BotUserService botUserService;


    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private DingtouBuyService dingtouBuyService;


    public List<PlayerFixedBuy> getRunningList(){
        LambdaQueryWrapper<PlayerFixedBuy> qw = new LambdaQueryWrapper<>();
        qw.in(PlayerFixedBuy::getTaskStatus,Lists.newArrayList(0,1));
        return dataDao.selectList(qw);
    }


    public PlayerFixedBuy getRunningOne(String playerId){
        LambdaQueryWrapper<PlayerFixedBuy> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayerFixedBuy::getPlayerId,playerId);
        qw.in(PlayerFixedBuy::getTaskStatus,Lists.newArrayList(0,1));
        qw.last("limit 1");
        return dataDao.selectOne(qw);
    }

    public void startDingTou(@NotNull PlayerFixedBuy dtTask) {

        try {
            BotUser botUser = null;
            Player player = null;
            String playerId = dtTask.getPlayerId();
            String taskId = dtTask.getId();
            player = playerService.getById(playerId);
            if (null != player) {
                botUser = botUserService.getById(player.getBotUserId());
            }

            if (null == player || null == player) {
                dtTask.setTaskStatus(-1);
                dtTask.setEndTime(new Date());
                dtTask.setStopReason("参数错误：未知玩家或机器人信息");
                this.updateById(dtTask);
                return;
            }



            //System.out.println("===========================执行定投："+dtTask.getBuyDesc()+",玩家："+player.getNickname());

            Draw draw = null;


            //获取已执行定投的期数
            Long hasFollowed = playerBuyRecordService.countFollowDraws(playerId, taskId);
            if (null == hasFollowed) {
                hasFollowed = 0L;
            }
            //判断定投计划中是否有设置跟期数量
            Integer followDraws = dtTask.getFollowDraws();
            if (null == followDraws) {
                followDraws = 0;
            }
            if (followDraws > 0) {
                //统计定投的记录
                if (null != hasFollowed && hasFollowed > 0) {
                    if (hasFollowed >= followDraws) {
                        dtTask.setEndTime(new Date());
                        dtTask.setTaskStatus(-1);
                        dtTask.setStopReason("跟期结束");
                        this.updateById(dtTask);
                        return;
                    }
                }
            }

            int lotteryType = dtTask.getLotteryType();
            if(lotteryType==2){
                draw = p3DrawService.getLastDrawInfo();
            }else{
                draw = drawService.getLastDrawInfo();
            }

            //判断当期是否已按计划进行投注
            PlayerBuyRecord buyRecord = playerBuyRecordService.getOneByDrawNo(playerId, taskId, draw.getDrawId());
            if (null != buyRecord) {
                return;
            }

            BigDecimal buyPoints = null;
            String buyPointsValue = dtTask.getBuyPoints();
            String[] buyPointsArr = buyPointsValue.split("-");
            if (buyPointsArr.length == 1) {
                buyPoints = new BigDecimal(buyPointsArr[0]);
            } else {
                if (dtTask.getCircleType() == 0) {
                    int idx = (int) (hasFollowed % buyPointsArr.length);
                    buyPoints = new BigDecimal(buyPointsArr[idx]);
                } else if (dtTask.getCircleType() == 2) {
                    buyPoints = new BigDecimal(buyPointsArr[0]);
                } else {
                    //继续跟码
                    if (hasFollowed >= buyPointsArr.length) {
                        buyPoints = new BigDecimal(buyPointsArr[buyPointsArr.length - 1]);
                    } else {
                        int idx = (int) (hasFollowed % buyPointsArr.length);
                        buyPoints = new BigDecimal(buyPointsArr[idx]);
                    }
                }
            }

            String info = String.format("玩家：%s>>>定投：%s>>>期数:%s>>>开始执行", player.getNickname(), dtTask.getBuyDesc(), draw.getDrawId());
//            System.out.println(info);
            List<BuyRecord3DVO> buyList = null;
            String kuaixuanRule = dtTask.getKuaixuanRule();
            if (StringUtil.isNotNull(kuaixuanRule)) {
                buyList = JSONArray.parseArray(kuaixuanRule, BuyRecord3DVO.class);
                BigDecimal buyMoney = buyPoints;
                buyList.forEach(item -> {
                    item.setBuyMoney(buyMoney);
                });
                String buyDesc = dtTask.getBuyDesc() + "各" + buyMoney.stripTrailingZeros().toPlainString();
                ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser, player, buyDesc);
                dingtouBuyService.runTask(dtTask, fromMsg, botUser, player, buyList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
