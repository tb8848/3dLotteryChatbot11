package com.mq;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.*;
import com.beans.Dictionary;
import com.config.MinioUtils;
import com.dao.DictionaryDAO;
import com.dao.DrawBuyRecordDAO;
import com.rabbitmq.client.Channel;
import com.service.*;
import com.util.Cell;
import com.util.Code3DCreateUtils;
import com.util.PictureUtil;
import com.util.StringUtil;
import com.websocket.DrawOpenStatusMessage;
import io.minio.MinioClient;
import org.redisson.misc.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DrawOpenStatusMQService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private DictionaryDAO dictionaryDAO;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerReturnPointsService playerReturnPointsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DrawBuyRecordService drawBuyRecordService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private PlayerFixedBuyService playerFixedBuyService;


    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private LockTemplate lockTemplate;


    private Logger logger = LoggerFactory.getLogger(DrawOpenStatusMQService.class);



    @RabbitListener(queues = "botQueue_p3")
    public void recvP3Message(Message msg, final Channel channel) throws IOException {
        String body = new String(msg.getBody());
        //System.out.println(DateUtil.now() + ">>>>>>>>>>>>[排列三]收到[开关盘]消息:"+body);
        logger.info(">>>>>>>>>>>>[排列三]收到[开关盘]消息:"+body);
        String lockKey = "p3:chatbot:draw:mq";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }

        try{
            DrawOpenStatusMessage msgData = JSON.parseObject(body, DrawOpenStatusMessage.class);
            Integer openStatus = msgData.getOpenStatus();

            Integer drawNo = msgData.getDrawNo();
            Draw draw = p3DrawService.getByDrawNo(drawNo);
            drawBuyRecordDAO.copyTable(draw.getDrawId()+"");
            Integer drawStatus = draw.getDrawStatus();
            int flag = 0;
            if(openStatus==1 || openStatus==3){
                flag = 1;
            }
            redisTemplate.boundValueOps("p3chatbot-pushLeftTime").set(flag);
            redisTemplate.boundValueOps("p3chatbot-pushData").set(JSON.toJSONString(msgData));

            int lotteryType = 2;
            String lotteryName = "P3";
            if(null!=drawStatus && drawStatus == 4){
                clearDrawPrizeResult(draw,lotteryType,lotteryName);
            }else if(null!=drawStatus && drawStatus==3){
                //3D网已结账
                pushDrawPrizeResult(draw,lotteryType,lotteryName);
                pushDrawCode(draw,lotteryType,lotteryName);

            }else{
                if(openStatus==1){
                    pushDrawStatus(openStatus,lotteryType,lotteryName);
                    setDingTouStartTime();
                }else if(openStatus==0){
                    pushDrawStatus(openStatus,lotteryType,lotteryName);
                    Thread.sleep(3000);
                    pushSuccessOrder(drawNo,lotteryType,lotteryName);
                }
            }
            //发送中奖号码
            //^^--|  119集-7|4|1|2|1|尨

            //redisTemplate.boundValueOps(msgKey).set("1",1, TimeUnit.DAYS);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);

        } catch (Exception e) {
            e.printStackTrace();
            if(msg.getMessageProperties().getRedelivered()){
                logger.info(">>>>>>>>>>>>[排列三]重复失败丢弃消息:"+body);
                //System.out.println("重复失败丢弃消息 plan:{}"+msg.getBody());
                channel.basicReject(msg.getMessageProperties().getDeliveryTag(), false);
            }else{
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false,true);
            }
        }finally {
            lockTemplate.releaseLock(lockInfo);
        }
    }


    /**
     * 接收开关盘消息
     * @param msg
     * @param channel
     */
    @RabbitListener(queues = "botQueue_3d")
    public void recv3DMessage(Message msg, final Channel channel) throws IOException {
        String body = new String(msg.getBody());
        logger.info(">>>>>>>>>>>>[3D]收到开关盘消息:"+body);

        String lockKey = "3d:chatbot:draw:mq";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }

        //System.out.println(DateUtil.now() + ">>>>>>>>>>>>收到[开关盘]消息:"+body);
        try{
            DrawOpenStatusMessage msgData = JSON.parseObject(body, DrawOpenStatusMessage.class);
            Integer openStatus = msgData.getOpenStatus();

            Integer drawNo = msgData.getDrawNo();
            Draw draw = drawService.getByDrawNo(drawNo);
            drawBuyRecordDAO.copyTable(draw.getDrawId()+"");
            Integer drawStatus = draw.getDrawStatus();
            int flag = 0;
            if(openStatus==1 || openStatus==3){
                flag = 1;
            }
            redisTemplate.boundValueOps("3dchatbot-pushLeftTime").set(flag);
            redisTemplate.boundValueOps("3dchatbot-pushData").set(JSON.toJSONString(msgData));

            int lotteryType = 1;
            String lotteryName = "3D";

            if(null!=drawStatus && drawStatus==4){
               clearDrawPrizeResult(draw,lotteryType,lotteryName);
            }else if(null!=drawStatus && drawStatus==3){
                //3D网已结账
                pushDrawPrizeResult(draw,lotteryType,lotteryName);
                pushDrawCode(draw,lotteryType,lotteryName);
            }else{
                if(openStatus==1){
                    pushDrawStatus(openStatus,lotteryType,lotteryName);
                    setDingTouStartTime();
                }else if(openStatus==0){
                    pushDrawStatus(openStatus,lotteryType,lotteryName);
                    Thread.sleep(3000);
                    pushSuccessOrder(drawNo,lotteryType,lotteryName);
                }
            }
            //发送中奖号码
            //^^--|  119集-7|4|1|2|1|尨

            //redisTemplate.boundValueOps(msgKey).set("1",1, TimeUnit.DAYS);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);

        } catch (Exception e) {
            e.printStackTrace();
            if(msg.getMessageProperties().getRedelivered()){
                logger.info(">>>>>>>>>>>>[3D]重复失败丢弃消息:"+body);
                //System.out.println("重复失败丢弃消息 plan:{}"+msg.getBody());
                channel.basicReject(msg.getMessageProperties().getDeliveryTag(), false);
            }else{
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false,true);
            }
        }finally {
            lockTemplate.releaseLock(lockInfo);
        }
    }

    /**
     * 设置定投任务开始执行时间
     */

    private void setDingTouStartTime() {
        Date startTime =  DateUtil.offsetMinute(new Date(),5);
        int rows = playerFixedBuyService.updateStartTime(startTime,1);
        String info = String.format("开盘后设置定投执行时间为：%s,影响记录数量：%s",DateUtil.format(startTime,"yyyy-MM-dd HH:mm:ss"),rows);
        logger.info(">>>>>>>>>>>>"+info);
        //System.out.println(String.format("开盘后设置定投执行时间为：%s,影响记录数量：%s",DateUtil.format(startTime,"yyyy-MM-dd HH:mm:ss"),rows));
    }


    /**
     * 推送成功的下注订单
     * @param drawNo 期号
     */
    public void pushSuccessOrder(Integer drawNo,Integer lotteryType,String lotteryName){

        //1：从购买记录中提取玩家ID列表
        List<String> playerIdList = playerBuyRecordService.getPlayerIdsBy(drawNo,lotteryType);

        List<Player> playerList = null;
        //2:根据玩家ID，获取玩家列表
        if(null!=playerIdList){
            playerList = playerService.getListByIds(playerIdList);
            if(null!=playerList && playerList.size()>0){
                //按玩家的机器人账号分组
                Map<String,List<Player>> playerGroup = playerList.stream().collect(Collectors.groupingBy(Player::getBotUserId));
                Set<String> botUserIdSet = playerGroup.keySet();
                //4: 向不同账号推送成功订单，为保障消息的顺序，推送目的地为mq,而非终端
                for(String botUserId : botUserIdSet){
                    BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUserId);
                    BotUser botUser = botUserService.getById(botUserId);
                    boolean needPush = false;
                    List<Player> playerList1 = playerGroup.get(botUserId);
                    List<String> playerIdList1 = playerList1.stream().map(item->item.getId()).collect(Collectors.toList());
                    //玩家的成功订单，包含假托类型的订单
                    List<PlayerBuyRecord> successOrderList =  playerBuyRecordService.getListByPlayerIds(playerIdList1,drawNo,lotteryType);
                    StringBuffer msgBuffer = new StringBuffer();
                    if(null!=successOrderList && successOrderList.size()>0){
                        needPush = true;
                        msgBuffer.append("【"+lotteryName+"】本期成功订单("+drawNo+")\r\n");
                        Map<String,List<PlayerBuyRecord>> playerBuyGroup = successOrderList.stream().collect(Collectors.groupingBy(PlayerBuyRecord::getPlayerId));
                        playerBuyGroup.forEach((playerId,buylist)->{
                            StringBuffer msgBuffer1 = new StringBuffer();
                            Player player = playerList1.stream().filter(item->item.getId().equals(playerId)).findFirst().orElse(null);
                            if(null!=player){
                                msgBuffer1.append("【"+lotteryName+"】本期成功订单("+drawNo+")\r\n");
                                for (PlayerBuyRecord playerBuyRecord : buylist) {
                                    msgBuffer1.append("["+player.getNickname()+"]"+playerBuyRecord.getBuyDesc()+"\r\n");
                                    msgBuffer.append("["+player.getNickname()+"]"+playerBuyRecord.getBuyDesc()+"\r\n");
                                }
                                msgBuffer1.append("- - - - - - - - - -\r\n");
                                msgBuffer.append("- - - - - - - - - -\r\n");

                                //每个玩家的成功订单，单独发送给微信终端
                                if(player.getUserType()==2 && StringUtil.isNotNull(botUser.getWxId())){

                                    //System.out.println(String.format("%s 玩家成功订单 %s",player.getNickname(),msgBuffer1.toString()));
                                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),msgBuffer1.toString());
                                }

                                //玩家有效流水订单，只包括报网和吃奖类型的订单
                                List<PlayerBuyRecord> validOrderList = buylist.stream().filter(item->null!=item.getBuyType() && (item.getBuyType()==0 || item.getBuyType()==1)).collect(Collectors.toList());
                                if(null!=validOrderList && validOrderList.size()>0){
                                    BigDecimal validBuyPoints = validOrderList.stream().map(item->item.getBuyPoints()).reduce(BigDecimal.ZERO,BigDecimal::add);
                                    PlayerReturnPoints returnPoints = new PlayerReturnPoints();
                                    returnPoints.setDrawNo(drawNo);
                                    returnPoints.setValidBuyPoints(validBuyPoints); //有效流水
                                    returnPoints.setHsPercent(player.getHsvalue().multiply(BigDecimal.valueOf(0.0001))); //返水值
                                    returnPoints.setPlayerId(playerId);
                                    returnPoints.setBotUserId(botUserId);
                                    returnPoints.setCreateTime(new Date());
                                    returnPoints.setStatus(0);
                                    returnPoints.setLotteryType(lotteryType);
                                    BigDecimal totalHs = returnPoints.getValidBuyPoints().multiply(returnPoints.getHsPercent());
                                    returnPoints.setReturnPoints(totalHs);
                                    if(null!=botUserSetting.getHsAutoBack() && botUserSetting.getHsAutoBack()==1){
                                        //机器人设置了停盘后自动返水
                                        returnPoints.setReturnType(2);
                                        returnPoints.setReturnTime(new Date());
                                        returnPoints.setStatus(1);
                                        if(playerReturnPointsService.addOne(returnPoints)==1){
                                            playerService.updatePoint(playerId,totalHs,true);
                                        }
                                    }else{
                                        playerReturnPointsService.addOne(returnPoints);
                                    }
                                    //System.out.println(DateUtil.now()+">>>>>>>>>>"+String.format("玩家%s : 第%s期返水%s",player.getNickname(),drawNo,totalHs));
                                }
                            }
                        });
                    }
                    if(needPush){
                        ChatRoomMsg toMsg = new ChatRoomMsg();
                        toMsg.setFromUserType(1);
                        toMsg.setFromUserNick("机器人");
                        toMsg.setMsgType(0);
                        toMsg.setOptType(0);
                        toMsg.setToUserId("0");
                        toMsg.setToUserNick("");
                        toMsg.setToUserType(1);
                        toMsg.setMsg(msgBuffer.toString());
                        toMsg.setBotUserId(botUserId);
                        toMsg.setCreateTime(new Date());
                        toMsg.setOptType(10);
                        chatRoomMsgService.save(toMsg);
                        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
                    }

                }
            }
        }

    }


    /**
     * 推送中奖情况
     * @param draw
     */
    public void pushDrawPrizeResult(Draw draw,Integer lotteryType,String lotteryName){
        Integer drawNo = draw.getDrawId();

        playerBuyRecordService.updateStatus(drawNo,1,lotteryType);
        Map<String, Object> resultMap = drawService.settleAccounts(draw,lotteryType);

        Integer totalDrawCount = (Integer)resultMap.get("drawCount");
        logger.info(">>>>>>>>>>>>【"+lotteryName+"】["+drawNo+"]期中奖注数："+totalDrawCount);

        if(null!=totalDrawCount && totalDrawCount>0){

            //存在中奖记录
            List<DrawBuyRecord> drawBuyRecords = drawBuyRecordService.getDrawPrizeList(drawNo,lotteryType);
            Map<String,List<DrawBuyRecord>> playerGroup = drawBuyRecords.stream().collect(Collectors.groupingBy(DrawBuyRecord::getVipId));
            Set<String> playerIds = playerGroup.keySet();
            List<Player> playerList = playerService.getListByIds(playerIds.stream().collect(Collectors.toList()));
            Map<String,List<Player>> botUserIdGroup = playerList.stream().collect(Collectors.groupingBy(Player::getBotUserId));
            botUserIdGroup.forEach((botUserId,playerList1)->{
                BotUser botUser = botUserService.getById(botUserId);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【"+lotteryName+"】> > > > > > > > \r\n");
                BigDecimal allDrawMoney = BigDecimal.ZERO;
                for(Player player : playerList1){
                    StringBuffer buffer1 = new StringBuffer();
                    buffer1.append("【"+player.getNickname()+"】情况："+"\r\n");
                    List<DrawBuyRecord> drawList = playerGroup.get(player.getId());
                    for(DrawBuyRecord one : drawList){
                        String codeName = Code3DCreateUtils.getCodePrintName(Integer.valueOf(one.getLotteryMethodId()),"-",one.getBuyCodes());
                        buffer1.append(codeName+"，"+"中"+one.getBuyMoney().stripTrailingZeros().toPlainString()+"，获得:"+one.getDrawMoney().stripTrailingZeros().toPlainString()+"\r\n");
                    }
                    Map<String,List<DrawBuyRecord>> playerRecordIdGroup = drawList.stream().collect(Collectors.groupingBy(DrawBuyRecord::getBaopaiId));
                    playerRecordIdGroup.forEach((playerRecordId,drawList1)->{
                        PlayerBuyRecord playerBuyRecord = playerBuyRecordService.getById(playerRecordId);
                        if(null!=playerBuyRecord){
                            BigDecimal totalDrawMoney = drawList1.stream().map(item->item.getDrawMoney()).reduce(BigDecimal.ZERO,BigDecimal::add);
                            playerBuyRecord.setDrawPoints(totalDrawMoney);
                            playerBuyRecord.setEarnPoints(playerBuyRecord.getEarnPoints().add(totalDrawMoney));
                            playerBuyRecordService.updateById(playerBuyRecord);
                            if(StringUtil.isNotNull(playerBuyRecord.getDtTaskId())){
                                //当前中奖记录来自定投
                                String dtTaskId = playerBuyRecord.getDtTaskId();
                                checkDingTouTask(dtTaskId,totalDrawMoney);
                            }
                        }
                    });
                    BigDecimal totalDrawMoney = drawList.stream().map(item->item.getDrawMoney()).reduce(BigDecimal.ZERO,BigDecimal::add);
                    buffer1.append("合计获取："+totalDrawMoney.stripTrailingZeros().toPlainString()+"\r\n");
                    buffer1.append("【开前】："+player.getPoints().stripTrailingZeros().toPlainString()+"\r\n");
                    playerService.updatePoint(player.getId(),totalDrawMoney,true);
                    buffer1.append("【开后】："+player.getPoints().add(totalDrawMoney).stripTrailingZeros().toPlainString()+"\r\n");
                    buffer1.append("- - - - - - - - - -\r\n");
                    buffer.append(buffer1.toString());
                    allDrawMoney = allDrawMoney.add(totalDrawMoney);

                    //每个玩家的中奖情况，单独发送给微信终端
                    if(player.getUserType()==2 && (player.getLotteryType()==3 || player.getLotteryType()==1) && StringUtil.isNotNull(botUser.getWxId())){
                        //System.out.println(String.format("%s 玩家中奖订单 %s",player.getNickname(),buffer1.toString()));
                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),"【"+lotteryName+"】> > > > > > > > \r\n"+buffer1.toString());
                    }
                }

                buffer.append("【"+lotteryName+"本次总派送】："+allDrawMoney.stripTrailingZeros().toPlainString());
                if(buffer.length()>10){
                    ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUserId,buffer.toString());
                    toMsg.setOptType(11); //中奖情况
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
                }

            });
        }
    }


    /**
     * 清除中奖记录
     * @param draw
     */
    public void clearDrawPrizeResult(Draw draw,Integer lotteryType,String lotteryName){
        Integer drawNo = draw.getDrawId();
        //更改下注记录结算状态为：未结算
        playerBuyRecordService.updateStatus(drawNo,0,lotteryType);
        //获取中奖记录列表
        List<DrawBuyRecord> drawBuyRecords = drawBuyRecordService.getDrawPrizeList(drawNo,lotteryType);
        if(null == drawBuyRecords || drawBuyRecords.size()<1){
            logger.info("["+lotteryName+"]第"+drawNo+"期==============>无中奖记录");
            return;
        }

        Map<String,List<DrawBuyRecord>> playerGroup = drawBuyRecords.stream().collect(Collectors.groupingBy(DrawBuyRecord::getVipId));
        Set<String> playerIds = playerGroup.keySet();
        List<Player> playerList = playerService.getListByIds(playerIds.stream().collect(Collectors.toList()));

        for(Player player : playerList){

            List<DrawBuyRecord> drawList = playerGroup.get(player.getId());
            List<String> buyIdList = drawList.stream().map(item->item.getId()).collect(Collectors.toList());
            int rows = drawBuyRecordService.clearDrawPrizeList(drawNo,buyIdList);
            logger.info("["+lotteryName+"]第"+drawNo+"期==============>清除中奖记录："+rows);

            Map<String,List<DrawBuyRecord>> playerRecordIdGroup = drawList.stream().collect(Collectors.groupingBy(DrawBuyRecord::getBaopaiId));

            playerRecordIdGroup.forEach((playerRecordId,dlist)->{
                PlayerBuyRecord playerBuyRecord = playerBuyRecordService.getById(playerRecordId);
                if(null!=playerBuyRecord){
                    BigDecimal totalDrawMoney = dlist.stream().map(item->item.getDrawMoney()).reduce(BigDecimal.ZERO,BigDecimal::add);
                    playerBuyRecord.setDrawPoints(BigDecimal.ZERO);
                    playerBuyRecord.setEarnPoints(playerBuyRecord.getEarnPoints().subtract(totalDrawMoney));
                    playerBuyRecordService.updateById(playerBuyRecord);
                    if(StringUtil.isNotNull(playerBuyRecord.getDtTaskId())){
                        //当前中奖记录来自定投
                        String dtTaskId = playerBuyRecord.getDtTaskId();
                        PlayerFixedBuy playerFixedBuy = playerFixedBuyService.getById(dtTaskId);
                        if(null!=playerFixedBuy && 0==playerFixedBuy.getCircleType() && playerFixedBuy.getTaskStatus()==-1){
                            playerFixedBuy.setTaskStatus(1);
                            playerFixedBuy.setEndTime(null);
                            playerFixedBuy.setStopReason("");
                            playerFixedBuyService.updateById(playerFixedBuy);
                            logger.info("["+lotteryName+"]第"+drawNo+"期==============>清除定投结束状态："+playerFixedBuy.getBuyDesc());
                        }
                    }
                    playerService.updatePoint(player.getId(),totalDrawMoney,false);
                }
            });
        }
    }



    /**
     * 推送开奖号码
     * @param draw
     */
    public void pushDrawCode(Draw draw,Integer lotteryType,String lotteryName){
        Integer drawNo = draw.getDrawId();
        String msg1 = "【"+lotteryName+"】^^--|  "+drawNo+"集-"+draw.getDrawResult2T()+"|"+draw.getDrawResult3T()+"|"+draw.getDrawResult4T();

        List<Draw> last15List = null;
        if(lotteryType==2){
            last15List = p3DrawService.listN(15);
        }else{
            last15List = drawService.listN(15);
        }


        String filePath = uploadDir+"images/";
        //如果目录不存在，则创建
        File dir = new File(filePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName = drawNo+"-result.png";

        try {

            Cell.style0(lotteryName,last15List,filePath,fileName);
            //PictureUtil.getInstance().createImages(last15List, filePath, fileName);

            String bucketName = lotteryName.toLowerCase()+"-bot-draw-result";
            if(!minioUtils.bucketExists(bucketName)){
                minioUtils.createBucket(bucketName);
            }

            minioUtils.putObject(bucketName,fileName,filePath+fileName);

            List<BotUser> botUserList = botUserService.list();
            for (BotUser botUser : botUserList) {
                if (botUser.getStatus() == 1 && null!=botUser.getLotteryType() && (botUser.getLotteryType()==3 || botUser.getLotteryType()==lotteryType)) {
                    ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser.getId(), msg1);
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));

                    String fileUrl = null;
                    List<Dictionary> dicList = dictionaryDAO.selectList(new QueryWrapper<Dictionary>().eq("code","MinioUrl").eq("type","system"));
                    if (dicList.size() > 0){
                        Dictionary dictionary = dicList.get(0);
                        fileUrl = dictionary.getValue()+"/"+bucketName+"/"+fileName;
                    }
                    ChatRoomMsg toMsg1 = chatRoomMsgService.createMsg(botUser.getId(), fileUrl,1);
                    chatRoomMsgService.save(toMsg1);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg1));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("===============【"+lotteryName+"】"+drawNo+"期推送【开奖结果】异常");
            //System.out.println("===============【3D】"+drawNo+"期推送【开奖结果】异常");
        }

    }


    /**
     * 推送开关盘状态
     * @param openStatus 开关盘状态
     */
    public void pushDrawStatus(Integer openStatus,Integer lotteryType,String lotteryName){
        String msg = openStatus==1?"【"+lotteryName+"】^^★★★开始-上课★★★":"【"+lotteryName+"】^^★★★停止-上课★★★";
        List<BotUser> botUserList = botUserService.list();
        if(null!=botUserList){
            for (BotUser botUser : botUserList) {
                if (botUser.getStatus() == 1 && null!=botUser.getLotteryType() && (botUser.getLotteryType()==3 || botUser.getLotteryType()==lotteryType)) {
                    ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser.getId(), msg);
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));
                }
            }
        }
    }


    /**
     * 检测中奖后停止执行的定投任务
     * @param dtTaskId
     * @param drawMoney
     */
    public void checkDingTouTask(String dtTaskId,BigDecimal drawMoney){
        PlayerFixedBuy playerFixedBuy = playerFixedBuyService.getById(dtTaskId);
        if(null!=playerFixedBuy && playerFixedBuy.getTaskStatus()!=-1){
            if(0==playerFixedBuy.getCircleType()){ //中奖后停止
                playerFixedBuy.setTaskStatus(-1);
                playerFixedBuy.setEndTime(new Date());
                playerFixedBuy.setStopReason("中奖:"+drawMoney.stripTrailingZeros().toPlainString());
                playerFixedBuyService.updateById(playerFixedBuy);
            }
        }
    }

}
