package com.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.GlobalConst;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.ChatRoomMsg;
import com.beans.Dictionary;
import com.beans.Player;
import com.dao.ChatRoomMsgDAO;
import com.google.common.collect.Maps;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatRoomMsgService extends ServiceImpl<ChatRoomMsgDAO, ChatRoomMsg> {

    @Autowired
    private ChatRoomMsgDAO dataDao;

    @Autowired
    private DictionaryService dictionaryService;

//    @Value("${wechat.api.url}")
//    private String wechatApiUrl;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public ChatRoomMsg createFromWxMsg(BotUser botUser,Player player,String text){
        ChatRoomMsg fromMsg = new ChatRoomMsg();
        fromMsg.setFromUserId(player.getId());
        fromMsg.setFromUserImg(player.getHeadimg());
        fromMsg.setFromUserNick(player.getNickname());
        fromMsg.setOptType(0);
        fromMsg.setFromUserType(0);
        fromMsg.setMsgType(0);
        fromMsg.setToUserId(botUser.getId());
        fromMsg.setBotUserId(botUser.getId());
        fromMsg.setToUserType(2);
        fromMsg.setCreateTime(new Date());
        fromMsg.setMsg(text);
        fromMsg.setSource(1);
        return fromMsg;
    }

    public static void main(String[] args) {
        String s = "3D单选456,237,789各1\n" +
                "3d直选百456,十578,个089各1\n" +
                "P3跨度0,7,9各1\n" +
                "3d组三5拖578各1";
        String[] multiArr = s.split("\n");

        for(String cmdText : multiArr){
            String txt = "";
            String text1 = cmdText.toUpperCase();
            if(text1.startsWith("P3") || text1.startsWith("3D")){
                txt = text1.substring(2);
            }else{
                txt = text1.substring(1);
            }
            for(String word : GlobalConst.keywords2){
                if(txt.startsWith(word)){
                    String fromMsg = "3D单选456,237,789各1\n" +
                            "3d直选百456,十578,个089各1\n" +
                            "P3跨度0,7,9各1\n" +
                            "3d组三5拖578各1";
                    String content = fromMsg.substring(2);
                    String[] arr = content.split("各");
                    Map<String,Object> resMap = Maps.newHashMap();
                    for (String word2 : GlobalConst.keywords1) {
                        String typePart = arr[0].toUpperCase();
                        if (typePart.startsWith(word)) {
                            String type = word.toUpperCase();
                            String code = null;
                            String[] typeArr = typePart.split(word);
                            code = typeArr[1];
                            String[] splitArr = code.split("\\.|,|，");
                            List<String> cclist = Arrays.stream(splitArr).map(item->item.trim()).collect(Collectors.toList());
                            switch (type) {
                                case "单选":
                                    resMap = codeBuy(new BigDecimal("1"), code, "1");
                                    break;
                                default:
                                    return;
                            }


                            System.out.println(resMap);
                        }
                    }
                }
            }
        }
    }

    private static Map<String,Object> codeBuy(BigDecimal buyMoney, String codeRule, String lmId) {
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] codeArr = codeRule.split("\\.|,|，");
        if(codeArr.length==0){
            resMap.put("errmsg","号码格式错误");
            return resMap;
        }
        List<String> cclist = Arrays.stream(codeArr).map(item->item.trim()).collect(Collectors.toList());
        for(String code : cclist){
            if(code.length()!=3){
                resMap.put("errmsg","单选号码["+code+"]错误");
                return resMap;
            }
        }

        List<String> codeList = cclist;
        String value = codeList.stream().collect(Collectors.joining(","));
        String hzname = "直选";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc("直选："+value);
        oneRecord.setCodeList(codeList);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }

    public ChatRoomMsg createMsg(BotUser botUser,Player player,String msg){
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(botUser.getId());
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsg(msg);
        toMsg.setMsgType(0);
        toMsg.setToUserType(0);
        toMsg.setToUserNick(player.getNickname());
        toMsg.setToUserId(player.getId());
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(botUser.getId());
        toMsg.setOptType(0);
        return toMsg;
    }

    public ChatRoomMsg getErrorMsg(BotUser botUser,Player player){
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(botUser.getId());
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsg("格式错误");
        toMsg.setMsgType(0);
        toMsg.setToUserType(0);
        toMsg.setToUserNick(player.getNickname());
        toMsg.setToUserId(player.getId());
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(botUser.getId());
        toMsg.setOptType(0);
        toMsg.setSource(1);
        return toMsg;
    }



    @Async
    public void sendMsg(String toWxId, String wxId, String text){
        String wechatApiUrl = "";
        Dictionary dic = dictionaryService.getDicByCode("system","wxApi");
        if (dic != null){
            wechatApiUrl = dic.getValue();
        }
        String url = wechatApiUrl+"Msg/SendTxt";
        Map<String,Object> reqData = new HashMap<>();
        reqData.put("Content",text);
        reqData.put("ToWxid",toWxId);
        reqData.put("Type",1);
        reqData.put("Wxid",wxId);

        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        System.out.println(DateUtil.now()+">>>>>>Msg/SendTxt>>>>>>"+result);

    }

    @Async
    public void sendMsgGroup(String toWxId, String wxId, String text, String groupName, String wxNick){
        String wechatApiUrl = "";
        Dictionary dic = dictionaryService.getDicByCode("system","wxApi");
        if (dic != null){
            wechatApiUrl = dic.getValue();
        }
        String url = wechatApiUrl+"Msg/SendTxt";
        Map<String,Object> reqData = new HashMap<>();
        reqData.put("At",toWxId);
        reqData.put("Content","@"+wxNick+" "+text);
        reqData.put("ToWxid",groupName);
        reqData.put("Type",1);
        reqData.put("Wxid",wxId);

//        System.out.println(reqData);
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        System.out.println(DateUtil.now()+">>>>>>Msg/SendTxt>>>>>>"+result);

    }

    public void saveAndSendMsgGroup(ChatRoomMsg toMsg,String toWxId,String fromWxId,String groupName,String wxNick){
        dataDao.insert(toMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));
        sendMsgGroup(toWxId, fromWxId, toMsg.getMsg(),groupName,wxNick);
    }

    public void saveAndSendMsg(ChatRoomMsg toMsg,String toWxId,String fromWxId){
        dataDao.insert(toMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));
        sendMsg(toWxId, fromWxId, toMsg.getMsg());
    }


}
