package com.action;

import com.beans.ResponseBean;
import com.service.BetRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/admin/betRecord")
public class BetRecordAction {

    @Autowired
    private BetRecordService betRecordService;

    /**
     * 获取总货明细数据
     * @param username 账号
     * @param buyCodes 号码
     * @param lieType 列出：0赔率、1金额、2退码
     * @param start 开始值，根据列出查询，如列出为0，则根据赔率范围查询，如果列表为2退码，则同列出1 金额查询
     * @param end 结束值，同开始值
     * @param lotterySettingType 分类：包括号码类别，购买路径
     * @param drawId 期号
     * @param drawStatus 中奖状态：0未中奖、1中奖
     * @param userId 越级操作->会员层级->点击会员名称传递过来
     * @return
     */
    @RequestMapping("betRecordList")
    public ResponseBean betRecordList (@RequestParam(defaultValue = "1")Integer page, @RequestParam(defaultValue = "40")Integer limit, String username, String buyCodes, Integer lieType,
                                              BigDecimal start, BigDecimal end, String lotterySettingType, String drawId, String drawStatus, String userId, String botName,
                                              @RequestHeader(value = "lang") String lang) {
        Map<String, Object> resultMap = betRecordService.betRecordList(page, limit, username, buyCodes, lieType, start, end, lotterySettingType, drawId, drawStatus, userId,botName, lang);
        return new ResponseBean(0, (Long)resultMap.get("totalSize"), "成功", resultMap);
    }

}
