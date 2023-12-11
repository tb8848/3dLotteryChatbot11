package com.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beans.BotUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BotUserDAO extends BaseMapper<BotUser> {


    IPage<BotUser> getUserRoleByPage(Page page, @Param(value = "username")String username, @Param(value = "userId")String userId,
                                   @Param(value = "nickName")String nickName, @Param(value = "fundMode")Integer fundMode,
                                   @Param(value = "companyId")String companyId, @Param(value = "status")Integer status);

    @Select("select getAllUsers(#{id})")
    String getAllChildUser(String id);
}
