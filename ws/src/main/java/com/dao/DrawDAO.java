package com.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.ChatRoom;
import com.beans.Draw;
import com.config.DataSourceConfig;

@DS("3dlottery")
public interface DrawDAO extends BaseMapper<Draw> {


}
