package com.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.LotterySetting;
import com.dao.LotterySettingDAO;
import org.springframework.stereotype.Service;

@Service
public class LotterySettingService extends ServiceImpl<LotterySettingDAO, LotterySetting> {
}
