package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Draw;
import com.dao.DrawDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    /**
     * 获取最新一期开奖信息
     * @return
     */
    public Draw getNewDrawInfo () {
        LambdaQueryWrapper<Draw> query = new LambdaQueryWrapper<>();
        query.ne(Draw::getDrawStatus, 1);
        query.last("limit 1");
        return drawDAO.selectOne(query);
    }


    /**
     * 获取最近68期数据
     * @return
     */
    public List<Draw> getDrawList68 () {
        LambdaQueryWrapper<Draw> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Draw::getDrawId);
        query.last("limit 68");
        List<Draw> drawList = drawDAO.selectList(query);
        if (!drawList.isEmpty()) {
            drawList.stream().forEach(draw -> {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.parse(draw.getDrawDate(), fmt);
                DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("MM-dd");
                String date = localDate.format(fmt2);
                draw.setDrawIdStr(date + "(" + draw.getDrawId() + ")");
            });
        }
        return drawList;
    }
}
