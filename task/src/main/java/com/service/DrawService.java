package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Draw;
import com.dao.DrawDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrawService extends ServiceImpl<DrawDAO, Draw> {


    @Autowired
    private DrawDAO drawDAO;

    /**
     * 获取最新一期开奖信息
     * @return
     */
    public Draw getLastDrawInfo () {
        LambdaQueryWrapper<Draw> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Draw::getDrawId);
        query.last("limit 1");
        return drawDAO.selectOne(query);
    }

    public Draw getByDrawNo(Integer drawNo) {
        LambdaQueryWrapper<Draw> query = new LambdaQueryWrapper<>();
        query.eq(Draw::getDrawId,drawNo);
        query.last("limit 1");
        return drawDAO.selectOne(query);
    }



    public List<Draw> listN(int n) {
        LambdaQueryWrapper<Draw> query = new LambdaQueryWrapper<>();
        query.in(Draw::getDrawStatus, Lists.newArrayList(1,2,3));
        query.orderByDesc(Draw::getDrawId);
        query.last("limit "+n);
        return drawDAO.selectList(query);
    }
}
