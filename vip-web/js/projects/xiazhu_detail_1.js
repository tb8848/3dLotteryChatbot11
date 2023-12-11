var $ = layui.$;
var backCodeList=[];
var orderList = [];
var drawId = null;
var isXian = null;
var lotterySettingId = null;
var dataType = null;
var detailType = 1;
var buyType = 0;
var currpage = 1;
var limit=50;
var pages = 1;
var totals = 0;

window.onload=function(e){
    drawId = GetQueryString("period_number");
    getLotterySettingList();
    getTableData();
}


function getLotterySettingList(){
    //遍历分类下选框
    $.ajax({
        url: HOST+"lotterySettingPC/getAllLotterySetting"
        , type: "GET"
        , contentType: 'application/json'
        , success: function (res) {

            $.each(res.data, function (index, item) {
                var lsList = item.lotterySettingList;
                if(lsList.length>1){
                    $('#dataType').append(new Option(item.bettingMethod, item.id+"-00"));// 下拉菜单里添加元素

                    $.each(item.lotterySettingList,function(idx,it1){
                        $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                    });
                }else{
                    $.each(item.lotterySettingList,function(idx,it1){
                        $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                    });
                }

                // <option value="2">快打</option><option value="3">快选</option><option value="1">退码</option>
            });
            $("#dataType").append(new Option('快打',2));
            $("#dataType").append(new Option('快选',3));
            $("#dataType").append(new Option('txt导入',5));
            $("#dataType").append(new Option('二定',6));
            $("#dataType").append(new Option('汇总表',7));
            //layui.form.render("select");
        }
    });
}

function bpcodesDetail(rid){
    var html = $("#bp-"+rid).html();
    var title = html.substr(html.lastIndexOf(",")+1);
    localStorage.setItem("bptitle",title);
    localStorage.setItem("bprid",rid);
    window.parent.open("baopaiDetail.html");
}


function __emptyDataRow(){
    return '<tr> <td colspan="11">暂无数据!</td> </tr>  ';
}

function getTableData(){
    var lsId = "";
    var lmId = "";
    var dataType = $("#dataType").val();
    if(dataType.indexOf("-")>-1){
        var arr = dataType.split("-");
        if(arr[1]=='00'){
            lmId = arr[0];
            dataType = -1;
        }else if(arr[1]=="01"){
            lsId = arr[0];
            dataType = -1;
        }
    }
    var isXian = $("#xianFlag").attr("checked")==true?1:0;

    var qs = "buyCodes="+$("#buyCodes").val()+"&dataType="+dataType+"&colType="+$("#colType").val()+
        "&isXian="+isXian+"&minValue="+$("#minValue").val()+"&maxValue="+$("#maxValue").val()+
        "&lotterySettingsId="+lsId+"&buyType="+$("#buyType").val()+"&printNo="+$("#printNo").val()
        +"&page="+currpage+"&limit="+limit+"&drawNo="+drawId;
    console.log("=======================qs="+qs);
    $.ajax({
        url:HOST+'drawBuyRecordPC/drawBuyRecordByPage?'+qs,
        type:'get',
        success:function(res){
            if(res.code==0){
                pages = res.data.pages;
                totals = res.data.total;
                var dataList = res.data.dataList;
                __buildDataTable(dataList);
                initPager();
            }else{
                $("#tbody").html(__emptyDataRow())
            }
        }
    })
}

function  initPager() {
    $(".pageindex").html(currpage);
    $(".pagecount").html(pages);
    $(".recordcount").html(totals);
}

function __buildDataTable(dlist){
    var bodydata = "";
    if(dlist.length==0){
        $("#tbody").html(__emptyDataRow());
        return;
    }
    var totalMoney = 0;
    var totalHs = 0;
    var totalEarn = 0;
    for(var i=0,len=dlist.length;i<len;i++){
        var item = dlist[i];
        var codes = '';
        if(item.buyType==2){
            codes= '<a href="javascript:void(0)" id="bp-'+item.id+'" style="color:blue;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.buyCodes+'</a>'
        }else{
            if(item.hasOneFlag==1){
                codes= '<span>'+item.buyCodes+'<span style="color: orangered;">现</span></span>'
            }else{
                codes= item.buyCodes;
            }
        }
        var buyMoney = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.buyMoney+'</a>'
            : item.buyMoney

        var peiRate = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.peiRate+'</a>'
            :item.peiRate;
        var createTime = item.backCodeFlag==1? item.createTime+"<br/>退:"+item.backCodeTime : item.createTime;

        var cell7 = item.drawStatus==1?item.drawMoney:'--';
        var cell8 = item.huishui;
        var cell9 = item.buyMoney - item.huishui;
        var cell10 = item.backCodeFlag==0?"成功":item.backCodeFlag==1?"<span style='color:orangered'>已退码</span>":'--';
        var cell11 = item.backCodeStatus==1 && item.backCodeFlag==0?'<input type="checkbox" onclick="chooseOne(this)" value="'+item.id+'" name="'+item.printId+'"/>':'--'
        var cls = item.backCodeFlag==1?'class="bg4"':'class=""';
        var tr = '<tr '+cls+'><td>彩票1</td><td>'+item.printId+'</td><td>'+createTime+'</td>' +
            '<td>'+codes+'</td>' +
            '<td>'+buyMoney+'</td>' +
            '<td>'+peiRate+'</td>' +
            '<td>'+cell7+'</td><td>'+cell8+'</td><td>'+cell9+'</td><td>'+cell10+'</td><td>'+cell11+'</td></tr>';
        bodydata = bodydata + tr;
    }
    if(dlist.length>0){
        var tr = '<tr'+cls+'><td colspan="2">合计</td><td>'+dlist.length+'</td><td>'+createTime+'</td>' +
            '<td>'+codes+'</td>' +
            '<td>'+buyMoney+'</td>' +
            '<td>'+peiRate+'</td>' +
            '<td>'+cell7+'</td><td>'+cell8+'</td><td>'+cell9+'</td><td>'+cell10+'</td><td>'+cell11+'</td></tr>';
    }
    $("#tbody").html(bodydata);

}

$("#btnSearch").on("click","",function (e) {
    currpage = 1;
    getTableData();
})

$("#pager").on("click",".fn-first",function (e) {
    if(currpage!=1){
        currpage = 1;
        getTableData();
    }
})

$("#pager").on("click",".fn-next",function (e) {
    if(currpage<pages){
        currpage++;
        getTableData();
    }
})

$("#pager").on("click",".fn-prev",function (e) {
    if(currpage>1){
        currpage--;
        getTableData();
    }
})

$("#pager").on("click",".fn-last",function (e) {
    if(currpage!=pages){
        currpage=pages
        getTableData();
    }
})
$("#pager").on("click",".fn-go",function(e){
    var goValue = $("#goTo").val();
    if(parseInt(goValue)>=1 && parseInt(goValue)<=pages && parseInt(goValue)!=currpage){
        currpage  = parseInt(goValue);
        getTableData();
    }else{

    }
})

$("#tuiOrder").on("click","",function (e) {
    if(backCodeList.length<1){
        layer.msg("请选择单号");
        return;
    }
    $.ajax({
        url:HOST+"codes/getOrderInfo",
        type:'post',
        data:JSON.stringify(orderList),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                if(res.count>0){
                    var popContent = '';
                    res.data.forEach((item,idx)=>{
                        popContent = popContent +'<p>'+item.orderNo+',共['+item.size+']个号码</p>';
                    });
                    popContent = popContent+'是否确定退码？'
                    layer.confirm(popContent, {icon: 3, title:'整单退码'}, function(index){
                        layer.close(index);
                        orderTuima();
                    });
                }
            }
        },
        fail:function (res) {
            layer.closeAll();
        }
    })
    return false;
})


$("#tuima").on("click","",function(e){
    console.log("点击退码了")
    if(backCodeList.length<1){
        layer.msg("未选择号码");
        return;
    }
    layer.msg('正在处理...', {
        icon: 16,shade: 0.3,time:-1
    });
    $.ajax({
        url:HOST+"draw/delUnbuyCode",
        type:'post',
        data:JSON.stringify({
            idArr:backCodeList,
            drawId:drawId
        }),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                currpage=1;
                getTableData();
            }else{
                layer.msg(res.msg);
            }
        },
        fail:function (res) {
            layer.closeAll();
        }
    })
    return false;
})

function orderTuima(){
    if(orderList.length<1){
        layer.msg("未选择号码");
        return;
    }
    layer.msg('正在处理...', {
        icon: 16,shade: 0.3,time:-1
    });
    $.ajax({
        url:HOST+"codes/delOrder",
        type:'post',
        data:JSON.stringify(orderList),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                layer.msg("操作完成");
                currpage=1;
                getTableData();
            }else{
                layer.msg(res.msg);
            }
        },
        fail:function (res) {
            layer.closeAll();
        }
    })

}


function chooseOne(obj){
    if(obj.checked){
        //选中
        if(!backCodeList.includes(obj.value)){
            backCodeList.push(obj.value);
        }
        if(!orderList.includes(obj.name)){
            orderList.push(obj.name);
        }
    }else{
        if(backCodeList.includes(obj.value)){
            backCodeList.splice(backCodeList.indexOf(obj.value),1);
        }
        if(orderList.includes(obj.name)){
            orderList.splice(orderList.indexOf(obj.name),1);
        }
    }
    var arr = $("#tbody").find(":checkbox");
    if(backCodeList.length === arr.length){
        $("#selectAll").attr("checked",true)
    }else{
        $("#selectAll").attr("checked",false)
    }
    return false;
}

$("#selectAll").on("click","",function(){
    if(this.checked){
        $("#tbody").find(":checkbox").each(function(){
            if(this.value!="checkAll"){
                this.checked=true;
                if(!backCodeList.includes(this.value)){
                    backCodeList.push(this.value);
                }
                if(!orderList.includes(this.name)){
                    orderList.push(this.name);
                }
            }

        })
    }else{
        $("#tbody").find(":checkbox").each(function(){
            this.checked=false;
        })
        backCodeList = [];
        orderList = [];
    }
});

