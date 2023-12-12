package com.action;

import com.auth.AuthContext;
import com.auth.AuthInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.Admin;
import com.beans.ResponseBean;
import com.config.NoLogin;
import com.service.*;
import com.util.*;
import io.swagger.annotations.ApiOperation;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminAction {

    @Autowired
    private AdminService adminService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AuthContext authContext;

    /**
     * 超级管理员登录(权限)
     * @param username  用户名
     * @param password  密码
     * @return
     */
    @RequestMapping("/adminLogin")
    @NoLogin
    public ResponseBean adminLogin(HttpServletRequest request, String username, String password, @RequestHeader(value = "lang") String lang) {
        if(StringUtil.isNull(username) || StringUtil.isNull(password)){
            return ResponseBean.error("admin.login.accountNotNull");
        }
        //获取ip
        String ip = IpUtil.getIpAddr(request);

        String rkey = "3d-chatbot-"+ip;

        //从redis里取登录失败次数
        BoundValueOperations b = redisTemplate.boundValueOps(rkey);
        if ("-1".equals(b.get())) {
            return new ResponseBean(500, "admin.login.maxLoginCount", new Object[]{b.getExpire().intValue()});
        }

        if (!redisTemplate.hasKey(rkey)) {
            b.set("1");
            b.expire(60, TimeUnit.SECONDS);
            //登录失败次数小于十，次数加一
        } else if (Integer.parseInt(b.get().toString()) < 10) {
            int tmp = Integer.parseInt(b.get().toString());
            b.set(String.valueOf(++tmp));
            b.expire(60, TimeUnit.SECONDS);
            //大于十，参数为-1,并一分钟内禁止登录
        } else {
            b.set("-1");
            b.expire(60, TimeUnit.SECONDS);
            return new ResponseBean(500, "admin.login.maxLoginCount", new Object[]{b.getExpire().intValue()});
        }

        String pwd = PasswordUtil.jiami(password);
        Admin admin = adminService.adminLogin(username, pwd);
        if (admin == null) {
            return ResponseBean.error("admin.login.accountWrong");
        }
        if (admin.getStatus() != 1) {
            return ResponseBean.error("admin.login.accountEnable");
        } else{
            //修改登录IP
            admin.setUpdateUserIp(ip);
            admin.setOnlineStatus(1);
            admin.setAppOnlineStatus(1);
            admin.setPcOnlineStatus(1);
            if(null == admin.getMultiLogin()){
                admin.setMultiLogin(1);
            }else{
                admin.setMultiLogin(admin.getMultiLogin()+1);
            }
            adminService.updateById(admin);

            Map map = new HashMap();
            map.put("token", JwtUtil.generToken(admin.getId(), null, null));
            map.put("user", admin);
            map.put("id", admin.getId());
            map.put("loginRole",1);
            map.put("initPwd", admin.getInitPwdUpdate());
            return new ResponseBean(200, "成功", map);
        }
    }

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    @RequestMapping("/getUser")
    public ResponseBean getUser(String id, @RequestHeader(value = "lang") String lang){
        Map result = new HashMap();
        Admin admin = adminService.getById(id);
        if(admin == null ){
            return ResponseBean.error("admin.resetPassword.unFindUser");
        }

        result.put("admin",admin);
        return new ResponseBean(0,"查询成功",result);
    }

    /**
     * 修改基本资料信息
     * @param admin
     * @return
     */
    @RequestMapping("/updateBasicUser")
    public ResponseBean updateBasicUser(@RequestBody Admin admin){
        if(StringUtil.isNull(admin.getId())){
            return ResponseBean.error("admin.resetPassword.unFindUser");
        }
        if(adminService.updateById(admin)){
            return new ResponseBean(0,1,"common.updateSuccess","");
        }
        return new ResponseBean(1,1,"common.operationSuccess","");
    }

    /**
     * 每5秒取session，判断是否登录
     */
    @RequestMapping("isLogin")
    public ResponseBean isLogin(@RequestHeader(value = "token") String token){
        String uId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uId)){
            return new ResponseBean(1,1,"admin.changePwd.unLogin",null);
        }else{
            Admin admin = adminService.getById(uId);
            if (null == admin) {
                return new ResponseBean(1,1,"admin.changePwd.unLogin",null);
            }
            return new ResponseBean(0,1,"已登录！", admin.getUsername());
        }
    }

    /**
     * 获取机器人管理员拥有彩种类型
     * @param token
     * @return
     */
    @RequestMapping("getLotteryTypeByAdmin")
    public ResponseBean getLotteryTypeByAdmin (@RequestHeader(value = "token") String token) {
        String uId = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uId)){
            return new ResponseBean(1,1,"admin.changePwd.unLogin",null);
        }else{
            Admin admin = adminService.getById(uId);
            if (null != admin) {
                return new ResponseBean(200,"成功", admin.getLotteryType(), true);
            }
        }
        return new ResponseBean(1,"获取失败", "", true);
    }

    /**
     * 修改管理员密码
     * @param password
     * @param newPassword
     * @param token
     * @return
     */
    @ApiOperation(value = "管理员修改密码")
    @RequestMapping("/adminChangePassword")
    public ResponseBean adminChangePassword(String password, String newPassword, @RequestHeader(value = "token" , required = false) String token) {

//        System.out.println("旧密码："+password+" 新密码："+newPassword+" token："+token);
        if(StringUtil.isNull(token)){
            return ResponseBean.error("admin.changePwd.unLogin");
        }
        try{
            JwtUtil.verifyToken(token) ;
        }catch(Exception e) {
            return ResponseBean.error("admin.changePwd.unLogin");
        }
        String uid = JwtUtil.getUsername(token);
        String pwd = PasswordUtil.jiami(newPassword);
        String res = adminService.adminChangePassword(uid, password, pwd);
        if (StringUtil.isNotNull(res)) {
            return ResponseBean.error(res);
        } else {
            return ResponseBean.ok("成功");
        }
    }
}
