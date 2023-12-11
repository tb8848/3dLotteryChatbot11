package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
     * 根据地区查询代理IP
     * @param province
     * @param city
     * @return
     */
    public Proxy getProxyByArea (String province, String city) {
        LambdaQueryWrapper<Proxy> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtil.isNotNull(province)) {
            queryWrapper.like(Proxy::getProvince, province+"%");
        }
        if (StringUtil.isNotNull(city)) {
            queryWrapper.like(Proxy::getCity, city+"%");
        }
        queryWrapper.eq(Proxy::getStatus,1);
        queryWrapper.last("limit 1");
        return proxyDAO.selectOne(queryWrapper);
    }

    public Proxy getUnuseProxy () {
        LambdaQueryWrapper<Proxy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Proxy::getStatus,1);
        queryWrapper.last("limit 1");
        return proxyDAO.selectOne(queryWrapper);
    }
}
