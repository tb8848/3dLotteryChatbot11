//会员资料页面脚本
var $ = layui.$;
var layer = layui.layer;

const vipInfoVm = new Vue({
    el: '#vipinfo',
    data: {
        userId:'',
        bsInfo:{ //基本资料

        },
        fundMode:0,
        oddsList:[]
    },
    methods: {
        init(){
          this.bs();
          this.prList();
        },
        bs(){
            this.userId = GetQueryString("parentId");
            var _this = this;
            $.ajax({
                url:HOST+'vip/info/getVipBasicSetting?vipId='+_this.userId,
                type:'get',
                success:function (res) {
                    if(res.code==0){
                        _this.bsInfo = res.data.basicSetting;
                        _this.fundMode = res.data.fundMode;
                        // $("#input_mode"+bs.autoOrEnter).attr("checked",true);
                        // $("#show_mode"+bs.printOrShowLottery).attr("checked",true);
                        // $("#odds_type"+bs.realOrTransformOdds).attr("checked",true);
                        // if(bs.printScreencut==1){
                        //     $("#show_snapshot_input").attr("checked",true);
                        // }else{
                        //     $("#show_snapshot_input").attr("checked",false);
                        // }
                        // $("input[name='store_name']").val(bs.saleName);
                    }
                }
            })
        },
        prList() {
            var _this = this;
            $.ajax({
                url:HOST+'vip/info/selectList?vipId='+_this.userId,
                type:'get',
                success:function (res) {
                    if(res.code==0){
                        _this.oddsList = res.data;
                        _this.oddsList.forEach((item,idx)=>{
                            item.lotterySettingList.forEach((item1,idx1)=>{
                                var peiRateRangeList = item1.peiRateRangeList;
                                item1.hsSelector = _this.buildHuiShuiSelector(item1.id,item.id,peiRateRangeList,item1.huiShui,idx1);
                                item1.prSelector = _this.buildPeiRateSelector(item1.id,item.id,peiRateRangeList,item1.vipPeiRate,idx1,item1.peiRate)
                            })
                        })
                        // //
                        // var bodyData = "";
                        // for(var i=0,len=resData.length;i<len;i++){
                        //     var item = resData[i];
                        //     if(item.lotterySettingList.length>1){
                        //         bodyData = bodyData + __buildExpandRows(item);
                        //     }else{
                        //         bodyData = bodyData + __buildOneRow(item)
                        //     }
                        // }
                        // $("#tbody").html(bodyData);
                    }
                }
            })
        },
        buildHuiShuiSelector(lsId,lmId,dataList,selectValue,idx){ //生成回水选择器
            var objId ="hs-"+lmId+"-"+lsId+"-"+idx;
            var hsSelector = {
                lmId:lmId,
                lsId:lsId,
                index:idx,
                groupId:'group'+lmId,
                id:objId,
                first:idx==0?1:0,
                selectall:idx==0?1:0,
                options:[]
            }
            for(var i=0,len=dataList.length;i<len;i++){
                var item = dataList[i];
                hsSelector.options.push({
                    value:item.earnWater,
                    text:item.earnWater,
                    selected:item.earnWater==selectValue?true:false
                })
            }
            return hsSelector;
        },
        buildPeiRateSelector(lsId,lmId,dataList,selectValue,idx,prLimit){
            var objId ="pr-"+lmId+"-"+lsId+"-"+idx;
            var prSelector = {
                lmId:lmId,
                lsId:lsId,
                index:idx,
                groupId:'group'+lmId,
                id:objId,
                first:idx==0?1:0,
                selectall:idx==0?1:0,
                options:[]
            }
            // var wclass = 'w70';
            // if(lmId=='5' || lmId=='6' || lmId=='7'){
            //     wclass = 'w90'
            // }
            for(var i=0,len=dataList.length;i<len;i++){
                var item = dataList[i];
                var hs = item.earnWater;
                if(prLimit.indexOf("/")>-1) {
                    var prArr = prLimit.split("/");
                    for (var j = 0; j < prArr.length; j++) {
                        prArr[j] = accSub(parseFloat(prArr[j]), parseFloat(hs));
                    }
                    dataList[i].peiRate = prArr.join("/");
                }else{
                    dataList[i].peiRate = accSub(parseFloat(prLimit), parseFloat(hs));
                }
            }
            for(var i=0,len=dataList.length;i<len;i++){
                var item = dataList[i];
                prSelector.options.push({
                    value:item.peiRate,
                    text:item.peiRate,
                    selected:item.peiRate==selectValue?true:false
                })
            }
            return prSelector;
        },
        changeHSAndPr(selectObj){//修改回水和赔率
            var id = selectObj.id;
            var idx = selectObj.selectedIndex;
            var first = $(selectObj).attr("first");
            var groupId =  $(selectObj).attr("groupId");
            if(id.indexOf("hs")>-1){
                //修改了回水
                var prSelectorId = id.replace('hs','pr');
                $("#"+prSelectorId).get(0).selectedIndex=idx;
                if(first=="1"){
                    //修改了第1个,同类型都修改
                    $(selectObj).parents("tr").siblings().each(function(it1){
                        $(this).find("select").each(function(it2){
                            if($(this).attr('groupId')===groupId) {
                                $(this).get(0).selectedIndex = idx
                            }
                        })
                    });
                }
            }else if(id.indexOf('pr')>-1){
                //修改了赔率
                var hsSelectorId = id.replace('pr','hs');
                $("#"+hsSelectorId).get(0).selectedIndex=idx;
                if(first=="1"){
                    //修改了第1个,同类型都修改
                    $(selectObj).parents("tr").siblings().each(function(it1){
                        $(this).find("select").each(function(it2){
                            if($(this).attr('groupId')===groupId){
                                $(this).get(0).selectedIndex=idx
                            }
                        })
                    });
                }
            }
        },

        // subSetting(){//提交修改
        //     bs.saleName = $("input[name='store_name']").val();
        //     bs.printScreencut = document.getElementById("show_snapshot_input").checked==true?1:0;
        //     bs.printOrShowLottery =  $("input[name='show_mode']").val();
        //     bs.autoOrEnter = $("input[name='input_mode']").val();
        //     bs.realOrTransformOdds = $("input[name='odds_type']").val();
        //     var params = [];
        //     $("#tbody").find("tr").each(function(e){
        //         var ps = null;
        //         var hs = null;
        //         var lsId = null;
        //         $(this).find("select").each(function(e1){
        //             var id = this.id;
        //             var arr = id.split("-");
        //             lsId = arr[2];
        //             var pfx = arr[0];
        //             if(pfx === 'hs'){
        //                 hs = this.value;
        //             }else{
        //                 ps = this.value;
        //             }
        //         })
        //         if(ps!=null){
        //             params.push({
        //                 lotterySettingId:lsId,
        //                 earnWater:hs,
        //                 peiRate:ps
        //             });
        //         }
        //     })
        //     bs.prSetList = params;
        //     layer.msg('正在处理...', {
        //         icon: 16
        //         ,shade: 0.3
        //         ,time:-1
        //     });
        //     $.ajax({
        //         url:HOST+'vipInfo/upBasicSetting',
        //         type:'post',
        //         data:JSON.stringify(bs),
        //         contentType:"application/json",
        //         success:function (res) {
        //             layer.closeAll()
        //             if(res.code==0){
        //                 layer.msg("更改成功");
        //                 window.parent.getInfo();
        //             }else{
        //                 layer.msg(res.msg);
        //             }
        //         },
        //         error:function (e) {
        //             layer.closeAll();
        //         }
        //     })
        //     return false;
        // }
    },

})
vipInfoVm.init();
//
// __bs();
// __prList();


//
// function bs(){
//     $.ajax({
//         url:HOST+'draw/getVipBasicSetting',
//         type:'get',
//         success:function (res) {
//             if(res.code==0){
//                 bs = res.data.basicSetting;
//                 $("#input_mode"+bs.autoOrEnter).attr("checked",true);
//                 $("#show_mode"+bs.printOrShowLottery).attr("checked",true);
//                 $("#odds_type"+bs.realOrTransformOdds).attr("checked",true);
//                 if(bs.printScreencut==1){
//                     $("#show_snapshot_input").attr("checked",true);
//                 }else{
//                     $("#show_snapshot_input").attr("checked",false);
//                 }
//                 $("input[name='store_name']").val(bs.saleName);
//             }
//         }
//     })
//
// }
//

// //修改赔率和回水
// function changeHSAndPr(selectObj){
//     var id = selectObj.id;
//     var idx = selectObj.selectedIndex;
//     var first = $(selectObj).attr("first");
//     var groupId =  $(selectObj).attr("groupId");
//     if(id.indexOf("hs")>-1){
//         //修改了回水
//         var prSelectorId = id.replace('hs','pr');
//         $("#"+prSelectorId).get(0).selectedIndex=idx;
//         if(first=="1"){
//             //修改了第1个,同类型都修改
//             $(selectObj).parents("tr").siblings().each(function(it1){
//                 $(this).find("select").each(function(it2){
//                     if($(this).attr('groupId')===groupId) {
//                         $(this).get(0).selectedIndex = idx
//                     }
//                 })
//             });
//         }
//     }else if(id.indexOf('pr')>-1){
//         //修改了赔率
//         var hsSelectorId = id.replace('pr','hs');
//         $("#"+hsSelectorId).get(0).selectedIndex=idx;
//         if(first=="1"){
//             //修改了第1个,同类型都修改
//             $(selectObj).parents("tr").siblings().each(function(it1){
//                 $(this).find("select").each(function(it2){
//                     if($(this).attr('groupId')===groupId){
//                         $(this).get(0).selectedIndex=idx
//                     }
//                 })
//             });
//         }
//     }
// }
//
// //回水选择器
// function __buildHuiShuiSelector(lsId,lmId,dataList,selectValue,idx){
//     var objId ="hs-"+lmId+"-"+lsId+"-"+idx;
//     var selectObj = '';
//     if(idx==0){
//         selectObj = '<select id="'+objId+'" index="'+idx+'" first="1" selectall="1" _name="return_water" groupId="group'+lmId+'" class="w70 s_erd" style="height: 24px" onchange="changeHSAndPr(this)">'
//     }else{
//         selectObj = '<select id="'+objId+'" index="'+idx+'" first="0" selectall="0" _name="return_water" groupId="group'+lmId+'" class="w70 s_erd" style="height: 24px" onchange="changeHSAndPr(this)">'
//     }
//     for(var i=0,len=dataList.length;i<len;i++){
//         var option = "";
//         var item = dataList[i];
//         option = (item.earnWater==selectValue)?
//             '<option value="'+item.earnWater+'" selected>'+item.earnWater+'</option>'
//             :
//             '<option value="'+item.earnWater+'">'+item.earnWater+'</option>'
//
//         selectObj = selectObj + option;
//
//     }
//     selectObj = selectObj+'</select>';
//     return selectObj;
// }
//
// //赔率选择器
// function __buildPeiRateSelector(lsId,lmId,dataList,selectValue,idx,prLimit){
//     var objId ="pr-"+lmId+"-"+lsId+"-"+idx;
//
//     var wclass = 'w70';
//     if(lmId=='5' || lmId=='6' || lmId=='7'){
//         wclass = 'w90'
//     }
//     for(var i=0,len=dataList.length;i<len;i++){
//         var item = dataList[i];
//         var hs = item.earnWater;
//         if(prLimit.indexOf("/")>-1) {
//             var prArr = prLimit.split("/");
//             for (var j = 0; j < prArr.length; j++) {
//                 prArr[j] = accSub(parseFloat(prArr[j]), parseFloat(hs));
//             }
//             dataList[i].peiRate = prArr.join("/");
//         }else{
//             dataList[i].peiRate = accSub(parseFloat(prLimit), parseFloat(hs));
//         }
//     }
//
//     var selectObj = '';
//     if(idx==1){
//         selectObj = '<select id="'+objId+'" index="'+idx+'" first="1" selectall="1" _name="odds_limit" groupId="group'+lmId+'" class="'+wclass+' s_erd" style="height: 24px" onchange="changeHSAndPr(this)">'
//     }else{
//         selectObj = '<select id="'+objId+'" index="'+idx+'" first="0" selectall="0" _name="odds_limit" groupId="group'+lmId+'" class="'+wclass+' s_erd" style="height: 24px" onchange="changeHSAndPr(this)">'
//     }
//     for(var i=0,len=dataList.length;i<len;i++){
//         var option = "";
//         var item = dataList[i];
//         option = (item.peiRate==selectValue)?
//             '<option value="'+item.peiRate+'" selected>'+item.peiRate+'</option>'
//             :
//             '<option value="'+item.peiRate+'">'+item.peiRate+'</option>'
//
//         selectObj = selectObj + option;
//
//     }
//     selectObj = selectObj+'</select>';
//     return selectObj;
// }

//
// function  __buildSubTable(dataList) {
//     var tbodyData = "";
//     dataList.forEach((item,idex)=>{
//         var tr = __createLotterySettingRow(item,idex);
//         tbodyData = tbodyData + tr;
//     })
//     return '<table class="t-1">' +'<tbody>'+tbodyData+'</tbody>'+ '</table>';
// }
//
//
// function __createLotterySettingRow(item,idex){
//     var peiRateRangeList = item.peiRateRangeList;
//     var huishuiSelector = __buildHuiShuiSelector(item.id,item.lotteryMethodId,peiRateRangeList,item.huiShui,idex);
//     var prSelector = __buildPeiRateSelector(item.id,item.lotteryMethodId,peiRateRangeList,item.vipPeiRate,idex,item.peiRate)
//
//     var tr = '<tr class="fn-hover"> <td width="6%"></td> <td width="13%">'+item.bettingRule+'</td>\n' +
//         ' <td width="10%">'+item.minBettingPrice+'</td> ' +
//         '<td width="13%">'+item.peiRate+'</td> ' +
//         '<td width="13%">'+item.maxBettingCount+'</td> ' +
//         '<td width="13%">'+item.maxNumberTypeCount+'</td> ' +
//         '<td width="13%">'+huishuiSelector+'</td>\n' +
//         ' <td width="19%">'+prSelector+'</td>\n' +
//         ' </tr>';
//     return tr;
// }
//
// /**
//  *
//  * <tr id="td_erd"> <td colspan="8" style="padding:4px;">
//  <table class="t-1">
//  <tbody>
//  <tr class="fn-hover"> <td width="6%"></td> <td width="13%">口口XX</td>
//  <td width="10%">1</td> <td width="13%">98</td> <td width="13%">2000</td> <td width="13%">90000</td> <td width="13%"><select id="1" index="0" first="1" selectall="1" _name="return_water" class="w70 s_erd"><option value="0.18">0.18</option><option value="0.179">0.179</option></select>  </td>
//  <td width="19%"> <select index="0" selectall="1" _name="odds_limit" class="s_a s_erd"><option value="80">80</option><option value="80.1">80.1</option><option value="80.2">80.2</option></select>
//  </td>
//  </tr>
//  *
//  *
//  *
//  * @param item
//  * @returns {string}
//  * @private
//  */
//
// function __buildExpandRows(item){
//     var id = item.id;
//     var name = item.bettingMethod;
//     var expandRow = '<tr class="bg3"><td><span href="javascript:void(0)" class="fn-ico-switch btn-pointer" _target="td_erd" onclick="expand(this,'+id+')">' +
//         '<img src="../img/ico-close-large.gif"></span></td>' +
//         '<td><span href="javascript:void(0)" class="btn-pointer" status="1" act="erd">'+name+'</span></td> ' +
//         '<td></td> <td></td> <td></td> <td></td> <td></td> <td></td>' +
//         '<</tr>';
//     var subTable = __buildSubTable(item.lotterySettingList);
//     var subTr = '<tr id="subtable'+id+'"> <td colspan="8" style="padding:4px;">'+subTable+'</td></tr>';
//     return expandRow + subTr;
// }
//
// function __buildOneRow(item){
//     var ls = item.lotterySettingList[0];
//     return __createLotterySettingRow(ls);
// }
//
// //展开与折叠
// function expand(obj,id){
//     $subTable = $("#subtable"+id);
//     if($subTable.hasClass('hide')){
//         $subTable.removeClass('hide');
//         $(obj).html('<img src="../img/ico-close-large.gif">');
//     }else{
//         $(obj).html('<img src="../img/ico-open-large.gif">');
//         $subTable.addClass('hide');
//     }
// }
//
//
//
//
// $("#memberbut").on("click","",function (res) {
//     bs.saleName = $("input[name='store_name']").val();
//     bs.printScreencut = document.getElementById("show_snapshot_input").checked==true?1:0;
//     bs.printOrShowLottery =  $("input[name='show_mode']").val();
//     bs.autoOrEnter = $("input[name='input_mode']").val();
//     bs.realOrTransformOdds = $("input[name='odds_type']").val();
//     var params = [];
//     $("#tbody").find("tr").each(function(e){
//         var ps = null;
//         var hs = null;
//         var lsId = null;
//         $(this).find("select").each(function(e1){
//             var id = this.id;
//             var arr = id.split("-");
//             lsId = arr[2];
//             var pfx = arr[0];
//             if(pfx === 'hs'){
//                 hs = this.value;
//             }else{
//                 ps = this.value;
//             }
//         })
//         if(ps!=null){
//             params.push({
//                 lotterySettingId:lsId,
//                 earnWater:hs,
//                 peiRate:ps
//             });
//         }
//     })
//     bs.prSetList = params;
//     layer.msg('正在处理...', {
//         icon: 16
//         ,shade: 0.3
//         ,time:-1
//     });
//     $.ajax({
//         url:HOST+'vipInfo/upBasicSetting',
//         type:'post',
//         data:JSON.stringify(bs),
//         contentType:"application/json",
//         success:function (res) {
//             layer.closeAll()
//             if(res.code==0){
//                 layer.msg("更改成功");
//                 window.parent.getInfo();
//             }else{
//                 layer.msg(res.msg);
//             }
//         },
//         error:function (e) {
//             layer.closeAll();
//         }
//     })
//     return false;
// })
//


/**
 ** 减法函数，用来得到精确的减法结果
 ** 1.5 - 1.2 = 0.30000000000000004
 ** 说明：javascript的减法结果会有误差，在两个浮点数相减的时候会比较明显。这个函数返回较为精确的减法结果。
 ** 调用：accSub(arg1,arg2)
 ** 返回值：arg1加上arg2的精确结果
 **/
// function accSub(arg1, arg2){
//     var r1, r2, m, n
//     try {
//         r1 = arg1.toString().split('.')[1].length
//     } catch (e) {
//         r1 = 0
//     }
//     try {
//         r2 = arg2.toString().split('.')[1].length
//     } catch (e) {
//         r2 = 0
//     }
//     m = Math.pow(10, Math.max(r1, r2)) // last modify by deeka //动态控制精度长度
//     n = r1 >= r2 ? r1 : r2
//     return ((arg1 * m - arg2 * m) / m).toFixed(n)
// }

