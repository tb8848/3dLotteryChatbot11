package com.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicSettingVo implements Serializable {
    /**
     * 开盘密码
     */
    private String openPanPwd;

    /**
     * 显示可下金额：1选择离停盘、0不选择离停盘
     */
    private Integer showBetBalance;

    /**
     * 显示可下金额多少分钟禁止
     */
    private Integer showBetBalanceMinute;

    /**
     * 前台默认设置：0显示彩种、1小票打印
     */
    private Integer printOrShowLottery;

    /**
     * 退码：1批量退码、0单个退码
     */
    private Integer batchOrSingleCancel;

    /**
     * 赔率：0转换赔率、1实际赔率
     */
    private Integer realOrTransformOdds;

    /**
     * 二字定栏目离停盘多少分钟禁止
     */
    private Integer twoFixForbiddenMinute;

    /**
     * 会员历史账单显示多少期
     */
    private Integer vipHistoryBillShow;

    /**
     * 赔率上限小于多少变色
     */
    private Integer oddsChangeColor;

    /**
     * 在线分钟开盘
     */
    private Integer onlineMinuteOpen;

    /**
     * 在线分钟停盘
     */
    private Integer onlineMinuteStop;

    /**
     * 在多少分钟内可以退码
     */
    private Integer cancelBetMinute;

    /**
     * 离停盘多少分钟
     */
    private Integer batchLimitMinute;

    /**
     * 离停盘多少分钟快选里批量发送限制为多少个码
     */
    private Integer batchLimitCount;

    /**
     * 多少分钟快选和txt导入开放
     */
    private Integer openQuickTxt;

    /**
     * 单注和单项关闭：0不关闭、1关闭
     */
    private Integer oneBetOrOneItemClose;

    /**
     * 下级月报表显示多少期，0为无限制
     */
    private Integer lowerMonthReportShow;

    /**
     * 新增会员信用额限制
     */
    private String newVipCreditLimit;

    /**
     * 限制股东及下级修改赚水：0不限制、1限制
     */
    private Integer shareHolderLowerReturnWaterLimit;

    /**
     * 大股东报表显示福利：0不显示、1显示
     */
    private Integer bigShareHolderReportShowBenefit;

    /**
     * 限制大股东修改赚水：0不限制、1限制
     */
    private Integer limitBigShareHolderModifyWater;

    /**
     * 一定位
     */
    private Integer colorYiZiDing;

    /**
     * 二定位
     */
    private Integer colorErZiDing;

    /**
     * 三定位
     */
    private Integer colorSanZiDing;

    /**
     * 四定位
     */
    private Integer colorSiZiDing;

    /**
     * 二字现
     */
    private String colorErZiXian;

    /**
     * 三字现
     */
    private String colorSanZiXian;

    /**
     * 四字现
     */
    private String colorSiZiXian;

    /**
     * 一定位
     */
    private Integer showYiZiDing;

    /**
     * 二定位
     */
    private Integer showErZiDing;

    /**
     * 三定位
     */
    private Integer showSanZiDing;

    /**
     * 四定位
     */
    private Integer showSiZiDing;

    /**
     * 二字现
     */
    private String showErZiXian;

    /**
     * 三字现
     */
    private String showSanZiXian;

    /**
     * 四字现
     */
    private String showSiZiXian;

    /**
     * 前台跑马灯内容
     */
    private String frontendMarquee;

    /**
     * 后台跑马灯内容
     */
    private String backendMarquee;

    /**
     * 临时公告
     */
    private String tempBulletin;

    /**
     * 临时公告开关：1打开、0关闭
     */
    private Integer tempBulletinOpen;

    /**
     * 定位置：0关闭、1开启
     */
    private Integer dingWeiZhi;

    /**
     * 合分：0关闭、1开启
     */
    private Integer heFen;

    /**
     * 不定位合分：0关闭、1开启
     */
    private Integer buDingWeiHeFen;

    /**
     * 值范围：0关闭、1开启
     */
    private Integer zhiFanWei;

    /**
     * 全转：0关闭、1开启
     */
    private Integer quanZhuan;

    /**
     * 排除：0关闭、1开启
     */
    private Integer paiChu;

    /**
     * 乘号位置：0关闭、1开启
     */
    private Integer chengHaoWeiZhi;

    /**
     * 复式：0关闭、1开启
     */
    private Integer fuShi;

    /**
     * 双重：0关闭、1开启
     */
    private Integer shuangChong;

    /**
     * 双双重：0关闭、1开启
     */
    private Integer shuangShuangChong;

    /**
     * 三重：0关闭、1开启
     */
    private Integer sanChong;

    /**
     * 四重：0关闭、1开启
     */
    private Integer siChong;

    /**
     * 二兄弟：0关闭、1开启
     */
    private Integer erXiongDi;

    /**
     * 三兄弟：0关闭、1开启
     */
    private Integer sanXiongDi;

    /**
     * 四兄弟：0关闭、1开启
     */
    private Integer siXiongDi;

    /**
     * 对数：0关闭、1开启
     */
    private Integer duiShu;

    /**
     * 单：0关闭、1开启
     */
    private Integer dan;

    /**
     * 双：0关闭、1开启
     */
    private Integer shuang;

    /**
     * 上奖：0关闭、1开启
     */
    private Integer shangJiang;

    /**
     * 四字定：0关闭、1开启
     */
    private Integer siZiDing;

    /**
     * 固定位置：0关闭、1开启
     */
    private Integer guDingWeiZhi;

    /**
     * 大：0关闭、1开启
     */
    private Integer da;

    /**
     * 小：0关闭、1开启
     */
    private Integer xiao;

    /**
     * 开盘密码
     */
    private String openPwd;

    /**
     * 管理员密码是否启用:0否、1是
     */
    private Integer isAdminPwd;

    /**
     * 是否支持包牌下注开关：0否、1是
     */
    private Integer bpBetFlag;

    private static final long serialVersionUID = 1L;
}
