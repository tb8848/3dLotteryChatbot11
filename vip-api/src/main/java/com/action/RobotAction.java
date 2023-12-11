package com.action;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.BotUser;
import com.beans.BotUserPan;
import com.beans.ResponseBean;
import com.google.common.collect.Maps;
import com.service.BotUserPanService;
import com.service.BotUserService;
import com.service.PlayerBuyRecordService;
import com.util.JwtUtil;
import com.util.PasswordUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/9 11:04
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/bot/robot")
public class RobotAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private BotUserPanService botUserPanService;

    /**
     * 盘口登录
     * @param botUser
     * @return
     */
    @RequestMapping("/robotLogin")
    public ResponseBean robotLogin(@RequestBody BotUser botUser, @RequestHeader(value = "token")String token){

        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser user = botUserService.getById(uid);
        if(null == user) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }

        long x = botUserService.count(new QueryWrapper<BotUser>().eq("lottery3dAccount",botUser.getLottery3dAccount()).eq("id",uid));
        long y = botUserService.count(new QueryWrapper<BotUser>().eq("lottery3dAccount",botUser.getLottery3dAccount()));
        if (x==0 && y>0){
            return new ResponseBean(-1,0,"该账号已在其他地方登录",null,true);
        }

//        System.out.println("用户名："+botUser.getLottery3dAccount()+" 密码："+botUser.getLottery3dPwd()+" 地址："+botUser.getLottery3dUrl());

        if (StringUtil.isNull(botUser.getLottery3dAccount()) || StringUtil.isNull(botUser.getLottery3dPwd())){
            return new ResponseBean(-1,0,"用户名或密码不能为空",null,true);
        }
        if (StringUtil.isNull(botUser.getLottery3dUrl())){
            return new ResponseBean(-1,0,"网址不能为空",null,true);
        }
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("uname", botUser.getLottery3dAccount());
        paramMap.put("pwd", botUser.getLottery3dPwd());

        String data = HttpUtil.post(botUser.getLottery3dUrl() + "robot/handicapLogin", paramMap);
        Map dataMaps = (Map) JSON.parse(data);
        //登录成功，写数据库
        if (Integer.parseInt(dataMaps.get("code").toString()) == 0){
            user.setLottery3dAccount(botUser.getLottery3dAccount());
            user.setLottery3dUrl(botUser.getLottery3dUrl());

            String pwd = botUser.getLottery3dPwd();
            if (StringUtil.isNotNull(pwd)){
                user.setLottery3dPwd(PasswordUtil.jiami(pwd));
            }

            Map datas = (Map) JSON.parse(dataMaps.get("data").toString());
            user.setLogin3dToken(datas.get("token").toString());
            botUserService.updateById(user);
        }
        return new ResponseBean(Integer.parseInt(dataMaps.get("code").toString()),Integer.parseInt(dataMaps.get("count").toString()),dataMaps.get("msg").toString(),dataMaps.get("data"),true);
    }

    /**
     * 获取最新一期信息
     * @param token
     * @return
     */
    @RequestMapping("/getDrawInfo")
    public ResponseBean getDrawInfo(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (StringUtil.isNull(botUser.getLogin3dToken())){
            return new ResponseBean(-1,0,"机器人未登录网盘",null,true);
        }
        HttpResponse execute = HttpRequest.post(botUser.getLottery3dUrl()+"robot/getDrawInfo")
                .header("token",botUser.getLogin3dToken())
                .execute();
        String body = execute.body();
        Map dataMaps = (Map) JSON.parse(body);
        String data = dataMaps.get("data").toString();
        Map map = (Map) JSON.parse(data);
        String drawId = map.get("drawId").toString();
        if (StringUtil.isNotNull(drawId)){
            BigDecimal baowangMoney = playerBuyRecordService.getBaowangCountByDrawId(drawId,uid);
            map.put("baowangMoney",baowangMoney==null ? BigDecimal.ZERO : baowangMoney);
        }else{
            map.put("baowangMoney",0);
        }
        return new ResponseBean(Integer.parseInt(dataMaps.get("code").toString()),dataMaps.get("msg").toString(),map,true);
    }

    /**
     * 判断盘口是否登录
     * @param token
     * @return
     */
    @PostMapping("/isRobotLogin")
    public ResponseBean isRobotLogin(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (StringUtil.isNull(botUser.getLogin3dToken())){
            return new ResponseBean(-1,0,"机器人未登录网盘",null,true);
        }
        return new ResponseBean(0,0,"盘口已登录",botUser,true);
    }

    /**
     * 盘口退出登录
     * @param token
     * @return
     */
    @PostMapping("/logout")
    public ResponseBean logout(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        botUser.setLottery3dUrl("");
        botUser.setLottery3dPwd("");
        botUser.setLottery3dAccount("");
        botUser.setLogin3dToken("");
        if (botUserService.updateById(botUser)){
            return new ResponseBean(0,0,"退出成功",null,true);
        }
        return new ResponseBean(-1,0,"退出失败",null,true);
    }



    @PostMapping("/checkRobotLogin")
    public ResponseBean checkRobotLogin(@RequestHeader(value = "token")String token,Integer lotteryType){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        if(null == lotteryType || (lotteryType!=1 && lotteryType!=2)){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        BotUserPan botUserPan = botUserPanService.getOneBy(lotteryType,botUser.getId());
        if (null==botUserPan || StringUtil.isNull(botUserPan.getLogin3dToken())){
            return new ResponseBean(-1,0,"机器人未登录网盘",null,true);
        }
        return new ResponseBean(0,0,"盘口已登录",botUserPan.getId(),true);
    }


}
