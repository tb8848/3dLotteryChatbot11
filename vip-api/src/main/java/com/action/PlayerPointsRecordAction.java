package com.action;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.*;
import com.service.BotUserService;
import com.service.ChatRoomMsgService;
import com.service.PlayerPointsRecordService;
import com.service.PlayerService;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/bot/player/points")
public class PlayerPointsRecordAction {

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private BotUserService botUserService;

    /**
     * 上下分记录
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/list")
    public ResponseBean list(@RequestHeader(value = "token")String token,
                             Integer pageNo,Integer pageSize,String playerId,Integer upDownType,Integer dayRange,HttpServletRequest request){
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
        if(null == upDownType){
            upDownType = 0; //默认查询上分记录
        }
        if(null == dayRange){
            dayRange = 1; //默认查找当天记录
        }

        String startTime = null;
        String endTime = null;
        if(dayRange==1){
            //当天
            startTime = DateUtil.beginOfDay(new Date()).toString();
            endTime = DateUtil.endOfDay(new Date()).toString();
        }else if(dayRange==-1){
            //昨天
            startTime = DateUtil.beginOfDay(DateUtil.yesterday()).toString();
            endTime = DateUtil.endOfDay(DateUtil.yesterday()).toString();
        }else if(dayRange==7){
            //本周
            startTime = DateUtil.beginOfWeek(new Date()).toString();
            endTime = DateUtil.endOfWeek(new Date()).toString();
        }

        Player player = null;
        if(StringUtil.isNotNull(playerId)){
            player = playerService.getById(playerId);
        }

        try{
            IPage<PlayerPointsRecord> pageData = playerPointsRecordService.getByPage(pageNo,pageSize,upDownType,playerId,startTime,endTime, uid);
            List<PlayerPointsRecord> dataList = pageData.getRecords();
            for(PlayerPointsRecord item : dataList){
                if(null!=player){
                    item.setPlayer(player);
                }else{
                    Player p = playerService.getById(item.getPlayerId());
                    item.setPlayer(p);
                }
            }
            pageData.setRecords(dataList);
            return new ResponseBean(0,pageData.getTotal(),"",pageData.getRecords());//用户名或密码为空
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 上下分
     * @param token
     * @return
     */
    /*@PostMapping(value = "/updatePoints")
    public ResponseBean updatePoints (@RequestHeader(value = "token")String token, @RequestBody PlayerPointsRecord playerPointsRecord) {
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if (StringUtil.isNull(playerPointsRecord.getPlayerId())) {
            return new ResponseBean(500,"系统繁忙",null);//用户名或密码为空
        }
        Player player = playerService.getById(playerPointsRecord.getPlayerId());

        // 机器人上下分
        if (playerPointsRecord.getType() == 1) {
            // 机器人下分
            if (playerPointsRecord.getOptType() == 1) {
                player.setPoints(player.getPoints().subtract(playerPointsRecord.getPoints()));
                playerPointsRecord.setPoints(playerPointsRecord.getPoints().negate());
            }else {
                player.setPoints(player.getPoints().add(playerPointsRecord.getPoints()));
            }

            playerService.updateById(player);

            playerPointsRecord.setApplyTime(new Date());
            playerPointsRecord.setAuthTime(new Date());
            playerPointsRecord.setAuthStatus(1);

        }else {
            playerPointsRecord.setApplyTime(new Date());
            playerPointsRecord.setAuthStatus(0);
        }
        playerPointsRecord.setBotUserId(uid);
        if (playerPointsRecordService.save(playerPointsRecord)) {
            return ResponseBean.ok("操作成功");
        }
        return ResponseBean.error("系统繁忙");
    }*/

    /**
     * 查询玩家所有待审核的上下分
     * @param token
     * @return
     */
    @PostMapping(value = "/getAllAuditPointsRecord")
    public ResponseBean getAllAuditPointsRecord(@RequestHeader(value = "token")String token){
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
//        List<Player> players = playerService.list(new QueryWrapper<Player>().eq("status",1).eq("botUserId",uid).in("userType",1,2));
//        List<String> ids = new ArrayList<>();
//        if (!players.isEmpty()){
//            ids = players.stream().map(player -> player.getId()).collect(Collectors.joining(","));
//            ids = players.stream().map(player -> player.getId()).collect(Collectors.toList());
//        }
//        List<PlayerPointsRecord> pointsRecords = playerPointsRecordService.list(new QueryWrapper<PlayerPointsRecord>().eq("authStatus",0).in("playerId",ids));
        List<PlayerPointsRecord> pointsRecords = playerPointsRecordService.list(new QueryWrapper<PlayerPointsRecord>().eq("authStatus",0).eq("botUserId",uid));
        for (PlayerPointsRecord playerPointsRecord : pointsRecords){
            Player player = playerService.getById(playerPointsRecord.getPlayerId());
            playerPointsRecord.setPlayer(player);
        }
        return new ResponseBean(200,"查询成功",pointsRecords,true);
    }

    /**
     * 审核上下分
     * @param token
     * @param id 上下分记录ID
     * @param authStatus 审核状态：1：同意，2：拒绝，-1：删除，-2：一键关闭
     * @return
     */
    @RequestMapping(value = "/applyPoints")
    public ResponseBean applyPoints (@RequestHeader(value = "token")String token, String id, Integer authStatus) {
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }
        //一键关闭
        if (authStatus == -2){
//            List<Player> players = playerService.list(new QueryWrapper<Player>().eq("status",1).eq("botUserId",uid).in("userType",1,2));
//            List<String> ids = new ArrayList<>();
//            if (!players.isEmpty()){
//                ids = players.stream().map(player -> player.getId()).collect(Collectors.toList());
//            }
//            List<PlayerPointsRecord> pointsRecords = playerPointsRecordService.list(new QueryWrapper<PlayerPointsRecord>().eq("authStatus",0).in("playerId",ids));
            List<PlayerPointsRecord> pointsRecords = playerPointsRecordService.list(new QueryWrapper<PlayerPointsRecord>().eq("authStatus",0).eq("botUserId",uid));
            for (PlayerPointsRecord pointsRecord : pointsRecords){
                pointsRecord.setAuthStatus(-1);
                playerPointsRecordService.updateById(pointsRecord);
            }
            return new ResponseBean(200,0,"操作成功","",true);
        }
        PlayerPointsRecord playerPointsRecord = playerPointsRecordService.getById(id);
        if (null == playerPointsRecord) {
            return new ResponseBean(500,0,"数据异常","",true);
        }
        Player player = playerService.getById(playerPointsRecord.getPlayerId());
        if (authStatus == 1) {
            if (playerPointsRecord.getOptType() == 1) {
                player.setPoints(player.getPoints().subtract(playerPointsRecord.getPoints()));
            }else {
                player.setPoints(player.getPoints().add(playerPointsRecord.getPoints()));
            }
            playerService.updateById(player);
        }
        BotUser botUser = botUserService.getById(player.getBotUserId());

        playerPointsRecord.setAuthTime(new Date());
        playerPointsRecord.setAuthStatus(authStatus);
        if (playerPointsRecordService.updateById(playerPointsRecord)) {
            if(authStatus==1){//审核通过，发送微信消息通知
                chatRoomMsgService.upDownPointsMsg(player,playerPointsRecord.getPoints(),playerPointsRecord.getOptType()==0);
            }else if(authStatus==2){
                String typeName = playerPointsRecord.getOptType()==1?"下分":"上分";
                typeName = typeName+"请求审核未通过";
                ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,typeName);
                chatRoomMsgService.pushMsg(toMsg,player);
            }
            return new ResponseBean(200,0,"操作成功","",true);
        }
        return new ResponseBean(500,0,"系统繁忙","",true);
    }


    /**
     * 上下分待审核列表
     * @param token
     * @param id
     * @param authStatus
     * @return
     */
    @PostMapping(value = "/applyList")
    public ResponseBean applyList (@RequestHeader(value = "token")String token, String id, Integer authStatus) {
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }
        List<PlayerPointsRecord> unauthList = playerPointsRecordService.getUnAuthList(uid);
        return new ResponseBean(0,unauthList.size(),"",unauthList);
    }

}
