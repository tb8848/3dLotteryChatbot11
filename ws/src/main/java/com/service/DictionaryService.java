package com.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Dictionary;
import com.dao.DictionaryDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryService extends ServiceImpl<DictionaryDAO, Dictionary> {

    @Autowired
    private DictionaryDAO dataDAO;

    public Dictionary getDicByCode (String type, String code){
        List<Dictionary> dicList = dataDAO.selectList(new QueryWrapper<Dictionary>().eq("code",code).eq("type",type));
        if (dicList.size() >0) {
            return dicList.get(0);
        }
        return null;
    }

}
