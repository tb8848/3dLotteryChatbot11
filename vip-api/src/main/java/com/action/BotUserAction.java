package com.action;

import cn.hutool.core.date.DateUtil;
import com.auth.AuthContext;
import com.beans.*;
import com.service.*;
import com.util.JwtUtil;
import com.util.PasswordUtil;
import com.util.StringUtil;
import com.vo.BotUserCountData;
import com.vo.SummaryDataItem;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/bot/user")
public class BotUserAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerReturnPointsService playerReturnPointsService;

    @Autowired
    private BotUserLogService botUserLogService;

    @Autowired
    private AuthContext authContext;

    @GetMapping(value = "/info")
    public ResponseBean info(@RequestHeader(value = "token")String token,HttpServletRequest request){

        if(StringUtil.isNull(token)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
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

            Long onlineUsers = botUserService.countOnlineUsers();
            if(null == onlineUsers){
                onlineUsers = 0L;
            }


            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("id",botUser.getLoginName());
            resultMap.put("ip",botUser.getLastLoginIp());
            resultMap.put("logTime",DateUtil.format(botUser.getLastLoginTime(),"yyyy-MM-dd HH:mm:ss"));
            resultMap.put("dueTime", DateUtil.format(botUser.getDueDate(),"yyyy-MM-dd"));
            resultMap.put("onlineUsers", onlineUsers);
            resultMap.put("setting", botUserSetting);
            resultMap.put("lotteryType", botUser.getLotteryType());
            return new ResponseBean(0, 0, "", resultMap);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }

    //当日汇总数据
    @GetMapping(value = "/dayData")
    public ResponseBean dayData(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        BotUserCountData botUserCountData = new BotUserCountData();

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }
            Date startTime = DateUtil.beginOfDay(new Date());
            Date endTime = DateUtil.endOfDay(new Date());
            BigDecimal playerTotalPoints = playerService.countTotalPoints(botUser.getId());
            botUserCountData.setPlayerTotalPoints(playerTotalPoints);
            PlayerBuyRecord sumData = playerBuyRecordService.countDayData(uid,startTime,endTime);
            if(null!=sumData){
                if(null!=sumData.getBuyPoints()){
                    botUserCountData.setDayBuyMoney(sumData.getBuyPoints());
                }
                if(null!=sumData.getEarnPoints()){
                    botUserCountData.setDayEarnMoney(sumData.getEarnPoints());
                }
                if(null!=sumData.getHsPoints()){
                    botUserCountData.setPlayerTotalHs(sumData.getHsPoints());
                }
            }

            PlayerPointsRecord sumData1 = playerPointsRecordService.countDayData(uid,startTime,endTime,0);
            if(null!=sumData1 && null!=sumData1.getPoints()){
                botUserCountData.setDayUpPoints(sumData1.getPoints());
            }
            sumData1 = playerPointsRecordService.countDayData(uid,startTime,endTime,1);
            if(null!=sumData1 && null!=sumData1.getPoints()){
                botUserCountData.setDayDownPoints(sumData1.getPoints());
            }

            BigDecimal playerHs = BigDecimal.ZERO;
            PlayerReturnPoints playerReturnPoints = playerReturnPointsService.countDayData(uid,startTime,endTime);
            if (playerReturnPoints != null){
                playerHs = playerReturnPoints.getReturnPoints()==null ? BigDecimal.ZERO : playerReturnPoints.getReturnPoints();
            }

            List<SummaryDataItem> itemList = Lists.newArrayList();
            itemList.add(new SummaryDataItem("玩家总分",botUserCountData.getPlayerTotalPoints()));
            itemList.add(new SummaryDataItem("当日总投",botUserCountData.getDayBuyMoney()));
            itemList.add(new SummaryDataItem("当日盈亏",botUserCountData.getDayEarnMoney()));
            itemList.add(new SummaryDataItem("当日上分",botUserCountData.getDayUpPoints()));
            itemList.add(new SummaryDataItem("当日下分",botUserCountData.getDayDownPoints()));
            itemList.add(new SummaryDataItem("玩家回水",playerHs));
            itemList.add(new SummaryDataItem("网盘余额",botUserCountData.getNetBalance()));
            itemList.add(new SummaryDataItem("当期报网",botUserCountData.getNetBuyMoney()));
            itemList.add(new SummaryDataItem("网盘回水",botUserCountData.getNetHsMoney()));

            //当日玩家总分，当日总投，当日盈亏，当日上分，当日下分，玩家回水，
            //网盘余额，当期报网，网盘回水
            return new ResponseBean(0, 0, "", itemList);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 配置信息修改
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/editSetting")
    public ResponseBean editSetting(@RequestHeader(value = "token")String token,@RequestBody BotUserSetting settingParam,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
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

            boolean isUpdate = false;
            boolean hsUpdate = false;

            //操作内容
            String contents = "";

            //统一回水值
            BigDecimal hsvalue = settingParam.getHsvalue();
            if(null!=hsvalue && hsvalue.compareTo(botUserSetting.getHsvalue())!=0){
                String content = StringUtil.isNull(contents) ? "统一回水值：" : ",统一回水值：";
                contents = contents + content +botUserSetting.getHsvalue()+"改为"+hsvalue;
                botUserSetting.setHsvalue(hsvalue);
                isUpdate= true;
                hsUpdate = true;
            }
            //是否停盘后自动返水
            Integer hsAutoBack = settingParam.getHsAutoBack();
            if(null!=hsAutoBack && hsAutoBack.intValue()!=botUserSetting.getHsAutoBack()){
                if (hsAutoBack == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认停盘后自动返水" : ",确认停盘后自动返水";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消停盘后自动返水" : ",取消停盘后自动返水";
                    contents = contents + cancelContent;
                }
                botUserSetting.setHsAutoBack(hsAutoBack);
                isUpdate= true;
            }
            //是否玩家自助返水
            Integer hsHelpBack = settingParam.getHsHelpBack();
            if(null!=hsHelpBack && hsHelpBack.intValue()!=botUserSetting.getHsHelpBack()){
                if (hsHelpBack == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认玩家自助返水" : ",确认玩家自助返水";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消玩家自助返水" : ",取消玩家自助返水";
                    contents = contents + cancelContent;
                }
                botUserSetting.setHsHelpBack(hsHelpBack);
                isUpdate= true;
            }
            //是否私聊下注
            Integer wxChatBuy = settingParam.getWxChatBuy();
            if(null!=wxChatBuy && wxChatBuy.intValue()!=botUserSetting.getWxChatBuy()){
                if (wxChatBuy == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认私聊下注" : ",确认私聊下注";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消私聊下注" : ",取消私聊下注";
                    contents = contents + cancelContent;
                }
                botUserSetting.setWxChatBuy(wxChatBuy);
                isUpdate= true;
            }
            //是否私聊发送图片
            Integer wxChatSend = settingParam.getWxChatSend();
            if(null!=wxChatSend && wxChatSend.intValue()!=botUserSetting.getWxChatSend()){
                if (wxChatSend == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认私聊发送图片" : ",确认私聊发送图片";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消私聊发送图片" : ",取消私聊发送图片";
                    contents = contents + cancelContent;
                }
                botUserSetting.setWxChatSend(wxChatSend);
                isUpdate= true;
            }
            //是否开启微信群发图片
            Integer wxGroupSend = settingParam.getWxGroupSend();
            if(null!=wxGroupSend && wxGroupSend.intValue()!=botUserSetting.getWxGroupSend()){
                if (wxGroupSend == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "开启微信群发图片" : ",开启微信群发图片";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消微信群发图片" : ",取消微信群发图片";
                    contents = contents + cancelContent;
                }
                botUserSetting.setWxGroupSend(wxGroupSend);
                isUpdate= true;
            }
            //是否开放房间
            Integer roomOpen = settingParam.getRoomOpen();
            if(null!=roomOpen && roomOpen.intValue()!=botUserSetting.getRoomOpen()){
                if (roomOpen == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认开放房间" : ",确认开放房间";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消开放房间" : ",取消开放房间";
                    contents = contents + cancelContent;
                }
                botUserSetting.setRoomOpen(roomOpen);
                isUpdate= true;
            }
            //是否开启快选
            Integer kxEnable = settingParam.getKuaixuanEnable();
            if(null!=kxEnable && kxEnable.intValue()!=botUserSetting.getKuaixuanEnable()){
                if (kxEnable == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认开启快选" : ",确认开启快选";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消开启快选" : ",取消开启快选";
                    contents = contents + cancelContent;
                }
                botUserSetting.setKuaixuanEnable(kxEnable);
                isUpdate= true;
            }
            //是否开启退码
            Integer tuimaEnable = settingParam.getTuimaEnable();
            if(null!=tuimaEnable && tuimaEnable.intValue()!=botUserSetting.getTuimaEnable()){
                if (tuimaEnable == 1){
                    String confirmContent = StringUtil.isNull(contents) ? "确认开启退码" : ",确认开启退码";
                    contents = contents + confirmContent;
                }else{
                    String cancelContent = StringUtil.isNull(contents) ? "取消开启退码" : ",取消开启退码";
                    contents = contents + cancelContent;
                }
                botUserSetting.setTuimaEnable(tuimaEnable);
                isUpdate= true;
            }

            //写操作日志
            if (StringUtil.isNotNull(contents)){
                BotUserLog botUserLog = new BotUserLog();
                botUserLog.setContents(contents);
                botUserLog.setOptTime(new Date());
                botUserLog.setUserId(uid);
                botUserLogService.save(botUserLog);
            }

            if(isUpdate){
                botUserSettingService.updateById(botUserSetting);
                if(hsUpdate){
                    //更改玩家的回水比例，过滤掉单独设置了回水的玩家
                    playerService.updateHsvalue(hsvalue,botUser.getId());
                }
            }
            return new ResponseBean(0, 0, "", null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 修改密码
     * @param token
     * @param request
     * @param paramMap
     *      key: oldpwd,
     *      key: newpwd
     * @return
     */
    @PostMapping(value = "/editPwd")
    public ResponseBean editPwd(@RequestHeader(value = "token")String token,@RequestBody Map<String,Object> paramMap,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }

        String oldpwd = (String)paramMap.get("oldpwd");
        String newpwd = (String)paramMap.get("newpwd");

        if(StringUtil.isNull(oldpwd) || StringUtil.isNull(newpwd)){
            return new ResponseBean(403,0,"必填参数为空",null,true);
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);
            }

            String md5Old = PasswordUtil.jiami(oldpwd);
            String md5New = PasswordUtil.jiami(newpwd);

            if(!md5Old.equals(botUser.getLoginPwd())){
                return new ResponseBean(-1,0,"原密码不正确",null,true);
            }

            botUserService.updatePwd(uid,md5New);

            return new ResponseBean(0, 0, "", null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 修改安全密码
     * @param token
     * @param request
     * @param paramMap
     *      key: oldpwd,
     *      key: newpwd
     * @return
     */
    @PostMapping(value = "/editSafePwd")
    public ResponseBean editSafePwd(@RequestHeader(value = "token")String token,@RequestBody Map<String,Object> paramMap,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }

        String oldpwd = (String)paramMap.get("spwd");
        String newpwd = (String)paramMap.get("snewpwd");

        if(StringUtil.isNull(oldpwd) || StringUtil.isNull(newpwd)){
            return new ResponseBean(-1,0,"密码不能为空",null,true);
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);
            }

            String md5Old = PasswordUtil.jiami(oldpwd);
            String md5New = PasswordUtil.jiami(newpwd);

            if(!md5Old.equals(botUser.getSafePwd())){
                return new ResponseBean(-1,0,"原密码不正确",null,true);
            }

            botUserService.updateSafePwd(uid,md5New);

            return new ResponseBean(0, 0, "", null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 验证安全密码
     * @param token
     * @param safePassword
     * @return
     */
    @RequestMapping("/verifySafePassword")
    public ResponseBean verifySafePassword(@RequestHeader(value = "token")String token,String safePassword){

        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (StringUtil.isNull(safePassword)){
            return new ResponseBean(-1,0,"安全密码不能为空",null,true);
        }
        String safePwd = PasswordUtil.jiami(safePassword);
        if (!safePwd.equals(botUser.getSafePwd())){
            return new ResponseBean(-1,0,"安全密码错误",null,true);
        }
        return new ResponseBean(0, 0, "", null);
    }

    /**
     * 判断是否设置安全密码
     * @param token
     * @return
     */
    @PostMapping("/isSetSafePassword")
    public ResponseBean isSetSafePassword(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (StringUtil.isNotNull(botUser.getSafePwd())){
            return new ResponseBean(0,0,"有安全密码","",true);
        }
        return new ResponseBean(-1,0,"未设置安全密码","",true);
    }

    /**
     * 设置安全密码
     * @param token
     * @param safePassword
     * @return
     */
    @GetMapping("/setSafePassword")
    public ResponseBean setSafePassword(@RequestHeader(value = "token")String token,String safePassword){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (StringUtil.isNull(safePassword)){
            return new ResponseBean(-1,0,"请输入安全密码！",null,true);
        }
        String safePwd = PasswordUtil.jiami(safePassword);
        botUser.setSafePwd(safePwd);
        if (botUserService.updateById(botUser)){
            return new ResponseBean(0, 0, "", null);
        }
        return new ResponseBean(-1,0,"设置失败！",null,true);
    }

    /**
     * 校验机器人是否过期
     * @param token
     * @return
     */
    @PostMapping("/verifyBotUserExpire")
    public ResponseBean verifyBotUserExpire(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);
        }
        if (new Date().after(botUser.getDueDate())){//当前时间在过期时间之后
            return new ResponseBean(0,0,"过期了",null,true);
        }
//        if (new Date().compareTo(botUser.getDueDate()) > 0){
//            return new ResponseBean(0,0,"过期了",null,true);
//        }
        return new ResponseBean(-1,0,"比较失败",null,true);
    }
}
