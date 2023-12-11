//预下注明细
var $ = layui.$;
var delList=[];
var drawId = null;
var dataType = null;
var currpage = 1;
var limit=50;
var pages = 1;
var totals = 0;
var newDrawId = "";

// window.addEventListener("message",function (e) {
//     var msg = e.data;
//     upOpenStatus(msg)
// })

var openStatus = 1;
function upOpenStatus(msg) {
    openStatus = msg.openStatus;
    if(msg.openStatus==1){
        $("#optCell").removeClass('hide');
    }else{
        $("#optCell").addClass('hide');
    }
    if(newDrawId!=msg.drawNo){
        newDrawId = msg.drawNo;
        initDrawList();
    }
}

function initDataType(){
    const classAll = $i18np.prop("huizongbiao.class.all");
    $("#dataType").html("").append('<option value="0" >'+classAll+'</option>')
    $.ajax({
        url: HOST+"lotterySettingPC/getAllLotterySetting"
        , type: "GET"
        , contentType: 'application/json'
        , success: function (data) {
            $.each(data.data, function (index, item) {
                var pid = item.id;
                if(pid=="1" || pid=='2' || pid=='3' || pid=='4'){
                    var betMethod = item.bettingMethod;
                    switch(pid){
                        case '1':
                            betMethod = $i18np.prop("yiding");
                            break;
                        case '2':
                            betMethod = $i18np.prop("erding");
                            break;
                        case '3':
                            betMethod = $i18np.prop("sanding");
                            break;
                        case '4':
                            betMethod = $i18np.prop("siding");
                            break;
                        default:
                            break;
                    }
                    var childList = item.lotterySettingList;
                    if(childList.length>1){
                        $('#dataType').append(new Option(betMethod, item.id+"-00"));
                        $.each(childList, function (index1, item1) {
                            $('#dataType').append(new Option(item1.bettingRule, item1.id+"-01"));
                        });
                    }else{
                        $.each(childList, function (index1, item1) {
                            $('#dataType').append(new Option(betMethod, item1.id+"-01"));
                        });
                    }
                }
            });
        }
    });
}


// var openStatus = sessionStorage.getItem("drawOpenStatus");
// if(openStatus==1){
//     $("#optCell").removeClass('hide');
// }else{
//     $("#optCell").addClass('hide');
// }

window.parent.frameLoad();

function initDrawList(){
    $.ajax({
        url:HOST+'draw/listN?size=4',
        type:'get',
        success:function(res){
            if(res.code==0){
                drawId = res.data.records[0].drawId;
                newDrawId = drawId;
                res.data.records.forEach((item,idx)=>{
                    $("#drawId").append('<option value="'+item.drawId+'">'+item.drawId+'</option>');
                })
                initDataType();
                getTableData();
            }
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
    return '<tr> <td colspan="11" i18n="huizongbiao.noData">'+$i18np.prop("huizongbiao.noData")+'</td> </tr>  ';
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
            lmId = "";
            dataType = -2;
        }
    }
    console.log("================dataType ==",dataType);
    var qs = "buyCodes="+$("#buyCodes").val()+"&dataType="+dataType+"&colType="+$("#colType").val()+
        "&minValue="+$("#minValue").val()+"&maxValue="+$("#maxValue").val()+
        "&lotterySettingsId="+lsId+"&lotteryMethodId="+lmId+"&buyFlag="+$("#buyFlag").val()+"&delFlag="+$("#delFlag").val()
        +"&page="+currpage+"&limit="+limit+"&drawId="+$("#drawId").val();
    console.log("=======================qs="+qs);
    $.ajax({
        url:HOST+'prebuy/search?'+qs,
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

function  transType(pid) {
    switch(pid){
        case '1':
            return '<span i18n="yiding">'+$i18np.prop("yiding")+'</span>';
            break;
        case '2':
            return '<span i18n="erding">'+$i18np.prop("erding")+'</span>';
            break;
        case '3':
            return '<span i18n="sanding">'+$i18np.prop("sanding")+'</span>';
            break;
        case '4':
            return '<span i18n="siding">'+$i18np.prop("siding")+'</span>';
            break;
        default:
            return '--';
    }
}


function __buildDataTable(dlist){
    var bodydata = "";
    if(totals==0){
        $("#tbody").html(__emptyDataRow());
        return;
    }
    for(var i=0,len=dlist.length;i<len;i++){
        var item = dlist[i];
        var codes = '';
        if(item.buyType==2){
            codes= '<a href="javascript:void(0)" id="bp-'+item.id+'" style="color:blue;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.buyCodes+'</a>'
        }else{
            if(item.hasOneFlag==1){
                codes= '<span>'+item.buyCodes+'<span style="color: orangered;" i18n="huizongbiao.xian">'+$i18np.prop("huizongbiao.xian")+'</span></span>'
            }else{
                codes= item.buyCodes;
            }
        }
        var buyMoney = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.buyMoney+'</a>'
            : item.buyMoney

        var peiRate = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.peiRate+'</a>'
            : item.peiRate;
        var createTime = item.createTime;

        //var types = item.lotterySetting!=null?item.lotterySetting.bettingRule:res.lotteryMethod!=null?res.lotteryMethod.bettingMethod:'--';

        var types = '--';
        if(item.lotterySetting!=null){
            types = item.lotterySetting.bettingRule;
            if(item.lotterySetting.lotteryMethodId=="4"){
                types = transType(item.lotterySetting.lotteryMethodId);
            }
        }else if(res.lotteryMethod!=null){
            types = res.lotteryMethod.bettingMethod;
            types = transType(res.lotteryMethod.lotteryMethodId);
        }


        var status = "";
        status = status + (item.printFlag==0?'<span i18n="huizongbiao.unPrint">'+$i18np.prop("huizongbiao.unPrint")+'</span>':'<span style="color:red" i18n="huizongbiao.hasPrint">'+$i18np.prop("huizongbiao.hasPrint")+'</span>')
        status = status + (item.buyFlag==0?'&nbsp;/&nbsp;<span i18n="huizongbiao.buyStatus.no">'+$i18np.prop("huizongbiao.buyStatus.no")+'</span>':'&nbsp;/&nbsp;<span style="color:red" i18n="huizongbiao.buyStatus.yes">'+$i18np.prop("huizongbiao.buyStatus.yes")+'</span>')
        status = status + (item.delFlag==0?'&nbsp;/&nbsp;<span i18n="huizongbiao.cancelStatus.no">'+$i18np.prop("huizongbiao.cancelStatus.no")+'</span>':'&nbsp;/&nbsp;<span style="color:red" i18n="huizongbiao.cancelStatus.yes">'+$i18np.prop("huizongbiao.cancelStatus.yes")+'</span>')

        var cell8 = "";
        if(openStatus==1){
            if(item.buyFlag==0 && item.delFlag==0){
                cell8 = '<td><input type="checkbox" onclick="chooseOne(this)" value="'+item.id+'" name="'+item.printId+'"/></td>';
            }else{
                cell8 = "<td>--</td>"
            }
        }else{
            cell8 = '<td class="hide"></td>';
        }
        var tr = '<tr><td>'+item.billNo+'</td>' +
            '<td>'+codes+'</td>' +
            '<td>'+buyMoney+'</td>' +
            '<td>'+peiRate+'</td>' +
            '<td>'+types+'</td>' +
            '<td>'+createTime+'</td>'+
            '<td>'+status+'</td>'+cell8+'</tr>';
        bodydata = bodydata + tr;
    }
    $("#tbody").html(bodydata);

}

$("#btnSearch").on("click","",function (e) {
    doSearch();
})

function doSearch(){
    currpage = 1;
    getTableData();
}

$("#btnPrint").on("click","",function (e) {
    doPrint();
})

function doPrint(){
    currpage = 1;
    window.open("hzb-detail.html?drawNo="+$("#drawId").val());
    return false;
}

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
    if(goValue!=""){
        var toPage = parseInt(goValue);
        if(toPage>=1 && toPage<=pages && toPage!=currpage){
            currpage  = toPage;
            getTableData();
        }
    }

})



//删除选中
function delSelected() {
    if(delList.length<1){
        layer.msg($i18np.prop("huizongbiao.noCodeBuy"))
        return;
    }
    layer.msg($i18np.prop("huizongbiao.chulizhong"), {
        icon: 16
        ,shade: 0.3
        ,time:-1
    });
    $.ajax({
        url:HOST+"prebuy/del",
        type:'post',
        data:JSON.stringify(delList),
        success:function(res){
            layer.closeAll();
            if(res.code==0){
                layer.msg($i18np.prop("huizongbiao.reqSuccess"));
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
}

$("#btnDel").on("click","",function (e) {
    delSelected();
    return false;
})

function chooseOne(obj){
    if(obj.checked){
        //选中
        if(!delList.includes(obj.value)){
            delList.push(obj.value);
        }
    }else{
        if(delList.includes(obj.value)){
            delList.splice(delList.indexOf(obj.value),1);
        }
    }
    var arr = $("#tbody").find(":checkbox");
    if(delList.length === arr.length){
        document.getElementById("selectAll").checked = true;
    }else{
        document.getElementById("selectAll").checked = false;
    }
    return false;
}

$("#selectAll").on("click","",function(){
    if(this.checked){
        $("#tbody").find(":checkbox").each(function(){
                this.checked=true;
                if(!delList.includes(this.value)){
                    delList.push(this.value);
                }
        })
    }else{
        $("#tbody").find(":checkbox").each(function(){
            this.checked=false;
        })
        delList = [];
    }
});
