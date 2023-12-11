package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.ChatDomain;
import com.dao.ChatDomainDAO;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatDomainService extends ServiceImpl<ChatDomainDAO, ChatDomain> {
    @Autowired
    private ChatDomainDAO chatDomainDAO;

    /**
     * 域名列表
     * @return
     */
    public List<ChatDomain> getChatDomainList (String status) {
        LambdaQueryWrapper<ChatDomain> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtil.isNotNull(status)) {
            lambdaQueryWrapper.eq(ChatDomain::getStatus, status);
        }
        List<ChatDomain> chatDomainList = chatDomainDAO.selectList(lambdaQueryWrapper);
        return chatDomainList;
    }
}
