package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Proxy;
import com.dao.ProxyDAO;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProxyService extends ServiceImpl<ProxyDAO, Proxy> {
    @Autowired
    private ProxyDAO proxyDAO;

    /**
     * 静态代理ip列表
     * @return
     */
    public List<Proxy> getProxyList (String status) {
        LambdaQueryWrapper<Proxy> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtil.isNotNull(status)) {
            lambdaQueryWrapper.eq(Proxy::getStatus, status);
        }
        List<Proxy> proxyList = proxyDAO.selectList(lambdaQueryWrapper);
        if (!proxyList.isEmpty()) {
            proxyList.stream().forEach(proxy -> {
                proxy.setArea(proxy.getProvince() + "," + proxy.getCity());
            });
        }
        return proxyList;
    }
}
