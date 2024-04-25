package com.action;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.*;
import com.config.NoLogin;
import com.model.res.ReportRes;
import com.service.*;
import com.util.JwtUtil;
import com.util.StringUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.util.StringUtil.convertToBase64;

@RestController
@RequestMapping(value = "/bot/player")
public class PlayerAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private ChatDomainService chatDomainService;

    @Autowired
    private MinioClient minioClient;

    /**
     * 添加玩家
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/add")
    public ResponseBean add(@RequestHeader(value = "token")String token, @RequestBody Player player,HttpServletRequest request){
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

            ChatDomain chatDomain = chatDomainService.getOne();
            if (null != chatDomain){
                player.setChaturl(chatDomain.getUrl());
            }

            player.setOpenid(UUID.randomUUID().toString().replace("-","").toUpperCase());
            player.setBotUserId(botUser.getId());
            player.setUserType(1);
            player.setHsvalue(botUserSetting.getHsvalue());
            player.setHsType(0);
            player.setLotteryType(botUser.getLotteryType());

            if(playerService.save(player)){
                return new ResponseBean(0, 0, "", null);
            }else{
                return new ResponseBean(403,0,"创建失败",null,true);//用户名或密码为空
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }

    /**
     * 拉黑玩家
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/black/add")
    public ResponseBean addToBlackList(@RequestHeader(value = "token")String token, @RequestBody Player playerParam,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        String playerId = playerParam.getId();

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId)){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }
            if(!player.getBotUserId().equals(uid)){
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }

            player.setStatus(2);

            if(playerService.updateById(player)){
                return new ResponseBean(0, 0, "", null);
            }else{
                return new ResponseBean(-1,0,"操作失败",null,true);//用户名或密码为空
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 移除黑名单
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/black/del")
    public ResponseBean removeFromBlackList(@RequestHeader(value = "token")String token, @RequestBody Player playerParam,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        String playerId = playerParam.getId();

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId)){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        try{
            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }
            if(!player.getBotUserId().equals(uid)){
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }

            player.setStatus(1);

            if(playerService.updateById(player)){
                return new ResponseBean(0, 0, "", null);
            }else{
                return new ResponseBean(-1,0,"操作失败",null,true);//用户名或密码为空
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 黑名单列表
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/black/list")
    public ResponseBean getBlackList(@RequestHeader(value = "token")String token, Integer pageNo,Integer pageSize,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(null == pageNo){
            pageNo = 1;
        }
        if(null == pageSize){
            pageSize = 10;
        }

        try{
            IPage<Player> pageData = playerService.getBlackListByPager(uid,pageNo,pageSize);
            return new ResponseBean(0,pageData.getTotal(),"",pageData.getRecords());//用户名或密码为空
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 删除玩家
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/del")
    public ResponseBean del(@RequestHeader(value = "token")String token, @RequestBody Player playerParam,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        String playerId = playerParam.getId();

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId)){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        try{

            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }
            if(!player.getBotUserId().equals(uid)){
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }

            player.setStatus(-1);

            if(playerService.updateById(player)){
                return new ResponseBean(0, 0, "", null);
            }else{
                return new ResponseBean(-1,0,"操作失败",null,true);//用户名或密码为空
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 玩家列表
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/list")
    public ResponseBean list(@RequestHeader(value = "token")String token, Integer pageNo,Integer pageSize,Integer userType,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(null == pageNo){
            pageNo = 1;
        }
        if(null == pageSize){
            pageSize = 10;
        }

        try{
            IPage<Player> pageData = playerService.getByPager(uid,pageNo,pageSize,userType);
            List<Player> dataList = pageData.getRecords();
            if(!dataList.isEmpty()){
                for(Player player : dataList){
                    player.setChaturl(player.getChaturl()+"?openId="+player.getOpenid());
                    PlayerBuyRecord countData = playerBuyRecordService.countData(player.getId());
                    if(null!=countData){
                        if(null!= countData.getBuyPoints()){
                            player.setDayTotalBuy(countData.getBuyPoints());
                        }
                        if(null!= countData.getEarnPoints()){
                            player.setDayTotalEarn(countData.getEarnPoints());
                        }
                    }
                    // 获取对象的InputStream
                    InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket("3d-robot-img").object(player.getHeadimg()).build());
                    // 将图像转换为Base64编码
                    String base64Image = convertToBase64(inputStream);
                    player.setHeadimg("data:image/jpeg;base64,"+base64Image);
                }
            }

            return new ResponseBean(0,pageData.getTotal(),"",dataList);//用户名或密码为空
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 玩家回水单独设置
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/setHs")
    public ResponseBean setHs(@RequestHeader(value = "token")String token, @RequestBody Player playerParam,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        String playerId = playerParam.getId();
        BigDecimal hsvalue = playerParam.getHsvalue();

        if(StringUtil.isNull(uid) ){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId) || null == hsvalue){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        try{

            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }
            if(!player.getBotUserId().equals(uid)){
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }

            player.setHsType(1);
            player.setHsvalue(hsvalue);

            if(playerService.updateById(player)){
                return new ResponseBean(0, 0, "", null);
            }else{
                return new ResponseBean(-1,0,"操作失败",null,true);//用户名或密码为空
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 玩家信息编辑
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/edit")
    public ResponseBean edit(@RequestHeader(value = "token")String token, @RequestBody Player playerParam,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        String playerId = playerParam.getId();

        if(StringUtil.isNull(uid) ){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId)){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        try{

            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }
            if(!player.getBotUserId().equals(uid)){
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }

            boolean isUpdate = false;
            String nickname = playerParam.getNickname();
            if(StringUtil.isNotNull(nickname) && !nickname.equals(player.getNickname())){
                isUpdate = true;
                player.setNickname(nickname);
            }

            String headimg = playerParam.getHeadimg();
            if(StringUtil.isNotNull(headimg) && !headimg.equals(player.getHeadimg())){
                isUpdate = true;
                player.setHeadimg(headimg);
            }

            Integer pretexting = playerParam.getPretexting();
            if(null!=pretexting && pretexting.intValue()!=player.getPretexting()){
                player.setPretexting(pretexting);
                isUpdate = true;
            }

            Integer eatPrize = playerParam.getEatPrize();
            if(null!=eatPrize && eatPrize.intValue()!=player.getEatPrize()){
                player.setEatPrize(eatPrize);
                isUpdate = true;
            }

            Integer reportNet = playerParam.getReportNet();
            if(null!=reportNet && reportNet.intValue()!=player.getReportNet()){
                player.setReportNet(reportNet);
                isUpdate = true;
            }

            if(isUpdate){
                playerService.updateById(player);
                return new ResponseBean(0, 0, "", null);
            }
            return new ResponseBean(-1,0,"操作失败",null,true);//用户名或密码为空
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 玩家上下分
     * @param token
     * @param request
     * @return
     */
    @PostMapping(value = "/upPoints")
    public ResponseBean upPoints(@RequestHeader(value = "token")String token, @RequestBody PlayerPointsRecord pointsRecord, HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        String playerId = pointsRecord.getPlayerId();
        BigDecimal points = pointsRecord.getPoints();
        Integer optType = pointsRecord.getOptType();

        if(StringUtil.isNull(uid) ){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId) || null == points || null == optType){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        if(optType!=0 && optType!=1){
            return new ResponseBean(-1,0,"非法参数值",null,true);//用户名或密码为空
        }

        if(points.compareTo(BigDecimal.ZERO)<=0){
            return new ResponseBean(-1,0,"非法参数值",null,true);//用户名或密码为空
        }

        try{

            BotUser botUser = botUserService.getById(uid);
            if(null == botUser) {
                return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
            }

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }

            if(!player.getBotUserId().equals(uid)){
                return new ResponseBean(-1,0,"玩家不存在",null,true);//用户名或密码为空
            }
            pointsRecord.setAuthStatus(1);
            pointsRecord.setApplyTime(new Date());
            pointsRecord.setAuthTime(new Date());
            pointsRecord.setBotUserId(uid);
            pointsRecord.setPoints(points);
            if(optType==0){//上分
                player.setPoints(player.getPoints().add(points));
            }else{//下分
                player.setPoints(player.getPoints().subtract(points));
            }
            playerPointsRecordService.save(pointsRecord);
            playerService.updateById(player);

            chatRoomMsgService.upDownPointsMsg(player,points,optType==0);

            return new ResponseBean(0, 0, "", null);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 验证openId
     * @param request
     * @return
     */
    @NoLogin
    @GetMapping(value = "/checkOpenId")
    public ResponseBean checkOpenId(HttpServletRequest request,String openId){

        if(StringUtil.isNull(openId) ){
            return new ResponseBean(-1,0,"参数错误",null,true);//用户名或密码为空
        }

        try{

            Player player = playerService.getByOpenId(openId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);
            }

            BotUser botUser = botUserService.getById(player.getBotUserId());
            if(null == botUser){
                return new ResponseBean(-1,0,"玩家不存在",null,true);
            }

            BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
            if(null == botUserSetting){
                return new ResponseBean(-1,0,"数据错误",null,true);
            }

            if(botUserSetting.getRoomOpen()==0){
                return new ResponseBean(10,0,"房间未开放",null,true);
            }

            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("userId",botUser.getId());
            resultMap.put("playerId",player.getId());
            resultMap.put("lotteryType",player.getLotteryType());
            resultMap.put("nickname",player.getNickname());
            resultMap.put("points",player.getPoints());
            resultMap.put("headimg",player.getHeadimg());
            resultMap.put("kxOpen",botUserSetting.getKuaixuanEnable());
            return new ResponseBean(0, 0, "", resultMap);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 清空数据
     * @param request
     * @return
     */
    @PostMapping(value = "/clearData")
    public ResponseBean clearData(@RequestHeader(value = "token")String token,HttpServletRequest request,@RequestBody Player playerParam){
        String uid = JwtUtil.getUsername(token);
        String playerId = playerParam.getId();

        if(StringUtil.isNull(uid) ){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(StringUtil.isNull(playerId)){
            return new ResponseBean(-1,0,"必填参数为空",null,true);//用户名或密码为空
        }

        try{

            Player player = playerService.getById(playerId);
            if(null == player) {
                return new ResponseBean(-1,0,"玩家不存在",null,true);
            }

            playerPointsRecordService.clearData(playerId);
            playerBuyRecordService.clearData(playerId);

            return new ResponseBean(0, 0, "", null);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 玩家个人报表数据
     * @param
     * @return
     */
    @RequestMapping(value = "/report")
    public ResponseBean report(@RequestHeader(value = "token")String token,String playerId, String date) {
        ReportRes reportRes = new ReportRes();
        try {
            // 玩家上下分
            PlayerPointsRecord playerPointsRecord = playerPointsRecordService.getPointsRecordByPlayerId(playerId, date, 0);
            if (null != playerPointsRecord) {
                reportRes.setUpScore(playerPointsRecord.getPoints());
            }
            PlayerPointsRecord playerPointsRecord2 = playerPointsRecordService.getPointsRecordByPlayerId(playerId, date, 1);
            if (null != playerPointsRecord2) {
                reportRes.setDownScore(playerPointsRecord2.getPoints());
            }

            // 玩家总投，回水，总投笔数，中奖，盈亏
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startTime = DateUtil.beginOfDay(sdf.parse(date));
            Date endTime = DateUtil.endOfDay(sdf.parse(date));
            PlayerBuyRecord playerBuyRecord = playerBuyRecordService.getPlayerByRecordSumByPlayerId(playerId, startTime, endTime);
            if (null != playerBuyRecord) {
                reportRes.setDrawMoney(playerBuyRecord.getDrawPoints());
                reportRes.setTotalCount(playerBuyRecord.getBuyAmount());
                reportRes.setTotalHs(playerBuyRecord.getHsPoints());
                reportRes.setTotalMoney(playerBuyRecord.getBuyPoints());
                reportRes.setProfitLossMoney(playerBuyRecord.getEarnPoints());
            }
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.error("系统繁忙");
        }
        return new ResponseBean(200, "", reportRes);
    }
}
