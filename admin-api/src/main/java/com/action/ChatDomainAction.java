package com.action;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.beans.BotUser;
import com.beans.ChatDomain;
import com.beans.Player;
import com.beans.ResponseBean;
import com.service.BotUserService;
import com.service.ChatDomainService;
import com.service.PlayerService;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/admin/chatDomain")
public class ChatDomainAction {

    @Autowired
    private ChatDomainService chatDomainService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserService botUserService;

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    /**
     * 域名列表
     * @return
     */
    @RequestMapping(value = "getChatDomainList")
    public ResponseBean getChatDomainList (String status) {
        List<ChatDomain> proxyList = chatDomainService.getChatDomainList(status);
        return new ResponseBean(0, "", proxyList);
    }

    /**
     * 添加域名
     * @return
     */
    @PostMapping(value = "addDomain")
    public ResponseBean addDomain (@RequestBody ChatDomain chatDomain) {
        if (StringUtil.isNull(chatDomain.getUrl())) {
            return ResponseBean.error("common.lackRequiredParam");
        }
        chatDomain.setStatus(1);
        chatDomain.setCreateTime(new Date());
        if (chatDomainService.save(chatDomain)) {
            return ResponseBean.ok("common.operationSuccess");
        }
        return ResponseBean.error("common.operationFail");
    }

    /**
     * 编辑域名
     * @return
     */
    @PostMapping(value = "updateDomain")
    public ResponseBean updateDomain (@RequestBody ChatDomain chatDomain) {
        if (StringUtil.isNull(chatDomain.getUrl())) {
            return ResponseBean.error("common.lackRequiredParam");
        }
        ChatDomain cd = chatDomainService.getById(chatDomain.getId());
        if (chatDomainService.updateById(chatDomain)) {
            // 变更域名
            if (!cd.getUrl().equals(chatDomain.getUrl()) && chatDomain.getStatus() != 0) {
                // 更新玩家聊天域名
                LambdaQueryWrapper<Player> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Player::getChaturl, cd.getUrl());
                List<Player> playerList = playerService.list(queryWrapper);
                if (!playerList.isEmpty()) {
                    for (Player player : playerList) {
                        player.setChaturl(chatDomain.getUrl());
                        if(playerService.updateById(player)) {
                            // 发送微信消息
                            BotUser botUser = botUserService.getById(player.getBotUserId());
                            String chatUrl = player.getChaturl()+"/?openId="+player.getOpenid();
                            sendMsg(player.getWxFriendId(), botUser.getWxId(), "聊天地址变更为：" + chatUrl);
                        }
                    }
                }
            }

            // 启用变禁用
            if (cd.getStatus() == 1 && chatDomain.getStatus() == 0) {
                LambdaQueryWrapper<ChatDomain> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ChatDomain::getStatus, 1);
                List<ChatDomain> chatDomainList = chatDomainService.list(queryWrapper);
                if (!chatDomainList.isEmpty()) {
                    LambdaQueryWrapper<Player> playerQw = new LambdaQueryWrapper<>();
                    playerQw.eq(Player::getChaturl, cd.getUrl());
                    List<Player> playerList = playerService.list(playerQw);
                    if (!playerList.isEmpty()) {
                        for (Player player : playerList) {
                            int len = chatDomainList.size();
                            int idx = new Random().nextInt(len);
                            ChatDomain randomChatDomain = chatDomainList.get(idx);
                            player.setChaturl(randomChatDomain.getUrl());
                            if(playerService.updateById(player)) {
                                // 发送微信消息
                                BotUser botUser = botUserService.getById(player.getBotUserId());
                                String chatUrl = player.getChaturl()+"/?openId="+player.getOpenid();
                                sendMsg(player.getWxFriendId(), botUser.getWxId(), "聊天地址变更为：" + chatUrl);
                            }
                        }
                    }


                }
            }
            return ResponseBean.ok("common.operationSuccess");
        }
        return ResponseBean.error("common.operationFail");
    }

    /**
     * 删除域名
     * @return
     */
    @RequestMapping(value = "deleteDomain")
    public ResponseBean deleteDomain (String id) {
        if (StringUtil.isNull(id)) {
            return ResponseBean.error("common.paramWrong");
        }
        ChatDomain cd = chatDomainService.getById(id);
        if (chatDomainService.removeById(id)) {
            if (null != cd) {
                LambdaQueryWrapper<ChatDomain> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ChatDomain::getStatus, 1);
                List<ChatDomain> chatDomainList = chatDomainService.list(queryWrapper);
                if (!chatDomainList.isEmpty()) {
                    LambdaQueryWrapper<Player> playerQw = new LambdaQueryWrapper<>();
                    playerQw.eq(Player::getChaturl, cd.getUrl());
                    List<Player> playerList = playerService.list(playerQw);
                    if (!playerList.isEmpty()) {
                        for (Player player : playerList) {
                            int len = chatDomainList.size();
                            int idx = new Random().nextInt(len);
                            ChatDomain randomChatDomain = chatDomainList.get(idx);
                            player.setChaturl(randomChatDomain.getUrl());
                            if (playerService.updateById(player)) {
                                // 发送微信消息
                                BotUser botUser = botUserService.getById(player.getBotUserId());
                                String chatUrl = player.getChaturl() + "/?openId=" + player.getOpenid();
                                sendMsg(player.getWxFriendId(), botUser.getWxId(), "聊天地址变更为：" + chatUrl);
                            }
                        }
                    }
                }
            }
            return ResponseBean.ok("common.deleteSuccess");
        }
        return ResponseBean.error("common.deleteFail");
    }

    /**
     * 根据id获取域名
     * @return
     */
    @RequestMapping(value = "getDomainById")
    public ResponseBean getDomainById (String id) {
        ChatDomain chatDomain = chatDomainService.getById(id);
        return new ResponseBean(0, "", chatDomain);
    }

    /**
     * 发送消息
     * @param toWxId
     * @param wxId
     * @param text
     */
    private void sendMsg(String toWxId, String wxId, String text){
        String url = wechatApiUrl + "Msg/SendTxt";
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
}
