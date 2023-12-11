package com.config.mybatisplus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface MyBaseMapper  <T> extends BaseMapper<T> {
    //@TableField(fill = FieldFill.DEFAULT)
    int insertBatchSomeColumn(List<T> entityList);
}
