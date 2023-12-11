package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.dao.PlayerBuyRecordDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerBuyRecordService extends ServiceImpl<PlayerBuyRecordDAO, PlayerBuyRecord> {


    @Autowired
    private PlayerBuyRecordDAO dataDAO;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;


    public Long countFollowDraws(String playerId,String dtTaskId) {
        return dataDAO.countFollowDraws(playerId,dtTaskId);
    }



    public PlayerBuyRecord getOneByDrawNo(String playerId,String dtTaskId,Integer drawNo) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper<>();
        qw.lambda().eq(PlayerBuyRecord::getPlayerId,playerId).eq(PlayerBuyRecord::getDtTaskId,dtTaskId).eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }


    //统计当期总投
    public BigDecimal sumTotalBuy(String playerId, Integer drawNo) {
        return dataDAO.sumTotalBuy(playerId, drawNo);
    }


    public BigDecimal sumTotalBuy(String playerId, List<Integer> drawNoList) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(buyPoints) as buyPoints");
        qw.lambda().in(PlayerBuyRecord::getDrawNo,drawNoList)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr){
            return pr.getBuyPoints();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal sumTotalBuy(String playerId, Integer drawNo,Integer lotteryType) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(buyPoints) as buyPoints");
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo)
                .eq(PlayerBuyRecord::getLotteryType,lotteryType)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr){
            return pr.getBuyPoints();
        }
        return BigDecimal.ZERO;
    }


    public BigDecimal sumYKBuy(String playerId, List<Integer> drawNoList) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(earnPoints) as earnPoints");
        qw.lambda().in(PlayerBuyRecord::getDrawNo,drawNoList)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1))
                .in(PlayerBuyRecord::getBuyType,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr && null!=pr.getEarnPoints()){
            return pr.getEarnPoints();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal sumYKBuy(String playerId, Integer drawNo,Integer lotteryType) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(earnPoints) as earnPoints");
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .eq(PlayerBuyRecord::getLotteryType,lotteryType)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1))
                .in(PlayerBuyRecord::getBuyType,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr && null!=pr.getEarnPoints()){
            return pr.getEarnPoints();
        }
        return BigDecimal.ZERO;
    }

    //统计当期有效投注金额
    public BigDecimal sumValidBuy(String playerId, Integer drawNo) {
        return dataDAO.sumValidBuy(playerId, drawNo);
    }


    public BigDecimal sumValidBuy(String playerId, List<Integer> drawNoList) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(buyPoints) as buyPoints");
        qw.lambda().in(PlayerBuyRecord::getDrawNo,drawNoList)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1))
                .in(PlayerBuyRecord::getBuyType,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr){
            return pr.getBuyPoints();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal sumValidBuy(String playerId, Integer drawNo,Integer lotteryType) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(buyPoints) as buyPoints");
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .eq(PlayerBuyRecord::getLotteryType,lotteryType)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1))
                .in(PlayerBuyRecord::getBuyType,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr){
            return pr.getBuyPoints();
        }
        return BigDecimal.ZERO;
    }


    //创建失败的订单
    public PlayerBuyRecord createFailRecord(Player player, BotUser botUser,
                                            PlayerFixedBuy fixedBuy,String buyDesc,
                                            Integer drawNo,BigDecimal buyPoints,Integer buyAmount) {
        PlayerBuyRecord failOne = new PlayerBuyRecord();
        failOne.setPlayerId(player.getId());
        failOne.setBuyDesc(buyDesc);
        failOne.setBuyStatus(-2);
        failOne.setDrawNo(drawNo);
        failOne.setBotUserId(botUser.getId());
        failOne.setBuyPoints(buyPoints);
        failOne.setBuyAmount(buyAmount);
        if(null!=fixedBuy){
            failOne.setBuyFrom(1);
            failOne.setDtTaskId(fixedBuy.getId());
            failOne.setDtStatus(0);
            failOne.setDtDesc(fixedBuy.getStopReason());
        }else{
            failOne.setBuyFrom(0);
        }
        failOne.setBuyTime(new Date());
        return failOne;
    }


    public List<PlayerBuyRecord> getListBy(List<Integer> drawNoList,String playerId) {
        LambdaQueryWrapper<PlayerBuyRecord> qw = new LambdaQueryWrapper<>();
        qw.in(PlayerBuyRecord::getDrawNo,drawNoList);
        qw.eq(PlayerBuyRecord::getPlayerId,playerId);
        qw.in(PlayerBuyRecord::getBuyType, Lists.newArrayList(0,1));
        qw.in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1));
        return dataDAO.selectList(qw);
    }

    public List<PlayerBuyRecord> getListBy(String playerId,Integer drawNo,Integer lotteryType) {
        LambdaQueryWrapper<PlayerBuyRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.eq(PlayerBuyRecord::getPlayerId,playerId);
        qw.eq(PlayerBuyRecord::getLotteryType,lotteryType);
        qw.in(PlayerBuyRecord::getBuyType, Lists.newArrayList(0,1));
        qw.in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1));
        return dataDAO.selectList(qw);
    }

    /**
     * 获取玩家当期的下注记录
     * @param playerId 玩家ID
     */
    public List<PlayerBuyRecord> getListBy(String playerId) {
        Draw p3Draw = p3DrawService.getLastDrawInfo();
        Draw d3Draw = drawService.getLastDrawInfo();
        List<Integer> list = Lists.newArrayList();
        if(null!=d3Draw){
            list.add(d3Draw.getDrawId());
        }
        if(null!=p3Draw){
            list.add(p3Draw.getDrawId());
        }
        List<PlayerBuyRecord> alist = getListBy(list,playerId);
        return alist;

    }


}
