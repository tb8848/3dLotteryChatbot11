package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 下注信息
 */
@Data
public class DrawBuyRecord {


    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 下注号码
     */
    private String buyCodes;

    /**
     * 会员ID
     */
    private String vipId;

    /**
     * 开奖期号ID
     */
    private String drawId;

    /**
     * 下注金额
     */
    private BigDecimal buyMoney;

    /**
     * 下注数量，计算值
     */
    private Integer buyAmount;

    /**
     * 赔率
     */
    private BigDecimal peiRate;

    /**
     * 是否中奖：0否，1是
     */
    private Integer drawStatus;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否已打印，0：未打印；1：已打印
     */
    private Integer printFlag = 0;

    /**
     * 打印ID
     */
    private String printId;


    /**
     * 是否退码，0：否；1：是
     */
    private Integer backCodeFlag = 0;

    /**
     *
     */
    private String lotterSettingId;

    /**
     * 是否选择"现", 0:否，1：是
     */
    private Integer hasOneFlag=0;


    /**
     * 是否可以退码，0：否；1：是
     */
    private Integer backCodeStatus = 1;

    /**
     * 是否已开奖标识：0否，1是
     */
    private Integer drawLotteryFlag = 0;

    /**
     * 号码来源(购买路径): 1 app端 快打，2 app端 快选，3网页端 快打,4网页端 快选，5：网页端 导入，6：网页端 二字定，7：汇总表,-1:包牌号码
     */
    private Integer codesFrom;

    /**
     *
     */
    private String printCacheId;

    /**
     * 是否已兑奖
     */
    private Integer prizeTakeFlag = 0;

    /**
     * 回水值
     */
    private BigDecimal huishui;

    /**
     * 中奖金额
     */
    private BigDecimal drawMoney;

    /**
     * 退码时间
     */
    private Date backCodeTime;

    /**
     * 会员账号
     */
    @TableField(exist = false)
    private String vipName;

    /**
     * 下线回水
     */
    @TableField(exist = false)
    private BigDecimal downLineHuiShui;

    /**
     * 自己回水
     */
    @TableField(exist = false)
    private BigDecimal selfHuiShui;

    /**
     * 购买方式：0：报网；1：吃奖，2:假托
     */
    private Integer buyType;

    /**
     * 包牌ID
     */
    private String baopaiId="0";

    /**
     * 父级（所有）回水值，多个用/符号分割
     */
    private String parentsHuiShui;

    /**
     * 父级（所有）回水金额，多个用/符号分割
     */
    private String parentsHuiShuiMoney;

    /**
     * 父级（所有）拦货占成值，多个用/符号分割
     */
    private String parentsLanhuoMoney;

    /**
     * 父级（所有）贡献占成值，多个用/符号分割
     */
    //private String parentsContributeMoney;

    /**
     * 父级用户ID，多个用斜杠“/"分隔
     */
    private String parentsUserId;

    /**
     * 父级用户ID的占成值
     */
    private String paresntLanhuoValue;

    /**
     * 号码全转标识，1：号码全转；0：不做处理
     */
    @TableField(exist = false)
    private Integer quanzhuan;

    /**
     * ip
     */
    private String ip;

    /**
     * 实收下线
     */
    @TableField(exist = false)
    private BigDecimal ssDownLine;

    /**
     * 实付上线
     */
    @TableField(exist = false)
    private BigDecimal sfUpLine;

    /**
     * 赚水
     */
    @TableField(exist = false)
    private BigDecimal zhuanShui;

    /**
     * 包牌组数
     */
    @TableField(exist = false)
    private String baoPai;

    /**
     * 自己赔率，总监后台则是总监赔率
     */
    @TableField(exist = false)
    private String selfPeiRate;

    /**
     * 包牌设置ID
     */
    private String bpSettingId;

    /**
     * 拦货-回水
     */
    @TableField(exist = false)
    private BigDecimal returnWater;

    /**
     * 拦货-盈亏
     */
    @TableField(exist = false)
    private BigDecimal profitLossMoney;

    /**
     * 拦货-赔率上限
     */
    @TableField(exist = false)
    private String peiRateUpper;

    /**
     * 拦货-占成
     */
    @TableField(exist = false)
    private Integer zhanCheng;

    /**
     * 拦货-实货金额
     */
    @TableField(exist = false)
    private BigDecimal shMoney;

    /**
     * 总货明细-合计笔数
     */
    @TableField(exist = false)
    private Integer totalCount;

    /**
     * 总货明细-合计下注金额
     */
    @TableField(exist = false)
    private BigDecimal totalAmount;

    /**
     * 总货明细-合计中奖
     */
    @TableField(exist = false)
    private BigDecimal totalDrawAmount;

    /**
     * 总货明细-合计回水
     */
    @TableField(exist = false)
    private BigDecimal totalReturnWater;

    /**
     * 总货明细-合计实收下线
     */
    @TableField(exist = false)
    private BigDecimal totalSsXx;

    /**
     * 会员回水值，用于计算各层级回水
     */
    @TableField(exist = false)
    private BigDecimal hsValue;

    /**
     * 包牌组数
     */
    private String bpGroup;

    /**
     * 实货明细-盈亏
     */
    @TableField(exist = false)
    private BigDecimal shProfitLoss;

    /**
     * 总货中奖小票时间
     */
    @TableField(exist = false)
    private Date printTime;

    /**
     * 父级（所有）赔率，多个用/符号分割
     */
    private String parentPeiRate;

    /**
     * 预留字段1 : 总金额
     */
    private String param1;
    /**
     * 预留字段2:和值，多个之间用逗号分割
     */
    private String param2;
    /**
     * 预留字段3，购买赔率，存在多重情况
     */
    private String param3;

    /**
     * 小票编号
     */
    @TableField(exist = false)
    private String batchNo;

    /**
     * 号码来源
     */
    @TableField(exist = false)
    private String codeFormName;

    /**
     * 号码类别名称
     */
    @TableField(exist = false)
    private String bettingRule;

    /**
     * 号码类别名称泰语
     */
    @TableField(exist = false)
    private String bettingRuleTh;

    /**
     * 此字段控制购买记录是否变色显示
     * 1:是，0：否
     */
    @TableField(exist = false)
    private Integer showDiffColor=0;

    /**
     * 号码已购买金额
     */
    @TableField(exist = false)
    private BigDecimal hasBuyMoney;

    /**
     * 号码当前的赔率
     */
    @TableField(exist = false)
    private String nextBuyOdds;

    private String bai;

    private String shi;

    private String ge;

    private String lotteryMethodId;

    private Integer huizongFlag = 1;

    private String huizongName;

    /**
     * 号码的简称
     * 仅适用于大,小,奇,偶,拖拉机,三同号，分别对应:da,xiao,ji,ou,tlj,c3t
     */
    @TableField(exist = false)
    private String buyCodeShortName;

    /**
     * 玩家名称
     */
    @TableField(exist = false)
    private String playerName;

    /**
     * 玩家所属机器人
     */
    @TableField(exist = false)
    private String botName;

    /**
     * 下注类型：报网，吃码，假托
     */
    @TableField(exist = false)
    private String betType;

    /**
     * 是否假托，0：否，1：是
     */
    @TableField(exist = false)
    private Integer  pretexting = 0;

    /**
     * 是否报网，0：否，1：是
     */
    @TableField(exist = false)
    private Integer  reportNet = 1;

    /**
     * 是否吃奖，0：否，1：是
     */
    @TableField(exist = false)
    private Integer  eatPrize = 0;


    @TableField(exist = false)
    private String lotteryName;
}
