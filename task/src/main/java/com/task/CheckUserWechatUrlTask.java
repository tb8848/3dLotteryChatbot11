package com.task;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beans.BotUser;
import com.beans.ChatDomain;
import com.beans.Player;
import com.service.*;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class CheckUserWechatUrlTask {
    private static final String CHECK_URL = "http://mp.weixinbridge.com/mp/wapredirect?url=%s";
    @Autowired
    private ChatDomainService chatDomainService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private BotUserService botUserService;

    /**
     * 每10s执行-检测微信聊天域名是否有效
     */
    @Scheduled(cron = "0 * * * * ?")
    @Async
    public void task(){
        try {
            List<Player> players = playerService.list();
            if (!players.isEmpty()) {
                for (Player player : players) {
                    if (StringUtil.isNotNull(player.getChaturl()) && checkUrl(player.getChaturl())) {
                        continue;
                    }
                    LambdaQueryWrapper<ChatDomain> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(ChatDomain::getStatus, 1);
                    List<ChatDomain> chatDomains = chatDomainService.list(lambdaQueryWrapper);
                    if (!chatDomains.isEmpty()) {
                        int len = chatDomains.size();
                        int idx = new Random().nextInt(len);
                        ChatDomain randomChatDomain = chatDomains.get(idx);
                        player.setChaturl(randomChatDomain.getUrl());
                        if (playerService.updateById(player)) {
                            // 发送微信消息
                            BotUser botUser = botUserService.getById(player.getBotUserId());
                            String chatUrl = player.getChaturl() + "/?openId=" + player.getOpenid();
                            chatRoomMsgService.sendMsg(player.getWxFriendId(),botUser.getWxAccount(), "聊天地址变更为：" + chatUrl);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean checkUrl(String url){
        String curl = String.format(CHECK_URL,url);
        HttpRequest request = new HttpRequest(UrlBuilder.of(curl));
        request.header("User-Agent","Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        request.method(Method.GET);
        request.setFollowRedirects(true);
        HttpResponse response = request.execute();
        String body = response.body();
        if(body.indexOf("weixin110.qq.com")>-1){
            return false;
        }
        return  true;
    }
}
