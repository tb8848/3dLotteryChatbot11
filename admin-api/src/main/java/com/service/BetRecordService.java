package com.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.Draw;
import com.beans.DrawBuyRecord;
import com.beans.Player;
import com.config.I18nUtils;
import com.config.RequestDataHelper;
import com.dao.DrawBuyRecordDAO;
import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class BetRecordService extends ServiceImpl<DrawBuyRecordDAO, DrawBuyRecord> {
    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;

    @Autowired
    private DrawService drawService;

    @Autowired
    private I18nUtils i18nUtils;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserService botUserService;

    /**
     * 获取总货明细数据
     * @param username 账号
     * @param buyCodes 号码
     * @param lieType 列出：0赔率、1金额、2退码
     * @param start 开始值，根据列出查询，如列出为0，则根据赔率范围查询，如果列表为2退码，则同列出1 金额查询
     * @param end 结束值，同开始值
     * @param lotterySettingType 分类：包括号码类别，购买路径
     * @param drawId 期号
     * @param drawStatus 中奖状态：0未中奖、1中奖
     * @param userId 越级操作->会员层级->点击会员名称传递过来
     * @return
     */
    public Map<String, Object> betRecordList (Integer page, Integer limit, String username, String buyCodes, Integer lieType, BigDecimal start, BigDecimal end,
                                                      String lotterySettingType, String drawId, String drawStatus, String userId, String botName, String lang) {
        Map<String, Object> resultMap = new HashMap<>();
        Page p = new Page(page, limit);
        QueryWrapper<DrawBuyRecord> query = new QueryWrapper<>();
        Draw draw = drawService.getNewDrawInfo();
        String dwId = "";
        if (StringUtil.isNull(drawId)) {
            dwId = String.valueOf(draw.getDrawId());
        }else {
            dwId = drawId;
        }
        // 会员用户
        if (StringUtil.isNotNull(userId)) {
            query.eq("c.vipId", userId);
        }

        query.in("huizongFlag", Lists.newArrayList("0", "-1"));

        if (StringUtil.isNotNull(drawStatus)) {
            query.eq("drawStatus", 1);
            query.eq("backCodeFlag", 0);
        }

        if (StringUtil.isNotNull(buyCodes)) {
            query.eq("buyCodes", buyCodes);
        }

        if (StringUtil.isNotNull(username)) {
            LambdaQueryWrapper<Player> adminQuery = new LambdaQueryWrapper();
            adminQuery.eq(Player::getNickname, username);
            adminQuery.last("limit 1");
            Player admin = playerService.getOne(adminQuery);
            if (null == admin) {
                query.eq("c.vipId", "");
            }else {
                query.eq("c.vipId", admin.getId());
            }
        }

        if (StringUtil.isNotNull(botName)) {
            LambdaQueryWrapper<BotUser> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(BotUser::getLoginName, botName);
            queryWrapper.last("limit 1");
            BotUser botUser = botUserService.getOne(queryWrapper);
            if (null == botUser) {
                query.eq("c.parentsUserId", "");
            }else {
                query.eq("c.parentsUserId", botUser.getId());
            }
        }

        if (lieType != null) {
            if (lieType == 0) {
                if (start == null && end != null) {
                    query.le("peiRate", end);
                }else if (start != null && end == null) {
                    query.ge("peiRate", start);
                }else if (start != null && end != null) {
                    query.ge("peiRate", start);
                    query.le("peiRate", end);
                }
            }else if (lieType == 1) {
                buildLieQuery(start, end, query);
            }else if (lieType == 2) {
                query.eq("backCodeFlag", 1);
                buildLieQuery(start, end, query);
            }
        }

        if (StringUtil.isNotNull(lotterySettingType)) {
            Pattern pattern = Pattern.compile("-?[0-9]+(.[0-9]+)?");
            if(pattern.matcher(lotterySettingType).matches()){
                Integer typeId = Integer.valueOf(lotterySettingType);
                if (typeId > 0) {
                    query.eq("lotterSettingId", lotterySettingType);
                }
            }else {
                if (lotterySettingType.equals("a")) {
                    query.eq("lotteryMethodId", "1");
                }else if (lotterySettingType.equals("b")) {
                    query.eq("lotteryMethodId", "3");
                }else if (lotterySettingType.equals("c")) {
                    query.eq("lotteryMethodId", "4");
                }else if (lotterySettingType.equals("d")) {
                    query.eq("lotteryMethodId", "6");
                }else if (lotterySettingType.equals("e")) {
                    query.eq("lotteryMethodId", "7");
                }else if (lotterySettingType.equals("f")) {
                    query.eq("lotteryMethodId", "8");
                }else if (lotterySettingType.equals("g")) {
                    query.eq("lotteryMethodId", "5");
                }
            }
        }

        String finalDwId = dwId;
        RequestDataHelper.setRequestData(new HashMap<String, Object>() {{
            put("qihao", finalDwId);
        }});

        Locale locale = Locale.CHINA;
        if (lang.equals("th")) {
            locale = new Locale("th");
        }

        page = (page - 1) * limit;
        List<DrawBuyRecord> iPage = drawBuyRecordDAO.dynamicQuery(query, finalDwId, page, limit);
        if (!iPage.isEmpty()) {
            long totalCount = iPage.stream().filter(drawBuyRecord -> drawBuyRecord.getBackCodeFlag() == 0).count();
            resultMap.put("totalCount", totalCount);
            BigDecimal totalAmount = iPage.stream().filter(drawBuyRecord -> drawBuyRecord.getBackCodeFlag() == 0).map(DrawBuyRecord::getBuyMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            resultMap.put("totalAmount", totalAmount);
            BigDecimal totalDrawAmount = iPage.stream().filter(drawBuyRecord -> null != drawBuyRecord.getDrawMoney() && drawBuyRecord.getBackCodeFlag() == 0).map(DrawBuyRecord::getDrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            resultMap.put("totalDrawAmount", totalDrawAmount);
            BigDecimal totalReturnWater = BigDecimal.ZERO;
            BigDecimal ssxx = BigDecimal.ZERO;

            String bp = i18nUtils.getKey("bpGroup.bpCode", locale);
            String group = i18nUtils.getKey("bpGroup.group", locale);
            for (DrawBuyRecord drawBuyRecord : iPage) {
                if (StringUtil.isNotNull(drawBuyRecord.getBpGroup())) {
                    if (lang.equals("th")) {
                        drawBuyRecord.setBpGroup(drawBuyRecord.getBettingRuleTh() +  bp + drawBuyRecord.getBpGroup() + group);
                    }else {
                        drawBuyRecord.setBpGroup(drawBuyRecord.getBettingRule() +  bp + drawBuyRecord.getBpGroup() + group);
                    }
                }

                if (drawBuyRecord.getPretexting() == 1) {
                    drawBuyRecord.setBetType("假托");
                }else {
                    if (drawBuyRecord.getEatPrize() == 1) {
                        drawBuyRecord.setBetType("吃码");
                    }else {
                        drawBuyRecord.setBetType("报网");
                    }
                }

                if (null != drawBuyRecord.getBuyType()) {
                    if (drawBuyRecord.getBuyType() == 1) {
                        drawBuyRecord.setLotteryName("福彩3D");
                    }else {
                        drawBuyRecord.setLotteryName("排列三");
                    }
                }

                if (StringUtil.isNotNull(drawBuyRecord.getIp())) {
                    String ip = drawBuyRecord.getIp();
                    int x = ip.indexOf(".",ip.indexOf(".")+1);
                    drawBuyRecord.setIp(ip.substring(0,x)+".*.*");
                }
            }
            resultMap.put("total", iPage.stream().filter(drawBuyRecord -> drawBuyRecord.getBackCodeFlag() == 0).count());
        }

        Long count = drawBuyRecordDAO.dynamicQueryCount(query, finalDwId, page, limit);
        resultMap.put("totalSize", count);
        resultMap.put("dataList", iPage);
        return resultMap;
    }

    private void buildLieQuery(BigDecimal start, BigDecimal end, QueryWrapper<DrawBuyRecord> query) {
        if (start == null && end != null) {
            query.le("buyMoney", end);
        }else if (start != null && end == null) {
            query.ge("buyMoney", start);
        }else if (start != null && end != null) {
            query.ge("buyMoney", start);
            query.le("buyMoney", end);
        }
    }

    /**
     * 根据机器人ID 查询当日总投和总盈亏
     * @param userId
     * @return
     */
    public DrawBuyRecord getBetRecordByBotUserId (String userId) {
        Draw draw = drawService.getLastDrawInfo();
        Date currDate = DateUtil.date(); //当前时间
        Date startDate = DateUtil.beginOfDay(currDate); //当前时间的开始时间
        Date endDate = DateUtil.endOfDay(currDate); //当前时间的结束时间
        QueryWrapper<DrawBuyRecord> qw = new QueryWrapper();
        qw.select("sum(buyMoney) as buyMoney,sum(drawMoney) as drawMoney");
        qw.lambda().eq(DrawBuyRecord::getDrawId,draw.getDrawId())
                .eq(DrawBuyRecord::getParentsUserId,userId)
                .ge(DrawBuyRecord::getCreateTime, startDate)
                .le(DrawBuyRecord::getCreateTime, endDate);
        qw.last("limit 1");
        return drawBuyRecordDAO.selectOne(qw);
    }
}
