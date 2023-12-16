package com.action;

import com.auth.OpLogIpUtil;
import com.beans.ResponseBean;
import com.model.res.BillsRes;
import com.service.BillsService;
import com.util.IpUtil;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping(value = "/bot/bills")
public class BillsAction {

    @Autowired
    private BillsService billsService;

    @Autowired
    private OpLogIpUtil opLogIpUtil;

    @RequestMapping("test")
    public ResponseBean test (String ip) {
        Map<String,Object> addr = opLogIpUtil.getCityInfoV2(ip);
        if(null!=addr) {
            String prov = null;
            String city = null;
            //如果ip解析返回省份，则使用省份
            if (addr.containsKey("province")) {
                prov = (String) addr.get("province");
                if (addr.containsKey("city")) {
                    city = (String) addr.get("city");
                }
            }
            //如果省份为空，但返回国家，则将国家视为省份
            if (StringUtil.isNull(prov)) {
                if (addr.containsKey("country")) {
                    prov = (String) addr.get("country");
                }
            }
//            System.out.println(String.format("ip：%s,省份：%s,城市:%s", ip, prov, city));
        }

        return ResponseBean.OK;
    }

    /**
     * 机器人账单信息
     * @param token
     * @param date
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "getBillsInfo")
    public ResponseBean getBillsInfo (@RequestHeader(value = "token")String token, String date) throws ParseException {
        String uid = JwtUtil.getUsername(token);
        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }
        BillsRes billsRes = billsService.getBillsInfo(uid, date);
        return new ResponseBean(200, "", billsRes);
    }
}
