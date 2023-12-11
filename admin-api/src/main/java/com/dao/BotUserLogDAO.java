package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beans.BotUserLog;

import org.apache.ibatis.annotations.Param;

public interface BotUserLogDAO extends BaseMapper<BotUserLog> {

    IPage<BotUserLog> selectLog(Page page, @Param(value = "opType")int opType, @Param(value = "startTime")String startTime, @Param(value = "endTime")String endTime);
}
