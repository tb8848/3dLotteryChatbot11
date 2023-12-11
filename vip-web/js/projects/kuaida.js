var $ = layui.jquery;
var glayer = layui.layer;
var printInfo={
    batchNo:'',
    printCount:1,
    screenCutCount:1,
    printTime:'',
    printNo:'',
    printCacheId:'',
    totalAmount:"",
    totalMoney:"",
    dataList:[],
};
var buyRecord={
    "buyCodes":'',
    "buyMoney":'',
    "drawId":'',
    "hasOneFlag":0,
    "lotterSettingId":'',
    "codesFrom":3,
    "quanzhuan":0
};

var stopBuyPager={
  pages:0,
  currpage:1,
  total:0
};

var delStopBuyPager={
    pages:0,
    currpage:1,
    total:0
};

var backCodesArr=[];
var stopCodesArr=[];

var drawId = "";//当前查询期数
var drawNo = '';//期号

var stopCodesList=[];

var xzuuid = "";

window.onload=function () {
    getUUID();
}


function getUUID(){
    $.ajax({
        url:HOST+'getUUid',
        type:'get',
        success:function(res){
            if(res.code==0){
                xzuuid = res.data.saveXZ;
            }
        },
        error:function (e) {

        }
    });
}

function upOpenStatus(msg) {
    var drawOpenStatus = msg.openStatus;
    if(drawOpenStatus==1){
        $("#hasDataShow").removeClass('hide');
        $("#noDataShow").addClass('hide');
        if(drawNo!=msg.drawNo){
            drawNo = msg.drawNo;
            drawId = msg.id;
            initBuyList();
            initDrawList();
            getStopBuyCodes(0);
            getStopBuyCodes(1)
        }

    }else{
        $("#hasDataShow").addClass('hide');
        $("#noDataShow").removeClass('hide');
        if(drawOpenStatus==3){
            $("#openStatusDesc").html('<span i18n="kuaida.drawOpening">'+$i18np.prop('kuaida.drawOpening')+'</span>');
        }else if(drawOpenStatus==2){
            $("#openStatusDesc").html('<span i18n="kuaida.drawUnopen">'+$i18np.prop('kuaida.drawUnopen')+'</span>');
        }else if(drawOpenStatus==0){
            $("#openStatusDesc").html('<span i18n="kuaida.drawClose">'+$i18np.prop('kuaida.drawClose')+'</span>');
        }
    }
}



$("#period").on('change',"",function (e) {
    drawId = this.value;
    stopCodesList=[];
    getStopBuyCodes(0);
    getStopBuyCodes(1);
})


function bpcodesDetail(rid,codes,groupCount){
    console.log(codes,groupCount);
    var html = $("#bp-"+rid).html();
    console.log(html);
    //var title = html.substr(html.lastIndexOf(",")+1);
    var title = codes +","+$i18np.prop("kuaida.baopai")+groupCount+$i18np.prop("kuaida.group");
    sessionStorage.setItem("bptitle",title);
    sessionStorage.setItem("bprid",rid);
    sessionStorage.setItem("mingxi_drawNo",drawNo);
    window.parent.open("../xiazhu/baopaiDetail.html");
}

function doFastBuy(buyRecord){
    buyRecord.drawId = drawId;
    glayer.msg($i18np.prop("kuaida.chulizhong"), {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+'v1/draw/buy?xzuuid='+xzuuid,
        type:'post',
        data:JSON.stringify(buyRecord),
        success:function(res){
            glayer.closeAll();
            $("#vipPeiRate").html("");
            $("#leftBuy").html("");
            $("#money").val("");
            $("#numberNo").val("");
            getUUID();
            if(res.code==0){
                if(res.data.errcode==0){
                    buyRecord.lotterSettingId = '';
                    buyRecord.hasOneFlag='';
                    initBuyList();
                    window.parent.getCredit();
                    window.parent.reloadPrintTable();
                }else{
                    glayer.msg(res.data.errmsg);
                }

            }else{
                glayer.msg(res.msg);
                if(res.code==1){
                    getStopBuyCodes(0);
                }
            }
        },
        fail:function (e) {
            glayer.closeAll();
        }
    });
    return false;
}

function getData(){
    layer.msg($i18np.prop("kuaida.dataLoading"),{time:-1,shade:0.3,icon:16})
    $.ajax({
        url:HOST+'code/drawInfo',
        type:'get',
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                drawId = res.data.id;
                drawNo = res.data.drawNo
                sessionStorage.setItem("drawNo",drawNo);
                $("#drawNo").html(drawNo);
                $("#drawNo1").html(drawNo);
                upOpenStatus(res.data);
                if(res.data.openStatus==1){
                    initBuyList();
                    initDrawList();
                    getStopBuyCodes(0);
                    getStopBuyCodes(1)
                }
            }
        },
        error:function (e) {
            layer.closeAll();
        }
    });
}


function initDrawList(){
    $.ajax({
        url:HOST+'draw/listN?size=3',
        type:'get',
        success:function(res){
            if(res.code==0){
                res.data.records.forEach((item,idx)=>{
                    $("#period").append('<option value="'+item.id+'">'+item.drawId+'</option>');
                })
            }
        }
    });
}


function __emptyRows(size){
    var hm = '';
    for(var i=0;i<size;i++){
        hm = hm + '<tr><td>--</td><td>--</td><td class="fb green">--</td>' +
            '<td class="fb">--</td><td class="fb red">--</td><td>--</td><td>--</td></tr>';
    }
    return hm;
}

function __buyRow(item){
    var c2 = item.printId;
    var c3 = "";
    if(item.buyType==2){
        var bpName = item.buyCodes+","+'<span i18n="kuaida.baopai">'+$i18np.prop("kuaida.baopai")+'</span>'+item.bpGroup+'<span i18n="kuaida.group">'+$i18np.prop("kuaida.group")+'</span>';
        c3= '<a href="javascript:void(0)" id="bp-'+item.id+'" style="color:blue;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\',\''+item.buyCodes+'\',\''+item.bpGroup+'\')">'+bpName+'</a>'
    }else{
        if(item.hasOneFlag==1){
            c3= '<span>'+item.buyCodes+'<span style="color: orangered;" i18n="kuaida.xian">'+$i18np.prop("kuaida.xian")+'</span></span>'
        }else{
            c3= item.buyCodes;
        }
    }
    var mm =item.buyMoney;
    if(item.buyType==2){
        mm = item.param1
    }

    var c4 = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.peiRate+'</a>' : item.peiRate;

    var c6 = item.backCodeFlag==0?'<span i18n="kuaida.success">'+$i18np.prop("kuaida.success")+'</span>':item.backCodeFlag==1?'<span i18n="kuaida.codeBacked">'+$i18np.prop("kuaida.codeBacked")+'</span>':"--";
    var c5 = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+mm+'</a>' : mm;

    var c7 = item.backCodeStatus==1 && item.backCodeFlag==0?'<input type="checkbox" name="ids" value="'+item.id+'" onclick="chooseOne(this,\''+item.id+'\')">':'--';
    var bgColor = item.backCodeFlag==1?'bg4':'';
    var hm = '<tr class="font-color '+bgColor+'"><td i18n="kuaida.drawName">'+$i18np.prop("kuaida.drawName")+'</td>' +
        '<td>'+c2+'</td>\n' +
        '<td class="fb f15"> <span class="green">'+c3+'</span> </td>\n' +
        '<td class="fb f15"> '+c4+' </td> <td class="fb f15 red"> '+c5+' </td>\n' +
        '<td>  '+c6+'  </td>\n' +
        '<td class="print_area">'+c7+'</td>' +
        ' </tr>';
    return hm;
}


function initBuyList(){
    $.ajax({
        url:HOST+'drawBuyRecordPC/listTopN?drawId='+drawId,
        type:'get',
        success:function(res){
            if(res.code==0){
                var dlist = res.data;
                if(dlist.length<1){
                    $("#tbody").html(__emptyRows(10));
                    return;
                }
                var len = dlist.length;
                var bodystr = "";
                for(var i=0;i<len;i++){
                    var item = dlist[i];
                    var row = __buyRow(item);
                    bodystr = bodystr + row;
                }
                var leftLines = 10-i;
                if(leftLines>0){
                    for(var i=0;i<leftLines;i++){
                        bodystr = bodystr + __emptyRows(1);
                    }
                }
                $("#tbody").html(bodystr);
            }else{
                $("#tbody").html(__emptyRows(10));
            }
        }
    })
}


//退码
$("#cancelbet").on('click',"",function(e){
    if(backCodesArr.length<1){
        layer.msg($i18np.prop("kuaida.noCodeSelect"));
    }else{
        layer.msg($i18np.prop("kuaida.chulizhong"), {
            icon: 16
            ,shade: 0.3
            ,time:-1
        });
        $.ajax({
            url:HOST+'draw/delUnbuyCode',
            type:'post',
            contentType:'application/json',
            data:JSON.stringify({
                idArr:backCodesArr,
                drawId:drawNo+""
            }),
            success:function (res) {
                layer.closeAll();
                if(res.code==0){
                    layer.alert($i18np.prop("kuaida.backcode.success"),{title:$i18np.prop("kuaida.alertTitle"),btn:[$i18np.prop("kuaida.confirm")]});
                    backCodesArr = [];
                    initBuyList();
                    $("#selectAll").prop("checked",false);
                    window.parent.getCredit();
                    window.parent.reloadPrintTable();
                }else{
                    layer.msg(res.msg);
                }
                getUUID();
            },
            fail:function (err) {
                layer.closeAll();
            }
        })
    }
})


var selectedNums=0;
function chooseOne(obj,v){
    if(obj.checked){
        //选中
        if(!backCodesArr.includes(v)){
            backCodesArr.push(v);
        }
    }else{
        if(backCodesArr.includes(v)){
            var idx = backCodesArr.indexOf(v);
            if(idx>-1){
                backCodesArr.splice(idx,1)
            }
        }
    }
    var arr = $("#tbody").find(":checkbox");
    if(backCodesArr.length === arr.length){
        $("#selectAll").prop("checked",true)
    }else{
        $("#selectAll").prop("checked",false)
    }
    return false;
}




function chooseLackOne(obj,v){
    if(obj.checked){
        //选中
        if(!stopCodesArr.includes(v)){
            stopCodesArr.push(v);
        }
    }else{
        if(stopCodesArr.includes(v)){
            var idx = stopCodesArr.indexOf(v);
            if(idx>-1){
                stopCodesArr.splice(idx,1)
            }
        }
    }
    var arr = $("#tbody_lack").find(":checkbox:checked");
    if(stopCodesArr.length === arr.length){
        $("#selectAll_lack").attr("checked",true)
    }else{
        $("#selectAll_lack").attr("checked",false)
    }
    return false;
}

$("#selectAll").on("click","",function (e) {
    var _this = this;
    if(_this.checked){
        $("#tbody").find(":checkbox").each(function(item){
            this.checked = true;
            //选中
            if(!backCodesArr.includes(this.value)){
                backCodesArr.push(this.value);
            }
        })
        selectedNums = backCodesArr.length;
    }else{
        $("#tbody").find(":checkbox").each(function(item){
            this.checked = false;
        })
        backCodesArr = [];
        selectedNums = 0;
    }
})



$("#selectAll_lack").on("click","",function (e) {
    var _this = this;
    if(_this.checked){
        $("#tbody_lack").find(":checkbox").each(function(item){
            this.checked = true;
            //选中
            if(!stopCodesArr.includes(this.value)){
                stopCodesArr.push(this.value);
            }
        })
    }else{
        $("#tbody_lack").find(":checkbox").each(function(item){
            this.checked = false;
        })
        stopCodesArr = [];
    }
})



//极速下注
$("#fn-quickbet").on("click","",function (e) {
    var popw = 500,poph = 420;
    if(defaultLang=='th'){
        popw = 650;
    }
    layer.open({
        type: 2,
        offset: 'rb',
        area:[popw+'px', poph+'px'],
        content:'fastBuy.html',
        title:$i18np.prop("kuaida.fast")
    })
    return false;
})



//删除停押号码
$("#delStopBuyCodes").on("click","",function (e) {
    if(stopCodesArr.length<1){
        layer.msg($i18np.prop("req.noStopCodeSelect"));
        return;
    }
    layer.msg($i18np.prop("kuaida.chulizhong"), {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+'stopBuyCodes/delete',
        type:'post',
        data:JSON.stringify({
            idList:stopCodesArr
        }),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                getStopBuyCodes(0);
                getStopBuyCodes(1)
            }else{
                layer.msg(res.msg);
            }
        },
        error:function (e) {
            layer.closeAll();
            layer.msg($i18np.prop("req.error"));
        }
    })
});

//四字现 复选框
$("#toxian").on("click","",function(){
    var buyCodes = $("#betno").val();
    $("#allconvert").prop("checked",false)
    buyRecord.quanzhuan=0;
    if(this.checked){
        $("#xian").removeClass('hide');
        buyRecord.hasOneFlag=1;
    }else{
        $("#xian").addClass('hide');
        buyRecord.hasOneFlag=0;
    }
    if(buyCodes.length>0){
        getPeiRate();
    }
})

//全转 复选框
$("#allconvert").on("click","",function(){
    var buyCodes = $("#betno").val();
    $("#toxian").prop("checked",false)
    $("#xian").addClass('hide');
    buyRecord.hasOneFlag=0;
    if(this.checked){
        buyRecord.quanzhuan=1;
    }else{
        buyRecord.quanzhuan=0;
    }
    if(buyCodes.length>0){
        getPeiRate();
    }
})

//下注
$("#xiazhu").on('click',"",function(d){
    var buyCode = $("#betno").val();
    if(buyCode==''){
        layer.msg($i18np.prop("kuaida.noCodeInput"))
        return;
    }
    if(buyCode.toLowerCase()=='xxxx'){
        layer.msg($i18np.prop("kuaida.codeError"))
        return;
    }
    if(buyCode.length<4 && buyCode.indexOf('x')>-1){
        layer.msg($i18np.prop("kuaida.codeError"))
        return;
    }
    var money = $("#betmoney").val();
    if(money==''){
        layer.msg($i18np.prop("kuaida.noMoneyInput"))
        return;
    }
    try{
        money = parseFloat(money);
    }catch (e) {
        layer.msg($i18np.prop("kuaida.moneyError"))
        return;
    }
    if(buyRecord.lotterSettingId==""){
        return;
    }
    buyCode = buyCode.toUpperCase();
    if(buyCode.length<4){
        if(buyCode.indexOf('X')<0){
            buyRecord.hasOneFlag = 1;
        }
    }else{
        buyRecord.hasOneFlag = 0;
        if(buyCode.indexOf('X')<0){
            if($("#toxian").attr("checked")){
                buyRecord.hasOneFlag = 1;
                buyRecord.quanzhuan = 0;
            }
        }
    }
    buyRecord.buyCodes = buyCode.toUpperCase();
    buyRecord.buyMoney = money;
    buyRecord.drawId = drawId;

    layer.msg($i18np.prop("kuaida.chulizhong"), {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+'v1/draw/buy?xzuuid='+xzuuid,
        type:'post',
        data:JSON.stringify(buyRecord),
        success:function(res){
            layer.closeAll();
            $("#limit_odds").html("");
            $("#limit_betmoney").html("");
            $("#betno").val("");
            $("#toxian").prop("checked",false);
            $("#allconvert").prop("checked",false);
            $("#xian").addClass('hide');
            if(res.code==0){
                if(res.data.errcode==0){
                    buyRecord.lotterSettingId = '';
                    buyRecord.hasOneFlag='';
                    buyRecord.quanzhuan=0;
                    initBuyList()
                    window.parent.getCredit();
                    window.parent.reloadPrintTable();
                    __prShow(false);
                }else{
                    layer.alert(res.msg,{title:$i18np.prop("kuaida.alertTitle"),btn:[$i18np.prop("kuaida.confirm")]});
                }
                getUUID();
                getStopBuyCodes(0)
            }else{
                getUUID();
                layer.msg(res.msg);
                getStopBuyCodes(0)
            }
        },
        error:function (e) {
            layer.closeAll();
        }
    });
    return false;

})


function getStopBuyCodes(delFlag){
    var qs = 'drawId='+drawId+"&deleteFlag="+delFlag;
    if(delFlag==0){
        qs = qs +"&page="+stopBuyPager.currpage;
    }else{
        qs = qs +"&page="+delStopBuyPager.currpage;
    }
    $.ajax({
        url:HOST+'stopBuyCodes/pc/listByPage?'+qs,
        type:'get',
        success:function(res){
            if(res.code==0){
                if(delFlag == 0){
                    stopBuyPager.pages = res.count;
                    __initStopBuyPager();
                    //未删除
                    __initUndelCodesTable(res);
                }else if(delFlag==1){
                    delStopBuyPager.pages = res.count;
                    __initDelStopBuyPager();
                    //已删除
                    __initHasDelCodesTable(res);
                }
            }
        }
    })
}

//新增
function __initStopBuyPager(){
    $("#stopBuy-pages").html(stopBuyPager.pages);
    $("#stopBuy-currpage").html(stopBuyPager.currpage);
    if(stopBuyPager.pages>1){
        $("#stopBuy-pageRow").removeClass('hide')
    }else{
        $("#stopBuy-pageRow").addClass('hide')
    }
}

$("#stopBuy-nextPage").on("click","",function (e) {
    if(stopBuyPager.currpage<stopBuyPager.pages){
        stopBuyPager.currpage++;
        $("#stopBuy-currpage").html(stopBuyPager.currpage);
        getStopBuyCodes(0)
    }
});

$("#stopBuy-prevPage").on("click","",function (e) {
    if(stopBuyPager.currpage>1){
        stopBuyPager.currpage--;
        $("#stopBuy-currpage").html(stopBuyPager.currpage);
        getStopBuyCodes(0)
    }
});


//新增
function __initDelStopBuyPager(){
    $("#delStop-pages").html(delStopBuyPager.pages);
    $("#delStop-currpage").html(delStopBuyPager.currpage);
    if(delStopBuyPager.pages>1){
        $("#delStop-pageRow").removeClass('hide')
    }else{
        $("#delStop-pageRow").addClass('hide')
    }
}

$("#delStop-nextPage").on("click","",function (e) {
    if(delStopBuyPager.currpage<delStopBuyPager.pages){
        delStopBuyPager.currpage++;
        $("#delStop-currpage").html(delStopBuyPager.currpage);
        getStopBuyCodes(1)
    }
});

$("#delStop-prevPage").on("click","",function (e) {
    if(delStopBuyPager.currpage>1){
        delStopBuyPager.currpage--;
        $("#delStop-currpage").html(delStopBuyPager.currpage);
        getStopBuyCodes(1)
    }
});

//停押号码区
function __initUndelCodesTable(resData){
    $("#stopBuy-totalMoney").html(resData.data.totalM);
    if(resData.count>0){
        $("#stopBuy-bishu").html(resData.data.totolBishu);
        stopCodesList = resData.data.dataList;
        var dlist = resData.data.dataList;
        var bodyhtml = "";
        if(dlist.length>0){
            dlist.forEach((item,idx)=>{
                var codes = item.codes;
                if(item.bpSettingId!='0'){
                    codes = item.codes+","+'<span i18n="kuaida.baopai">'+$i18np.prop("kuaida.baopai")+'</span>'+item.groupCount+'<span i18n="kuaida.group">'+$i18np.prop("kuaida.group")+'</span>'
                }
                bodyhtml = bodyhtml +'<tr><td>'+codes+'</td><td>'+item.buyMoney+'</td>' +
                    '<td><input type="checkbox" value="'+item.id+'" onclick="chooseLackOne(this,\''+item.id+'\')"> </td></tr>'
            });
            $("#tbody_lack").html(bodyhtml);
        }
    }else{
        $("#stopBuy-totalMoney").html("0");
        $("#stopBuy-bishu").html("0");
        var emptyTip = '<tr><td colspan="3" class="blank_area"></td></tr>';
            // '<tr> <td colspan="3" class="fb tfoot"> 笔数:0&nbsp;&nbsp;总金额:0 </td></tr>';
        $("#tbody_lack").html(emptyTip);
    }
}


//删除停押号码保留区
function __initHasDelCodesTable(resData){
    $("#delStop-totalMoney").html(resData.data.totalM);
    if(resData.count>0){
        $("#delStop-bishu").html(resData.data.totolBishu);
        var dlist = resData.data.dataList;
        var bodyhtml = "";
        if(dlist.length>0){
            dlist.forEach((item,idx)=>{
                var codes = item.codes;
                if(item.bpSettingId!='0'){
                    codes = item.codes+","+'<span i18n="kuaida.baopai">'+$i18np.prop("kuaida.baopai")+'</span>'+item.groupCount+'<span i18n="kuaida.group">'+$i18np.prop("kuaida.group")+'</span>'
                }
                bodyhtml = bodyhtml +'<tr><td>'+codes+'</td><td>'+item.buyMoney+'</td></tr>'
            });
            $("#betlackKeep").html(bodyhtml);
        }
    }else{
        $("#delStop-bishu").html("0");
        $("#delStop-totalMoney").html("0");
        var emptyTip = '<tr><td colspan="3" class="blank_area"></td></tr>'
            // +
            // '<tr> <td colspan="3" class="fb tfoot"> 笔数:0&nbsp;&nbsp;总金额:0 </td></tr>';
        $("#betlackKeep").html(emptyTip);
    }
}



$("#delbetlack").on("click","",function (e) {
    if(stopCodesArr.length<1){
        layer.msg($i18np.prop("kuaida.noStopCodeSelect"));
        return;
    }
    layer.msg($i18np.prop("kuaida.chulizhong"), {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+'stopBuyCodes/delete',
        type:'post',
        contentType:'application/json',
        data:JSON.stringify({
            idList:stopCodesArr
        }),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                getStopBuyCodes(0);
                getStopBuyCodes(1);
                $("#selectAll_lack").attr("checked",false)
            }else{
                layer.msg(res.msg);
            }
        },
        error:function (e) {
            layer.closeAll();
            layer.msg($i18np.prop("req.error"));
        }
    })
    return false;
})

//号码
$("#betno").on("keyup","",function(e){

    if(e.key=='X' || e.key=='x' || (e.key>='0' && e.key<='9')){

    }else{
        this.value = this.value.substr(0,this.value.length-1);
    }
    if(this.value.length==4){
        $("#betmoney").focus();
    }
    this.value = this.value.toUpperCase();
    if(this.value==""){
        $("#xian").addClass('hide');
        $("#limit_odds").html("");
        $("#limit_betmoney").html("");
    }else{
        if(this.value.indexOf('X')<0){
            $("#xian").removeClass('hide');
            if(this.value.length==4){
                if($("#toxian").attr("checked")){
                    $("#xian").removeClass('hide');
                    buyRecord.hasOneFlag=1;
                }else{
                    $("#xian").addClass('hide');
                    buyRecord.hasOneFlag=0;
                }
            }else{
                buyRecord.hasOneFlag=1
            }
        }else{
            $("#xian").addClass('hide');
            $("#toxian").prop("checked",false)
            buyRecord.hasOneFlag=0
        }
        getPeiRate();
    }

});

//金额
$("#betmoney").on("keyup","",function(e){
    if(e.key=='.' || (e.key>='0' && e.key<='9')){
        if(this.value.indexOf(".")==0){
            this.value = this.value.substr(1,this.value.length-1);
        }
    }else{
        this.value = this.value.substr(0,this.value.length-1);
    }
});

function getPeiRate(){
    var buyCodes = $("#betno").val();
    buyCodes = buyCodes.toUpperCase();
    var isXian = 0;
    if(buyCodes.indexOf("X")<0){//不包含定位
        if(buyCodes.length<2){
            __prShow(false);
            return;
        }
        if(buyCodes.length==4){
            if(buyRecord.hasOneFlag==1){
                isXian = 1;
            }else{
                isXian=0;
            }
        }else{
            isXian = 1;
        }
    }else{
        if(buyCodes=='XXXX'){
            __prShow(false);
            return;
        }
        if(buyCodes.length<4){
            __prShow(false);
            return;
        }
    }
    $.ajax({
        url:HOST+'draw/getPeiRate',
        type:'post',
        contentType:'application/json',
        data:JSON.stringify({
            buyCodes:buyCodes.toUpperCase(),
            drawId:drawId,
            hasOneFlag:isXian
        }),
        success:function(res){
            if(res.code==0){
                $("#limit_odds").html(res.data.peiRate);
                $("#limit_betmoney").html(res.data.maxNumberTypeCount);
                __prShow(true);
                buyRecord.lotterSettingId = res.data.settingId;
            }else{
                __prShow(false);
                buyRecord.lotterSettingId = "";
            }
        }
    })
}



function __prShow(isShow){
    if(isShow){
        $("#betno_odds").show();
        $("#betno_odds_money").show();
    }else{
        $("#limit_odds").html("");
        $("#limit_betmoney").html("");
        $("#betno_odds").hide();
        $("#betno_odds_money").hide();
    }
}
