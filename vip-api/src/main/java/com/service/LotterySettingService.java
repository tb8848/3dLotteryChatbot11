package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.LotterySetting;
import com.dao.LotterySettingDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LotterySettingService extends ServiceImpl<LotterySettingDAO, LotterySetting> {

    @Autowired
    private LotterySettingDAO dataDao;

    public  List<LotterySetting> getListBy(String lotteryMethodId){
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        try {
            QueryWrapper<LotterySetting> qw = new QueryWrapper<>();
            qw.eq("lotteryMethodId", lotteryMethodId);
            qw.eq("isShow",1);
            qw.notIn("id", excludeIds);
            return dataDao.selectList(qw);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public  List<LotterySetting> getListByRuleName(String ruleName){
        try {
            QueryWrapper<LotterySetting> qw = new QueryWrapper<>();
            qw.eq("bettingRule", ruleName);
            qw.eq("isShow",1);
            return dataDao.selectList(qw);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 查询所有规则
     * 排除无效的记录ID：19,20,21
     * @return
     */
    public List<LotterySetting> getLotterySettingList(){
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        try {
            QueryWrapper<LotterySetting> qw = new QueryWrapper<>();
            qw.eq("isShow",1);
            qw.notIn("id", excludeIds);
            return dataDao.selectList(qw);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    /**
     * 查询所有规则
     * 排除无效的记录ID：19,20,21
     * @return
     */
    public List<LotterySetting> getLotterySettingList(Integer isShow){
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        try {
            QueryWrapper<LotterySetting> qw = new QueryWrapper<>();
            if(null!=isShow){
                qw.eq("isShow",isShow);
            }

            qw.notIn("id", excludeIds);
            return dataDao.selectList(qw);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询所有规则
     * 排除无效的记录ID：19,20,21
     * @return
     */
    public LotterySetting getLotterySettingByTypeId(String lmId,String lsTypeId){
        LambdaQueryWrapper<LotterySetting> qw = new LambdaQueryWrapper<>();
        qw.eq(LotterySetting::getLotteryMethodId, lmId);
        qw.eq(LotterySetting::getIsShow,1);
//        qw.eq(LotterySetting::getTypeId, lsTypeId);
        qw.last("limit 1");
        return dataDao.selectOne(qw);

    }

}
