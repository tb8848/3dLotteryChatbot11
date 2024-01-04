package com.service;

import cn.hutool.core.date.DateUtil;
import com.auth.AuthContext;
import com.auth.AuthInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.BotUserSetting;
import com.beans.Dictionary;
import com.beans.ResponseBean;
import com.config.I18nUtils;
import com.dao.BotUserDAO;
import com.dao.BotUserSettingDAO;
import com.google.common.collect.Maps;

import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BotUserService extends ServiceImpl<BotUserDAO, BotUser> {

    @Autowired
    private BotUserDAO userDAO;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private BotUserSettingDAO botUserSettingDAO;

    /**
     * 验证机器人账号是否存在
     * @param loginName
     * @return
     */
    public boolean checkLoginNameExist(String loginName){
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BotUser::getLoginName,loginName);
        lambdaQueryWrapper.in(BotUser::getStatus,Lists.newArrayList(1,2));
        Long count = userDAO.selectCount(lambdaQueryWrapper);
        if(null!=count && count>0){
            return true;
        }
        return false;
    }


    /**
     * 验证机器人账号是否存在
     * @param loginName
     * @return
     */
    public IPage<BotUser> listPager(String loginName,Integer pageNo,Integer pageSize, String uId,int status){
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Page<BotUser> page = new Page<>(pageNo,pageSize);
        if (status != -1){
            lambdaQueryWrapper.eq(BotUser::getStatus,status);
        }
        if(StringUtil.isNotNull(loginName)){
            lambdaQueryWrapper.like(BotUser::getLoginName,loginName);
        }
        IPage<BotUser> iPage = userDAO.selectPage(page,lambdaQueryWrapper);
        if (!iPage.getRecords().isEmpty()) {
            iPage.getRecords().stream().forEach(botUser -> {
                LambdaQueryWrapper<BotUserSetting> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(BotUserSetting::getBotUserId, botUser.getId());
                queryWrapper.last("limit 1");
                BotUserSetting botUserSetting = botUserSettingDAO.selectOne(queryWrapper);
                botUser.setHsValue(botUserSetting.getHsvalue());
                botUser.setBotCount(botUserSetting.getDummyAmount());
            });
        }
        return iPage;
    }

    /**
     * 即将到期机器人列表
     * @param loginName
     * @return
     */
    public IPage<BotUser> dueList(String loginName,Integer pageNo,Integer pageSize){
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Page<BotUser> page = new Page<>(pageNo,pageSize);
        if(StringUtil.isNotNull(loginName)){
            lambdaQueryWrapper.like(BotUser::getLoginName,loginName);
        }

        int beforeDays = 15; //提前提醒的天数,默认值
        //读取数据字典中的提前提醒天数配置
        Dictionary dic = dictionaryService.getDicByCode("system","dueDays");
        if(dic!=null){
            beforeDays = Integer.valueOf(dic.getValue());
        }
        Date currDate = DateUtil.date(); //当前时间
        Date searchStartDate = DateUtil.beginOfDay(currDate); //当前时间的开始时间
        Date searchEndDate = DateUtil.endOfDay(DateUtil.offsetDay(currDate,beforeDays));
        lambdaQueryWrapper.ge(BotUser::getDueDate,searchStartDate);
        lambdaQueryWrapper.le(BotUser::getDueDate,searchEndDate);

        IPage<BotUser> iPage = userDAO.selectPage(page,lambdaQueryWrapper);
        if (!iPage.getRecords().isEmpty()) {
            for (BotUser botUser : iPage.getRecords()) {
                if (null != botUser.getDueDate()) {
                    LocalDateTime today = LocalDateTime.now();
                    Instant instant = botUser.getDueDate().toInstant();
                    ZoneId zoneId = ZoneId.of("GMT+08:00");
                    LocalDateTime dueDate = instant.atZone(zoneId).toLocalDateTime();
                    long days = Duration.between(today, dueDate).toDays();
                    botUser.setValidDays(days);
                }
            }
        }
        return iPage;
    }

    /**
     * 已到期机器人列表
     * @param loginName
     * @return
     */
    public IPage<BotUser> expiredList(String loginName,Integer pageNo,Integer pageSize){
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Page<BotUser> page = new Page<>(pageNo,pageSize);
        if(StringUtil.isNotNull(loginName)){
            lambdaQueryWrapper.like(BotUser::getLoginName,loginName);
        }
        lambdaQueryWrapper.lt(BotUser::getDueDate,new Date());
        IPage<BotUser> iPage = userDAO.selectPage(page,lambdaQueryWrapper);
        return iPage;
    }
}
