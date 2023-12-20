package com.action;

import com.beans.Dictionary;
import com.beans.ResponseBean;
import com.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/12/20 14:24
 */
@CrossOrigin
@RestController
@RequestMapping("/dictionary")
public class DictionaryAction {

    @Autowired
    private DictionaryService dictionaryService;

    /**
     * 查询多线路IP
     * @return
     */
    @RequestMapping("/selectMultiLineIp")
    public ResponseBean selectMultiLineIp(){
        Dictionary dictionary = dictionaryService.getDicByCode("system","MultiLine");
        if (dictionary == null){
            return ResponseBean.error("未找到数据");
        }
        return new ResponseBean(200,"查询成功",dictionary.getValue());
    }
}
