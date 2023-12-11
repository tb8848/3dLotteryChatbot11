package com.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.LotteryMethod;
import com.dao.LotteryMethodDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LotteryMethodService extends ServiceImpl<LotteryMethodDAO, LotteryMethod> {

    @Autowired
    private LotteryMethodDAO dataDao;

    public List<LotteryMethod> getListByType(Integer lotteryType){

        try {
            QueryWrapper<LotteryMethod> qw = new QueryWrapper<>();
            qw.eq("lotteryTypeId", lotteryType);
            return dataDao.selectList(qw);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


}
