package com.action;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.*;
import com.config.NoLogin;
import com.service.*;
import com.util.JwtUtil;
import com.util.StringUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

import static com.util.StringUtil.convertToBase64;

/**
 * 玩家定投
 */
@RestController
@RequestMapping(value = "/bot/player/dingtou")
public class PlayerFixedBuyAction {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private PlayerFixedBuyService playerFixedBuyService;

    @Autowired
    private MinioClient minioClient;

    /**
     * 增加定投
     * 修改定投
     * @param fixedBuy
     * @return
     */
    @NoLogin
    @PostMapping(value = "/add")
    public ResponseBean add(@RequestBody PlayerFixedBuy fixedBuy){

        String playerId = fixedBuy.getPlayerId();
        String botUserId = fixedBuy.getUserId();
        String buyDesc = fixedBuy.getBuyDesc();
        String buyPoints = fixedBuy.getBuyPoints();
        Integer lotteryType = fixedBuy.getLotteryType();

        if(null==lotteryType || StringUtil.isNull(playerId) || StringUtil.isNull(botUserId) || StringUtil.isNull(buyDesc) || StringUtil.isNull(buyPoints)){
            return new ResponseBean(-1, 0, "缺少必填参数", null, true);
        }

        if(lotteryType!=1 && lotteryType!=2){
            return new ResponseBean(-1, 0, "参数值错误", null, true);
        }

        String result = playerFixedBuyService.editPlan(fixedBuy);
        if (StringUtil.isNull(result)) {
            return new ResponseBean(0, 0, "", null);
        }
        return new ResponseBean(-1, 0, result, null, true);

    }



    /**
     * 获取玩家的定投计划
     * @param fixedBuy
     * @return
     */
    @NoLogin
    @PostMapping(value = "/getOne")
    public ResponseBean getOne(@RequestBody PlayerFixedBuy fixedBuy){

        String playerId = fixedBuy.getPlayerId();
        String botUserId = fixedBuy.getUserId();

        try {

            BotUser botUser = botUserService.getById(botUserId);
            if (null == botUser) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }

            Player player = playerService.getById(playerId);
            if (null == player) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }

            if (!player.getBotUserId().equals(botUserId)) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }
            PlayerFixedBuy playerFixedBuy = playerFixedBuyService.getRunningOne(playerId);
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("leftPoints",player.getPoints());
            dataMap.put("fixedBuyPlan",playerFixedBuy);

            return new ResponseBean(0, 0, "", dataMap, true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }



    /**
     * 获取玩家的定投计划
     * @param fixedBuy
     * @return
     */
    @NoLogin
    @PostMapping(value = "/getList")
    public ResponseBean getList(@RequestBody PlayerFixedBuy fixedBuy){

        String playerId = fixedBuy.getPlayerId();
        String botUserId = fixedBuy.getUserId();
        Integer lotteryType = fixedBuy.getLotteryType();
        try {

            BotUser botUser = botUserService.getById(botUserId);
            if (null == botUser) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }

            Player player = playerService.getById(playerId);
            if (null == player) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }

            if (!player.getBotUserId().equals(botUserId)) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }
            List<PlayerFixedBuy> playerFixedBuyList = playerFixedBuyService.getList(playerId,lotteryType);
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("leftPoints",player.getPoints());
            dataMap.put("planList",playerFixedBuyList);

            return new ResponseBean(0, 0, "", dataMap, true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }




    /**
     * 停止定投计划
     * @param fixedBuy
     * @return
     */
    @NoLogin
    @PostMapping(value = "/stop")
    public ResponseBean stop(@RequestBody PlayerFixedBuy fixedBuy){

        String playerId = fixedBuy.getPlayerId();
        String botUserId = fixedBuy.getUserId();

        try {

            BotUser botUser = botUserService.getById(botUserId);
            if (null == botUser) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }

            Player player = playerService.getById(playerId);
            if (null == player) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }

            if (!player.getBotUserId().equals(botUserId)) {
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }
            PlayerFixedBuy existOne = playerFixedBuyService.getRunningOne(playerId);
            if(null == existOne){
                return new ResponseBean(-1, 0, "参数错误", null, true);
            }
            if(existOne.getTaskStatus()==-1){
                return new ResponseBean(0, 0, "", null, true);
            }

            existOne.setTaskStatus(-1);
            existOne.setEndTime(new Date());
            if(playerFixedBuyService.updateById(existOne)){
                return new ResponseBean(0, 0, "", null, true);
            }
            return new ResponseBean(-1, 0, "操作失败", null, true);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

    /**
     * 获取机器人跟码列表
     * @param token
     * @return
     */
    @GetMapping("/getBotUserFixedBuy")
    public ResponseBean getBotUserFixedBuy(@RequestHeader(value = "token")String token, Integer pageNo,Integer pageSize) throws Exception {

        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
        }
        IPage<PlayerFixedBuy> iPage = playerFixedBuyService.getBotUserFixedBuy(uid,pageNo,pageSize);
        for (PlayerFixedBuy playerFixedBuy : iPage.getRecords()){
            Player player = playerService.getById(playerFixedBuy.getPlayerId());
            if (player != null){
                playerFixedBuy.setPlayerName(player.getNickname());
//                playerFixedBuy.setPlayerHeadImg(player.getHeadimg());
                // 获取对象的InputStream
                InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket("3d-robot-img").object(player.getHeadimg()).build());
                // 将图像转换为Base64编码
                String base64Image = convertToBase64(inputStream);
                playerFixedBuy.setPlayerHeadImg("data:image/jpeg;base64,"+base64Image);
            }
        }
        return new ResponseBean(0,iPage.getTotal(),"",iPage.getRecords());
    }


    /**
     * 机器人停止定投计划
     * @param token
     * @param id
     * @return
     */
    @GetMapping(value = "/stopFollow")
    public ResponseBean stopFollow(@RequestHeader(value = "token")String token,String id){

        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }
        BotUser botUser = botUserService.getById(uid);
        if(null == botUser) {
            return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
        }

        PlayerFixedBuy playerFixedBuy = playerFixedBuyService.getById(id);
        if(null == playerFixedBuy){
            return new ResponseBean(-1, 0, "参数错误", null, true);
        }
        if(playerFixedBuy.getTaskStatus() == -1){
            return new ResponseBean(0, 0, "", null, true);
        }

        playerFixedBuy.setTaskStatus(-1);
        playerFixedBuy.setEndTime(new Date());
        if(playerFixedBuyService.updateById(playerFixedBuy)){
            return new ResponseBean(0, 0, "", null, true);
        }
        return new ResponseBean(-1, 0, "操作失败", null, true);

    }

}
