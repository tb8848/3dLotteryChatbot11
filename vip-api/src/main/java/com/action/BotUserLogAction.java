package com.action;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.ResponseBean;
import com.service.BotUserLogService;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/6 14:26
 */
@RestController
@RequestMapping(value = "/bot/userLog")
public class BotUserLogAction {

    @Autowired
    private BotUserLogService botUserLogService;

    /**
     * 分页查询机器人操作日志
     * @param token
     * @param pageNo
     * @param pageSize
     * @param dayRange 时间（1：当日  2：昨日  3：本周）
     * @return
     */
    @RequestMapping("/getUserLogByPage")
    public ResponseBean getUserLogByPage(@RequestHeader(value = "token")String token, @RequestParam(defaultValue = "1") Integer pageNo,
                                         @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "1") Integer dayRange){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return ResponseBean.error("请先登录！");
        }
        IPage iPage = botUserLogService.getUserLogByPage(uid,pageNo,pageSize,dayRange);
        return new ResponseBean(0,iPage.getTotal(),"查询成功",iPage.getRecords());
    }
}
