package com.action;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.LotterySetting;
import com.beans.PlaySetting;
import com.beans.ResponseBean;
import com.service.PlaySettingService;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/10/31 15:16
 */
@CrossOrigin
@RestController
@RequestMapping("/admin/playSetting")
public class PlaySettingAction {

    @Autowired
    private PlaySettingService playSettingService;

    /**
     * 查询玩法信息
     * @return
     */
    @RequestMapping("/getLotterySetting")
    public ResponseBean getLotterySetting(){
        List<LotterySetting> lotterySettingList = playSettingService.getLotterySetting();
        return new ResponseBean(200,"查询成功",lotterySettingList);
    }

    /**
     * 设置玩法显示隐藏
     * @param lotterySettingList
     * @return
     */
    @RequestMapping("/setLotterySetting")
    public ResponseBean setLotterySetting(@RequestBody List<LotterySetting> lotterySettingList){
        String msg = playSettingService.setLotterySetting(lotterySettingList);
        if (StringUtil.isNotNull(msg)){
            return ResponseBean.error(msg);
        }
        return ResponseBean.ok("成功");
    }

    /**
     * 查询玩法信息
     * @return
     */
    @RequestMapping("/getPlaySetting")
    public ResponseBean getPlaySetting(){
        PlaySetting playSetting = playSettingService.getOne(new QueryWrapper<>());
        return new ResponseBean(200,"查询成功",playSetting);
    }

    /**
     * 设置玩法信息
     * @param playSetting
     * @return
     */
    @RequestMapping("/setPlaySetting")
    public ResponseBean setPlaySetting(@RequestBody PlaySetting playSetting){
        if (playSettingService.updateById(playSetting)){
            return ResponseBean.ok("成功");
        }
        return ResponseBean.error("修改失败");
    }
}
