package com.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerPointsRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface PlayerPointsRecordDAO extends BaseMapper<PlayerPointsRecord> {
    @Select("select d.*, a.nickname as playerName, b.loginName as botName " +
            "from player_points_record d " +
            "join (select id from player_points_record c " + " ${ew.customSqlSegment}" + " limit #{page2}, #{size}) e on e.id = d.id " +
            "left join `player` a on d.playerId = a.id " +
            "left join `bot_user` b on a.botUserId = b.id where a.userType in (1,2)"
    )
    List<PlayerPointsRecord> dynamicQuery(@Param("ew") QueryWrapper wrapper, @Param("page2") int page2 , @Param("size") int limit);

    @Select("select count(*)" +
            " from player_points_record c " +
            " left join `player` a on c.playerId = a.id " +
            " left join `bot_user` b on a.botUserId = b.id " + "${ew.customSqlSegment} and a.userType in (1,2)"
    )
    Long dynamicQueryCount(@Param("ew") QueryWrapper wrapper);

    @Select("select sum(points) as points " +
            "from player_points_record pb " +
            "join (select id from player where status=1 and userType in (1,2) and botUserId=#{botUserId}) p on p.id=pb.playerId " +
            "where pb.applyTime between #{startTime} and #{endTime} and pb.optType = #{optType} and pb.authStatus = 1")
    PlayerPointsRecord getSumPoints(@Param("botUserId") String botUserId, @Param("startTime") Date startTime, @Param("endTime")Date endTime, @Param("optType") String optType);

    @Select("select sum(points) as points " +
            "from player_points_record pb " +
            "join (select id from player where status=1 and userType in (1,2)) p on p.id=pb.playerId " +
            "where pb.applyTime between #{startTime} and #{endTime} and pb.optType = #{optType} and pb.authStatus = 1 and pb.playerId = #{playerId} ")
    PlayerPointsRecord getSumPointsByPlayerId(@Param("playerId") String playerId, @Param("startTime") Date startTime, @Param("endTime")Date endTime, @Param("optType") String optType);
}
