package com.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beans.DrawBuyRecord;
import com.config.DataSourceConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS(DataSourceConfig.SHARDING_DATA_SOURCE_NAME)
public interface Draw3DBuyRecordDAO extends BaseMapper<DrawBuyRecord> {


    //批量新增
    int batchAddBuyCode(List<DrawBuyRecord> list);
//
//
//    void copyTable(@Param("no") String no);


    @Select("select * from xxx " + " ${ew.customSqlSegment}" + " and ((bai = #{bai} and shi = #{shi}) or (shi = #{shi} and ge = #{ge}) or (bai = #{bai} and ge = #{ge})) ")
    List<DrawBuyRecord> tx2DrawCode (@Param("ew") LambdaQueryWrapper wrapper, @Param("bai") String bai , @Param("shi") String shi, @Param("ge") String ge);

//
//
//
//    //此查询不会对记录进行降序排序
//    @Select("select d.id,d.buyCodes,d.vipId,d.drawId,d.buyMoney,d.buyAmount,d.peiRate,d.drawStatus,d.createTime,d.printFlag," +
//            "d.printId,d.backCodeFlag,d.lotterSettingId,d.hasOneFlag,d.backCodeStatus,d.drawLotteryFlag,d.codesFrom," +
//            "d.printCacheId,d.prizeTakeFlag,d.huishui,d.drawMoney,d.backCodeTime,d.buyType,d.baopaiId,d.parentsHuiShui," +
//            "d.parentsHuiShuiMoney,d.parentsLanhuoMoney,d.parentsUserId,d.paresntLanhuoValue," +
//            "d.bpSettingId,bpGroup,e.printTime as printTime,d.parentPeiRate,d.param1,d.param2,d.param3,d.huizongName,d.huizongFlag," +
//            "e.batchNo as batchNo,d.ip " +
//            "from xxx d " +
//            "join (select c.id as id,tpc.batchNo as batchNo,tpc.printTime as printTime from xxx c left join 3d_lottery.`ticket_print_cache` tpc on c.printCacheId = tpc.id" + " ${ew.customSqlSegment}"+" and c.vipId=#{vipId} limit #{page},#{size}) e on e.id=d.id "
//            //+"left join prize_worm.`ticket_print_cache` tpc on d.printCacheId = tpc.id "
//    )
//    List<DrawBuyRecord> dynamicQuery(@Param("ew") QueryWrapper wrapper,
//                                     @Param("drawId") String drawId, @Param("vipId") String vipId, @Param("page")int page, @Param("size")int limit);
//
//
//
//
//
//    @Select("select count(1) from xxx d left join 3d_lottery.`ticket_print_cache` tpc on d.printCacheId = tpc.id " + " ${ew.customSqlSegment}" + " and d.vipId=#{vipId}")
//    Long getTotals(@Param("ew") QueryWrapper wrapper,@Param("drawId") String drawId,@Param("vipId") String vipId);
//
//
//
//
//    //此查询不会对记录进行降序排序
//    @Select("select d.id,d.buyCodes,d.vipId,d.drawId,d.buyMoney,d.buyAmount,d.peiRate,d.drawStatus,d.createTime,d.printFlag," +
//            "d.printId,d.backCodeFlag,d.lotterSettingId,d.hasOneFlag,d.backCodeStatus,d.drawLotteryFlag,d.codesFrom," +
//            "d.printCacheId,d.prizeTakeFlag,d.huishui,d.drawMoney,d.backCodeTime,d.buyType,d.baopaiId,d.parentsHuiShui," +
//            "d.parentsHuiShuiMoney,d.parentsLanhuoMoney,d.parentsUserId,d.paresntLanhuoValue," +
//            "d.bpSettingId,bpGroup,tpc.printTime as printTime,d.parentPeiRate,d.param1,d.param2,d.param3,d.huizongName,d.huizongFlag,d.lotteryMethodId," +
//            "tpc.batchNo as batchNo,d.ip " +
//            "from xxx d " +
//            "join (select id from xxx c " + " ${ew.customSqlSegment}"+" and c.vipId=#{vipId} limit #{page},#{size}) e on e.id=d.id "+
//            "left join 3d_lottery.`ticket_print_cache` tpc on d.printCacheId = tpc.id "
//    )
//    List<DrawBuyRecord> dynamicQueryForApp(@Param("ew") QueryWrapper wrapper,
//                                           @Param("drawId") String drawId, @Param("vipId") String vipId, @Param("page")int page, @Param("size")int limit);
//
//
//    @Select("select count(1) from xxx d " + " ${ew.customSqlSegment}" + " and d.vipId=#{vipId}")
//    Long getTotalsForApp(@Param("ew") QueryWrapper wrapper,@Param("drawId") String drawId,@Param("vipId") String vipId);
//
//
//
//
//
//
//
//    @Select("select d.id,d.buyCodes,d.vipId,d.drawId,d.buyMoney,d.buyAmount,d.peiRate,d.drawStatus,d.createTime,d.printFlag," +
//            "d.printId,d.backCodeFlag,d.lotterSettingId,d.hasOneFlag,d.backCodeStatus,d.drawLotteryFlag,d.codesFrom," +
//            "d.printCacheId,d.prizeTakeFlag,d.huishui,d.drawMoney,d.backCodeTime,d.buyType,d.baopaiId,d.parentsHuiShui," +
//            "d.parentsHuiShuiMoney,d.parentsLanhuoMoney,d.parentsUserId,d.paresntLanhuoValue," +
//            "d.bpSettingId,bpGroup,d.parentPeiRate,d.param1,d.param1,d.param2,d.param3,d.huizongName,d.huizongFlag,d.lotteryMethodId " +
//            "from xxx d " +
//            "join (select id from xxx c where backCodeFlag=0 and huizongFlag in (0,-1) and printCacheId=#{printCacheId} limit #{page},#{size}) e on e.id=d.id "
//    )
//    List<DrawBuyRecord> getPrintCacheCodesByPage(@Param("drawId") String drawId, @Param("printCacheId") String printCacheId,
//                                                 @Param("page")int page, @Param("size")int limit);
//
//
//
//    @Select("select count(1) from xxx where backCodeFlag=0 and printCacheId=#{printCacheId}")
//    Long getPrintCacheTotals(@Param("drawId") String drawId,@Param("printCacheId") String printCacheId);

}
