package com.action;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.*;
import com.google.common.collect.Maps;
import com.service.*;
import com.util.JwtUtil;
import com.util.MD5Util;
import com.util.PasswordUtil;
import com.util.StringUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/admin/botuser")
public class BotUserAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private BotUserOddsService botUserOddsService;

    @Autowired
    private LotterySettingService lotterySettingService;

    @Autowired
    private LotteryMethodService lotteryMethodService;

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DictionaryService dictionaryService;

    /**
     * 添加机器人账号
     * @param botUser  机器人账号信息
     * @return
     */
    @ApiOperation(value = "添加机器人账号")
    @PostMapping("/add")
    public ResponseBean add(@RequestBody BotUser botUser, HttpServletRequest request,@RequestHeader(value = "token") String token) {
        String uId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uId)){
            return new ResponseBean(1,1,"admin.changePwd.unLogin",null);
        }

        Dictionary dictionary = dictionaryService.getDicByCode("system", "MaxRobotCount");
        if (null != dictionary) {
            long c = botUserService.count();
            if (c > Long.valueOf(dictionary.getValue())) {
                return new ResponseBean(500,1,"添加失败,机器人数量已达到上限",null, true);
            }
        }

        String username = botUser.getLoginName();
        String password = botUser.getLoginPwd();
        Integer useMonth = botUser.getUseMonth();
        if(StringUtil.isNull(username) || StringUtil.isNull(password) || null == useMonth){
            return ResponseBean.error("admin.login.accountNotNull");
        }

        if (botUserService.checkLoginNameExist(botUser.getLoginName())) {
            return ResponseBean.error("admin.addAdmin.userExist");
        }

        Admin admin = adminService.getById(uId);
        if (null != admin && null != admin.getLotteryType()) {
            botUser.setLotteryType(admin.getLotteryType());
        }

        int offDays;
        if (botUser.getUseType() == 1) {
            offDays = 31;
            switch(useMonth){
                case 6:
                    offDays = 183;
                    break;
                case 12:
                    offDays = 365;
                    break;
                default:
                    offDays = useMonth*offDays;
                    break;
            }
        }else {
            offDays = useMonth;
        }


        Date dueDate = DateUtil.offsetDay(new Date(),offDays);
        botUser.setDueDate(dueDate);
        botUser.setLoginPwd(PasswordUtil.jiami(password));
        botUser.setStatus(1);
//        botUser.setSafePwd(PasswordUtil.jiami("123456"));
        try{
            if(botUserService.save(botUser)){
                //添加账号成功后，新增一条账号配置信息、房间信息
                BotUserSetting botUserSetting = new BotUserSetting();
                botUserSetting.setBotUserId(botUser.getId());
                botUserSettingService.save(botUserSetting);

                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setUserId(botUser.getId());
                chatRoom.setRoomName(botUser.getLoginName()+"的聊天室");
                chatRoom.setCreateTime(new Date());
                chatRoomService.save(chatRoom);

                if (null != admin && null != admin.getLotteryType()) {
                    List<LotterySetting> lotterySettingList = lotterySettingService.list();
                    if (admin.getLotteryType() == 3) {
                        if (!lotterySettingList.isEmpty()) {
                            lotterySettingList.stream().forEach(lotterySetting -> {
                                BotUserOdds botUserOdds = new BotUserOdds();
                                botUserOdds.setBotUserId(botUser.getId());
                                botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                botUserOdds.setLotterySettingId(lotterySetting.getId());
                                botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                botUserOdds.setOdds(lotterySetting.getPeiRate());
                                botUserOdds.setParentId(lotterySetting.getParentId());
                                botUserOdds.setShortNo(lotterySetting.getShortNo());
                                botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                botUserOdds.setLotteryType(1);
                                botUserOddsService.save(botUserOdds);

                                BotUserOdds botUserOdds2 = new BotUserOdds();
                                botUserOdds2.setBotUserId(botUser.getId());
                                botUserOdds2.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                botUserOdds2.setLotterySettingId(lotterySetting.getId());
                                botUserOdds2.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                botUserOdds2.setMinBuy(lotterySetting.getMinBettingPrice());
                                botUserOdds2.setOdds(lotterySetting.getPeiRate());
                                botUserOdds2.setParentId(lotterySetting.getParentId());
                                botUserOdds2.setShortNo(lotterySetting.getShortNo());
                                botUserOdds2.setBettingRule(lotterySetting.getBettingRule());
                                botUserOdds2.setLotteryType(2);
                                botUserOddsService.save(botUserOdds2);
                            });
                        }
                    }else {
                        if (!lotterySettingList.isEmpty()) {
                            lotterySettingList.stream().forEach(lotterySetting -> {
                                BotUserOdds botUserOdds = new BotUserOdds();
                                botUserOdds.setBotUserId(botUser.getId());
                                botUserOdds.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                                botUserOdds.setLotterySettingId(lotterySetting.getId());
                                botUserOdds.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                                botUserOdds.setMinBuy(lotterySetting.getMinBettingPrice());
                                botUserOdds.setOdds(lotterySetting.getPeiRate());
                                botUserOdds.setParentId(lotterySetting.getParentId());
                                botUserOdds.setShortNo(lotterySetting.getShortNo());
                                botUserOdds.setBettingRule(lotterySetting.getBettingRule());
                                botUserOdds.setLotteryType(admin.getLotteryType());
                                botUserOddsService.save(botUserOdds);
                            });
                        }
                    }
                }

                return new ResponseBean(0, "成功", null,true);

            }else{
                return new ResponseBean(-1, "添加失败", null,true);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, "系统繁忙", null,true);
        }
    }


    /**
     * 分页加载数据
     * @return
     */
    @ApiOperation(value = "分页加载数据")
    @GetMapping("/listPage")
    public ResponseBean listPage(String keyword,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer limit,
                                 HttpServletRequest request,@RequestHeader(value = "token") String token) {
        String uId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uId)){
            return new ResponseBean(1,1,"admin.changePwd.unLogin",null);
        }
       IPage<BotUser> pager = botUserService.listPager(keyword,page,limit, uId);
       return new ResponseBean(0, pager.getTotal(),"", pager.getRecords());
    }

    /**
     * 根据ID获取机器人信息
     * @return
     */
    @ApiOperation(value = "根据ID获取机器人信息")
    @GetMapping("/getBotUserById")
    public ResponseBean getBotUserById(String userId) {
        BotUser botUser = botUserService.getById(userId);
        return new ResponseBean(0, "", botUser);
    }

    /**
     * 机器人账号续费
     * @return
     */
    @RequestMapping("/xufei")
    public ResponseBean xufei(String userId, Integer month, Integer useType) {
        if (month <= 0) {
            return ResponseBean.error("botUserOdds.xufei.timeLeOneMonth");
        }
        BotUser botUser = botUserService.getById(userId);
        if (null != botUser) {
            int offDays;
            if (useType == 1) {
                offDays = 31;
                switch(month){
                    case 6:
                        offDays = 183;
                        break;
                    case 12:
                        offDays = 365;
                        break;
                    default:
                        offDays = month*offDays;
                        break;
                }
            }else {
                offDays = month;
            }
            if (botUser.getStatus() == 2) {
                Date dueDate = DateUtil.offsetDay(new Date(),offDays);
                botUser.setDueDate(dueDate);
            }else if (botUser.getStatus() == 1) {
                Date dueDate = DateUtil.offsetDay(botUser.getDueDate(),offDays);
                botUser.setDueDate(dueDate);
            }

            if (botUser.getStatus() == 2) {
                botUser.setStatus(1);
            }
            botUserService.updateById(botUser);
            return ResponseBean.ok("botUserOdds.xufei.success");
        }
        return ResponseBean.error("common.systemError");
    }

    /**
     * 修改机器人账号信息
     * @param botUser
     * @return
     */
    @RequestMapping("/updateBotUser")
    public ResponseBean updateBotUser (@RequestBody BotUser botUser) {
        if (StringUtil.isNotNull(botUser.getLoginPwd())) {
            botUser.setLoginPwd(PasswordUtil.jiami(botUser.getLoginPwd()));
        }
        if (botUserService.updateById(botUser)) {
            return new ResponseBean(200,"common.updateSuccess","");
        }
        return ResponseBean.error("common.systemError");
    }

    /**
     * 即将到期机器人列表
     * @return
     */
    @ApiOperation(value = "即将到期机器人列表")
    @GetMapping("/dueList")
    public ResponseBean dueList(String keyword,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer limit) {
        IPage<BotUser> pager = botUserService.dueList(keyword,page,limit);
        return new ResponseBean(0, pager.getTotal(),"", pager.getRecords());
    }

    /**
     * 已到期机器人列表
     * @return
     */
    @ApiOperation(value = "已机器人列表")
    @GetMapping("/expiredList")
    public ResponseBean expiredList(String keyword,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer limit) {
        IPage<BotUser> pager = botUserService.expiredList(keyword,page,limit);
        return new ResponseBean(0, pager.getTotal(),"", pager.getRecords());
    }

    /**
     * 根据机器人ID查询当日信息
     * @param userId
     * @return
     */
    @GetMapping("/getBotInfoById")
    public ResponseBean getBotInfoById (String userId) {
        Map<String, Object> resultMap = Maps.newHashMap();
        BotUser botUser = botUserService.getById(userId);
        resultMap.put("botName", botUser.getLoginName());
        // 获取当日上下分记录
        List<PlayerPointsRecord> playerPointsRecordList = playerPointsRecordService.getCurrentDayScorceByBotUserId(userId);
        if (!playerPointsRecordList.isEmpty()) {
            BigDecimal upScore = playerPointsRecordList.stream().filter(playerPointsRecord -> playerPointsRecord.getOptType() == 0).map(PlayerPointsRecord::getPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downScore = playerPointsRecordList.stream().filter(playerPointsRecord -> playerPointsRecord.getOptType() == 1).map(PlayerPointsRecord::getPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
            resultMap.put("upScore", upScore);
            resultMap.put("downScore", downScore);
        }else {
            resultMap.put("upScore", 0);
            resultMap.put("downScore", 0);
        }
        // 获取玩家总分
        List<Player> playerList = playerService.getPlayerListByBotUserId(userId);
        if (!playerList.isEmpty()) {
            BigDecimal totalScore = playerList.stream().map(Player::getPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
            resultMap.put("totalScore", totalScore);
        }else {
            resultMap.put("totalScore", 0);
        }

        // 获取当日总投和总盈亏
        Date currDate = DateUtil.date(); //当前时间
        Date startDate = DateUtil.beginOfDay(currDate); //当前时间的开始时间
        Date endDate = DateUtil.endOfDay(currDate); //当前时间的结束时间
        PlayerBuyRecord playerBuyRecord = playerBuyRecordService.countDayData(userId, startDate, endDate);
        if (null != playerBuyRecord) {
            resultMap.put("totalAmount", playerBuyRecord.getBuyPoints());
            resultMap.put("totalProfitLoss", playerBuyRecord.getEarnPoints());
            resultMap.put("backWater", playerBuyRecord.getHsPoints());
        }else {
            resultMap.put("totalAmount", 0);
            resultMap.put("totalProfitLoss", 0);
            resultMap.put("backWater", 0);
        }

        try {
            HttpResponse res = HttpRequest.post(botUser.getLottery3dUrl()+"robot/getDrawInfo")
                    .header("token",botUser.getLogin3dToken())
                    .execute();
            String body = res.body();
            JSONObject jsonObject = JSONObject.parseObject(body);
            if (jsonObject.getInteger("code") == 200) {
                JSONObject dataJson = jsonObject.getJSONObject("data");
                resultMap.put("networkDiskBalance", dataJson.getBigDecimal("creditLimit"));
                resultMap.put("networkDiskBackWater", dataJson.getBigDecimal("huishuiCount"));
            }else {
                resultMap.put("networkDiskBalance", 0);
                resultMap.put("networkDiskBackWater", 0);
            }
        }catch (Exception e) {
            e.printStackTrace();
            resultMap.put("networkDiskBalance", 0);
            resultMap.put("networkDiskBackWater", 0);
        }


        PlayerBuyRecord playerBuyRecord2 = playerBuyRecordService.getSumMoneyByBw(userId);
        if (null != playerBuyRecord2) {
            resultMap.put("drawAmount", playerBuyRecord2.getBuyPoints());
        }else {
            resultMap.put("drawAmount", 0);
        }
        return new ResponseBean(200, "", resultMap);
    }

    /**
     * 更新机器人赔率配置-手动执行
     * @return
     */
    @RequestMapping(value = "syncLs")
    public ResponseBean syncLs () {
        List<BotUser> botUserList = botUserService.list();
        if (!botUserList.isEmpty()) {
            for (BotUser botUser : botUserList) {
                List<LotterySetting> lsList = lotterySettingService.list();
                for (LotterySetting lotterySetting : lsList) {
                    LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BotUserOdds::getBotUserId, botUser.getId());
                    queryWrapper.eq(BotUserOdds::getLotterySettingId, lotterySetting.getId());
                    queryWrapper.last("limit 1");
                    BotUserOdds botUserOdds = botUserOddsService.getOne(queryWrapper);
                    if (null == botUserOdds) {
                        if (botUser.getLotteryType() == 3) {
                            BotUserOdds buo = new BotUserOdds();
                            buo.setBotUserId(botUser.getId());
                            buo.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                            buo.setLotterySettingId(lotterySetting.getId());
                            buo.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                            buo.setMinBuy(lotterySetting.getMinBettingPrice());
                            buo.setOdds(lotterySetting.getPeiRate());
                            buo.setParentId(lotterySetting.getParentId());
                            buo.setShortNo(lotterySetting.getShortNo());
                            buo.setBettingRule(lotterySetting.getBettingRule());
                            buo.setLotteryType(1);
                            botUserOddsService.save(buo);

                            BotUserOdds buo2 = new BotUserOdds();
                            buo2.setBotUserId(botUser.getId());
                            buo2.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                            buo2.setLotterySettingId(lotterySetting.getId());
                            buo2.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                            buo2.setMinBuy(lotterySetting.getMinBettingPrice());
                            buo2.setOdds(lotterySetting.getPeiRate());
                            buo2.setParentId(lotterySetting.getParentId());
                            buo2.setShortNo(lotterySetting.getShortNo());
                            buo2.setBettingRule(lotterySetting.getBettingRule());
                            buo2.setLotteryType(2);
                            botUserOddsService.save(buo2);
                        }else {
                            BotUserOdds buo = new BotUserOdds();
                            buo.setBotUserId(botUser.getId());
                            buo.setLotteryMethodId(lotterySetting.getLotteryMethodId());
                            buo.setLotterySettingId(lotterySetting.getId());
                            buo.setMaxBuy(lotterySetting.getMaxNumberTypeCount());
                            buo.setMinBuy(lotterySetting.getMinBettingPrice());
                            buo.setOdds(lotterySetting.getPeiRate());
                            buo.setParentId(lotterySetting.getParentId());
                            buo.setShortNo(lotterySetting.getShortNo());
                            buo.setBettingRule(lotterySetting.getBettingRule());
                            buo.setLotteryType(botUser.getLotteryType());
                            botUserOddsService.save(buo);
                        }

                    }
                }
            }
        }
        return ResponseBean.OK;
    }
}
