package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.Dictionary;
import com.beans.Player;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;


public interface PlayerDAO extends BaseMapper<Player> {

    @Update("update player set points=#{leftCredit} where id=#{uid}")
    void updatePoint(String uid, BigDecimal leftCredit, BigDecimal hasUsed);
}
