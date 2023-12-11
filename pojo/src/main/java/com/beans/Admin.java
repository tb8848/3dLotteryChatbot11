package com.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class Admin implements Serializable {

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态(1为启用  0为禁用  2暂停下注)
     */
    private Integer status;

    /**
     * 联系方式
     */
    private String phone;

    /**
     * 代码
     */
    private String nickName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 上级用户ID
     */
    private String parentId;

    /**
     * 上级用户角色
     */
    @TableField(exist = false)
    private String parentRoleName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色名称
     */
    @TableField(exist = false)
    private String  roleName;

    /**
     * 用户角色Id
     */
    @TableField(exist = false)
    private String  roleNameId;

    @TableField(exist = false)
    private String role;

    /**
     * 信用额度（或者现金)
     */
    private BigDecimal creditLimit;

    /**
     * 已用信用额度
     */
    private BigDecimal useCreditLimit;

    /**
     * 剩余信用额度
     */
    private BigDecimal surplusCreditLimit;

    /**
     * 信用额度类型：0一次性（用完后则终止会员下注），1可重复使用（每期开奖后重置）
     */
    private Integer creditLimitType;

    /**
     * 上级拦货占成
     */
    private Integer parentLanHuoUpperLimit;

    /**
     * 拦货占成
     */
    private Integer lanHuoUpperLimit;

    /**
     * 是否会员用户标识：0不是、1是
     */
    private Integer vipFlag;

    /**
     * 开盘密码
     */
    private String drawPassword;

    /**
     * 资金模式：0现金、1信用
     */
    private Integer fundMode;

    /**
     * 公司ID
     */
    private String companyId;

    /**
     * 修改用户id
     */
    private String updateUserId;

    /**
     * 修改用户ip
     */
    private String updateUserIp;

    /**
     * 初始密码修改状态
     */
    private Integer initPwdUpdate=0;

    /**
     * 是否开启谷歌验证：0未启用、1已启用
     */
    private Integer gaFlag = 0;

    /**
     * 贡献度占成上限
     */
    private Integer contributionUpperLimit;

    /**
     * 开盘密码是否启用为管理员密码：0否、1是
     */
    private Integer isAdminPwd;

    /**
     * 谷歌验证随机秘钥
     */
    private String secretKey;

    /**
     * 用户在线状态：1在线、0离线
     */
    private Integer onlineStatus;

    /**
     * 用户在线状态：1在线、0离线
     */
    private Integer appOnlineStatus;

    /**
     * 用户在线状态：1在线、0离线
     */
    private Integer pcOnlineStatus;

    /**
     * 修改者用户
     */
    @TableField(exist = false)
    private String  updateUserName;

    /**
     * 父级拦货占成
     */
    @TableField(exist = false)
    private Integer pLanHuoLimit;

    /**
     * 修改者角色
     */
    @TableField(exist = false)
    private String updateRoleName;

    /**
     * 修改者角色（泰语）
     */
    @TableField(exist = false)
    private String updateRoleNameTh;

    /**
     * 上级用户角色（泰语）
     */
    @TableField(exist = false)
    private String parentRoleNameTh;

    /**
     * 角色名称（泰语）
     */
    @TableField(exist = false)
    private String roleNameTh;

    /**
     * 信用额度
     */
    @TableField(exist = false)
    private String creditLimitStr;

    private static final long serialVersionUID = 1L;

    /**
     * 记录顶层账号的多端登录值
     * 用于判断账号的最终上下限状态
     */
    private Integer multiLogin = 0;

    /**
     * 归属彩票类型，1:3D，2：P3,3:3D+P3
     */
    private Integer lotteryType;
}
