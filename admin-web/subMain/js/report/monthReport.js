function GetQueryString(name){
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var a = decodeURI(window.location.search);
    var r = a.substr(1).match(reg);//search,查询？后面的参数，并匹配正则
    if(r!=null)return  unescape(r[2]); return null;
}

window.sessionStorage.setItem("yueji_nav","");
const vm = new Vue({
    el: '#main2',
    data: {
        parentRole: "",
        selfRole: "",
        childRole: "",
        yearMonthList: "",
        monthData: "",
        month: "",
        all: "",
        dataList: [],
        totalData: {},
        userId: "",
        selfName:"",
        isBack:'',
        logUserId:'',
        userDetail: "",
        start: "",
        end: "",
    },
    methods: {
        init() {
            const flag = GetQueryString("userDetail");
            const monthQuery = GetQueryString("months");
            this.all = GetQueryString("all");
            this.userId = GetQueryString("parentId");
            this.userDetail = GetQueryString("userDetail");
            if (flag == 1) {
                $("#top").hide();
                $("#lnk_print").hide()
            }
            if (monthQuery != null && monthQuery != "" && monthQuery != undefined) {
                this.month = monthQuery;
                this.monthData = monthQuery;
            }
            if (this.userId != null && this.userId != '' && this.userId != undefined) {
                $("#dgd").hide();
                $("#dgd2").hide();
            }
            const _this = this;

            $.ajax({
                url:HOST+"report/getReportHeader?userId=" + _this.userId ,
                success:function(res){
                    if(res.code == 200) {
                        _this.parentRole = res.data.parentRole
                        _this.selfRole = res.data.selfRole
                        _this.childRole = res.data.childRole
                        _this.selfName = res.data.selfName;
                        _this.logUserId = res.data.userId;
                        // var qs = {
                        //     uid:_this.userId,
                        //     roleName:_this.selfRole,
                        //     userName:_this.selfName,
                        //     months:_this.month,
                        //     all:_this.all,
                        //     isBack:1
                        // }
                        // _this.buildNavPath(qs);
                    }
                }
            });

            $.ajax({
                url: HOST + "draw/getDrawList68"
                , type: "get"
                , success: function (res) {
                    if (res.code == 200) {
                        let c = 1;
                        let startDraw = GetQueryString("startDraw");
                        let endDraw = GetQueryString("endDraw");
                        // 期号select
                        for(const i in res.data) {
                            if (startDraw != "" && startDraw != null && startDraw != undefined) {
                                if (startDraw == res.data[i].drawId) {
                                    $("#start_period_no").append("<option value='"+res.data[i].drawId+"' selected>"+res.data[i].drawIdStr+"</option>")
                                }else {
                                    $("#start_period_no").append("<option value='"+res.data[i].drawId+"'>"+res.data[i].drawIdStr+"</option>")
                                }
                            }else {
                                if (c == res.data.length) {
                                    $("#start_period_no").append("<option value='"+res.data[i].drawId+"' selected>"+res.data[i].drawIdStr+"</option>")
                                }else {
                                    $("#start_period_no").append("<option value='"+res.data[i].drawId+"'>"+res.data[i].drawIdStr+"</option>")
                                }
                            }

                            c ++;
                        }
                        for(const i in res.data) {
                            if (endDraw != "" && endDraw != null && endDraw != undefined) {
                                if (endDraw == res.data[i].drawId) {
                                    $("#end_period_no").append("<option value='"+res.data[i].drawId+"' selected>"+res.data[i].drawIdStr+"</option>")
                                }else {
                                    $("#end_period_no").append("<option value='"+res.data[i].drawId+"'>"+res.data[i].drawIdStr+"</option>")
                                }
                            }else {
                                $("#end_period_no").append("<option value='"+res.data[i].drawId+"'>"+res.data[i].drawIdStr+"</option>")
                            }

                        }
                        _this.initList();
                    } else {
                        layer.msg("数据异常");
                    }
                }
            });
        },
        initList() {
            layer.msg("数据加载中...",{
                time:-1,
                icon:16,
                shade:0.3
            })
            const _this = this;
            if (GetQueryString("all") == 1 || _this.all == 1) {
                _this.start = $("#start_period_no").val();
                _this.end = $("#end_period_no").val();
            }
            $.ajax({
                url: HOST + "report/getMonthReportList?startDrawId=" + _this.start + "&endDrawId=" + _this.end + "&month=" + _this.month + "&userId=" + _this.userId
                , type: "get"
                , success: function (res) {
                    layer.closeAll();
                    if (res.code == 200) {
                        _this.yearMonthList = res.data.yearMonthList;
                        _this.dataList = res.data.dataList
                        _this.totalData = res.data.total
                    } else {
                        layer.msg("数据异常");
                    }
                }
            });
        },
        changeSelect () {
            this.start = $("#start_period_no").val();
            this.end = $("#end_period_no").val();
            this.all = 1
            this.initList();
            //window.location.href = 'yuebaobiao.html?all=1&startDraw=' + startDraw + "&endDraw=" + endDraw
        },
        buildNavPath(qs){
            var that = this;
            var pp = sessionStorage.getItem("yueji_nav");
            var navPath = [];
            var pathStr = "";
            if(pp==null || pp==""){
                navPath.push(qs)
            }else{
                var newArr=[];
                navPath=JSON.parse(pp);
                var exist = false;
                for(var i=0,len=navPath.length;i<len;i++){
                    var item = navPath[i];
                    newArr.push(item);
                    if(item.uid==that.uid){
                        exist = true;
                        break;
                    }
                }
                if(exist && that.isBack=="1"){
                    navPath = newArr;
                }else{
                    navPath.push(qs)
                }
            }
            navPath.forEach((item,idx)=>{
                var url = "yuebaobiao.html?userId="+item.uid+"&isBack="+item.isBack+"&all="+that.all+"&months="+that.month;
                var alink = '<a class="blue" href="'+url+'" title="查看'+item.userName+'">\n' +
                    '<span style="color: #0000FF;">'+item.userName+'</span>\n' +
                    '<span style="color: #0000FF;">('+item.roleName+')</span>\n' +
                    '</a>';
                if(pathStr==''){
                    pathStr = alink;
                }else{
                    pathStr = pathStr + ">>"+alink;
                }
            });
            $("#navPath").html(pathStr);
            sessionStorage.setItem("yueji_nav",JSON.stringify(navPath))
        },
        toWeekReport(){
            sessionStorage.setItem("yueji_nav","");
            window.location.href="zhoubaobiao.html";
        },
        toMonthReport(){
            sessionStorage.setItem("yueji_nav","");
            window.location.href="yuebaobiao.html?all=1";
        },
        toDayReport(){
            sessionStorage.setItem("yueji_nav","");
            window.location.href="daybaobiao.html";
        },
        print() {
            $("#div_print").jqprint();
        },
        clickAll() {
            this.month = ""
            this.monthData = ""
            this.all = 1
            this.initList();
        },
        clickMonth(month) {
            this.month = month
            this.monthData = month
            this.all = 0
            this.initList();
        },
    }
})
vm.init();