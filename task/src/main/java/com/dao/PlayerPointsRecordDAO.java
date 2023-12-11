package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerPointsRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

public interface PlayerPointsRecordDAO extends BaseMapper<PlayerPointsRecord> {




    @Select("select sum(points) as points from player_points_record pp " +
            "join (select id from player where status=1 and userType in (1,2) and botUserId=#{botUserId}) p on p.id=pp.playerId " +
            "where pp.authStatus=1 and pp.optType=#{optType} and pp.applyTime between #{startTime} and #{endTime}")
    public PlayerPointsRecord countDayDataBy(@Param("botUserId") String botUserId, @Param("startTime") Date startTime, @Param("endTime")Date endTime,@Param("optType")Integer optType);




}
