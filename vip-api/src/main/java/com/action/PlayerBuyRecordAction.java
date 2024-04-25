package com.action;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.*;
import com.config.NoLogin;
import com.service.*;
import com.util.JwtUtil;
import com.util.StringUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;

import static com.util.StringUtil.convertToBase64;

@RestController
@RequestMapping(value = "/bot/player/buy")
public class PlayerBuyRecordAction {

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private MinioClient minioClient;

    /**
     * 玩家列表
     * @param token
     * @param request
     * @return
     */
    @GetMapping(value = "/list")
    public ResponseBean list(@RequestHeader(value = "token")String token,
                             Integer pageNo,Integer pageSize,String playerId,Integer buyType,Integer dayRange,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        if(null == pageNo){
            pageNo = 1;
        }
        if(null == pageSize){
            pageSize = 10;
        }
        if(null == buyType){
            buyType = -1;
        }
        if(null == dayRange){
            dayRange = 1;
        }

        String startTime = null;
        String endTime = null;
        if(dayRange==1){
            //当天
            startTime = DateUtil.beginOfDay(new Date()).toString();
            endTime = DateUtil.endOfDay(new Date()).toString();
        }else if(dayRange==-1){
            //昨天
            startTime = DateUtil.beginOfDay(DateUtil.yesterday()).toString();
            endTime = DateUtil.endOfDay(DateUtil.yesterday()).toString();
        }else if(dayRange==7){
            //本周
            startTime = DateUtil.beginOfWeek(new Date()).toString();
            endTime = DateUtil.endOfWeek(new Date()).toString();
        }

        Player player = null;
        if(StringUtil.isNotNull(playerId)){
            player = playerService.getById(playerId);
        }

        try{
            IPage<PlayerBuyRecord> pageData = playerBuyRecordService.getByPage(pageNo,pageSize,buyType,playerId,startTime,endTime,uid);
            List<PlayerBuyRecord> dataList = pageData.getRecords();
            for(PlayerBuyRecord item : dataList){
                if(null!=player){
                    item.setPlayer(player);
                }else{
                    Player p = playerService.getById(item.getPlayerId());
                    if (StringUtil.isNotNull(p.getHeadimg())){
                        // 获取对象的InputStream
                        InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket("3d-robot-img").object(player.getHeadimg()).build());
                        // 将图像转换为Base64编码
                        String base64Image = convertToBase64(inputStream);
                        p.setHeadimg("data:image/jpeg;base64,"+base64Image);
                    }
                    item.setPlayer(p);
                }
            }
            pageData.setRecords(dataList);
            return new ResponseBean(0,pageData.getTotal(),"",pageData.getRecords());//用户名或密码为空
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }


    /**
     *
     * 当天购买记录
     * 最新10条
     * @param pageNo
     * @param pageSize
     * @param playerId
     * @param request
     * @return
     */
    @NoLogin
    @GetMapping(value = "/today/topN")
    public ResponseBean todayTopN(Integer pageNo,Integer pageSize,String playerId,HttpServletRequest request){
        if(StringUtil.isNull(playerId)){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }
        if(null == pageNo){
            pageNo = 1;
        }
        if(null == pageSize){
            pageSize = 10;
        }
        //当天
        String startTime = DateUtil.beginOfDay(new Date()).toString();
        String endTime = DateUtil.endOfDay(new Date()).toString();

        Player player = null;
        if(StringUtil.isNotNull(playerId)){
            player = playerService.getById(playerId);
        }
        if(null == player){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }

        try{
            IPage<PlayerBuyRecord> pageData = playerBuyRecordService.todayTopN(pageNo,pageSize,playerId,startTime,endTime,null);
            return new ResponseBean(0,pageData.getTotal(),"",pageData.getRecords());
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }



    /**
     * 退码
     * @param token
     * @param buyId 下注记录ID
     * @param request
     * @return
     */
    @GetMapping(value = "/tuima")
    public ResponseBean tuima(@RequestHeader(value = "token")String token,String buyId,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);
        }

        if(StringUtil.isNull(buyId)){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }

        try{

            String errmsg = playerBuyRecordService.tuima(buyId,uid);
            if(StringUtil.isNotNull(errmsg)){
                return new ResponseBean(-1,0,errmsg,null,true);
            }else{
                return new ResponseBean(0,0,"",null,true);
            }

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }
    }

}
