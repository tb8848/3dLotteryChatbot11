package com.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.LotterySetting;
import com.beans.PlaySetting;
import com.dao.LotterySettingDAO;
import com.dao.PlaySettingDAO;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/10/31 15:16
 */
@Service
public class PlaySettingService extends ServiceImpl<PlaySettingDAO, PlaySetting> {

    @Autowired
    private LotterySettingDAO lotterySettingDAO;

    /**
     * 获取玩法信息
     * @return
     */
    public List<LotterySetting> getLotterySetting(){
        //玩法集合
        List<LotterySetting> lotterySettingList = new ArrayList<>();
        List<LotterySetting> list = lotterySettingDAO.selectList(new QueryWrapper<>());

        //过滤码拖和复式
        //过滤码拖和复式
        for (LotterySetting lotterySetting : list){
            String id = lotterySetting.getId();
            String parentId = lotterySetting.getParentId();
            if (id.equals("64")){
                lotterySetting.setBettingRule("组三胆拖");
                lotterySettingList.add(lotterySetting);
            }else if (id.equals("81")){
                lotterySetting.setBettingRule("组六胆拖");
                lotterySettingList.add(lotterySetting);
            }else if (id.equals("56") || id.equals("57") || id.equals("58") || id.equals("59") || id.equals("60") || id.equals("61") || id.equals("62") || id.equals("63")
                    || id.equals("65") || id.equals("66") || id.equals("67") || id.equals("68") || id.equals("69") || id.equals("70") || id.equals("71") || id.equals("74")
                    || id.equals("75") || id.equals("76") || id.equals("77") || id.equals("78") || id.equals("79") || id.equals("80") || id.equals("82") || id.equals("83")
                    || id.equals("84") || id.equals("85") || id.equals("86") || id.equals("87") || id.equals("88") || id.equals("89") || id.equals("90") || id.equals("91")
                    || id.equals("92") || id.equals("93") || id.equals("94") || id.equals("95") || id.equals("98") || id.equals("99") || id.equals("100") || id.equals("101")
                    || id.equals("102") || id.equals("103") || id.equals("104") || id.equals("55") || id.equals("105") || id.equals("52") || id.equals("14") || id.equals("16")
                    || id.equals("96") || id.equals("97") || id.equals("53") || id.equals("72") || id.equals("8") || id.equals("20") || id.equals("23") || id.equals("26")){

            }else if(StringUtil.isNotNull(parentId) && (parentId.equals("5") || parentId.equals("41") || parentId.equals("8") || parentId.equals("100") || parentId.equals("200") || parentId.equals("300")
                    || parentId.equals("3") || parentId.equals("4") || parentId.equals("6") || parentId.equals("7") || parentId.equals("1"))){

            }else{
                if (StringUtil.isNotNull(parentId)){
                    LotterySetting parentLotterySetting = lotterySettingDAO.selectById(parentId);
                    if (parentLotterySetting != null){
                        lotterySetting.setBettingRule(parentLotterySetting.getBettingRule()+"-"+lotterySetting.getBettingRule());
                    }
                }
                lotterySettingList.add(lotterySetting);
            }
        }
        return lotterySettingList;
    }

    /**
     * 修改玩法显示隐藏
     * @param lotterySettingList
     * @return
     */
    public String setLotterySetting(List<LotterySetting> lotterySettingList){
        //设置玩法显示隐藏
        for (LotterySetting lotterySetting : lotterySettingList){
            String id = lotterySetting.getId();
//            if (id.equals("64")){
//                String[] ids = new String[]{"64","65","66","67","68","69","70","71"};
//                for (String lsId : ids){
//                    LotterySetting setting = lotterySettingDAO.selectById(lsId);
//                    setting.setIsShow(lotterySetting.getIsShow());
//                    lotterySettingDAO.updateById(setting);
//                }
//            }else if (id.equals("81")){
//                String[] ids = new String[]{"81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96"};
//                for (String lsId : ids){
//                    LotterySetting setting = lotterySettingDAO.selectById(lsId);
//                    setting.setIsShow(lotterySetting.getIsShow());
//                    lotterySettingDAO.updateById(setting);
//                }
//            }else{
//                LotterySetting setting = lotterySettingDAO.selectById(id);
//                setting.setIsShow(lotterySetting.getIsShow());
//                lotterySettingDAO.updateById(setting);
//            }
            LotterySetting setting = lotterySettingDAO.selectById(id);
            setting.setIsShow(lotterySetting.getIsShow());
            lotterySettingDAO.updateById(setting);
        }
        return null;
    }
}
