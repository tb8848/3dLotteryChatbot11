var $ = layui.jquery;
var layer = layui.layer;

const vm = new Vue({
    el: '#main',
    data: {
        totalHs:0,
        totalMoney:0,
        userName:"",
        roleName:"",
        dataTypeList:[{
            value:0,
            text:'全部'
        }],
        dataType:0,
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
            dataType:"",
            colType:"",
            isXian:0,
            minValue:'',
            maxValue:'',
            lotterySettingsId:'',
            lmId:'',
            drawNo:'',
            buyType:0,
            codeWinType:0
        },
        heji:{
            amount:0,
            money:0,
            huishui:0,
            earn:0
        }
    },
    methods: {
        goBack(){
            window.location.href="../../baobiao/daybaobiao.html?userId="+this.userId;
            //history.back();
        },
        init(detailType) {
            this.searchParam.codeWinType = detailType;
            console.log(this.searchParam.codeWinType);
            this.userName = window.sessionStorage.getItem("userName");
            this.roleName = window.sessionStorage.getItem("roleName");
            this.userId = GetQueryString("vipId");
            this.pid = GetQueryString("pid");
            var drawNo = GetQueryString("drawNo");
            if(null!=drawNo){
                this.searchParam.drawNo = drawNo;
            }
            this.getTypeList();
            this.getDrawList();
            this.getBuyList();
        },
        getTypeList() {
            var _this = this;
            $.ajax({
                url: HOST + "lotteryMethod/getAll?lotteryType=1"
                , type: "GET"
                , contentType: 'application/json'
                , success: function (res) {
                    $.each(res.data, function (index, item) {
                        var lsList = item.lotterySettingList;
                        if (lsList.length > 1) {
                            _this.dataTypeList.push({
                                value: item.id + "-00",
                                text: item.bettingMethod,
                                type: 0
                            })
                            //$('#dataType').append(new Option(item.bettingMethod, item.id+"-00"));// 下拉菜单里添加元素
                            $.each(item.lotterySettingList, function (idx, it1) {
                                _this.dataTypeList.push({
                                    value: it1.id + "-01",
                                    text: it1.bettingRule,
                                    type: 1
                                })
                                // $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                            });
                        } else {
                            $.each(item.lotterySettingList, function (idx, it1) {
                                _this.dataTypeList.push({
                                    value: it1.id + "-01",
                                    text: it1.bettingRule,
                                    type: 1
                                })
                                // $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                            });
                        }
                    });
                    _this.dataTypeList.push({
                        value: 2, text: '快打', type: 0
                    })
                    _this.dataTypeList.push({
                        value: 3, text: '快选', type: 0
                    })
                    _this.dataTypeList.push({
                        value: 5, text: 'txt导入', type: 0
                    })
                    _this.dataTypeList.push({
                        value: 6, text: '二定', type: 0
                    })
                    _this.dataTypeList.push({
                        value: 7, text: '汇总表', type: 0
                    })
                }
            });
        },
        getDrawList() {
            //期号选择
            var _this = this;
            $.ajax({
                url: HOST + "draw/getDrawList68"
                , type: "GET"
                , success: function (res) {
                    if (res.code == 200) {
                        _this.drawList = res.data;
                        if(_this.searchParam.drawNo==""){
                            _this.searchParam.drawNo = res.data[0].drawId;
                        }
                    } else {

                    }
                }
            });
        },
        getBuyList() {
            var _this = this;
            var qs = "buyCodes=" + this.searchParam.buyCodes + "&dataType=" + this.searchParam.dataType + "&colType=" + this.searchParam.colType +
                "&isXian=" + this.searchParam.isXian + "&minValue=" + this.searchParam.minValue + "&maxValue=" + this.searchParam.maxValue +
                "&lotterySettingsId=" + this.searchParam.lotterySettingsId + "&buyType=" + this.searchParam.buyType
                + "&page=" + this.pager.currpage + "&limit=" + this.pager.limit + "&drawNo=" + this.searchParam.drawNo
                + "&vipId=" + this.userId+"&codeWinType="+this.searchParam.codeWinType;
            $.ajax({
                url: HOST + "admin/drawbuy/listByPage?" + qs
                , type: "GET"
                , success: function (res) {
                    if (res.code == 0) {
                        _this.pager.pages = res.data.pages;
                        _this.pager.total = res.data.total;
                        _this.buyList = res.data.dataList;
                        _this.heji.amount = _this.buyList.length;
                        _this.heji.earn = res.data.heji.totalEarn;
                        _this.heji.money = res.data.heji.totalMoney;
                        _this.heji.huishui = res.data.heji.totalHs;
                    } else {

                    }
                    // $("#dataType").append(new Option('快打',2));
                    // $("#dataType").append(new Option('快选',3));
                    // $("#dataType").append(new Option('txt导入',5));
                    // $("#dataType").append(new Option('二定',6));
                    // $("#dataType").append(new Option('汇总表',7));
                    //layui.form.render("select");
                }
            });
        },
        toSearch() {
            var lsId = "";
            var lmId = "";
            if (this.dataType.indexOf("-") > -1) {
                var arr = this.dataType.split("-");
                if (arr[1] == '00') {
                    lmId = arr[0];
                    this.searchParam.lmId = lmId;
                    this.searchParam.dataType = -1;
                } else if (arr[1] == "01") {
                    lsId = arr[0];
                    this.searchParam.lotterySettingsId = lsId;
                    this.searchParam.dataType = -1;
                }
            }
            this.getBuyList();
        },

        firstPage() {
            if (this.searchParam.currpage != 1) {
                this.searchParam.currpage = 1;
                this.getBuyList();
            }
        },
        nextPage() {
            if (this.searchParam.currpage < this.searchParam.pages) {
                this.searchParam.currpage++;
                this.getBuyList();
            }
        },
        prePage() {
            if (this.searchParam.currpage > 1) {
                this.searchParam.currpage--;
                this.getBuyList();
            }
        },
        lastPage() {
            if (this.searchParam.currpage < this.searchParam.pages) {
                this.searchParam.currpage = this.searchParam.pages;
                this.getBuyList();
            }
        },
        goPage() {
            if (parseInt(this.searchParam.toPage) >= this.searchParam.currpage
                && parseInt(this.searchParam.toPage) >= this.searchParam.pages) {
                this.searchParam.currpage = parseInt(this.searchParam.toPage);
                this.getBuyList();
            }
        },
        changeDrawId() {
            this.searchParam.drawNo = $("#sel_period_no").val();
            this.getBuyList();
        },
        bpcodesDetail(item) {
            var rid = item.id;
            var html = $("#bp-" + rid).html();
            var title = html.substr(html.lastIndexOf(",") + 1);
            sessionStorage.setItem("bptitle", title);
            sessionStorage.setItem("bprid", rid);
            sessionStorage.setItem("drawNo",this.searchParam.drawNo)
            sessionStorage.setItem("vipId", this.userId);
            window.parent.open("baopaiDetail.html");
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