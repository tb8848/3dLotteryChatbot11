package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class Dictionary implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;      //主键
    private String name;    //字典名字
    private String code;    //字典code
    private String type;    //字典type
    private String remark;     //描述
    private String value;   //字典值
}
