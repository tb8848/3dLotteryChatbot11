package com.action;

import com.beans.Proxy;
import com.beans.ResponseBean;
import com.model.res.ReportRes;
import com.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/admin/report")
public class ReportAction {

    @Autowired
    private ReportService reportService;

    /**
     * 机器人报表
     * @return
     */
    @RequestMapping(value = "getBotReportList")
    public ResponseBean getBotReportList (String botName, String startDate, String endDate) throws ParseException {
        ReportRes reportRes = reportService.getBotReportList(botName, startDate, endDate);
        return new ResponseBean(0, "", reportRes);
    }

    /**
     * 机器人下玩家报表
     * @return
     */
    @RequestMapping(value = "getPlayerReportList")
    public ResponseBean getPlayerReportList (String playerName, String startDate, String endDate, String playerId, String botUserId) throws ParseException {
        ReportRes reportRes = reportService.getPlayerReportList(playerName, startDate, endDate, playerId, botUserId);
        return new ResponseBean(0, "", reportRes);
    }
}
