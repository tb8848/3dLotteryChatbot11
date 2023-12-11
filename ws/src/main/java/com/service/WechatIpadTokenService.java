package com.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.ChatRoomMsg;
import com.beans.WechatIpadToken;
import com.dao.WechatIpadTokenDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WechatIpadTokenService extends ServiceImpl<WechatIpadTokenDAO, WechatIpadToken> {

    @Autowired
    private WechatIpadTokenDAO dataDAO;



    public WechatIpadToken getUnuseOne(){
        LambdaQueryWrapper<WechatIpadToken> qw = new LambdaQueryWrapper<>();
        qw.eq(WechatIpadToken::getUseFlag,0);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }

    public WechatIpadToken getByUserId(String uid) {
        LambdaQueryWrapper<WechatIpadToken> qw = new LambdaQueryWrapper<>();
        qw.eq(WechatIpadToken::getBotUserId,uid);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }

    public int updateWxStatus(String userId,Integer isLogin) {
        LambdaUpdateWrapper<WechatIpadToken> qw = new LambdaUpdateWrapper<>();
        qw.eq(WechatIpadToken::getBotUserId,userId);
        qw.set(WechatIpadToken::getWxStatus,2);
        return dataDAO.update(null,qw);
    }


    //发送消息给微信好友
    public void sendMsgToFriend(ChatRoomMsg msg){

        String url = "http://localhost:8080/rest/recvMsg";
        HttpUtil.post(url,JSON.toJSONString(msg));
    }
}
