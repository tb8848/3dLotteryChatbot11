package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerReturnPoints;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/15 14:21
 */
public interface PlayerReturnPointsDAO extends BaseMapper<PlayerReturnPoints> {

    @Select("select sum(returnPoints) as returnPoints from player_return_points prp " +
            "where prp.status=1 and prp.botUserId=#{botUserId} and prp.returnTime between #{startTime} and #{endTime}")
    PlayerReturnPoints countDayData(@Param("botUserId") String botUserId, @Param("startTime") Date startTime, @Param("endTime")Date endTime);
}
