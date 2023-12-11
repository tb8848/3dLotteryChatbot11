package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerBuyRecord;
import com.beans.PlayerReturnPoints;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface PlayerReturnPointsDAO extends BaseMapper<PlayerReturnPoints> {

}
