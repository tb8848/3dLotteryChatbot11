package com.action;

import com.beans.Draw;
import com.beans.ResponseBean;
import com.service.DrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/draw")
public class DrawAction {

    @Autowired
    private DrawService drawService ;


    /**
     * 根据id查询开奖号码
     * @param id
     * @return
     */
    @RequestMapping("/getDraw")
    public ResponseBean getDraw(String id ){
        Draw draw = drawService.getById(id);
        if(draw == null){
            return ResponseBean.error("draw.getDraw.unFindDrawCode");
        }
        return new ResponseBean(0,"查询成功",draw);
    }


    /**
     * 获取最近68期开奖
     * @return
     */
    @RequestMapping("/getDrawList68")
    public ResponseBean getDrawList68 () {
        List<Draw> drawList = drawService.getDrawList68();
        return new ResponseBean(200, "成功", drawList);
    }

    /**
     * 获取最新一期信息
     * @return
     */
    @RequestMapping("/selectLastDrawInfo")
    public ResponseBean selectLastDrawInfo(){
        Draw draw = drawService.getLastDrawInfo();
        if (draw == null){
            return ResponseBean.error("draw.selectLastDrawInfo.unFindLastDraw");
        }
        return new ResponseBean(200,"查询成功",draw);
    }

}
