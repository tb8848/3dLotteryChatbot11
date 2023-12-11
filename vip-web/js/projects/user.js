var $ = layui.jquery;
var bs = null;

$("#account").html(sessionStorage.getItem("account"))
$("#leftCredit").html(sessionStorage.getItem("leftCredit"))

function getData() {
    __bs();
    __prList();
}

function __bs(){
    $.ajax({
        url:HOST+'info',
        type:'post',
        success:function (res) {
            if(res.code==0){
                bs = res.data.vipBasicSetting;
                $("#input_mode"+bs.autoOrEnter).attr("checked",true);
                $("#show_mode"+bs.printOrShowLottery).attr("checked",true);
                $("#odds_type"+bs.realOrTransformOdds).attr("checked",true);
                if(bs.printScreencut==1){
                    $("#show_snapshot_input").attr("checked",true);
                }else{
                    $("#show_snapshot_input").attr("checked",false);
                }
                $("input[name='store_name']").val(bs.saleName);
                if(res.data.fundMode==0){
                    $("#fundMode").html($i18np.prop("user.cashBalance"));
                }else{
                    $("#fundMode").html($i18np.prop("user.creditBalance"));
                }
                $("#leftCredit").html(res.data.totalCredit);
            }
        }
    })
}

function __prList() {
    $.ajax({
        url:HOST+'vipInfo/selectList',
        type:'get',
        success:function (res) {
            if(res.code==0){
                var resData = res.data;
                var bodyData = "";
                for(var i=0,len=resData.length;i<len;i++){
                    var item = resData[i];
                    if(item.lotterySettingList.length>1){
                        bodyData = bodyData + __buildExpandRows(item);
                    }else{
                        bodyData = bodyData + __buildOneRow(item)
                    }
                }
                $("#tbody").html(bodyData);
            }
        }
    })
}


//修改赔率和回水
function changeHSAndPr(selectObj){
    var id = selectObj.id;
    var idx = selectObj.selectedIndex;
    var first = $(selectObj).attr("first");
    var groupId =  $(selectObj).attr("groupId");
    if(id.indexOf("hs")>-1){
        //修改了回水
        var prSelectorId = id.replace('hs','pr');
        $("#"+prSelectorId).get(0).selectedIndex=idx;
        console.log("idx==",idx)
        if(first=="1"){
            //修改了第1个,同类型都修改
            $(selectObj).parents("tr").siblings().each(function(it1){
                $(this).find("select").each(function(it2){
                    if($(this).attr('groupId')===groupId) {
                        var options =  $(this).get(0).options;
                        if(options.length>idx+1){//同类型其他的下拉框的选项长度大于第一个选中的下标，则同步修改
                            $(this).get(0).selectedIndex = idx
                        }
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
                        var options =  $(this).get(0).options;
                        if(options.length>idx+1){//同类型其他的下拉框的选项长度大于第一个选中的下标，则同步修改
                            $(this).get(0).selectedIndex = idx
                        }
                    }
                })
            });
        }
    }
}

//回水选择器
function __buildHuiShuiSelector(lsId,lmId,dataList,selectValue,idx){
    var objId ="hs-"+lmId+"-"+lsId+"-"+idx;
    var selectObj = '';
    if(idx==0){
        selectObj = '<select id="'+objId+'" index="'+idx+'" first="1" selectall="1" _name="return_water" groupId="group'+lmId+'" class="w70 s_erd" style="height: 24px" onchange="changeHSAndPr(this)">'
    }else{
        selectObj = '<select id="'+objId+'" index="'+idx+'" first="0" selectall="0" _name="return_water" groupId="group'+lmId+'" class="w70 s_erd" style="height: 24px" onchange="changeHSAndPr(this)">'
    }
    if(null!=dataList){
        for(var i=0,len=dataList.length;i<len;i++){
            var option = "";
            var item = dataList[i];
            option = (item.earnWater==selectValue)?
                '<option value="'+item.earnWater+'" selected>'+item.earnWater+'</option>'
                :
                '<option value="'+item.earnWater+'">'+item.earnWater+'</option>'

            selectObj = selectObj + option;

        }
    }

    selectObj = selectObj+'</select>';
    return selectObj;
}

//赔率选择器
function __buildPeiRateSelector(lsId,lmId,dataList,selectValue,idx,prLimit){
    var objId ="pr-"+lmId+"-"+lsId+"-"+idx;

    var wclass = 'w70';
    if(lmId=='5' || lmId=='6' || lmId=='7'){
        wclass = 'w90'
    }
    var selectObj = '';
    if(idx==1){
        selectObj = '<select id="'+objId+'" index="'+idx+'" first="1" selectall="1" _name="odds_limit" groupId="group'+lmId+'" class="'+wclass+' s_erd" style="height: 24px;width: 120px" onchange="changeHSAndPr(this)">'
    }else{
        selectObj = '<select id="'+objId+'" index="'+idx+'" first="0" selectall="0" _name="odds_limit" groupId="group'+lmId+'" class="'+wclass+' s_erd" style="height: 24px;width: 120px" onchange="changeHSAndPr(this)">'
    }
    if(null!=dataList) {
        for (var i = 0, len = dataList.length; i < len; i++) {
            var option = "";
            var item = dataList[i];
            option = (item.peiRate == selectValue) ?
                '<option value="' + item.peiRate + '" selected>' + item.peiRate + '</option>'
                :
                '<option value="' + item.peiRate + '">' + item.peiRate + '</option>'

            selectObj = selectObj + option;

        }
    }
    selectObj = selectObj+'</select>';
    return selectObj;
}


function  __buildSubTable(dataList) {
    var tbodyData = "";
    dataList.forEach((item,idex)=>{
        if(item.typeId!=0){
            var tr = __createLotterySettingRow(item,idex);
            tbodyData = tbodyData + tr;
        }
    })
    return '<table class="t-1">' +'<tbody>'+tbodyData+'</tbody>'+ '</table>';
}


function __createLotterySettingRow(item,idex){
    var peiRateRangeList = item.peiRateRangeList;
    var huishuiSelector = __buildHuiShuiSelector(item.id,item.lotteryMethodId,peiRateRangeList,item.huiShui,idex);
    var prSelector = __buildPeiRateSelector(item.id,item.lotteryMethodId,peiRateRangeList,item.vipPeiRate,idex,item.peiRate);
    var name = item.bettingRule;
    // if(item.lotteryMethodId=="4" || item.lotteryMethodId=="5" || item.lotteryMethodId=="6" || item.lotteryMethodId=="7"){
    //     name = getLotteryMethodI18nName(item.lotteryMethodId); //国际化名称
    // }

        var tr = '<tr class="fn-hover"> <td width="6%"></td> <td width="13%">'+name+'</td>\n' +
            ' <td width="10%">'+item.minBettingPrice+'</td> ' +
            '<td width="13%">'+item.vipPeiRate+'</td> ' +
            '<td width="13%">'+item.maxBettingCount+'</td> ' +
            '<td width="13%">'+item.maxNumberTypeCount+'</td> ' +
            '<td width="13%">'+huishuiSelector+'</td>\n'+
            ' <td width="19%">'+prSelector+'</td>\n' +
            ' </tr>';
        return tr;


}

//根据大分类ID获取国际化名称
function getLotteryMethodI18nName(lmId){
    var name = "";
    switch(lmId){
        case "1":
            name = '<span i18n="user.yiding">'+$i18np.prop("user.yiding")+'</span>';
            break;
        case "2":
            name = '<span i18n="user.erding">'+$i18np.prop("user.erding")+'</span>';
            break;
        case "3":
            name = '<span i18n="user.sanding">'+$i18np.prop("user.sanding")+'</span>';
            break;
        case "4":
            name = '<span i18n="user.siding">'+$i18np.prop("user.siding")+'</span>';
            break;
        case "5":
            name = '<span i18n="user.erxian">'+$i18np.prop("user.erxian")+'</span>';
            break;
        case "6":
            name = '<span i18n="user.sanxian">'+$i18np.prop("user.sanxian")+'</span>';
            break;
        case "7":
            name = '<span i18n="user.sixian">'+$i18np.prop("user.sixian")+'</span>';
            break;
    }
    return name;
}

/**
 *
 * <tr id="td_erd"> <td colspan="8" style="padding:4px;">
 <table class="t-1">
 <tbody>
 <tr class="fn-hover"> <td width="6%"></td> <td width="13%">口口XX</td>
 <td width="10%">1</td> <td width="13%">98</td> <td width="13%">2000</td> <td width="13%">90000</td> <td width="13%"><select id="1" index="0" first="1" selectall="1" _name="return_water" class="w70 s_erd"><option value="0.18">0.18</option><option value="0.179">0.179</option></select>  </td>
 <td width="19%"> <select index="0" selectall="1" _name="odds_limit" class="s_a s_erd"><option value="80">80</option><option value="80.1">80.1</option><option value="80.2">80.2</option></select>
 </td>
 </tr>
 *
 *
 *
 * @param item
 * @returns {string}
 * @private
 */

function __buildExpandRows(item){
    var id = item.id;
    var name = item.bettingMethod;
    var expandRow = '<tr class="bg3"><td><span href="javascript:void(0)" class="fn-ico-switch btn-pointer" _target="td_erd" onclick="expand(this,'+id+')">' +
        '<img src="../img/ico-close-large.gif"></span></td>' +
        '<td><span href="javascript:void(0)" class="btn-pointer" status="1" act="erd">'+name+'</span></td> ' +
        '<td></td> <td></td> <td></td> <td></td> <td></td> <td></td>' +
        '</tr>';
    var subTable = __buildSubTable(item.lotterySettingList);
    var subTr = '<tr id="subtable'+id+'"> <td colspan="8" style="padding:4px;">'+subTable+'</td></tr>';
    return expandRow + subTr;
}

function __buildOneRow(item){
    var ls = item.lotterySettingList[0];
    return __createLotterySettingRow(ls);
}

//展开与折叠
function expand(obj,id){
    $subTable = $("#subtable"+id);
    if($subTable.hasClass('hide')){
        $subTable.removeClass('hide');
        $(obj).html('<img src="../img/ico-close-large.gif">');
    }else{
        $(obj).html('<img src="../img/ico-open-large.gif">');
        $subTable.addClass('hide');
    }
}




$("#memberbut").on("click","",function (res) {
    bs.saleName = $("input[name='store_name']").val();
    bs.printScreencut = document.getElementById("show_snapshot_input").checked==true?1:0;
    bs.printOrShowLottery =  $("input[name='show_mode']:checked").val();
    bs.autoOrEnter = $("input[name='input_mode']:checked").val();
    bs.realOrTransformOdds = $("input[name='odds_type']:checked").val();
    var params = [];
    $("#tbody").find("tr").each(function(e){
        var ps = null;
        var hs = null;
        var lsId = null;
        $(this).find("select").each(function(e1){
            var id = this.id;
            var arr = id.split("-");
            lsId = arr[2];
            var pfx = arr[0];
            if(pfx === 'hs'){
                hs = this.value;
            }else{
                ps = this.value;
            }
        })
        if(ps!=null){
            params.push({
                lotterySettingId:lsId,
                earnWater:hs,
                peiRate:ps
            });
        }
    })
    bs.prSetList = params;
    layer.msg($i18np.prop("user.chulizhong"), {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+'vipInfo/upBasicSetting',
        type:'post',
        data:JSON.stringify(bs),
        contentType:"application/json",
        success:function (res) {
            layer.closeAll()
            if(res.code==0){
                layer.msg($i18np.prop("user.success"));
                window.parent.getInfo();
                __bs();
                __prList();
            }else{
                layer.msg(res.msg);
            }
        },
        error:function (e) {
            layer.closeAll();
        }
    })
    return false;
})



/**
 ** 减法函数，用来得到精确的减法结果
 ** 1.5 - 1.2 = 0.30000000000000004
 ** 说明：javascript的减法结果会有误差，在两个浮点数相减的时候会比较明显。这个函数返回较为精确的减法结果。
 ** 调用：accSub(arg1,arg2)
 ** 返回值：arg1加上arg2的精确结果
 **/
function accSub(arg1, arg2){
    var r1, r2, m, n
    try {
        r1 = arg1.toString().split('.')[1].length
    } catch (e) {
        r1 = 0
    }
    try {
        r2 = arg2.toString().split('.')[1].length
    } catch (e) {
        r2 = 0
    }
    m = Math.pow(10, Math.max(r1, r2)) // last modify by deeka //动态控制精度长度
    n = r1 >= r2 ? r1 : r2
    return ((arg1 * m - arg2 * m) / m).toFixed(n)
}

