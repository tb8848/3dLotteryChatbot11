package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerBuyRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface PlayerBuyRecordDAO extends BaseMapper<PlayerBuyRecord> {


    @Select("select sum(buyPoints) as buyPoints from player_buy_record where playerId=#{playerId} and drawNo=#{drawNo} and buyStatus in (0,1)")
    BigDecimal sumTotalBuy(@Param("playerId")String playerId, @Param("drawNo")Integer drawNo);


    @Select("select sum(buyPoints) as buyPoints from player_buy_record where playerId=#{playerId} and drawNo=#{drawNo} and buyStatus in (0,1) and buyType in (0,1)")
    BigDecimal sumValidBuy(String playerId, Integer drawNo);

    @Select("select count(distinct drawNo) from player_buy_record where playerId=#{playerId} and dtTaskId=#{dtTaskId}")
    Long countFollowDraws(String playerId,String dtTaskId);


}
