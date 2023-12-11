var $ = layui.jquery;
var drawId = "";
var drawNo = "";
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
var print_pages=0;
var print_curpage=1;
var print_limit=500;
var print_totals=0;

var sendMsg = false;
var openStatus = 0;
var screen_cut_pages = 1;
var screen_cut_curpage=1;
var screen_cut_size=500;
var total_size = 0;


function initScreenCutTable(){ //截图弹窗中的分页
    //表格1
    $.ajax({
        url:HOST + 'drawPC/printCachePager?page='+screen_cut_curpage+"&limit="+screen_cut_size,
        type:'get',
        success:function(res) {
            if (res.code == 0) {
                var totals = res.data.totals; //所有记录数
                var pageTotalMoney = res.data.totalMoneyOfPager;
                var dataList = res.data.dataList;
                $("#totalBetMoney_1").html(pageTotalMoney);
                $("#recordsCount_1").html(res.data.dataList.length);
                var printhtml = "";
                var dataHeader = '<tr class="tc f14 height22" style="width:30%"><td i18n="main.buyCode">'+$i18np.prop("main.buyCode")+'</td> <td i18n="main.odds">'+$i18np.prop("main.odds")+'</td> <td i18n="main.buyMoney">'+$i18np.prop("main.buyMoney")+'</td></tr>';
                for(var i=0,len=dataList.length;i<len;i++){
                    var item = dataList[i];
                    var codeName = getCodePrintName(item.lotteryMethodId,"-",item.buyCodes);
                    //var buyCode = item.hasOneFlag==1?'<span>'+item.buyCodes+'<span style="color: orangered;" i18n="main.isXian">'+$i18np.prop("main.isXian")+'</span></span>':item.buyCodes;
                    var pr = item.param3
                    printhtml = printhtml + '<tr class="height22">'+'<td align="center" style="width:30%">'+codeName+'</td>'+'<td align="center">'+pr+'</td>'+'<td align="center">'+item.buyMoney+'</td></tr>'
                }
                printhtml = dataHeader + printhtml;
                $("#tbody2_data").html(printhtml); //号码数据，包括编号等
                screen_cut_pages = Math.ceil(totals/screen_cut_size);
                $("#totalPages").html(screen_cut_pages);
                $("#currPages").html(screen_cut_curpage);

            }else{

            }
        }
    })
}

$("#screenSelect").on("change","",function (e) {
    screen_cut_size = this.value;
    screen_cut_pages = Math.ceil(total_size/screen_cut_size)
    // if(total_size%screen_cut_size==0){
    //     screen_cut_pages = parseInt(total_size/screen_cut_size);
    // }else{
    //     screen_cut_pages = parseInt(total_size/screen_cut_size)+1;
    // }
    screen_cut_curpage=1;
    $("#totalPages").html(screen_cut_pages);
    $("#currPages").html(screen_cut_curpage);
    initScreenCutTable();
})

$("#fnPrev").on('click',"",function (e) {
    if(screen_cut_curpage>1){
        screen_cut_curpage--;
        $("#currPages").html(screen_cut_curpage);
        initScreenCutTable();
    }
})

$("#fnNext").on('click',"",function (e) {
    if(screen_cut_curpage<screen_cut_pages){
        screen_cut_curpage++;
        $("#currPages").html(screen_cut_curpage);
        initScreenCutTable();
    }
})


$("#btnScreenCut").on('click',"",function (e) {
    if(print_totals<1){
        layer.alert($i18np.prop("main.noScreenCutData"),{title:$i18np.prop("main.tipTitle"),btn:[$i18np.prop("main.confirm")]});
        return;
    }
    var _this = this;
    _this.value=$i18np.prop("main.downloading");
    _this.disabled = true;
    $("#btnClear").hide();//隐藏按钮
    $("#pagePart").html($i18np.prop("main.di")+screen_cut_curpage+$i18np.prop("main.ye")+" "+$i18np.prop("main.heji"));
    $("#pagePart").parents('tr').addClass('border-none');
    html2canvas(document.querySelector("#screenCutWin")).then(canvas => {
        var baseb4 = canvas.toDataURL('image/jpg');
        var params = {
            len:$("#recordsCount_1").html(),
            money:$("#totalBetMoney_1").html(),
            printNo:printInfo.batchNo,
            curpage:screen_cut_curpage,
            base64:baseb4
        };
        $.ajax({
            url:HOST+"screencut/onePage",
            type:'post',
            contentType:'application/json',
            data:JSON.stringify(params),
            success:function (res) {
                _this.value=$i18np.prop("main.screenCut");
                _this.disabled = false;
                $("#btnClear").show();//隐藏按钮
                $("#pagePart").html("");
                $("#pagePart").parents('tr').removeClass('border-none');
                if(res.code==0){
                    var fname = res.data;
                    window.open(HOST+'screencut/getImg?imgName='+fname)
                    //window.open(Server_HOST+"/image/"+fname);
                }else{
                    layer.msg($i18np.prop("main.downFail"));
                }
            }
        })
    });
})



//全截
$("#screencutall").on('click',"",function (e) {
    if(print_totals<1){
        layer.alert($i18np.prop("main.noScreenCutData"),{title:$i18np.prop("main.tipTitle"),btn:[$i18np.prop("main.confirm")]}); //"暂无截图的数据"
        return;
    }
    fullCut(this);
})



//全截
$("#btnScreenCutAll").on('click',"",function (e) {
    if(print_totals<1){
        layer.alert($i18np.prop("main.noScreenCutData"),{title:$i18np.prop("main.tipTitle"),btn:[$i18np.prop("main.confirm")]}); //"暂无截图的数据"
        return;
    }
    fullCut(this);
})


function  fullCut(_this) {
    //_this.value = $i18np.prop("main.downloading");
    const params = [{name:'XXX',value:$i18np.prop("main.downloading")}];
    _this.value=$i18np.propByPlaceholder("main.fullScreenCutValue",params)
    _this.disabled = true;
    // $("#clearPart").hide();//隐藏按钮
    // // $("#heji").html("第" + screen_cut_curpage + "页 合计:");
    // // $("#heji").parents('tr').addClass('border-none');
    // $("#lastRows").show();
    var reqUrl = HOST + "screencut/fullCut";
    if(defaultLang=='th'){
        reqUrl = HOST + "screencut/fullCut2";
    }
    $.ajax({
        url: reqUrl,
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify({}),
        success: function (res) {
            //_this.value = $i18np.prop("main.fullScreenCut");
            const params = [{name:'XXX',value:$i18np.prop("main.fullScreenCut")}];
            _this.value =$i18np.propByPlaceholder("main.fullScreenCutValue",params)
            _this.disabled = false;
            // $("#clearPart").show();//隐藏按钮
            // $("#heji").html("");
            // $("#heji").parents('tr').removeClass('border-none');
            // $("#lastRows").hide()
            if (res.code == 0) {
                var fname = res.data;
                window.open(HOST+'screencut/getPdf2?imgName='+fname)
                //window.open(Server_HOST+"/image/"+fname);
            } else {
                layer.msg($i18np.prop("main.downFail"));
            }
        }
    })

}