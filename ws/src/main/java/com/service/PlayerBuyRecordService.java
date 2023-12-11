package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Draw;
import com.beans.PlayerBuyRecord;
import com.beans.PlayerPointsRecord;
import com.dao.PlayerBuyRecordDAO;
import com.dao.PlayerPointsRecordDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
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


    public List<String> getPlayerIdsBy(Integer drawNo) {

        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper<>();
        qw.select("distinct(playerId) as playerId");
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.lambda().in(PlayerBuyRecord::getBuyStatus, Lists.newArrayList(0,1));
        List<PlayerBuyRecord> list = dataDAO.selectList(qw);
        if(null != list && list.size()>0){
            return list.stream().map(item->item.getPlayerId()).collect(Collectors.toList());
        }else{
            return null;
        }
    }


    public List<String> getPlayerIdsBy(Integer drawNo,Integer lotteryType) {

        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper<>();
        qw.select("distinct(playerId) as playerId");
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.lambda().eq(PlayerBuyRecord::getLotteryType,lotteryType);
        qw.lambda().in(PlayerBuyRecord::getBuyStatus, Lists.newArrayList(0,1));
        List<PlayerBuyRecord> list = dataDAO.selectList(qw);
        if(null != list && list.size()>0){
            return list.stream().map(item->item.getPlayerId()).collect(Collectors.toList());
        }else{
            return null;
        }
    }

    public List<PlayerBuyRecord> getListByPlayerIds(List<String> playerIdList1, Integer drawNo) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper<>();
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.lambda().in(PlayerBuyRecord::getPlayerId,playerIdList1);
        qw.lambda().in(PlayerBuyRecord::getBuyStatus, Lists.newArrayList(0,1));
        return dataDAO.selectList(qw);
    }

    public List<PlayerBuyRecord> getListByPlayerIds(List<String> playerIdList1, Integer drawNo,Integer lotteryType) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper<>();
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.lambda().eq(PlayerBuyRecord::getLotteryType,lotteryType);
        qw.lambda().in(PlayerBuyRecord::getPlayerId,playerIdList1);
        qw.lambda().in(PlayerBuyRecord::getBuyStatus, Lists.newArrayList(0,1));
        return dataDAO.selectList(qw);
    }

    //更新记录的状态
    public int updateStatus(Integer drawNo, int i) {
        LambdaUpdateWrapper<PlayerBuyRecord> qw = new LambdaUpdateWrapper<>();
        qw.eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.eq(PlayerBuyRecord::getBuyStatus,0);
        qw.set(PlayerBuyRecord::getBuyStatus,1);
        qw.set(PlayerBuyRecord::getSettlementTime,new Date());
        return dataDAO.update(null,qw);
    }


    public int updateStatus(Integer drawNo, int status,int lotteryType) {
        LambdaUpdateWrapper<PlayerBuyRecord> qw = new LambdaUpdateWrapper<>();
        qw.eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.eq(PlayerBuyRecord::getLotteryType,lotteryType);
        qw.eq(PlayerBuyRecord::getBuyStatus,0);
        qw.set(PlayerBuyRecord::getBuyStatus,1);
        qw.set(PlayerBuyRecord::getSettlementTime,new Date());
        return dataDAO.update(null,qw);
    }

    //统计当期总投
    public BigDecimal sumTotalBuy(String playerId, Integer drawNo) {
        return dataDAO.sumTotalBuy(playerId, drawNo);
    }

    //统计当期有效投注金额
    public BigDecimal sumValidBuy(String playerId, Integer drawNo) {
        return dataDAO.sumValidBuy(playerId, drawNo);
    }


    public BigDecimal sumTotalBuy(String playerId,Integer drawNo,Integer lotteryType) {
        QueryWrapper<PlayerBuyRecord> qw = new QueryWrapper();
        qw.select("sum(buyPoints) as buyPoints");
        qw.lambda().eq(PlayerBuyRecord::getDrawNo,drawNo)
                .eq(PlayerBuyRecord::getPlayerId,playerId)
                .eq(PlayerBuyRecord::getLotteryType,lotteryType)
                .in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1));
        PlayerBuyRecord pr = dataDAO.selectOne(qw);
        if(null!=pr){
            return pr.getBuyPoints();
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


    public List<PlayerBuyRecord> getListBy(String playerId,Integer drawNo,Integer lotteryType) {
        LambdaQueryWrapper<PlayerBuyRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayerBuyRecord::getDrawNo,drawNo);
        qw.eq(PlayerBuyRecord::getPlayerId,playerId);
        qw.eq(PlayerBuyRecord::getLotteryType,lotteryType);
        qw.in(PlayerBuyRecord::getBuyType, Lists.newArrayList(0,1));
        qw.in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(0,1));
        return dataDAO.selectList(qw);
    }
}
