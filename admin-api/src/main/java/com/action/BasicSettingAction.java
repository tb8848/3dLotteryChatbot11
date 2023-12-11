package com.action;

import com.beans.ResponseBean;
import com.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/admin/basicSetting")
public class BasicSettingAction {
    @Autowired
    private DictionaryService dictionaryService;

    @RequestMapping(value = "getBasicSetting")
    public ResponseBean getBasicSetting () {
        return ResponseBean.OK;
    }
}
