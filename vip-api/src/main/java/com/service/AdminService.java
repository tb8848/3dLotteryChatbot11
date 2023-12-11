package com.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Admin;
import com.dao.AdminDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService extends ServiceImpl<AdminDAO, Admin> {

    @Autowired
    private AdminDAO adminDAO;

}
