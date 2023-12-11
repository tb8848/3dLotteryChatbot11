package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class PlaySetting {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 直选
     */
    private Integer zx;

    /**
     * 复式
     */
    private Integer fs;

    /**
     * 组三
     */
    private Integer z3;

    /**
     * 组六
     */
    private Integer z6;

    /**
     * 和数
     */
    private Integer hs;

    /**
     * 跨度
     */
    private Integer kd;

    /**
     * 独胆
     */
    private Integer dd;

    /**
     * 包选
     */
    private Integer bx;

    /**
     * 1d
     */
    private Integer d1;

    /**
     * 2d
     */
    private Integer d2;

    /**
     * 大小
     */
    private Integer dx;

    /**
     * 奇偶
     */
    private Integer jo;

    /**
     * 拖拉机
     */
    private Integer tlj;

    /**
     * 猜三同
     */
    private Integer c3t;

    /**
     * 组三和值
     */
    private Integer z3hz;

    /**
     * 组六和值
     */
    private Integer z6hz;

    /**
     * 直选和值
     */
    private Integer zxhz;

    /**
     * 组三胆拖
     */
    private Integer z3dt;

    /**
     * 组六胆拖
     */
    private Integer z6dt;

    /**
     * 猜1D
     */
    private Integer c1d;

    /**
     * 猜2D
     */
    private Integer c2d;
}
