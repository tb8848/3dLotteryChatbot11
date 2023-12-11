package com.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.DrawBuyRecord;
import com.config.DataSourceConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS(DataSourceConfig.SHARDING_DATA_SOURCE_NAME)
public interface DrawBuyRecordDAO extends BaseMapper<DrawBuyRecord> {

    @Select("select d.id,d.buyCodes,d.vipId,d.drawId,d.buyMoney,d.buyAmount,d.peiRate,d.drawStatus,d.createTime,d.printFlag," +
            "d.printId,d.backCodeFlag,d.lotterSettingId,d.hasOneFlag,d.backCodeStatus,d.drawLotteryFlag,d.codesFrom," +
            "d.printCacheId,d.prizeTakeFlag,d.huishui,d.drawMoney,d.backCodeTime,d.buyType,d.baopaiId,d.parentsHuiShui," +
            "d.parentsHuiShuiMoney,d.parentsLanhuoMoney,d.parentsUserId,d.paresntLanhuoValue,d.lotteryMethodId,d.huizongFlag," +
            "d.bpSettingId,d.bpGroup, d.parentPeiRate,d.huizongName,d.param2,d.param3,a.nickname as playerName, b.loginName as botName," +
            "ls.bettingRule as bettingRule,ls.bettingRuleTh as bettingRuleTh, ls.peiRate as selfPeiRate, d.ip, a.pretexting as pretexting, " +
            "a.reportNet as reportNet, a.eatPrize as eatPrize " +
            "from xxx d " +
            "join (select id from xxx c " + " ${ew.customSqlSegment}" + " and c.drawId = #{drawId}" + " limit #{page2}, #{size}) e on e.id = d.id " +
            "left join 3d_chatbot.`player` a on d.vipId = a.id " +
            "left join 3d_chatbot.`bot_user` b on a.botUserId = b.id " +
            "left join 3d_chatbot.`lottery_setting` ls on d.lotterSettingId = ls.id where a.userType in (1,2)"
    )
    List<DrawBuyRecord> dynamicQuery(@Param("ew") QueryWrapper wrapper, @Param("drawId") String drawId, @Param("page2") int page2 , @Param("size") int limit);

    @Select("select count(*)" +
            " from xxx c " +
            "left join 3d_chatbot.`player` a on c.vipId = a.id " +
            " ${ew.customSqlSegment}" + " and c.drawId = #{drawId} and a.userType in (1,2)"
    )
    Long dynamicQueryCount(@Param("ew") QueryWrapper wrapper, @Param("drawId") String drawId, @Param("page2") int page2 , @Param("size") int limit);

    void copyTable(@Param("no") String no);

}
