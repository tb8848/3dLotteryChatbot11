package com.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUserLog;
import com.dao.BotUserLogDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/6 14:25
 */
@Service
public class BotUserLogService extends ServiceImpl<BotUserLogDAO, BotUserLog> {

    @Autowired
    private BotUserLogDAO botUserLogDAO;

    /**
     * 分页查询机器人操作日志
     * @param uid
     * @param pageNo
     * @param pageSize
     * @param dayRange 时间（1：当日  2：昨日  3：本周）
     * @return
     */
    public IPage getUserLogByPage(String uid,int pageNo,int pageSize,int dayRange){
        String startTime = null;
        String endTime = null;
        if(dayRange==1){
            //当天
            startTime = DateUtil.beginOfDay(new Date()).toString();
            endTime = DateUtil.endOfDay(new Date()).toString();
        }else if(dayRange==2){
            //昨天
            startTime = DateUtil.beginOfDay(DateUtil.yesterday()).toString();
            endTime = DateUtil.endOfDay(DateUtil.yesterday()).toString();
        }else if(dayRange==3){
            //本周
            startTime = DateUtil.beginOfWeek(new Date()).toString();
            endTime = DateUtil.endOfWeek(new Date()).toString();
        }
        Page p = new Page(pageNo,pageSize);
        QueryWrapper q = new QueryWrapper();
        q.eq("userId",uid);
        q.ge("optTime",startTime);
        q.le("optTime",endTime);
        q.orderByDesc("optTime");
        IPage iPage = botUserLogDAO.selectPage(p,q);
        return iPage;
    }
}
