package com.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.PlayerPointsRecord;
import com.service.PlayerPointsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Description:    聊天上下分，间隔申请时间后2分钟删除
 * @Auther: tz
 * @Date: 2023/6/12 15:20
 */
@Component
public class PlayerPointsRecordTask {

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    /**
     * 每分钟的第六秒执行
     */
    @Scheduled(cron = "6 * * * * ?")
    public void task(){
//        List<PlayerPointsRecord> pointsRecordList = playerPointsRecordService.list(new QueryWrapper<PlayerPointsRecord>().eq("authStatus",0));
        Date endTime = timeProcess(new Date(),-2);
        List<PlayerPointsRecord> pointsRecordList = playerPointsRecordService.list(new QueryWrapper<PlayerPointsRecord>().eq("authStatus",0).le("applyTime",endTime));
        if (!pointsRecordList.isEmpty()){
            for (PlayerPointsRecord pointsRecord : pointsRecordList){
//                Date endTime = timeProcess(pointsRecord.getApplyTime());
//                if (endTime.before(new Date())){
                    pointsRecord.setAuthStatus(-1);
                    playerPointsRecordService.updateById(pointsRecord);
//                }
            }
        }
    }

    /**
     * 将传入的时间加上n分钟并返回
     * @param date
     * @return
     */
    public Date timeProcess(Date date,int n) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(date);
        rightNow.add(Calendar.MINUTE, n);
//        rightNow.add(Calendar.MINUTE, +2); //加2分钟
        //rightNow.add(Calendar.HOUR,1);//加一个小时
//        rightNow.add(Calendar.HOUR,14);//加14个小时，测试能不能加到明天，能不能加到下个月（结果是都可以,而且不受30天、31天的影响）
        //rightNow.add(Calendar.DAY_OF_MONTH,1);//加一天
        //rightNow.add(Calendar.MONTH,1);//加一个月
        Date dt1 = rightNow.getTime();
        return dt1;
    }
}
