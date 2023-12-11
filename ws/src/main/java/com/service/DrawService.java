package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Draw;
import com.beans.DrawBuyRecord;
import com.dao.DrawDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DrawService extends ServiceImpl<DrawDAO, Draw> {


    @Autowired
    private DrawDAO drawDAO;

    @Autowired
    private DrawBuyRecordService drawBuyRecordService;

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

    private Map<String,Object> updateDrawBuyRecord(Draw draw,Integer lotteryType) {
        Map<String, Object> resultMap = drawBuyRecordService.getDrawRecordByCodes(String.valueOf(draw.getDrawId()), draw,lotteryType);
        return resultMap;
    }

    public Map<String, Object> settleAccounts (Draw draw,Integer lotteryType) {
        Map<String, Object> resultMap = new HashMap<>();

        // 购买记录表中奖数据更新返回中奖条数
        Map<String, Object> map1 = updateDrawBuyRecord(draw,lotteryType);
        // 总
        BigDecimal totalAmount = (BigDecimal) map1.get("totalAmount");
        // 中奖金额
        BigDecimal totalDrawAmount = (BigDecimal) map1.get("totalDrawAmount");
        resultMap.put("drawCount", map1.get("drawCount"));
        resultMap.put("totalDrawAmount", totalDrawAmount);
        resultMap.put("totalAmount", totalAmount);

        return resultMap;
    }



    public List<Draw> listN(int n) {
        LambdaQueryWrapper<Draw> query = new LambdaQueryWrapper<>();
        query.in(Draw::getDrawStatus, Lists.newArrayList(1,2,3));
        query.orderByDesc(Draw::getDrawId);
        query.last("limit "+n);
        return drawDAO.selectList(query);
    }


}
