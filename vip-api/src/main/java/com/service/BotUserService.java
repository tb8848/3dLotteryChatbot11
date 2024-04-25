package com.service;

import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.dao.BotUserDAO;
import com.dao.DictionaryDAO;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static com.util.StringUtil.convertToBase64;

@Service
public class BotUserService extends ServiceImpl<BotUserDAO, BotUser> {

    @Autowired
    private BotUserDAO dataDAO;

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private DictionaryDAO dictionaryDAO;

    public BotUser getBy(String uname){
        LambdaQueryWrapper<BotUser> qw = new LambdaQueryWrapper();
        qw.eq(BotUser::getLoginName,uname);
        qw.eq(BotUser::getStatus,1);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }

    public void updateOnlineStatus(String uid,Integer pcOnline,Integer appOnline,Integer onlineStatus){
        LambdaUpdateWrapper<BotUser> qw = new LambdaUpdateWrapper();
        qw.eq(BotUser::getId,uid);
        qw.set(BotUser::getOnlineStatus,onlineStatus);
//        qw.set(Admin::getAppOnlineStatus,appOnline);
//        qw.set(Admin::getPcOnlineStatus,pcOnline);
        dataDAO.update(null,qw);
    }

    /**
     * 更新余额
     * @param uid 会员ID
     * @param newCredit 待增加或减少的信用额度值
     * @param upOrDown true:增加，false:减少
     * @return
     */
    public BigDecimal updateCredit(String uid,BigDecimal newCredit,boolean upOrDown){
//        LockInfo lockInfo = null;
//        try {
//            lockInfo = lockTemplate.lock("updatebalance-"+uid,3600000,3600000);
//            BotUser vip = dataDAO.selectById(uid);
//            if (null != vip) {
//                BigDecimal leftCredit = vip.getSurplusCreditLimit();
//                BigDecimal hasUsed = vip.getUseCreditLimit();
//                if (null == hasUsed) {
//                    hasUsed = BigDecimal.ZERO;
//                }
//                if (upOrDown) {
//                    leftCredit = leftCredit.add(newCredit);
//                    hasUsed = hasUsed.subtract(newCredit);
//                } else {
//                    leftCredit = leftCredit.subtract(newCredit);
//                    hasUsed = hasUsed.add(newCredit);
//                }
//                if (leftCredit.compareTo(BigDecimal.ZERO) == -1) {
//                    leftCredit = BigDecimal.ZERO;
//                }
//                if (hasUsed.compareTo(BigDecimal.ZERO) == -1) {
//                    hasUsed = BigDecimal.ZERO;
//                }
//                adminDAO.updateCredit(uid, leftCredit, hasUsed);
//                return leftCredit;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if(null!=lockInfo){
//                lockTemplate.releaseLock(lockInfo);
//            }
//        }
        return BigDecimal.ZERO;

    }

    public void updatePwd(String uid, String md5New) {
        LambdaUpdateWrapper<BotUser> qw = new LambdaUpdateWrapper();
        qw.eq(BotUser::getId,uid);
        qw.set(BotUser::getLoginPwd,md5New);
        qw.set(BotUser::getInitPwdUpdate,1);
        dataDAO.update(null,qw);
    }

    public void updateSafePwd(String uid, String md5New) {
        LambdaUpdateWrapper<BotUser> qw = new LambdaUpdateWrapper();
        qw.eq(BotUser::getId,uid);
        qw.set(BotUser::getSafePwd,md5New);
        dataDAO.update(null,qw);
    }

    /**
     * 随机获取头像
     * @return
     */
    public Map chooseImg() throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket("3d-robot-img").build());
        List<Item> list = new ArrayList<>();
        results.forEach(itemResult -> {
            try {
                list.add(itemResult.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        int len = list.size();
        int idx = new Random().nextInt(len);
        Item imgName = list.get(idx);

        // 获取对象的InputStream
        InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket("3d-robot-img").object(imgName.objectName()).build());
        // 将图像转换为Base64编码
        String base64Image = convertToBase64(inputStream);
        Map map = new HashMap();
        map.put("base64",base64Image);
        map.put("url",imgName.objectName());

        return map;

//        List<Dictionary> dicList = dictionaryDAO.selectList(new QueryWrapper<Dictionary>().eq("code","MinioUrl").eq("type","system"));
//        if (dicList.size() > 0){
//            Dictionary dictionary = dicList.get(0);
//            String icon = dictionary.getValue()+"/3d-robot-img/"+imgName.objectName();
//            return icon;
//        }
//        return "";
    }

    public List<BotUser> listBy() {
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(BotUser::getStatus, Arrays.asList(1,2));
        return dataDAO.selectList(lambdaQueryWrapper);
    }


    public int clearPanInfo(String botUserId) {
        LambdaUpdateWrapper<BotUser> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BotUser::getId,botUserId);
        lambdaUpdateWrapper.set(BotUser::getLogin3dToken,"");
        return dataDAO.update(null,lambdaUpdateWrapper);
    }

    public Long countOnlineUsers() {
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(BotUser::getStatus, Arrays.asList(1));
        lambdaQueryWrapper.eq(BotUser::getOnlineStatus, 1);
        return dataDAO.selectCount(lambdaQueryWrapper);
    }
}
