package com.action;

import com.beans.Proxy;
import com.beans.ResponseBean;
import com.service.ProxyService;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/admin/proxy")
public class ProxyAction {

    @Autowired
    private ProxyService proxyService;

    /**
     * 静态ip池列表
     * @return
     */
    @RequestMapping(value = "getProxyList")
    public ResponseBean getProxyList (String status) {
        List<Proxy> proxyList = proxyService.getProxyList(status);
        return new ResponseBean(0, "", proxyList);
    }

    /**
     * 添加代理ip
     * @param proxy
     * @return
     */
    @PostMapping(value = "addProxy")
    public ResponseBean addProxy (@RequestBody Proxy proxy) {
        if (StringUtil.isNull(proxy.getIp())) {
            return ResponseBean.error("proxy.add.ipNotNull");
        }
        if (StringUtil.isNull(proxy.getProvince()) || StringUtil.isNull(proxy.getCity())) {
            return ResponseBean.error("proxy.add.areaValidation");
        }
        proxy.setStatus(1);
        proxy.setCreateTime(new Date());
        if (proxyService.save(proxy)) {
            return ResponseBean.ok("common.operationSuccess");
        }
        return ResponseBean.error("common.operationFail");
    }

    /**
     * 编辑代理ip
     * @param proxy
     * @return
     */
    @PostMapping(value = "updateProxy")
    public ResponseBean updateProxy (@RequestBody Proxy proxy) {
        if (StringUtil.isNull(proxy.getIp())) {
            return ResponseBean.error("proxy.add.ipNotNull");
        }
        if (StringUtil.isNull(proxy.getProvince()) || StringUtil.isNull(proxy.getCity())) {
            return ResponseBean.error("proxy.add.areaValidation");
        }
        if (proxyService.updateById(proxy)) {
            return ResponseBean.ok("common.operationSuccess");
        }
        return ResponseBean.error("common.operationFail");
    }

    /**
     * 删除ip
     * @return
     */
    @RequestMapping(value = "deleteProxy")
    public ResponseBean deleteProxy (String id) {
        if (StringUtil.isNull(id)) {
            return ResponseBean.error("common.paramWrong");
        }
        if (proxyService.removeById(id)) {
            return ResponseBean.ok("common.deleteSuccess");
        }
        return ResponseBean.error("common.deleteFail");
    }

    /**
     * 根据id获取ip信息
     * @return
     */
    @RequestMapping(value = "getProxyById")
    public ResponseBean getProxyById (String id) {
        Proxy proxy = proxyService.getById(id);
        return new ResponseBean(0, "", proxy);
    }
}
