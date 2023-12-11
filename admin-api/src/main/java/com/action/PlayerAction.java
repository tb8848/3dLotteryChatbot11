package com.action;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.beans.Player;
import com.beans.ResponseBean;
import com.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/admin/player")
public class PlayerAction {
    @Autowired
    private PlayerService playerService;

    /**
     * 分页加载数据
     * @return
     */
    @GetMapping("/listPagerByBotUserId")
    public ResponseBean listPagerByBotUserId(String nickName, String userId,String userType, String pretexting, String reportNet, String eatPrize,
                                             @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer limit, @RequestHeader(value = "lang") String lang) {
        IPage<Player> pager = playerService.listPagerByBotUserId(userId,nickName,userType, pretexting,reportNet,eatPrize, page,limit);
        return new ResponseBean(0, pager.getTotal(),"", pager.getRecords());
    }
}
