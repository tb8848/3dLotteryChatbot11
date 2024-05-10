package com.action;

import cn.hutool.core.date.DateUtil;
import com.beans.BotUser;
import com.beans.ChatRoomMsg;
import com.beans.ResponseBean;
import com.config.NoLogin;
import com.service.BotUserService;
import com.service.ChatRoomMsgService;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/bot/chatmsg")
public class ChatRoomMsgAction {

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private BotUserService botUserService;

    /**
     * 加载历史消息
     * @param endTime
     * @return
     * @throws ParseException
     */
    @NoLogin
    @RequestMapping(value = "listHistory")
    public ResponseBean listHistory (String endTime,String roomId) {

        if(StringUtil.isNull(endTime) || StringUtil.isNull(roomId)){
            return new ResponseBean(-1,0,"缺少必填参数",null,true);
        }
        try{
            BotUser botUser = botUserService.getById(roomId);
            if(null == botUser){
                return new ResponseBean(-1,0,"参数错误",null,true);
            }

            if(endTime.indexOf("-")<0){
                Date endDate = DateUtil.date(Long.valueOf(endTime));
                endTime = DateUtil.format(endDate,"yyyy-MM-dd HH:mm:ss");
            }
            List<ChatRoomMsg> hisList = chatRoomMsgService.listHistory(roomId,endTime,15);
            if(null!=hisList && hisList.size()>0){
                Collections.reverse(hisList);
            }
            return new ResponseBean(0, "", hisList);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500,0,"系统繁忙",null,true);
        }
    }
}
