//脚本文件
//3d号码下注 通用方法定义

var $ = layui.$;
var layer = layui.layer;
/**
 * 3D号码
 * @returns {[]}
 */


function create3DCodes() {
    var allCodesArr = [];
    for(var i=0;i<10;i++){
        for(var j=0;j<10;j++){
            for(var k=0;k<10;k++){
                var code = i+""+j+""+k;
                allCodesArr.push({
                    code:code,
                    sum:(i+j+k),
                    bai:i,
                    shi:j,
                    ge:k
                });
            }
        }
    }
    return allCodesArr;
}

var backCodesArr = [];
var selectedNums=0;
var drawNo = '';
var drawId = '';

//停押列表分页对象
var stopBuyPager={
    pages:0,
    currpage:1,
    total:0
};
var stopCodesList=[];

$(function (e) {
    getData();
})

function getData(){
    $.ajax({
        url:HOST+'code/drawInfo',
        type:'get',
        success:function(res){
            if(res.code==0){
                drawId = res.data.id;
                drawNo = res.data.drawNo
                upOpenStatus(res.data);
                if(res.data.openStatus==1){
                    initBuyList();
                    stopBuyPager.currpage=1;
                    getStopBuyCodes(0);
                }
            }
        },
        error:function (e) {
            layer.closeAll();
        }
    });
}


function getStopBuyCodes(delFlag){
    var qs = 'drawId='+drawId+"&deleteFlag="+delFlag;
    if(delFlag==0){
        qs = qs +"&page="+stopBuyPager.currpage;
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
                var fanganDiv = '<div style="width: 200px;text-overflow: ellipsis;white-space: nowrap;overflow:hidden;" title="'+item.fanganName+'">'+item.fanganName+'</div>'
                bodyhtml = bodyhtml +'<tr><td>'+fanganDiv+'</td><td>'+codes+'</td><td>'+item.buyMoney+'</td><td>'+item.createTime+'</td>' +
                    // '<td><input type="checkbox" value="'+item.id+'" onclick="chooseLackOne(this,\''+item.id+'\')"> </td>' +
                    '</tr>'
            });
            $("#tbody_lack").html(bodyhtml);
        }
    }else{
        $("#stopBuy-totalMoney").html("0");
        $("#stopBuy-bishu").html("0");
        var emptyTip = '<tr><td colspan="4" class="blank_area"></td></tr>';
        // '<tr> <td colspan="3" class="fb tfoot"> 笔数:0&nbsp;&nbsp;总金额:0 </td></tr>';
        $("#tbody_lack").html(emptyTip);
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


function stopBuy(){
    $("#stopBuy").show();
    $("#doc3").hide();
    $("#tbody").html(__emptyBuyRow(10));
}

function startBuy() {
    $("#stopBuy").hide();
    $("#doc3").show();
    $("#openStatusDesc").html('<span></span>');
}

//监听
function upOpenStatus(msg) {
    var drawOpenStatus = msg.openStatus;
    if(drawOpenStatus==1){
        startBuy()
        if(drawNo!=msg.drawNo){
            drawNo = msg.drawNo;
            drawId = msg.id;
            initBuyList();
            stopBuyPager.currpage = 1;
            getStopBuyCodes(0)
        }
    }else{
        stopBuy()
        if(drawOpenStatus==3){
            $("#openStatusDesc").html('<span>开盘中</span>');
        }else if(drawOpenStatus==2){
            $("#openStatusDesc").html('<span>未开盘</span>');
        }else if(drawOpenStatus==0){
            $("#openStatusDesc").html('<span>已停盘</span>');
        }
        if(drawNo!=msg.drawNo){
            stopBuyPager.currpage = 1;
            getStopBuyCodes(0)
        }
    }
}

function __emptyBuyRow(rowSize){
    var hm = '';
    for(var i=0;i<rowSize;i++){
        hm = hm + '<tr><td>--</td><td>--</td><td>--</td><td class="fb green">--</td>' +
            '<td class="fb">--</td><td class="fb red">--</td><td>--</td><td>--</td></tr>';
    }
    return hm;
}


function __buyRow(item){
    var c1 = item.huizongName;
    if(item.huizongFlag==1){
        c1= '<a href="javascript:void(0)" id="bp-'+item.id+'" style="color:blue;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\',\''+item.huizongName+'\',\''+item.bpGroup+'\')">'+item.huizongName+'</a>'
    }
    var c2 = item.printId;
    var c3 = item.huizongFlag==1?"["+item.buyAmount+"注]":item.buyCodes;
    var mm = item.param1; //金额
    var pr = item.huizongFlag==1?'<a href="javascript:void(0)" id="bp-'+item.id+'" style="color:blue;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\',\''+item.huizongName+'\',\''+item.bpGroup+'\')">查看赔率</a>':item.param3; //赔率
    var c6 = item.backCodeFlag==0?'成功':item.backCodeFlag==1?'已退码':"--";
    var c7 = item.backCodeStatus==1 && item.backCodeFlag==0?'<input type="checkbox" name="ids" value="'+item.id+'" onclick="chooseOne(this,\''+item.id+'\')">':'--';
    var bgColor = item.backCodeFlag==1?'bg4':'';
    var hm = '<tr class="font-color '+bgColor+'">' +
        '<td>福彩3D</td>' +
        '<td><div style="width: 200px;text-overflow: ellipsis;white-space: nowrap;overflow:hidden;" title="'+item.huizongName+'">'+c1+'</div></td>\n' +
        '<td>'+c2+'</td>\n' +
        '<td class="fb f15"> <span class="green">'+c3+'</span> </td>\n' +
        '<td class="fb f15 red"> '+pr+' </td>\n' +
        '<td class="fb f15"> '+mm+' </td> ' +
        '<td >  '+c6+'  </td>\n' +
        '<td class="print_area">'+c7+'</td>' +
        ' </tr>';
    return hm;
}


function bpcodesDetail(rid,codes,groupCount){
    sessionStorage.setItem("bptitle",codes);
    sessionStorage.setItem("bprid",rid);
    sessionStorage.setItem("mingxi_drawNo",drawNo);
    window.parent.open("../xiazhu/fanganDetail.html");
}


//获取最新10条购买记录
function initBuyList(){
    $.ajax({
        url:HOST+'pc/drawBuyRecord/listTopN?drawId=',
        type:'get',
        success:function(res){
            if(res.code==0){
                var dlist = res.data;
                if(dlist.length<1){
                    $("#tbody").html(__emptyBuyRow(10));
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
                        bodystr = bodystr + __emptyBuyRow(1);
                    }
                }
                $("#tbody").html(bodystr);
            }else{
                $("#tbody").html(__emptyBuyRow(10));
            }
        }
    })
}


//单选
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


//退码
$("#cancelbet").on('click',"",function(e){
    if(backCodesArr.length<1){
        layer.msg("请选择号码");
    }else{
        layer.msg('处理中...', {
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
                    layer.alert("退码成功");
                    backCodesArr = [];
                    initBuyList();
                    $("#selectAll").prop("checked",false);
                    window.parent.getCredit();
                    window.parent.reloadPrintTable();
                }else{
                    layer.msg(res.msg);
                }
                //getUUID();
            },
            fail:function (err) {
                layer.closeAll();
            }
        })
    }
})

//全选
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
