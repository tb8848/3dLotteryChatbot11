package com.action;

import com.beans.BotUserOdds;
import com.beans.LotteryMethod;
import com.beans.ResponseBean;
import com.google.common.collect.Maps;
import com.service.BotUserOddsService;
import com.service.LotteryMethodService;
import com.util.JwtUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/admin/botUserOdds")
public class BotUserOddsAction {

    @Autowired
    private BotUserOddsService botUserOddsService;

    @Autowired
    private LotteryMethodService lotteryMethodService;

    /**
     * 赔率设置列表
     * @return
     */
    @GetMapping("/oddsList")
    public ResponseBean listPage(String userId, @RequestHeader(value = "lang") String lang,@RequestParam(defaultValue = "1")int lotteryType) {
        List<LotteryMethod> list = lotteryMethodService.list();
        for(LotteryMethod lm : list){
            lm.setBotUserOddsList(botUserOddsService.getListByUserId(userId, lm.getId(), lotteryType));
        }
        return new ResponseBean(0,1,list);
    }

    /**
     * 批量用户赔率设置数据
     * @param botUserOddsList
     * @return
     */
    @RequestMapping("/batchUpdateOdds")
    public ResponseBean batchUpdateOdds (@RequestBody List<BotUserOdds> botUserOddsList, @RequestHeader(value = "token") String token,
                                         @RequestHeader(value = "lang") String lang) {
        if(botUserOddsList.size()<1){
            return ResponseBean.ok("common.operationSuccess");
        }
        String uid = JwtUtil.getUsername(token);
        List<BotUserOdds> lsList = Lists.newArrayList();
        Map<String, Object> resultMap = Maps.newLinkedHashMap();
        for(BotUserOdds ls : botUserOddsList){
            resultMap = botUserOddsService.updateOdds(ls, uid, lsList, lang);
            if (resultMap.containsKey("param")) {
                if (resultMap.get("param") instanceof String[]) {
                    String[] sts = (String[]) resultMap.get("param");
                    return new ResponseBean(500, resultMap.get("error").toString(), new Object[]{sts[0], sts[1]});
                }else {
                    return new ResponseBean(500, resultMap.get("error").toString(), new Object[]{resultMap.get("param")});
                }
            }
        }
        if (lsList.size() == botUserOddsList.size()) {
            botUserOddsService.updateBatchById(lsList);
            return ResponseBean.ok("common.operationSuccess");
        }

        return ResponseBean.error(resultMap.get("error").toString());
    }
}
