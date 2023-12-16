package com.action;

import com.alibaba.fastjson.JSON;
import com.auth.AuthContext;
import com.auth.AuthInfo;
import com.beans.BotUser;
import com.beans.Dictionary;
import com.beans.LotterySetting;
import com.beans.ResponseBean;
import com.config.NoLogin;
import com.google.common.collect.Maps;
import com.service.BotUserService;
import com.service.DictionaryService;
import com.service.LoginService;
import com.util.*;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.service.LotterySettingService;

@RestController
@RequestMapping(value = "/bot")
public class LoginAction {

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private LoginService loginService;

    @Autowired
    private AuthContext authContext;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private MinioClient minioClient;

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Autowired
    private LotterySettingService lotterySettingService;

    @NoLogin
    @PostMapping(value = "/login")
    public ResponseBean login(@RequestBody BotUser user,
                              HttpServletRequest request){
        String uname = user.getLoginName();
        String pwd = user.getLoginPwd();

        if(StringUtil.isNull(uname) || StringUtil.isNull(pwd)){
            return new ResponseBean(-1,0,"用户名或密码为空",null,true);//用户名或密码为空
        }

        String from = request.getHeader("reqFrom");
        if(StringUtil.isNull(from)){
            from = "app";
        }
        if(!"app".equals(from) && !"pc".equals(from)){
            return new ResponseBean(-1,0,"缺少必填参数",null,true);
        }

        int loginFrom = 0; //默认app端登录
//        if(null != memberLogin.getLoginFrom()){
//            loginFrom = memberLogin.getLoginFrom();
//        }
//        if(loginFrom!=0 && loginFrom!=1){
//            return new ResponseBean(-1,0,"req.paramNull",null);
//        }

        try {
            BotUser exist = botUserService.getBy(uname);
            if (null == exist){
                return new ResponseBean(-1, 0, "账号不存在或已过期", null,true);//账号不存在
            }

            String rkey = "3dbot:"+uname + ":pwderror";

            if (stringRedisTemplate.hasKey(rkey)) {
                Integer tryTimes = Integer.valueOf(stringRedisTemplate.boundValueOps(rkey).get());
                Map<String,String> holderMap = new HashMap<>();
                holderMap.put("XXX",String.valueOf(tryTimes));
                return new ResponseBean(-1, 0, "已输错密码 "+tryTimes+" 次，3分钟后重试", null,holderMap);
            }

            String jiamiPwd = PasswordUtil.jiami(pwd);
            if (!jiamiPwd.toLowerCase().equals(exist.getLoginPwd().toLowerCase())) {
                Integer tryTimes = 1;
                String v = stringRedisTemplate.boundValueOps(rkey).get();
                //密码不正确
                //Integer tryTimes = Integer.valueOf(redisTemplate.boundValueOps(rkey).get());
                if (StringUtil.isNotNull(v)) {
                    tryTimes++;
                }
                if (tryTimes >= 3) {
                    stringRedisTemplate.boundValueOps(rkey).set(tryTimes+"", 3, TimeUnit.MINUTES);
                    Map<String,String> holderMap = new HashMap<>();
                    holderMap.put("XXX",String.valueOf(tryTimes));
                    return new ResponseBean(-1, 0, "已输错密码 "+tryTimes+" 次，3分钟后重试", null,holderMap);
                }
                return new ResponseBean(-1, 0, "账号或者密码错误", null,true);//密码不正确
            }

            stringRedisTemplate.delete(rkey);

            if (new Date().after(exist.getDueDate())){//当前时间在过期时间之后
                return new ResponseBean(-1,0,"账号已过期",null,true);
            }

            //String logKey = uname+":token:"+loginFrom;
            AuthInfo authInfo = authContext.getInfo();
            if (null == authInfo) {
                authInfo = new AuthInfo();
                authContext.setInfo(authInfo);
            }

            authContext.getInfo().setUserId(exist.getId());
            //logService.addLog(authContext.getInfo(), 0, null, "会员登录");

            //生成规则
            String token = JwtUtil.generToken(exist.getId(), null, null);
            //System.out.println("================login token :"+token);

            //redisTemplate.boundValueOps(logKey).set(token);

//            Integer needUpdatePwd = exist.getInitPwdUpdate();
//            if(null == needUpdatePwd){
//                needUpdatePwd = 0;
//            }

//            Integer appOnline=exist.getAppOnlineStatus();
//            if(null==appOnline){
//                appOnline = 0;
//            }
//            Integer pcOnline=exist.getPcOnlineStatus();
//            if(null==pcOnline){
//                pcOnline = 0;
//            }
//            if("app".equals(from)){
//                appOnline=1;
//            }
//            if("pc".equals(from)){
//                pcOnline=1;
//            }
//            int onlineStatus = pcOnline==0&&appOnline==0?0:1;
            exist.setOnlineStatus(1);
//            exist.setPcOnlineStatus(pcOnline);
//            exist.setAppOnlineStatus(appOnline);
            exist.setLastLoginIp(IpUtil.getIpAddr(request));
            exist.setLastLoginTime(new Date());
            botUserService.updateById(exist);
            stringRedisTemplate.boundValueOps("3d:chatbot:online:"+exist.getId()).set("1");
            //stringRedisTemplate.boundSetOps("3d:chatbot:newLogin").add(exist.getId());
//            long oc = adminService.getOnlineCount();
//            OnlineLog onlineLog = onlineLogService.getOnlineLog();
//            if (null == onlineLog) {
//                onlineLog = new OnlineLog();
//                if (exist.getVipFlag() == 0) {
//                    onlineLog.setAgentCount(1);
//                    onlineLog.setVipCount(0);
//                }else {
//                    onlineLog.setAgentCount(0);
//                    onlineLog.setVipCount(1);
//                }
//                onlineLog.setCreateDate(new Date());
//                onlineLogService.save(onlineLog);
//            }else {
//                Integer totalCount = onlineLog.getAgentCount() + onlineLog.getVipCount();
//                if (oc > totalCount) {
//                    if (exist.getVipFlag() == 1) {
//                        onlineLog.setVipCount(onlineLog.getVipCount() + 1);
//                    }else {
//                        onlineLog.setAgentCount(onlineLog.getAgentCount() + 1);
//                    }
//                }
//                onlineLogService.updateById(onlineLog);
//            }

            Map respData = new HashMap<>();
            respData.put("uname", uname);
            respData.put("token", token);
//            respData.put("leftCredit",exist.getSurplusCreditLimit());
//            respData.put("needUpdatePwd",needUpdatePwd);
            //respData.put("creditValue",)

            //判断当前登录用户停止操作的间隔时间
            Map<String,Object> result = loginService.checkRelogin(token);
            Integer  needRelogin = (Integer)result.get("3d-chatbot-needLogin");
            Integer  validTime = (Integer)result.get("3d-chatbot-validTime"); //最大失效时间，单位为分钟
            if(null!=needRelogin && null!=validTime && needRelogin==0 && validTime>0){
                //将登录时间的时间戳写入redis
                //System.out.println("==========================首次写入会员的登录操作时间");
                stringRedisTemplate.boundValueOps(token).set(new Date().getTime()+"",validTime+10,TimeUnit.MINUTES);
            }else{
                //将登录时间的时间戳写入redis
                validTime = 120;
                //System.out.println("==========================首次写入会员的登录操作时间");
                stringRedisTemplate.boundValueOps(token).set(new Date().getTime()+"",validTime,TimeUnit.MINUTES);
            }

            //发送机器人账号登录状态至mq : botUserMsg队列
            Map<String,Object> dataMap = Maps.newHashMap();
            dataMap.put("userId",exist.getId());
            dataMap.put("type","pcOnline");
            dataMap.put("value",1);
            rabbitTemplate.convertAndSend("exchange_botUserTopic_3d","botUserMsg", JSON.toJSONString(dataMap));

            return new ResponseBean(0, 0, "", respData);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "请求失败", null,true);
        }
    }

    @GetMapping(value = "/logout")
    public ResponseBean logout(@RequestHeader(value = "token")String token,HttpServletRequest request){

        if(StringUtil.isNull(token)){
            return new ResponseBean(403,0,"重新登录",null,true);//用户名或密码为空
        }
        String uid = JwtUtil.getUsername(token);
        BotUser botUser = botUserService.getById(uid);
        botUser.setOnlineStatus(0);
        botUserService.updateOnlineStatus(uid,0,0,0);
        stringRedisTemplate.boundValueOps("3d:chatbot:online:"+uid).set("0");
        return new ResponseBean(0,0,"",null);

    }

    /**
     * 随机选择头像
     * @param request
     * @return
     */
    @NoLogin
    @GetMapping(value = "/chooseHeadimg")
    public ResponseBean getHeadimg(HttpServletRequest request){

        String imgDir = uploadDir+"headimg";
        File dir = new File(imgDir);
        String[] fileList = dir.list();
        int len = fileList.length;

        int idx = new Random().nextInt(len);

        String imgName = fileList[idx];

        return new ResponseBean(0, 0, "", imgName);
    }

    /**
     * 随机选择头像
     * @param request
     * @return
     */
    @NoLogin
    @GetMapping(value = "/downHeadimg")
    public void downHeadimg(HttpServletRequest request, String imgName, HttpServletResponse resp){

        try {
            String imgPath = uploadDir + "headimg/" + imgName;

            //2.获取文件的名称
            //原理就是截取最后一个"/"后面的部分，“\\”是进行了转义
            String filename = "";
            filename = URLEncoder.encode(imgName, "UTF-8");//对文件名进行编码，否则中文名可能出现乱码问题
//            System.out.print(filename);
            //3.设置浏览器支持下载
            resp.setHeader("Content-Disposition", "attachment;filename=" + filename);
            //4.获取文件的输入流
            FileInputStream fis = new FileInputStream(imgPath);
            //5.创建缓冲区
            int len = 0;
            byte buffer[] = new byte[1024];
            //6.获取ServletOutputStream
            ServletOutputStream sos = resp.getOutputStream();
            //7.将输入流写入到缓冲区，再利用ServletOutputStream将数据发送给客户端
            while ((len = fis.read(buffer)) != -1) {
                sos.write(buffer, 0, len);
            }
            //不要忘记关闭流
            fis.close();
            sos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 随机选择头像
     * @param request
     * @return
     */
    @NoLogin
    @GetMapping(value = "/downImage")
    public void downImage(HttpServletRequest request, String imgName, HttpServletResponse resp){

        try {
            String imgPath = uploadDir + "images/" + imgName;

            //2.获取文件的名称
            //原理就是截取最后一个"/"后面的部分，“\\”是进行了转义
            String filename = "";
            filename = URLEncoder.encode(imgName, "UTF-8");//对文件名进行编码，否则中文名可能出现乱码问题
//            System.out.print(filename);
            //3.设置浏览器支持下载
            resp.setHeader("Content-Disposition", "attachment;filename=" + filename);
            //4.获取文件的输入流
            FileInputStream fis = new FileInputStream(imgPath);
            //5.创建缓冲区
            int len = 0;
            byte buffer[] = new byte[1024];
            //6.获取ServletOutputStream
            ServletOutputStream sos = resp.getOutputStream();
            //7.将输入流写入到缓冲区，再利用ServletOutputStream将数据发送给客户端
            while ((len = fis.read(buffer)) != -1) {
                sos.write(buffer, 0, len);
            }
            //不要忘记关闭流
            fis.close();
            sos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 随机获取头像
     * @return
     */
    @NoLogin
    @GetMapping(value = "/chooseImg")
    public ResponseBean chooseImg(){

        return new ResponseBean(0,0,"",botUserService.chooseImg(),true);
//        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket("3d-robot-img").build());
//        List<Item> list = new ArrayList<>();
//        results.forEach(itemResult -> {
//            try {
//                list.add(itemResult.get());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        int len = list.size();
//        int idx = new Random().nextInt(len);
//        Item imgName = list.get(idx);
//
//        Dictionary dictionary = dictionaryService.getDicByCode("system","MinioUrl");
//        String icon = dictionary.getValue()+"/3d-robot-img/"+imgName.objectName();
//        return new ResponseBean(0, 0, "", icon);
    }



    @NoLogin
    @GetMapping(value = "/lotterySetting")
    public ResponseBean lotterySetting(){

        List<LotterySetting> lotterySettings = lotterySettingService.getLotterySettingList(null);
        lotterySettings.forEach(ls->{
            ls.setMaxBettingCount(0L);
            ls.setMinBettingPrice(null);
            ls.setPeiRate("");
        });
        return new ResponseBean(0,0,"",lotterySettings,true);
//        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket("3d-robot-img").build());
//        List<Item> list = new ArrayList<>();
//        results.forEach(itemResult -> {
//            try {
//                list.add(itemResult.get());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        int len = list.size();
//        int idx = new Random().nextInt(len);
//        Item imgName = list.get(idx);
//
//        Dictionary dictionary = dictionaryService.getDicByCode("system","MinioUrl");
//        String icon = dictionary.getValue()+"/3d-robot-img/"+imgName.objectName();
//        return new ResponseBean(0, 0, "", icon);
    }


}
