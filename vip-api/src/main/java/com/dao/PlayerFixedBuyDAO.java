package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.PlayerBuyRecord;
import com.beans.PlayerFixedBuy;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface PlayerFixedBuyDAO extends BaseMapper<PlayerFixedBuy> {

}
