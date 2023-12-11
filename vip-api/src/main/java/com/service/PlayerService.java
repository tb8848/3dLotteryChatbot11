package com.service;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Player;
import com.dao.PlayerDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlayerService extends ServiceImpl<PlayerDAO, Player> {

    @Autowired
    private PlayerDAO dataDao;

    @Autowired
    private DrawService drawService;

    @Autowired
    private LockTemplate lockTemplate;

    public BigDecimal updatePoint(String uid, BigDecimal newCredit, boolean upOrDown){
        LockInfo lockInfo = null;
        try {
            lockInfo = lockTemplate.lock("updatebalance-"+uid,3600000,3600000);
            Player vip = dataDao.selectById(uid);
            if (null != vip) {
                BigDecimal leftCredit = vip.getPoints();
                BigDecimal hasUsed = null;
                if (null == hasUsed) {
                    hasUsed = BigDecimal.ZERO;
                }
                if (upOrDown) {
                    leftCredit = leftCredit.add(newCredit);
                    hasUsed = hasUsed.subtract(newCredit);
                } else {
                    leftCredit = leftCredit.subtract(newCredit);
                    hasUsed = hasUsed.add(newCredit);
                }
                if (leftCredit.compareTo(BigDecimal.ZERO) == -1) {
                    leftCredit = BigDecimal.ZERO;
                }
                if (hasUsed.compareTo(BigDecimal.ZERO) == -1) {
                    hasUsed = BigDecimal.ZERO;
                }
                dataDao.updatePoint(uid, leftCredit, hasUsed);
                return leftCredit;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null!=lockInfo){
                lockTemplate.releaseLock(lockInfo);
            }
        }
        return BigDecimal.ZERO;

    }

    public List<Player> dummyList(String botUserId){
        LambdaQueryWrapper<Player> dummyQw = new LambdaQueryWrapper<>();
        dummyQw.eq(Player::getBotUserId,botUserId);
        dummyQw.eq(Player::getUserType,0);
        dummyQw.eq(Player::getStatus,1);
        List<Player> dummyList = dataDao.selectList(dummyQw);
        return dummyList;
    }

    public Player getOneBy(String botUserId,String playerWxId){
        LambdaQueryWrapper<Player> qw = new LambdaQueryWrapper<>();
        qw.eq(Player::getBotUserId,botUserId);
        qw.eq(Player::getWxFriendId,playerWxId);
        qw.last("limit 1");
        return dataDao.selectOne(qw);
    }

    public IPage<Player> getByPager(String botUserId,Integer pageNo,Integer pageSize,Integer userType){
        Page<Player> page = new Page(pageNo,pageSize);
        LambdaQueryWrapper<Player> qw = new LambdaQueryWrapper<>();
        qw.eq(Player::getBotUserId,botUserId);
        qw.eq(Player::getStatus,1);
        if(null!=userType){
            qw.in(Player::getUserType,userType);
        }else{
            qw.in(Player::getUserType,Lists.newArrayList(1,2));
        }
        return dataDao.selectPage(page,qw);
    }


    public IPage<Player> getBlackListByPager(String botUserId,Integer pageNo,Integer pageSize){
        Page<Player> page = new Page(pageNo,pageSize);
        LambdaQueryWrapper<Player> qw = new LambdaQueryWrapper<>();
        qw.eq(Player::getBotUserId,botUserId);
        qw.eq(Player::getStatus,2);
        qw.in(Player::getUserType,1,2);
        return dataDao.selectPage(page,qw);
    }


    /**
     * 批量更改玩家的回水，过滤掉单独设置了回水的玩家
     * @param hsvalue
     * @param botUserId
     */
    public void updateHsvalue(BigDecimal hsvalue,String botUserId){
        LambdaUpdateWrapper<Player> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Player::getHsvalue,hsvalue);
        updateWrapper.eq(Player::getBotUserId,botUserId);
        updateWrapper.eq(Player::getHsType,0);
        dataDao.update(null,updateWrapper);
    }


    public Player getByOpenId(String openId) {
        LambdaQueryWrapper<Player> qw = new LambdaQueryWrapper<>();
        qw.eq(Player::getOpenid,openId);
        qw.eq(Player::getStatus,1);
        qw.last("limit 1");
        return dataDao.selectOne(qw);
    }

    /**
     * 统计玩家总分
     * @param botUserId
     * @return
     */
    public BigDecimal countTotalPoints(String botUserId) {
        QueryWrapper<Player> qw = new QueryWrapper<>();
        qw.select("sum(points) as points");
        qw.lambda().eq(Player::getBotUserId,botUserId);
        qw.lambda().in(Player::getUserType, Lists.newArrayList(1,2));
        qw.lambda().eq(Player::getStatus,1);
        Player one =  dataDao.selectOne(qw);
        if(null==one || null == one.getPoints()){
            return BigDecimal.ZERO;
        }else{
            return one.getPoints();
        }
    }

    public BigDecimal getPoints(String uid){
        Player vip = dataDao.selectById(uid);
        return vip.getPoints();
    }

}
