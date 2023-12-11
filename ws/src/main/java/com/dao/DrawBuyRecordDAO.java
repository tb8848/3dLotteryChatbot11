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

//import com.config.mybatisplus.MyBaseMapper;

@DS(DataSourceConfig.SHARDING_DATA_SOURCE_NAME)
public interface DrawBuyRecordDAO extends BaseMapper<DrawBuyRecord> {


    //批量新增
    int batchAddBuyCode(List<DrawBuyRecord> list);
//
//
    void copyTable(@Param("no") String no);


    @Select("select * from xxx " + " ${ew.customSqlSegment}" + " and ((bai = #{bai} and shi = #{shi}) or (shi = #{shi} and ge = #{ge}) or (bai = #{bai} and ge = #{ge})) ")
    List<DrawBuyRecord> tx2DrawCode (@Param("ew") LambdaQueryWrapper wrapper, @Param("bai") String bai , @Param("shi") String shi, @Param("ge") String ge);

}
