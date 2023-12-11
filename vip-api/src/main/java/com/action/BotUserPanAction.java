package com.action;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.BotUser;
import com.beans.BotUserPan;
import com.beans.ResponseBean;
import com.google.common.collect.Maps;
import com.service.BotUserPanService;
import com.service.BotUserService;
import com.service.PlayerBuyRecordService;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/25 16:41
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/bot/pan")
public class BotUserPanAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserPanService botUserPanService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    /**
     * 登录盘口
     * @param token
     * @param id
     * @return
     */
    @PostMapping("/loginUserPan")
    public ResponseBean loginUserPan(@RequestHeader(value = "token")String token,@RequestBody String id){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser user = botUserService.getById(uid);
        BotUserPan botUserPan = botUserPanService.getById(id);
        if(null == user || null == botUserPan) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }

        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("uname", botUserPan.getLottery3dAccount());
        paramMap.put("pwd", botUserPan.getLottery3dPwd());

        String url = botUserPan.getLottery3dUrl();
        if(url.indexOf("aa.3d11aa.com")>-1 || url.indexOf("aa.pai3aa11.com")>-1) {
            if ("/".equals(url.substring(url.length() - 1))) {
                url = url.substring(0, url.length() - 1);
            }
            if ("/".equals(url.substring(url.length() - 1))) {
                url = url.substring(0, url.length() - 1);
            }
            if (botUserPan.getLotteryType() == 1) {
                url = url + ":9092/";
            } else if (botUserPan.getLotteryType() == 2) {
                url = url + ":9292/";
            }
        }

        String data = HttpUtil.post(url + "robot/handicapLogin", paramMap);
        Map dataMaps = (Map) JSON.parse(data);
        //登录成功，写数据库
        if (Integer.parseInt(dataMaps.get("code").toString()) == 0){
            Map datas = (Map) JSON.parse(dataMaps.get("data").toString());
            botUserPan.setLogin3dToken(datas.get("token").toString());
            botUserPan.setLotteryType(Integer.parseInt(datas.get("lotteryType").toString()));
            botUserPan.setActiveStatus(1);
            botUserPanService.updateById(botUserPan);
        }
        return new ResponseBean(Integer.parseInt(dataMaps.get("code").toString()),Integer.parseInt(dataMaps.get("count").toString()),dataMaps.get("msg").toString(),dataMaps.get("data"),true);

    }

    /**
     * 删除盘口
     * @param token
     * @param id
     * @return
     */
    @RequestMapping("/delUserPan")
    public ResponseBean delUserPan(@RequestHeader(value = "token")String token,String id){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser user = botUserService.getById(uid);
        if(null == user) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (botUserPanService.removeById(id)){
            return new ResponseBean(0,0,"删除成功","",true);
        }
        return new ResponseBean(-1,0,"删除失败","",true);
    }

    /**
     * 添加盘口
     * @param botUserPan
     * @return
     */
    @PostMapping("/addUserPan")
    public ResponseBean addUserPan(@RequestBody BotUserPan botUserPan, @RequestHeader(value = "token")String token){

        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser user = botUserService.getById(uid);
        if(null == user) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (StringUtil.isNull(botUserPan.getLottery3dAccount()) || StringUtil.isNull(botUserPan.getLottery3dPwd())){
            return new ResponseBean(-1,0,"用户名或密码不能为空",null,true);
        }
        if (StringUtil.isNull(botUserPan.getLottery3dUrl())){
            return new ResponseBean(-1,0,"网址不能为空",null,true);
        }

        long x = botUserPanService.count(new QueryWrapper<BotUserPan>().eq("lottery3dAccount",botUserPan.getLottery3dAccount()).eq("botUserId",uid).eq("lottery3dUrl",botUserPan.getLottery3dUrl()));
        if (x > 0){
            return new ResponseBean(-1,0,"盘口已添加",null,true);
        }

        long c = botUserPanService.count(new LambdaQueryWrapper<BotUserPan>().eq(BotUserPan::getLotteryType, botUserPan.getLotteryType()).eq(BotUserPan::getBotUserId,uid));
        if (c > 0){
            String msg = "";
            if (botUserPan.getLotteryType()==1){
                msg = "已添加福彩3D盘口";
            }else if (botUserPan.getLotteryType()==2){
                msg = "已添加排列三盘口";
            }
            return new ResponseBean(-1,0,"一个彩种最多只能添加一个盘口,"+msg,null,true);
        }

//        String pwd = PasswordUtil.jiami(botUserPan.getLottery3dPwd());
//        botUserPan.setLottery3dPwd(pwd);
        botUserPan.setBotUserId(uid);botUserPan.setActiveStatus(0);

        /*String result = HttpUtil.get("http://127.0.0.1:8888/checkDomain?url=" + botUserPan.getLottery3dUrl());
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject.getInteger("code") == 200) {
            botUserPan.setLotteryType(jsonObject.getInteger("data"));
        }else {
            return new ResponseBean(-1,0,"添加失败","",true);
        }*/

        if (botUserPanService.save(botUserPan)){
            return new ResponseBean(0,0,"添加成功","",true);
        }
        return new ResponseBean(-1,0,"添加失败","",true);
    }

    /**
     * 修改盘口
     * @param botUserPan
     * @param token
     * @return
     */
    @PostMapping("/updUserPan")
    public ResponseBean updUserPan(@RequestBody BotUserPan botUserPan, @RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser user = botUserService.getById(uid);
        BotUserPan userPan = botUserPanService.getById(botUserPan.getId());
        if(null == user || null == userPan) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        long x = botUserPanService.count(new QueryWrapper<BotUserPan>().eq("lottery3dAccount",botUserPan.getLottery3dAccount()).eq("botUserId",uid).eq("lottery3dUrl",botUserPan.getLottery3dUrl()).eq("id",botUserPan.getId()));
        long y = botUserPanService.count(new QueryWrapper<BotUserPan>().eq("lottery3dAccount",botUserPan.getLottery3dAccount()).eq("botUserId",uid).eq("lottery3dUrl",botUserPan.getLottery3dUrl()));
        if (x==0 && y>0){
            return new ResponseBean(-1,0,"盘口已存在",null,true);
        }
        long c = botUserPanService.count(new LambdaQueryWrapper<BotUserPan>().eq(BotUserPan::getLotteryType, botUserPan.getLotteryType()).eq(BotUserPan::getBotUserId,uid));
        long d = botUserPanService.count(new LambdaQueryWrapper<BotUserPan>().eq(BotUserPan::getLotteryType, botUserPan.getLotteryType()).eq(BotUserPan::getBotUserId,uid).eq(BotUserPan::getId,botUserPan.getId()));
        if (c > 0 && d == 0){
            String msg = "";
            if (botUserPan.getLotteryType()==1){
                msg = "福彩3D盘口已存在";
            }else if (botUserPan.getLotteryType()==2){
                msg = "排列三盘口已存在";
            }
            return new ResponseBean(-1,0,"一个彩种最多只能添加一个盘口,"+msg,null,true);//一个彩种只能添加一个盘口
        }
        userPan.setLotteryType(botUserPan.getLotteryType());
        userPan.setLottery3dUrl(botUserPan.getLottery3dUrl());
        userPan.setLottery3dAccount(botUserPan.getLottery3dAccount());
        userPan.setLottery3dPwd(botUserPan.getLottery3dPwd());
        if (botUserPanService.updateById(userPan)){
            return new ResponseBean(0,0,"修改成功","",true);
        }
        return new ResponseBean(-1,0,"修改失败","",true);
    }

    /**
     * 查询盘口列表
     * @param token
     * @return
     */
    @RequestMapping("/selectUserPan")
    public ResponseBean selectUserPan(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        List<BotUserPan> list = botUserPanService.list(new QueryWrapper<BotUserPan>().eq("botUserId",uid));
        return new ResponseBean(200,"查询成功",list);
    }

    /**
     * 判断盘口是否登录
     * @param token
     * @return
     */
    @PostMapping("/isUserPanLogin")
    public ResponseBean isUserPanLogin(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser user = botUserService.getById(uid);
        if(null == user) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        List<BotUserPan> userPans = botUserPanService.list(new QueryWrapper<BotUserPan>().eq("botUserId",uid).eq("activeStatus",1));
        if (userPans.size() > 0){
            return new ResponseBean(0,0,"已登录",userPans.get(0),true);
        }
        return new ResponseBean(-1,0,"未登录","",true);
    }

    /**
     * 获取最新一期信息
     * @param token
     * @param id    登录盘口ID
     * @return
     */
    @RequestMapping("/getDrawInfo")
    public ResponseBean getDrawInfo(@RequestHeader(value = "token")String token,String id){
        try {
            String uid = JwtUtil.getUsername(token);
            if (StringUtil.isNull(uid)) {
                return new ResponseBean(403, 0, "请重新登录", null, true);
            }
            BotUser botUser = botUserService.getById(uid);
            BotUserPan botUserPan = botUserPanService.getById(id);
            if (null == botUser || null == botUserPan) {
                return new ResponseBean(-1, 0, "数据错误", null, true);
            }

            if (StringUtil.isNull(botUserPan.getLogin3dToken())) {
                return new ResponseBean(-1, 0, "机器人未登录网盘", null, true);
            }
            int lotteryType = botUserPan.getLotteryType();
            String url = botUserPan.getLottery3dUrl();
            if(url.indexOf("aa.3d11aa.com")>-1 || url.indexOf("aa.pai3aa11.com")>-1){
                if ("/".equals(url.substring(url.length() - 1))) {
                    url = url.substring(0, url.length() - 1);
                }
                if ("/".equals(url.substring(url.length() - 1))) {
                    url = url.substring(0, url.length() - 1);
                }
                if (lotteryType == 1) {
                    url = url + ":9092/";
                } else if (lotteryType == 2) {
                    url = url + ":9292/";
                }
            }
            HttpResponse execute = HttpRequest.post(url + "robot/getDrawInfo")
                    .header("token", botUserPan.getLogin3dToken())
                    .execute();
            String body = execute.body();
            Map dataMaps = (Map) JSON.parse(body);
            String data = dataMaps.get("data").toString();
            Map map = (Map) JSON.parse(data);
            String drawId = map.get("drawId").toString();
            if (StringUtil.isNotNull(drawId)) {
                BigDecimal baowangMoney = playerBuyRecordService.getBaowangCountByDrawId(drawId, uid, lotteryType);
                map.put("baowangMoney", baowangMoney == null ? BigDecimal.ZERO : baowangMoney);
            } else {
                map.put("baowangMoney", 0);
            }
            return new ResponseBean(Integer.parseInt(dataMaps.get("code").toString()), dataMaps.get("msg").toString(), map, true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "接口异常", null, true);
        }
    }

    /**
     * 退出盘口登录
     * @param token
     * @param id
     * @return
     */
    @PostMapping("/exitUserPan")
    public ResponseBean exitUserPan(@RequestHeader(value = "token")String token,@RequestBody String id){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        BotUserPan botUserPan = botUserPanService.getById(id);
        if(null == botUser || null == botUserPan) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        botUserPan.setLogin3dToken("");
        botUserPan.setActiveStatus(0);
        if (botUserPanService.updateById(botUserPan)){
            return new ResponseBean(0,0,"退出成功",null,true);
        }
        return new ResponseBean(-1,0,"退出失败",null,true);
    }
}
