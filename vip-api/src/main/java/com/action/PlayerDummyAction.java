package com.action;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.*;
import com.service.*;
import com.util.JwtUtil;
import com.util.StringUtil;
import com.utils.RandomNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

/**
 * 假人玩家
 */
@RestController
@RequestMapping(value = "/bot/player/dummy")
public class PlayerDummyAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Value("${upload.dir}")
    private String uploadDir;

    /**
     * 添加玩家(假人)
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/add")
    public ResponseBean add(@RequestHeader(value = "token")String token, Integer amount,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(null == amount){
            amount = 15;
        }
        if(amount>15){
            amount = 15;
        }
        //只能添加15个假人
        long count = playerService.count(new QueryWrapper<Player>().eq("botUserId",uid).eq("userType",0).eq("status",1));
        if (count > 0){
            int num = 15 - Integer.parseInt(count+"");//可添加数量
            if (amount > num){
                amount = num;
            }
        }

        try{

            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            BotUserSetting botUserSetting = botUserSettingService.getByUserId(uid);
            if(null == botUserSetting) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            for(int i=0;i<amount;i++){
                Player player = new Player();
                player.setUserType(0);
                player.setOpenid(UUID.randomUUID().toString().replace("-","").toUpperCase());
                player.setStatus(1);
                String nickname = RandomNameUtil.generateNickname(2);
                player.setNickname(nickname);
//                String randomImg = chooseImage();
//                player.setHeadimg("http://192.168.100.104:9192/bot/downHeadimg?imgName="+randomImg);
                player.setHeadimg((String) botUserService.chooseImg().get("url"));
                player.setChaturl("");
                player.setHsType(0);
                player.setBotUserId(uid);
                player.setPretexting(1);
                player.setEatPrize(1);
                player.setReportNet(0);
                player.setLotteryType(botUser.getLotteryType());
                playerService.save(player);
            }
            return new ResponseBean(0, 0, "", null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    private String chooseImage(){
        String imgDir = uploadDir+"headimg";
        File dir = new File(imgDir);
        String[] fileList = dir.list();
        int len = fileList.length;

        int idx = new Random().nextInt(len);

        String imgName = fileList[idx];
        return imgName;
    }

}
