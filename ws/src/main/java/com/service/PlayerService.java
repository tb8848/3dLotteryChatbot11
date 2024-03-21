package com.service;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Player;
import com.dao.PlayerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService extends ServiceImpl<PlayerDAO, Player> {


    @Autowired
    private PlayerDAO dataDAO;

    @Autowired
    private LockTemplate lockTemplate;


    /**
     * 更新积分
     * @param uid 会员ID
     * @param newCredit 待增加或减少的信用额度值
     * @param upOrDown true:增加，false:减少
     * @return
     */
    public BigDecimal updatePoint(String uid, BigDecimal newCredit, boolean upOrDown){
        LockInfo lockInfo = null;
        try {
            lockInfo = lockTemplate.lock("updatebalance-"+uid,3600000,3600000);
            Player vip = dataDAO.selectById(uid);
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
                dataDAO.updatePoint(uid, leftCredit, hasUsed);
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


    public BigDecimal getPoints(String uid){
        Player vip = dataDAO.selectById(uid);
        return vip.getPoints();
    }

    public List<Player> getListByIds(List<String> playerIdList) {
        if (!playerIdList.isEmpty()) {
            return dataDAO.selectBatchIds(playerIdList);
        }
        return new ArrayList<>();
    }

    public Player getOneBy(String uid, String wxId) {

        LambdaQueryWrapper<Player> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Player::getBotUserId,uid);
        lambdaQueryWrapper.eq(Player::getWxFriendId,wxId);
        lambdaQueryWrapper.last("limit 1");
        return dataDAO.selectOne(lambdaQueryWrapper);
    }

    /**
     * 获取微信玩家
     * @param botUserId
     * @return
     */
    public List<Player> getWxPlayerListBy(String botUserId) {

        LambdaQueryWrapper<Player> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Player::getBotUserId,botUserId);
        lambdaQueryWrapper.eq(Player::getUserType,2);
        lambdaQueryWrapper.eq(Player::getStatus,1);
        return dataDAO.selectList(lambdaQueryWrapper);
    }
}
