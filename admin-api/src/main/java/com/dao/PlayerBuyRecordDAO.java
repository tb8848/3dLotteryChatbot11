package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerBuyRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

public interface PlayerBuyRecordDAO extends BaseMapper<PlayerBuyRecord> {

    @Select("select sum(buyPoints) as buyPoints,sum(earnPoints) as earnPoints,sum(hsPoints) as hsPoints, sum(buyAmount) as buyAmount, sum(drawPoints) as drawPoints " +
            "from player_buy_record pb " +
            "join (select id from player where status=1 and userType in (1,2) and botUserId=#{botUserId}) p on p.id=pb.playerId " +
            "where pb.buyTime between #{startTime} and #{endTime} and pb.buyStatus in (0,1)")
    PlayerBuyRecord countDayDataBy(@Param("botUserId") String botUserId, @Param("startTime")Date startTime, @Param("endTime")Date endTime);

    @Select("select sum(buyPoints) as buyPoints,sum(earnPoints) as earnPoints,sum(hsPoints) as hsPoints,sum(drawPoints) as drawPoints, sum(buyAmount) as buyAmount " +
            "from player_buy_record pb " +
            "join (select id from player where status=1 and userType in (1,2) and id=#{playerId}) p on p.id=pb.playerId " +
            "where pb.buyTime between #{startTime} and #{endTime} and pb.buyStatus in (0,1)")
    PlayerBuyRecord getPlayerByRecordSumByPlayerId(@Param("playerId") String playerId, @Param("startTime")Date startTime, @Param("endTime")Date endTime);
}
