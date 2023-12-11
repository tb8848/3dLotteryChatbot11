package com.action;

import com.beans.ResponseBean;
import com.service.PlayerPointsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/admin/playerPointsRecord")
public class PlayerPointsRecordAction {
    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @RequestMapping("listPage")
    public ResponseBean listPage (@RequestParam(defaultValue = "1")Integer page, @RequestParam(defaultValue = "40")Integer limit, String playerName,
                                       String start, String end, String opType, String userId, String botName) {
        Map<String, Object> resultMap = playerPointsRecordService.listPage(page, limit, playerName, botName, opType, start, end,  userId);
        return new ResponseBean(0, (Long)resultMap.get("totalSize"), "成功", resultMap);
    }
}
