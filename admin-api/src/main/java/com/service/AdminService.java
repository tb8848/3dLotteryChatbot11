package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Admin;
import com.beans.BotUser;
import com.dao.AdminDAO;
import com.dao.BotUserDAO;
import com.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService extends ServiceImpl<AdminDAO, Admin> {

    @Autowired
    private AdminDAO adminDAO;

    /**
     * 超级管理员登录
     * @param username  用户名
     * @param password  密码
     * @return
     */
    public Admin adminLogin(String username, String password) {
        LambdaQueryWrapper<Admin> query = new LambdaQueryWrapper<>();
        query.eq(Admin::getUsername, username);
        query.eq(Admin::getPassword, password);
        query.last("limit 1");
        Admin admin = adminDAO.selectOne(query);
        return admin;
    }

    /**
     * 修改超级管理员密码
     * @param uid           用户ID
     * @param newPassword   新密码
     * @return
     */
    public String adminChangePassword(String uid, String password, String newPassword) {

        Admin admin = adminDAO.selectById(uid);
        if (admin == null){
            return "admin.changePwd.unLogin";
        }
        if (!admin.getPassword().equals(PasswordUtil.jiami(password))){
            return "updatePwd.oldPwdWrong";
        }
        long count = adminDAO.selectCount(new QueryWrapper<Admin>().eq("username",admin.getUsername()).eq("password",newPassword));
        if(count > 0){
            return "newPasswordNotOldPassword";
        }
        admin.setPassword(newPassword);
        admin.setInitPwdUpdate(1);
        adminDAO.updateById(admin);
        return "";
    }
}
