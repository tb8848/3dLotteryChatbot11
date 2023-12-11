var $ = layui.jquery;
var layer = layui.layer;

const vm = new Vue({
    el: '#main',
    data: {
        lines:0,
        totalMoney:0,
        userName:"",
        roleName:"",
        drawList:[],
        buyList:[],
        userId:'',
        pid:'',
        pager:{
          pages:0,
          total:-1,
          currpage:1,
          toPage:'',
            limit:50
        },
        searchParam:{ //基本资料
            buyCodes:"",
            drawId:'',
            deleteFlag:0,
        },
        dataArr:[],
    },
    methods: {
        goBack(){
            window.location.href="../userList2.html?id="+this.pid;
            //window.location.href="../../baobiao/daybaobiao.html?userId="+this.userId;
            //window.location.href="../userList2.html?id="+this.pid+"&isBack=1";
            //history.back();
        },
        init(delFlag) {
            this.userName = window.sessionStorage.getItem("userName");
            this.roleName = window.sessionStorage.getItem("roleName");
            this.userId = GetQueryString("vipId");
            this.pid = GetQueryString("pid");
            this.searchParam.deleteFlag = delFlag;
            this.getDrawList();
        },

        getDrawList() {
            //期号选择
            var _this = this;
            $.ajax({
                url: HOST + "admin/vip/stopBuyCodes/drawList"
                , type: "post"
                ,data:JSON.stringify({
                    vipId:_this.userId,
                    deleteFlag: _this.searchParam.deleteFlag
                })
                ,contentType:'application/json'
                , success: function (res) {
                    if (res.code == 0) {
                        _this.drawList = res.data;
                        _this.searchParam.drawId = res.data[0].id;
                        _this.getBuyList()
                    } else {

                    }
                }
            });
        },
        getBuyList() {
            var _this = this;
            $.ajax({
                url: HOST + "admin/vip/stopBuyCodes/listByPage"
                , type: "post"
                ,data:JSON.stringify({
                    "vipId":_this.userId,
                    "deleteFlag": _this.searchParam.deleteFlag,
                    "drawId":_this.searchParam.drawId,
                    "buyCodes":_this.searchParam.buyCodes,
                    "pageNo":_this.pager.currpage,
                    "pageSize":_this.pager.limit
                }),
                contentType:'application/json'
                , success: function (res) {
                    _this.dataArr = [];
                    if (res.code == 0) {
                        _this.pager.total = res.count;
                        _this.pager.pages = _this.pager.total%_this.pager.limit==0?parseInt(_this.pager.total/_this.pager.limit):parseInt(_this.pager.total/_this.pager.limit)+1;
                        _this.buyList = res.data.dataList;
                        _this.buyList.forEach((item,idx)=>{
                            _this.dataArr.push(getCodePrintName(item.lotteryMethodId,"|",item.codes));
                            _this.dataArr.push(item.buyMoney);
                        })
                        _this.totalMoney = res.data.totalM;
                        var dataLen = _this.buyList.length;
                        if(dataLen>0){
                            _this.lines = dataLen%10==0?parseInt(dataLen/10):parseInt(dataLen/10)+1;
                        }
                    } else {
                        _this.totalMoney='';
                        _this.buyList=[];
                    }
                }
            });
        },
        toSearch() {
            this.getBuyList();
        },
        getCellDataBy(row,loc,type){

            //var ll = parseInt(loc/2);
            console.log("=====",row,loc,type);
            if(row*10+loc-1<this.buyList.length){
                return '';
            }
            if(type==1){
                return this.buyList[row*10+loc-1].codes;
            }else{
                return this.buyList[row*10+loc-1].buyMoney;
            }
        },
        firstPage() {
            if (this.pager.currpage != 1) {
                this.pager.currpage = 1;
                this.getBuyList();
            }
        },
        nextPage() {
            if (this.pager.currpage < this.pager.pages) {
                this.pager.currpage++;
                this.getBuyList();
            }
        },
        prePage() {
            if (this.pager.currpage > 1) {
                this.pager.currpage--;
                this.getBuyList();
            }
        },
        lastPage() {
            if (this.pager.currpage < this.pager.pages) {
                this.pager.currpage = this.pager.pages;
                this.getBuyList();
            }
        },
        goPage() {
            if(this.pager.toPage!="") {
                const toPage = parseInt(this.pager.toPage);
                if (toPage >= 1 && toPage <= this.pager.pages && toPage != this.pager.currpage) {
                    this.pager.currpage = toPage;
                    this.getBuyList();
                }
            }
        },
        changeDrawId() {
            this.pager.currpage=1;
            this.getBuyList();
        }
    }

})


//vm.init();


// window.onload=function(e){
//     drawId = GetQueryString("period_number");
//     getLotterySettingList();
//     getTableData();
// }


// function getLotterySettingList(){
//     //遍历分类下选框
//     $.ajax({
//         url: HOST+"lotterySettingPC/getAllLotterySetting"
//         , type: "GET"
//         , contentType: 'application/json'
//         , success: function (res) {
//             $.each(res.data, function (index, item) {
//                 var lsList = item.lotterySettingList;
//                 if(lsList.length>1){
//                     $('#dataType').append(new Option(item.bettingMethod, item.id+"-00"));// 下拉菜单里添加元素
//
//                     $.each(item.lotterySettingList,function(idx,it1){
//                         $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
//                     });
//                 }else{
//                     $.each(item.lotterySettingList,function(idx,it1){
//                         $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
//                     });
//                 }
//
//                 // <option value="2">快打</option><option value="3">快选</option><option value="1">退码</option>
//             });
//             $("#dataType").append(new Option('快打',2));
//             $("#dataType").append(new Option('快选',3));
//             $("#dataType").append(new Option('txt导入',5));
//             $("#dataType").append(new Option('二定',6));
//             $("#dataType").append(new Option('汇总表',7));
//             //layui.form.render("select");
//         }
//     });
// }

//
//
// function bpcodesDetail(rid){
//     var html = $("#bp-"+rid).html();
//     var title = html.substr(html.lastIndexOf(",")+1);
//     localStorage.setItem("bptitle",title);
//     localStorage.setItem("bprid",rid);
//     window.parent.open("baopaiDetail.html");
// }
//
//
// function __emptyDataRow(){
//     return '<tr> <td colspan="11">暂无数据!</td> </tr>  ';
// }
//
// function getTableData(){
//     var lsId = "";
//     var lmId = "";
//     var dataType = $("#dataType").val();
//     if(dataType.indexOf("-")>-1){
//         var arr = dataType.split("-");
//         if(arr[1]=='00'){
//             lmId = arr[0];
//             dataType = -1;
//         }else if(arr[1]=="01"){
//             lsId = arr[0];
//             dataType = -1;
//         }
//     }
//     var isXian = $("#xianFlag").attr("checked")==true?1:0;
//
//     var qs = "buyCodes="+$("#buyCodes").val()+"&dataType="+dataType+"&colType="+$("#colType").val()+
//         "&isXian="+isXian+"&minValue="+$("#minValue").val()+"&maxValue="+$("#maxValue").val()+
//         "&lotterySettingsId="+lsId+"&buyType="+$("#buyType").val()+"&printNo="+$("#printNo").val()
//         +"&page="+currpage+"&limit="+limit+"&drawNo="+drawId;
//     console.log("=======================qs="+qs);
//     $.ajax({
//         url:HOST+'drawBuyRecordPC/drawBuyRecordByPage?'+qs,
//         type:'get',
//         success:function(res){
//             if(res.code==0){
//                 pages = res.data.pages;
//                 totals = res.data.total;
//                 var dataList = res.data.dataList;
//                 __buildDataTable(dataList);
//                 initPager();
//             }else{
//                 $("#tbody").html(__emptyDataRow())
//             }
//         }
//     })
// }
//
// function  initPager() {
//     $(".pageindex").html(currpage);
//     $(".pagecount").html(pages);
//     $(".recordcount").html(totals);
// }
//
// function __buildDataTable(dlist){
//     var bodydata = "";
//     if(dlist.length==0){
//         $("#tbody").html(__emptyDataRow());
//         return;
//     }
//     for(var i=0,len=dlist.length;i<len;i++){
//         var item = dlist[i];
//         var codes = '';
//         if(item.buyType==2){
//             codes= '<a href="javascript:void(0)" id="bp-'+item.id+'" style="color:blue;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.buyCodes+'</a>'
//         }else{
//             if(item.hasOneFlag==1){
//                 codes= '<span>'+item.buyCodes+'<span style="color: orangered;">现</span></span>'
//             }else{
//                 codes= item.buyCodes;
//             }
//         }
//         var buyMoney = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.buyMoney+'</a>'
//             : item.buyMoney
//
//         var peiRate = item.buyType==2?'<a href="javascript:void(0)" style="color:red;text-decoration: underline" onclick="bpcodesDetail(\''+item.id+'\')">'+item.peiRate+'</a>'
//             : "1:"+item.peiRate;
//         var createTime = item.backCodeFlag==1? item.createTime+"<br/>退:"+item.backCodeTime : item.createTime;
//
//         var cell7 = item.drawStatus==1?item.drawMoney:'--';
//         var cell8 = item.huishui;
//         var cell9 = item.buyMoney - item.huishui;
//         var cell10 = item.backCodeFlag==0?"成功":item.backCodeFlag==1?"<span style='color:orangered'>已退码</span>":'--';
//         var cell11 = item.backCodeStatus==1 && item.backCodeFlag==0?'<input type="checkbox" onclick="chooseOne(this)" value="'+item.id+'" name="'+item.printId+'"/>':'--'
//         var cls = item.backCodeFlag==1?'class="bg4"':'class=""';
//         var tr = '<tr '+cls+'><td>彩票1</td><td>'+item.printId+'</td><td>'+createTime+'</td>' +
//             '<td>'+codes+'</td>' +
//             '<td>'+buyMoney+'</td>' +
//             '<td>'+peiRate+'</td>' +
//             '<td>'+cell7+'</td><td>'+cell8+'</td><td>'+cell9+'</td><td>'+cell10+'</td><td>'+cell11+'</td></tr>';
//         bodydata = bodydata + tr;
//     }
//     $("#tbody").html(bodydata);
//
// }
//
// $("#btnSearch").on("click","",function (e) {
//     currpage = 1;
//     getTableData();
// })
//
// $("#pager").on("click",".fn-first",function (e) {
//     if(currpage!=1){
//         currpage = 1;
//         getTableData();
//     }
// })
//
// $("#pager").on("click",".fn-next",function (e) {
//     if(currpage<pages){
//         currpage++;
//         getTableData();
//     }
// })
//
// $("#pager").on("click",".fn-prev",function (e) {
//     if(currpage>1){
//         currpage--;
//         getTableData();
//     }
// })
//
// $("#pager").on("click",".fn-last",function (e) {
//     if(currpage!=pages){
//         currpage=pages
//         getTableData();
//     }
// })
// $("#pager").on("click",".fn-go",function(e){
//     var goValue = $("#goTo").val();
//     if(parseInt(goValue)>=1 && parseInt(goValue)<=pages && parseInt(goValue)!=currpage){
//         currpage  = parseInt(goValue);
//         getTableData();
//     }else{
//
//     }
// })
//
// $("#tuiOrder").on("click","",function (e) {
//     if(backCodeList.length<1){
//         layer.msg("请选择单号");
//         return;
//     }
//     $.ajax({
//         url:HOST+"codes/getOrderInfo",
//         type:'post',
//         data:JSON.stringify(orderList),
//         success:function(res){
//             layer.closeAll();
//             if(res.code==0){
//                 if(res.count>0){
//                     var popContent = '';
//                     res.data.forEach((item,idx)=>{
//                         popContent = popContent +'<p>'+item.orderNo+',共['+item.size+']个号码</p>';
//                     });
//                     popContent = popContent+'是否确定退码？'
//                     layer.confirm(popContent, {icon: 3, title:'整单退码'}, function(index){
//                         layer.close(index);
//                         orderTuima();
//                     });
//                 }
//             }
//         },
//         fail:function (res) {
//             layer.closeAll();
//         }
//     })
//     return false;
// })
//
//
// $("#tuima").on("click","",function(e){
//     console.log("点击退码了")
//     if(backCodeList.length<1){
//         layer.msg("未选择号码");
//         return;
//     }
//     layer.msg('正在处理...', {
//         icon: 16,shade: 0.3,time:-1
//     });
//     $.ajax({
//         url:HOST+"draw/delUnbuyCode",
//         type:'post',
//         data:JSON.stringify({
//             idArr:backCodeList,
//             drawId:drawId
//         }),
//         success:function(res){
//             layer.closeAll();
//             if(res.code==0){
//                 currpage=1;
//                 getTableData();
//             }else{
//                 layer.msg(res.msg);
//             }
//         },
//         fail:function (res) {
//             layer.closeAll();
//         }
//     })
//     return false;
// })
//
// function orderTuima(){
//     if(orderList.length<1){
//         layer.msg("未选择号码");
//         return;
//     }
//     layer.msg('正在处理...', {
//         icon: 16,shade: 0.3,time:-1
//     });
//     $.ajax({
//         url:HOST+"codes/delOrder",
//         type:'post',
//         data:JSON.stringify(orderList),
//         success:function(res){
//             layer.closeAll();
//             if(res.code==0){
//                 layer.msg("操作完成");
//                 currpage=1;
//                 getTableData();
//             }else{
//                 layer.msg(res.msg);
//             }
//         },
//         fail:function (res) {
//             layer.closeAll();
//         }
//     })
//
// }
//
//
// function chooseOne(obj){
//     if(obj.checked){
//         //选中
//         if(!backCodeList.includes(obj.value)){
//             backCodeList.push(obj.value);
//         }
//         if(!orderList.includes(obj.name)){
//             orderList.push(obj.name);
//         }
//     }else{
//         if(backCodeList.includes(obj.value)){
//             backCodeList.splice(backCodeList.indexOf(obj.value),1);
//         }
//         if(orderList.includes(obj.name)){
//             orderList.splice(orderList.indexOf(obj.name),1);
//         }
//     }
//     var arr = $("#tbody").find(":checkbox");
//     if(backCodeList.length === arr.length){
//         $("#selectAll").attr("checked",true)
//     }else{
//         $("#selectAll").attr("checked",false)
//     }
//     return false;
// }
//
// $("#selectAll").on("click","",function(){
//     if(this.checked){
//         $("#tbody").find(":checkbox").each(function(){
//             if(this.value!="checkAll"){
//                 this.checked=true;
//                 if(!backCodeList.includes(this.value)){
//                     backCodeList.push(this.value);
//                 }
//                 if(!orderList.includes(this.name)){
//                     orderList.push(this.name);
//                 }
//             }
//
//         })
//     }else{
//         $("#tbody").find(":checkbox").each(function(){
//             this.checked=false;
//         })
//         backCodeList = [];
//         orderList = [];
//     }
// });

// form.on('checkbox(checkAll)',function (data) {
//     if(data.elem.checked){
//         $("#tform").find(":checkbox").each(function(){
//             if(this.value!="checkAll"){
//                 this.checked=true;
//                 if(!backCodeList.includes(this.value)){
//                     backCodeList.push(this.value);
//                 }
//                 if(!orderList.includes(this.name)){
//                     orderList.push(this.name);
//                 }
//             }
//
//         })
//     }else{
//         $("#tform").find(":checkbox").each(function(){
//             this.checked=false;
//         })
//         backCodeList = [];
//         orderList = [];
//     }
//     console.log("backCodeList ======",JSON.stringify(backCodeList));
//     console.log("orderList ======",JSON.stringify(orderList));
//
//     form.render();
// })