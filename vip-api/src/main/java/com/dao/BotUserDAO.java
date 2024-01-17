package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.BotUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface BotUserDAO extends BaseMapper<BotUser> {

    @Update("update bot_user set wxId=#{user.wxId},wxHeadimg=#{user.wxHeadimg}," +
            "wxNick=#{user.wxNick},wxStatus=#{user.wxStatus}," +
            "wxLoginTime=#{user.wxLoginTime}, wxUserName=#{user.wxUserName}, wxPassword=#{user.wxPassword} " +
            "where id=#{user.id}")
    void updateWxInfo(@Param("user") BotUser botUser);
}
