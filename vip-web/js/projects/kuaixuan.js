var rules = {
    "isXian":0,
        "count":1,
        "lotteryMethodId":'',
        "fixCode":{
        "name":'定位置',
            "included":1,
            "excluded":0,
            "loc1":"",
            "loc2":"",
            "loc3":"",
            "loc4":"",
            "isShow":true
    },
    "matchCodes":{
        "name":'配数',
            "included":0,
            "excluded":0,
            "loc1":"",
            "loc2":"",
            "loc3":"",
            "loc4":"",
            "isXian":0,
            "isShow":false
    },
    "repeat":{
        "double1":{
            "name":"双重",
                "included":0,
                "excluded":0,
                "nums":2,
        },
        "triple":{
            "name":"三重",
                "included":0,
                "excluded":0,
                "nums":3,
        },
        "double2":{
            "name":"四重",
                "included":0,
                "excluded":0,
                "nums":4,
        },
        "doubledouble":{
            "name":"双双重",
                "included":0,
                "excluded":0,
                "nums":22,
        }
    },
    "brothers":{
        "br2":{
            "name":"两兄弟",
                "included":0,
                "excluded":0,
                "nums":2,
        },
        "br3":{
            "name":"三兄弟",
                "included":0,
                "excluded":0,
                "nums":3,
        },
        "br4":{
            "name":"四兄弟",
                "included":0,
                "excluded":0,
                "nums":4,
        }
    },
    "pairs":{
        "name":"对数",
            "included":0,
            "excluded":0,
            "pair1":"",
            "pair2":"",
            "pair3":"",
            "pair4":"",
            "pair5":""
    },
    "singleNum":{
        "name":"单数",
            "included":0,
            "excluded":0,
            "locArr":[0,0,0,0],
    },
    "doubleNum":{
        "name":"双数",
            "included":0,
            "excluded":0,
            "locArr":[0,0,0,0],
    },
    "bigNum":{
        "name":"大数",
            "included":0,
            "excluded":0,
            "locArr":[0,0,0,0],
    },
    "smallNum":{
        "name":"小数",
            "included":0, //1:取
            "excluded":0, //1：除
            "locArr":[0,0,0,0],//位置,

    },
    "sum2":{
        "name":"两(或三)数合",
            "included":0, //1:取
            "excluded":0, //1：除
            "sum":"",
            "sumType":0, //2：两数和；3：三数和
    },
    "others":{
        "name":'其他规则',
            "fullChange":'', //全转
            "shangJiang":'', //上奖
            'excludes':'', //排除
            'locArr':[0,0,0,0],//乘号规则
            "valueRange":["",""] //值范围
    },
    "fushi":{
        "name":'复式规则',
            "included":1,
            "excluded":0,
            "contains":'', //含
            "combines":'' //复式
    },
    "hefen":{
        "name":'合分',
            "included":1,
            "excluded":0,
            "binds":[
            {
                "name":'',
                "value":'',
                "locArr":[0,0,0,0]
            },
            {
                "name":'',
                "value":'',
                "locArr":[0,0,0,0]
            },
            {
                "name":'',
                "value":'',
                "locArr":[0,0,0,0]
            },
            {
                "name":'',
                "value":'',
                "locArr":[0,0,0,0]
            }
        ]
    }
};

var $ = layui.$;

var layer = layui.layer;
var lotteryMethodId = null;
var lotteryMethodList=[];
var lotteryMethodIdx=0;
var ruleItemIndex = 0;
var isXian = 0;
var codesNum = 0;
var codesList = [];
var emptyRules = JSON.stringify(rules);

function resetXuan(){
    rules = JSON.parse(emptyRules);
    $("#bd_betno").html("");
    $("#numberCount").val("");
    $("#numberAmount").html("");
    $("#bet_money").html("");
    if(lotteryMethodList.length>0){
        lotteryMethodId = lotteryMethodList[1].id;
        rules.lotteryMethodId = lotteryMethodList[1].id;
        initData();
        handleEleHideOrShow(null,lotteryMethodId,1);
    }else{
        getLotteryMethods();
    }

}

function _getRuleName(){
    var rname = "";
    lotteryMethodList.forEach((item,idx)=>{
        if(item.id == lotteryMethodId){
            rname = item.bettingMethod;
        }
    })
    return rname;
}

function initHtml(){
    for (var html = "", i = 1; i <= 10; i++)
        html += "<tr><td width='12.5%'>--</td><td width='12.5%'>--</td><td width='12.5%'>--</td>" +
            "<td width='12.5%'>--</td><td width='12.5%'>--</td><td width='12.5%'>--</td><td width='12.5%'>--</td><td width='12.5%'>--</td></tr>";
    return html
}

window.onload=function(){
    getLotteryMethods();
    getDrawInfo();
}

function getLotteryMethods(){
    $.ajax({
        url:HOST+'draw/lotteryMethods?lotterType=1',
        type:'get',
        success:function(res){
            if(res.code==0){
                lotteryMethodList = res.data;
                if(res.data.length>0){
                    lotteryMethodId = lotteryMethodList[1].id;
                    rules.lotteryMethodId = lotteryMethodList[1].id;
                    initData();
                    handleEleHideOrShow(null,lotteryMethodId,1);
                }

            }
        }
    })
}


function getDrawInfo(){
    $.ajax({
        url:HOST+'code/drawInfo',
        type:'get',
        success:function(res){
            if(res.code==0){
                upOpenStatus(res.data);
            }
        }
    });
}


function createCodes(){
    //alert(JSON.stringify(rules));
    layer.msg('正在处理...', {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+"code/create",
        type:'post',
        contentType:'application/json',
        data:JSON.stringify(rules),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                if(res.count<1){
                    layer.msg("未找到符合条件的号码");
                    //$("#showselectnumber").html(initHtml());
                }else{
                    codesList = res.data;
                    codesNum = res.count;
                    $("#numberCount").html(res.count);
                    initCodesToBuy(res.data)
                }
            }
        },
        error:function(){
            layer.closeAll();
        }
    })
    return false;
}


function moneyInput(obj){
    if(codesNum>0){
        var money = parseFloat(obj.value)*codesNum;
        $("#numberAmount").html(money);
    }else{
        $("#numberAmount").html("");
    }
    return false;
}

$("#bet_money").on("keyup","",function (e) {//金额输入框
    if((e.key>='0' && e.key<='9') || e.key=="."){

    }else{
        this.value = this.value.substr(0,this.value.length-1);
        if(this.value.startsWith(".")){
            this.value = this.value.substr(1,this.value.length-1);
        }
    }
    moneyInput(this);
    // var v = this.value;
    // if(noMoneyList.length>0 && v!=""){
    //     $(".keyup-total-money").html(noMoneyList.length * parseFloat(v))
    // }else{
    //     $(".keyup-total-money").html("0")
    // }
})

//下注
$("#btn_bet").on("click","",function(){
    var money = $("#bet_money").val();
    if(money==""){
        layer.msg("请输入金额")
        return;
    }
    if(codesNum>0){
        layer.msg('正在处理...', {
            icon: 16
            ,shade: 0.3
            ,time:-1
        });
        $.ajax({
            //url:HOST+'draw/batchBuy',
            url:HOST+'codes/batchBuy',
            type:'post',
            contentType:'application/json',
            data:JSON.stringify({
                "batchCodesList":codesList,
                "lotteryMethodId":lotteryMethodId,
                "isXian":isXian,
                "totalMoney":"",
                "buyMoney":money,
                "rules":rules,
                "codesFrom":4
            }),
            success:function (res) {
                layer.closeAll();
                if(res.code==0){
                    window.parent.toPage("kuaida");
                    window.parent.getCredit();
                    window.parent.reloadPrintTable();
                }else{
                    layer.msg(res.msg);
                }
            }
        })
    }
});


//录入汇总表
$("#btn_bet_will").on("click","",function(){
    var money = $("#bet_money").val();
    if(money==""){
        layer.msg("请输入金额")
        return;
    }
    if(codesNum>0){
        layer.msg('正在处理...', {
            icon: 16
            ,shade: 0.3
            ,time:-1
        });
        $.ajax({
            //url:HOST+'draw/batchBuy',
            url:HOST+'prebuy/batchBuy',
            type:'post',
            contentType:'application/json',
            data:JSON.stringify({
                "batchCodesList":codesList,
                "lotteryMethodId":lotteryMethodId,
                "isXian":isXian,
                "totalMoney":"",
                "buyMoney":money,
                "rules":rules,
                "codesFrom":4
            }),
            success:function (res) {
                layer.closeAll();
                if(res.code==0){
                    if(lotteryMethodId==2){
                        window.parent.toPage('erdPackage');
                    }else if(lotteryMethodId==3){
                        window.parent.toPage('sadPackage');
                    }else if(lotteryMethodId==4){
                    window.parent.toPage('sidPackage');
                }
                }else{
                    layer.msg(res.msg);
                }
            }
        })
    }
});


//包牌
$("#btn_bet_package").on("click","",function(){
    var money = $("#bet_money").val();
    if(money==""){
        layer.msg("请输入金额")
        return;
    }
    if(codesNum>0){
        layer.msg('正在处理...', {
            icon: 16
            ,shade: 0.3
            ,time:-1
        });
        $.ajax({
            //url:HOST+'draw/batchBuy',
            url:HOST+'baopai/batchBuy',
            type:'post',
            contentType:'application/json',
            data:JSON.stringify({
                "batchCodesList":codesList,
                "lotteryMethodId":lotteryMethodId,
                "isXian":isXian,
                "totalMoney":"",
                "buyMoney":money,
                "rules":rules,
                "codesFrom":4
            }),
            success:function (res) {
                layer.closeAll();
                if(res.code==0){
                    window.parent.toPage("kuaida");
                    window.parent.getCredit();
                    window.parent.reloadPrintTable();
                }else{
                    layer.msg(res.msg);
                }
            }
        })
    }
});

function initCodesToBuy(codesList){
    var rows = codesList.length%8==0 ? parseInt(codesList.length/8):parseInt(codesList.length/8)+1;
    var leftCodesNum = 0;
    if(codesList.length%8>0){
        leftCodesNum = 8-codesList.length%8;
    }
    var html = "";
    for(var i=0;i<rows;i++){
        var st = i*8;
        var et = (i+1)*8;
        if(et>codesList.length){
            et = codesList.length;
        }
        var r = '<tr>';
        for(var j=st;j<et;j++){
            r = r +"<td width='12.5%'>"+codesList[j].buyCode+"</td>";
        }
        console.log("i==rows-1",i,rows-1);
        if(i==rows-1){
            console.log("leftCodesNum2 ========= ",leftCodesNum);
            for(var k=0;k<leftCodesNum;k++){
                r = r +"<td width='12.5%'>--</td>";
            }
        }

        r = r+"</tr>"
        html = html+r;
    }
    for (var i=0;i<10-rows;i++){
        html = html + "<tr><td width='12%'>--</td width='12%'><td width='12%'>--</td><td width='12%'>--</td><td width='12%'>--</td><td width='12%'>--</td><td width='12%'>--</td><td width='12%'>--</td><td width='12%'>--</td></tr>"
    }
    var tabledata = '<table class="t-2 tc" id="numberList" cellspacing="0" cellpadding="0">'+'<tbody>'+html+'</tbody></table>'
    $("#bd_betno").html(tabledata);
}

function initData(){
    var html = '';
    lotteryMethodList.forEach((item,idx)=>{
        var styles = "background-color:green;color:white";
        var td = "";
        if(lotteryMethodId == item.id){
            td = '<td class="on" width="15%"><input id="kx_erd" type="button" value="'+item.bettingMethod+'" class="fn-module btn-large erd-btn" module="erd" onclick="handleEleHideOrShow(this,'+item.id+','+idx+')"></td>';
        }else{
            td = '<td width="15%"><input id="kx_erd" type="button" value="'+item.bettingMethod+'" class="fn-module btn-large erd-btn" module="erd" onclick="handleEleHideOrShow(this,'+item.id+','+idx+')"></td>';
        }
        html = html + td;
    });
    $("#lotteryMethodsContainer").html(html);

}

function clearInput() {
    //清除内容
    $("#filterContainer").find(":checkbox").attr("checked",false);
    $("#filterContainer").find(":input").val("");
    document.getElementById("__dingwei_qu").checked = true;
    document.getElementById("__hefen_qu").checked = true;
    document.getElementById("__peishu_qu2").checked = true;
    document.getElementById("__qu_1").checked = true;
}
function handleEleHideOrShow(obj,mid,idx){
    if(obj!=null){
        $(obj).parent('td').addClass("on");
        $(obj).parent('td').siblings().removeClass("on")
    }
    console.log("mid,idx="+mid+","+idx);
    if(lotteryMethodId!=mid){
        clearInput();
    }
    if(idx>3){
        ruleItemIndex = idx-3;
        isXian = 1;
        rules.isXian=1;
    }else{
        ruleItemIndex = idx;
        isXian = 0;
        rules.isXian=0;
    }
    lotteryMethodId = mid;
    rules.lotteryMethodId = mid;
    rules.count = idx;

    if(isXian==1){
        $("#btn_bet_package").hide();
        $("#btn_bet_will").hide();
        $("#dingweiContainer").hide();
        $("#dingweiContainer-1").hide();
        $("#dingweiHeader").hide();
        $("#peishuContainer").show();
        $("#peishuContainer-1").show();
        peishuEleHideOrShow();

    }else{
        $("#btn_bet_package").show();
        $("#btn_bet_will").show();
        $("#dingweiHeader").show();
        $("#dingweiContainer").show();
        $("#dingweiContainer-1").show();
        $("#peishuContainer").hide();
        $("#peishuContainer-1").hide();
        if(ruleItemIndex==0){
            $("#matchRotate").hide();
        }else{
            $("#matchRotate").show();
        }
    }
    repeatTypeEleHideOrShow();

    xiongdiTypeEleHideOrShow();

    duishuTypeEleHideOrShow();

    bdwHefenEleHideOrShow();

    hefenEleHideOrShow();

    otherEleHideOrShow();

}

function otherEleHideOrShow(){
    if(ruleItemIndex==0){
        $("#fushiContainer").hide();
        $("#otherRuleContainer").hide();
    }else{
        $("#fushiContainer").show();
        var rname = _getRuleName();
        $("#ruleName1").html(rname);
        $("#ruleName2").html(rname);
        if(isXian==0){
            $("#otherRuleContainer").show();
            //乘号位置处理
            if(lotteryMethodId<4){
                $("#ch1").show();
                $("#ch2").show();
            }else{
                $("#ch1").hide();
                $("#ch2").hide();
            }
        }else{
            $("#otherRuleContainer").hide();
        }
    }
}

function hefenEleHideOrShow(){
    if(ruleItemIndex==0 || isXian==1){
        $("#hefenContainer1").hide();
        $("#hefenContainer").hide();
    }else{
        $("#hefenContainer1").show();
        $("#hefenContainer").show();

    }
}

function peishuEleHideOrShow(){
    matchInputShow(true);
}


function  matchInputShow(isShow) {
    if(isShow){
        $("#peishuContainer-1").show();
        if(ruleItemIndex>2){
            $("#ps1").show();
            $("#ps2").show();
            $("#ps3").show();
            $("#ps4").show();
        }else if(ruleItemIndex>1){
            $("#ps1").show();
            $("#ps2").show();
            $("#ps3").show();
            $("#ps4").hide();
        }else if(ruleItemIndex>0){
            $("#ps1").show();
            $("#ps2").show();
            $("#ps3").hide();
            $("#ps4").hide();
        }else{
            if(isXian==0){
                $("#ps1").hide();
                $("#ps2").hide();
                $("#ps3").hide();
                $("#ps4").hide();
            }
        }
    }else{
        $("#peishuContainer-1").hide();
        $("#ps1").hide();
        $("#ps2").hide();
        $("#ps3").hide();
        $("#ps4").hide();
    }

}


function repeatTypeEleHideOrShow(){

    if(ruleItemIndex==0){
        $("#repeatTypeContainer").hide();
        $("#repeat1").hide();
        $("#repeat22").hide();
        $("#repeat3").hide();
        $("#repeat4").hide();
    }else if(ruleItemIndex>2){
        $("#repeatTypeContainer").show();
        $("#repeat1").show();
        if(isXian==0){
            $("#repeat22").show();
        }else{
            $("#repeat22").hide();
        }

        $("#repeat3").show();
        $("#repeat4").show();
    }else if(ruleItemIndex>1){
        $("#repeatTypeContainer").show();
        $("#repeat1").show();
        $("#repeat22").hide();
        $("#repeat3").show();
        $("#repeat4").hide();

    }else if(ruleItemIndex>0){
        $("#repeatTypeContainer").show();
        $("#repeat1").show();
        $("#repeat22").hide();
        $("#repeat3").hide();
        $("#repeat4").hide();

    }
}



function xiongdiTypeEleHideOrShow(){
    if(ruleItemIndex==0){
        $("#xiongdiContainer").hide();
        $("#xiongdi2").hide();
        $("#xiongdi3").hide();
        $("#xiongdi4").hide();
    }else if(ruleItemIndex>2){
        $("#xiongdiContainer").show();
        $("#xiongdi2").show();
        $("#xiongdi3").show();
        $("#xiongdi4").show();
    }else if(ruleItemIndex>1){
        $("#xiongdiContainer").show();
        $("#xiongdi2").show();
        $("#xiongdi3").show();
        $("#xiongdi4").hide();

    }else if(ruleItemIndex>0){
        $("#xiongdiContainer").show();
        $("#xiongdi2").show();
        $("#xiongdi3").hide();
        $("#xiongdi4").hide();

    }
}

function duishuTypeEleHideOrShow() {
    if(ruleItemIndex==0){
        $("#duishuContainer").hide();
    }else{
        $("#duishuContainer").show();
    }

}


function bdwHefenEleHideOrShow(){
    if(ruleItemIndex==0){
        $("#bdweihefenContainer").hide();
    }else{
        $("#bdweihefenContainer").show();
        if(ruleItemIndex>1){
            $("#bd1").show();
            $("#bd2").show();
        }else{
            $("#bd1").show();
            $("#bd2").hide();
        }
    }
}


function showChkResult(my,ruleTypeName,itemIdx) {
    switch(ruleTypeName){
        case 'bigNum':
            rules.bigNum.locArr[itemIdx]= (my.checked?1:0);
            break;
        case 'smallNum':
            rules.singleNum.locArr[itemIdx]=(my.checked?1:0);
            break;
        case 'doubleNum':
            rules.doubleNum.locArr[itemIdx]=(my.checked?1:0);
            break;
        case 'singleNum':
            rules.singleNum.locArr[itemIdx]=(my.checked?1:0);
            break;
        case 'xlocArr':
            rules.others.locArr[itemIdx]=(my.checked?1:0);
            break;
        case 'hefenItem1':
            rules.hefen.binds[0].locArr[itemIdx]=(my.checked?1:0)
            break;
        case 'hefenItem2':
            rules.hefen.binds[1].locArr[itemIdx]=(my.checked?1:0)
            break;
        case 'hefenItem3':
            rules.hefen.binds[2].locArr[itemIdx]=(my.checked?1:0)
            break;
        case 'hefenItem4':
            rules.hefen.binds[3].locArr[itemIdx]=(my.checked?1:0)
            break;
    }
}


function showdis(my, to,ruleTypeName,ruleName,included) {
    if(my.checked){
        $("#"+to).attr("checked",false);
    }
    switch(ruleTypeName){
        case 'fushi':
            if(included==0 && my.checked){
                rules.fushi.excluded=1;
                rules.fushi.included=0;
            }else if(included==1 && my.checked){
                rules.fushi.excluded=0;
                rules.fushi.included=1;
            }else{
                rules.fushi.excluded=0;
                rules.fushi.included=0;
            }
            break;
        case 'hefen':
            if(included==0 && my.checked){
                rules.hefen.excluded=1;
                rules.hefen.included=0;
            }else if(included==1 && my.checked){
                rules.hefen.excluded=0;
                rules.hefen.included=1;
            }else{
                rules.hefen.excluded=0;
                rules.hefen.included=0;
            }
            break;
        case 'fixLoc':
            if(included==0 && my.checked){
                rules.fixCode.excluded=1;
                rules.fixCode.included=0;
            }else if(included==1 && my.checked){
                rules.fixCode.excluded=0;
                rules.fixCode.included=1;
            }else{
                rules.fixCode.excluded=0;
                rules.fixCode.included=0;
                rules.fixCode.loc1='';
                rules.fixCode.loc2='';
                rules.fixCode.loc3='';
                rules.fixCode.loc4='';
            }
            $("#dingweiContainer").show();
            $("#dingweiContainer-1").show();
            $("#__peishu_qu").attr("checked",false);
            $("#__peishu_chu").attr("checked",false);
            matchInputShow(false);
            break;
        case 'match1': //配数全转
            if(included==0 && my.checked){
                rules.matchCodes.excluded=1;
                rules.matchCodes.included=0;
            }else if(included==1 && my.checked){
                rules.matchCodes.excluded=0;
                rules.matchCodes.included=1;
            }else{
                rules.matchCodes.excluded=0;
                rules.matchCodes.included=0;
                rules.matchCodes.loc1='';
                rules.matchCodes.loc2='';
                rules.matchCodes.loc3='';
                rules.matchCodes.loc4='';
            }
            rules.matchCodes.isXian=0;
            $("#dingweiContainer").hide();
            $("#dingweiContainer-1").hide();
            $("#__dingwei_qu").attr("checked",false);
            $("#__dingwei_chu").attr("checked",false);
            matchInputShow(true);
            break;
        case 'match2': //配数
            if(included==0 && my.checked){
                rules.matchCodes.excluded=1;
                rules.matchCodes.included=0;
            }else if(included==1 && my.checked){
                rules.matchCodes.excluded=0;
                rules.matchCodes.included=1;
            }else{
                rules.matchCodes.excluded=0;
                rules.matchCodes.included=0;
                rules.matchCodes.loc1='';
                rules.matchCodes.loc2='';
                rules.matchCodes.loc3='';
                rules.matchCodes.loc4='';
            }
            rules.matchCodes.isXian=1;
            matchInputShow(true);
            break;
        case 'repeat':
            initRepeatRule(ruleName,included,my.checked)
            break;
        case 'xiongdi':
            initXiongdiRule(ruleName,included,my.checked)
            break;
        case 'bigNum':
            if(included==0 && my.checked){
                rules.bigNum.excluded=1;
                rules.bigNum.included=0;
            }else if(included==1 && my.checked){
                rules.bigNum.excluded=0;
                rules.bigNum.included=1;
            }else{
                rules.bigNum.excluded=0;
                rules.bigNum.included=0;
                rules.bigNum.locArr=[0,0,0,0]
            }
            break;
        case 'smallNum':
            if(included==0 && my.checked){
                rules.smallNum.excluded=1;
                rules.smallNum.included=0;
            }else if(included==1  && my.checked){
                rules.smallNum.excluded=0;
                rules.smallNum.included=1;
            }else{
                rules.smallNum.excluded=0;
                rules.smallNum.included=0;
                rules.smallNum.locArr=[0,0,0,0]
            }
            break;
        case 'doubleNum':
            if(included==0 && my.checked){
                rules.doubleNum.excluded=1;
                rules.doubleNum.included=0;
            }else if(included==1 && my.checked){
                rules.doubleNum.excluded=0;
                rules.doubleNum.included=1;
            }else{
                rules.doubleNum.excluded=0;
                rules.doubleNum.included=0;
                rules.doubleNum.locArr=[0,0,0,0]
            }
            break;
        case 'singleNum':
            if(included==0 && my.checked){
                rules.singleNum.excluded=1;
                rules.singleNum.included=0;
            }else if(included==1 && my.checked){
                rules.singleNum.excluded=0;
                rules.singleNum.included=1;
            }else{
                rules.singleNum.excluded=0;
                rules.singleNum.included=0;
                rules.singleNum.locArr=[0,0,0,0]
            }
            break;
        case 'bdwhefen':
            if(ruleName==='sum2'){
                if(my.checked){
                    rules.sum2.included=1;
                    rules.sum2.sumType=2;
                }else{
                    rules.sum2.included=0;
                    rules.sum2.sum='';
                    rules.sum2.sumType='';
                }
            }else if(ruleName==='sum3'){
                if(my.checked){
                    rules.sum2.included=1;
                    rules.sum2.sumType=3;
                }else{
                    rules.sum2.included=0;
                    rules.sum2.sum='';
                    rules.sum2.sumType=''
                }
            }
            break;
        case 'duishu':
            if(included==0 && my.checked){
                rules.pairs.excluded=1;
                rules.pairs.included=0;
            }else if(included==1 && my.checked){
                rules.pairs.excluded=0;
                rules.pairs.included=1;
            }else{
                rules.pairs.excluded=0;
                rules.pairs.included=0;
            }
            break;


    }
}



function initRepeatRule(ruleName,included,checked){
    switch(ruleName){
        case 'repeat1':
            if(included==0 && checked){
                rules.repeat.double1.excluded=1;
                rules.repeat.double1.included=0;
            }else if(included==1 && checked){
                rules.repeat.double1.excluded=0;
                rules.repeat.double1.included=1;
            }else{
                rules.repeat.double1.excluded=0;
                rules.repeat.double1.included=0;
            }
            break;
        case 'repeat22': //双双重
            if(included==0 && checked){
                rules.repeat.doubledouble.excluded=1;
                rules.repeat.doubledouble.included=0;
            }else if(included==1 && checked){
                rules.repeat.doubledouble.excluded=0;
                rules.repeat.doubledouble.included=1;
            }else{
                rules.repeat.doubledouble.excluded=0;
                rules.repeat.doubledouble.included=0;
            }
            break;
        case 'repeat3':
            if(included==0 && checked){
                rules.repeat.triple.excluded=1;
                rules.repeat.triple.included=0;
            }else if(included==1 && checked){
                rules.repeat.triple.excluded=0;
                rules.repeat.triple.included=1;
            }else{
                rules.repeat.triple.excluded=0;
                rules.repeat.triple.included=0;
            }
            break;
        case 'repeat4':
            if(included==0 && checked){
                rules.repeat.double2.excluded=1;
                rules.repeat.double2.included=0;
            }else if(included==1 && checked){
                rules.repeat.double2.excluded=0;
                rules.repeat.double2.included=1;
            }else{
                rules.repeat.double2.excluded=0;
                rules.repeat.double2.included=0;
            }
            break;

    }
}

function initXiongdiRule(ruleName,included,checked){
    switch(ruleName){
        case 'xiongdi1':
            if(included==0 && checked){
                rules.brothers.br2.excluded=1;
                rules.brothers.br2.included=0;
            }else if(included==1 && checked){
                rules.brothers.br2.excluded=0;
                rules.brothers.br2.included=1;
            }else{
                rules.brothers.br2.excluded=0;
                rules.brothers.br2.included=0;
            }
            break;
        case 'xiongdi2':
            if(included==0 && checked){
                rules.brothers.br3.excluded=1;
                rules.brothers.br3.included=0;
            }else if(included==1 && checked){
                rules.brothers.br3.excluded=0;
                rules.brothers.br3.included=1;
            }else{
                rules.brothers.br3.excluded=0;
                rules.brothers.br3.included=0;
            }
            break;
        case 'xiongdi3':
            if(included==0 && checked){
                rules.brothers.br4.excluded=1;
                rules.brothers.br4.included=0;
            }else if(included==1 && checked){
                rules.brothers.br4.excluded=0;
                rules.brothers.br4.included=1;
            }else{
                rules.brothers.br4.excluded=0;
                rules.brothers.br4.included=0;
            }
            break;

    }
}


function limitRepeatNums(t,ruleTypeName,itemIdx) {
    var n = t.value;
    num = n.length;
    var y_n = n.slice(0, num - 1),g_n = n.slice(num - 1, num);
    - 1 != y_n.indexOf(g_n) ? t.value = y_n.replace(/[^0-9]/g, "") : t.value = t.value.replace(/[^0-9]/g, "");
    switch(ruleTypeName){
        case 'sum2':
            rules.sum2.sum=t.value;
            break;
        case 'duishu':
            pairCodeRuleValues(itemIdx,t.value);
            break;
        case 'peishu':
            matchRuleValues(itemIdx,t.value);
            break;
        case 'fixCode':
            fixCodeRuleValues(itemIdx,t.value);
            break;
        case 'hefenItem1':
            rules.hefen.binds[0].value=t.value;
            break;
        case 'hefenItem2':
            rules.hefen.binds[1].value=t.value;
            break;
        case 'hefenItem3':
            rules.hefen.binds[2].value=t.value;
            break;
        case 'hefenItem4':
            rules.hefen.binds[3].value=t.value;
            break;
        case 'fushi':
            rules.fushi.combines = t.value;
            break;
        case 'quanzhuan':
            rules.others.fullChange = t.value;
            break;
        case 'paichu':
            rules.others.excludes = t.value;
            break;
        case 'includes':
            rules.fushi.contains = t.value;
            break;
        case "shangjiang":
            rules.others.shangJiang=t.value;
            break;
        case "valueRange":
            rules.others.valueRange[itemIdx]=t.value;
            break;
    }
}


function matchRuleValues(locIdx,val){
    switch(locIdx){
        case 0:
            rules.matchCodes.loc1=val;
            break;
        case 1:
            rules.matchCodes.loc2=val;
            break;
        case 2:
            rules.matchCodes.loc3=val;
            break;
        case 3:
            rules.matchCodes.loc4=val;
            break;
    }
}


function fixCodeRuleValues(locIdx,val){
    switch(locIdx){
        case 0:
            rules.fixCode.loc1=val;
            break;
        case 1:
            rules.fixCode.loc2=val;
            break;
        case 2:
            rules.fixCode.loc3=val;
            break;
        case 3:
            rules.fixCode.loc4=val;
            break;
    }
}

function pairCodeRuleValues(locIdx,val){
    switch(locIdx){
        case 0:
            rules.pairs.pair1=val;
            break;
        case 1:
            rules.pairs.pair2=val;
            break;
        case 2:
            rules.pairs.pair3=val;
            break;
        case 3:
            rules.pairs.pair4=val;
            break;
        case 4:
            rules.pairs.pair5=val;
            break;
    }
}

