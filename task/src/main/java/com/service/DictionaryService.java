package com.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Dictionary;
import com.dao.DictionaryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryService extends ServiceImpl<DictionaryDAO, Dictionary> {

    @Autowired
    private DictionaryDAO dictionaryDAO;

    public List<Dictionary> getDicByType (String type){
        return dictionaryDAO.selectList(new QueryWrapper<Dictionary>().eq("type",type));
    }

    public Dictionary getDicByName (String type, String name){
        List<Dictionary> dicList = dictionaryDAO.selectList(new QueryWrapper<Dictionary>().eq("name",name).eq("type",type));
        if (dicList.size() >0) {
            return dicList.get(0);
        }
        return null;
    }

    public Dictionary getDicByCode (String type, String code){
        List<Dictionary> dicList = dictionaryDAO.selectList(new QueryWrapper<Dictionary>().eq("code",code).eq("type",type));
        if (dicList.size() >0) {
            return dicList.get(0);
        }
        return null;
    }

    /**
     * 参数分页
     * @param page
     * @param limit
     * @return
     */
    public IPage<Dictionary> selectDrawAll(Integer page, Integer limit){
        Page p = new Page(page,limit);
        IPage iPage = dictionaryDAO.selectPage(p,null);
        return iPage ;
    }

}
