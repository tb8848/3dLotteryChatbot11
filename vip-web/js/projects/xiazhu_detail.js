var $ = layui.jquery;
var layer = layui.layer;
var $$ = jQuery;

const vm = new Vue({
    el: '#betlist',
    i18n,
    data: {
        drawNo:'',
        openStatus:1,
        selectAllStatus:false,
        selectXian:false,
        totalHs:0,
        totalMoney:0,
        userName:"",
        roleName:"",
        dataTypeList:[],
        dataType:"0",
        drawList:[],
        buyList:[],
        orderList:[],
        backCodeList:[],
        userId:'',
        pager:{
          pages:0,
          total:-1,
          currpage:1,
          toPage:'',
            limit:40
        },
        searchParam:{ //基本资料
            buyCodes:"",
            dataType:"0",
            colType:1,
            isXian:0,
            minValue:'',
            maxValue:'',
            lotterySettingsId:'',
            lmId:'',
            drawNo:'',
            buyType:0,
            codeWinType:0,
            printNo:''
        },
        backCodeList:[],
        heji:{
            amount:0,
            money:0,
            huishui:0,
            earn:0
        },
        isBillDetail:false,
        billDrawNo:'',
        startPager:true
    },
    methods: {
        setOpenStatus(msg){
            this.openStatus = msg.openStatus;
            if(this.drawNo!=msg.drawNo){
                this.drawNo = msg.drawNo;
                this.init(0);
            }
            if(this.drawNo==this.billDrawNo){
                this.isBillDetail = false;
            }
        },
        init(detailType) {
            this.dataTypeList=[];
            this.openStatus = sessionStorage.getItem("drawOpenStatus");
            this.searchParam.codeWinType = detailType;
            this.userName = window.sessionStorage.getItem("userName");
            this.roleName = window.sessionStorage.getItem("roleName");
            this.userId = GetQueryString("vipId");
            var drawNo = GetQueryString("period_number");
            this.billDrawNo = drawNo;
            if(null!=drawNo){
                this.searchParam.drawNo = drawNo;
                if(this.drawNo!=drawNo){
                    this.isBillDetail = true;
                }else{
                    this.isBillDetail = false;
                }
            }
            this.getTypeList();
            this.getBuyList();
        },
        changeXianFlag(){
          this.selectXian = !this.selectXian;
          this.searchParam.isXian = this.selectXian?1:0
        },
        selectAll(){
            var that = this;
            this.selectAllStatus = !this.selectAllStatus;
            this.buyList.forEach((item,idx)=>{
                if(item.backCodeFlag==0 && item.backCodeStatus==1){
                    item.checked = that.selectAllStatus;
                }
            })
        },
        selectOne(dataItem){
            var that = this;
            var checkNum = 0;
            that.buyList.forEach((item,idx)=>{
                if(item.backCodeFlag==0 && item.backCodeStatus==1){
                    if(dataItem.id == item.id){
                        item.checked = !item.checked;
                    }
                    if(item.checked){
                        checkNum++;
                    }
                }
            })
            if(checkNum==that.heji.amount){
                that.selectAllStatus = true;
            }else{
                that.selectAllStatus = false;
            }
        },
        getTypeList() {

            var _this = this;
            _this.dataTypeList = [];
            $.ajax({
                url: HOST + "lotterySettingPC/getAllLotterySetting"
                , type: "GET"
                , contentType: 'application/json'
                , success: function (res) {
                    _this.dataTypeList.push({
                        value: "0",
                        text: _this.$t('message.all')
                    })
                    $.each(res.data, function (index, item) {
                        var lsList = item.lotterySettingList;
                        var betMethod = item.bettingMethod;
                        if (lsList.length > 1) {
                            _this.dataTypeList.push({
                                value: item.id + "-00",
                                text: betMethod,
                                type: 0
                            })
                            //$('#dataType').append(new Option(item.bettingMethod, item.id+"-00"));// 下拉菜单里添加元素
                            $.each(item.lotterySettingList, function (idx, it1) {
                                if(it1.typeId!=0){
                                    _this.dataTypeList.push({
                                        value: it1.id + "-01",
                                        text: "------"+it1.bettingRule,
                                        type: 1
                                    })
                                }

                                // $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                            });
                        } else {
                            $.each(item.lotterySettingList, function (idx, it1) {
                                if(it1.typeId!=0){
                                    _this.dataTypeList.push({
                                        value: it1.id + "-01",
                                        text: it1.bettingRule,
                                        type: 1
                                    })
                                }
                                // $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                            });
                        }
                    });
                    // _this.dataTypeList.push({
                    //     value: "2", text: _this.$t('message.kuaida'), type: 0
                    // })
                    // _this.dataTypeList.push({
                    //     value: "3", text: _this.$t('message.kuaixuan1'), type: 0
                    // })//'快选'
                    // _this.dataTypeList.push({
                    //     value: "5", text: _this.$t('message.txtimport'), type: 0
                    // })//'txt导入'
                    // _this.dataTypeList.push({
                    //     value: "6", text: _this.$t('message.erding1'), type: 0
                    // })//'二定'
                    // _this.dataTypeList.push({
                    //     value: "7", text: _this.$t('message.huizongbiao'), type: 0
                    // })//'汇总表'
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
            var qs = "buyCodes=" + this.searchParam.buyCodes.toUpperCase() + "&dataType=" + this.searchParam.dataType + "&colType=" + this.searchParam.colType +
                "&isXian=" + this.searchParam.isXian + "&minValue=" + this.searchParam.minValue + "&maxValue=" + this.searchParam.maxValue +
                "&lotterySettingsId=" + this.searchParam.lotterySettingsId + "&buyType=" + this.searchParam.buyType
                + "&page=" + this.pager.currpage + "&limit=" + this.pager.limit + "&drawNo=" + this.searchParam.drawNo
                + "&vipId=" + this.userId+"&codeWinType="+this.searchParam.codeWinType+"&printNo="+this.searchParam.printNo+"&lmId="+this.searchParam.lmId;
            $.ajax({
                url: HOST + "pc/drawBuyRecord/getByPage?" + qs
                , type: "GET"
                , success: function (res) {
                    if (res.code == 0) {
                        _this.pager.pages = res.data.pages;
                        _this.pager.total = res.data.total;
                        _this.buyList = res.data.dataList;
                        var heji = res.data.heji;
                        _this.heji.money = heji.totalMoney;
                        _this.heji.huishui = heji.totalHuiShui;
                        _this.heji.earn = heji.totalEarn;
                        _this.heji.amount=heji.amount;
                        _this.heji.draw = heji.totalDraw;
                        _this.buyList.forEach((item,idx)=>{
                            item.checked = false;
                        })
                        _this.startPager = false;
                    }
                }
            });
        },
        toSearch() {
            this.buyList = [];
            this.heji = {};
            this.startPager = true;
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
                    this.searchParam.lmId = "";
                    this.searchParam.lotterySettingsId = lsId;
                    this.searchParam.dataType = -1;
                }
            }else{
                this.searchParam.dataType = this.dataType;
                this.searchParam.lmId = "";
                this.searchParam.lsId = "";
            }
            this.pager.currpage = 1;

            this.getBuyList();
        },
        winDetail(){
            this.pager.currpage=1;
            this.searchParam.codeWinType=1;
            this.getBuyList();
        },
        firstPage() {
            if (this.pager.currpage != 1) {
                this.pager.currpage = 1;
                this.startPager = true;
                this.getBuyList();
            }
        },
        nextPage() {
            if (this.pager.currpage < this.pager.pages) {
                this.pager.currpage++;
                this.startPager = true;
                this.getBuyList();
            }
        },
        prevPage() {
            if (this.pager.currpage > 1) {
                this.pager.currpage--;
                this.startPager = true;
                this.getBuyList();
            }
        },
        lastPage() {
            if (this.pager.currpage < this.pager.pages) {
                this.pager.currpage = this.pager.pages;
                this.startPager = true;
                this.getBuyList();
            }
        },
        goPage() {
            const toPage = parseInt(this.pager.toPage);
            if (toPage>=1 && toPage<=this.pager.pages && toPage!=this.pager.currpage) {
                this.pager.currpage = parseInt(this.pager.toPage);
                this.startPager = true;
                this.getBuyList();
            }
        },
        changeDrawId() {
            this.searchParam.drawNo = $("#sel_period_no").val();
            this.pager.currpage=1;
            this.getBuyList();
        },
        bpcodesDetail(item) {
            var dno = "";
            if(this.searchParam.drawNo!=''){
                dno = this.searchParam.drawNo;
            }else{
                dno = this.drawNo;
            }
            var rid = item.id;
            const codes = item.huizongName;
            var title = codes;
            sessionStorage.setItem("bptitle", title);
            sessionStorage.setItem("bprid", rid);
            sessionStorage.setItem("mingxi_drawNo",dno)
            sessionStorage.setItem("vipId", this.userId);
            window.parent.open("fanganDetail.html");
        },
        tuiOrder(){
            this.orderList = [];
            var that = this;
            this.buyList.forEach((item,idx)=>{
                if(item.checked){
                    if(!that.orderList.includes(item.printId)){
                        that.orderList.push(item.printId)
                    }
                }
            })
            if(that.orderList.length<1){
                layer.msg($i18np.prop("xiazhu.noOrderSelect"));
                return;
            }
            if(that.orderList.length>1){
                layer.msg($i18np.prop("xiazhu.onlyOneOrder"));
                return;
            }
            $.ajax({
                url:HOST+"codes/getOrderInfo",
                type:'post',
                data:JSON.stringify(that.orderList),
                success:function(res){
                    layer.closeAll();
                    if(res.code==0){
                        if(res.count>0){
                            var popContent = '';
                            res.data.forEach((item,idx)=>{
                                const params = [
                                    {name:'XXX',value:item.orderNo},
                                    {name:'YYY',value:item.size}
                                ]
                                popContent = popContent +'<p>'+$i18np.propByPlaceholder("xiazhu.tip2",params)+'</p>';
                            });
                            popContent = popContent+$i18np.prop("xiazhu.tip1")
                            layer.confirm(popContent, {icon: 3, title:$i18np.prop("xiazhu.orderTuima"),btn:[$i18np.prop("xiazhu.confirm"),$i18np.prop("xiazhu.cancel")]}, function(index){
                                layer.close(index);
                                that.orderTuima();
                            });
                        }
                    }
                },
                fail:function (res) {
                    layer.closeAll();
                }
            })
            return false;
        },
        orderTuima() {
            var that = this;
            if (that.orderList.length < 1) {
                layer.msg($i18np.prop("xiazhu.noCodeSelect"));
                return;
            }
            layer.msg($i18np.prop("xiazhu.chulizhong"), {
                icon: 16, shade: 0.3, time: -1
            });
            $.ajax({
                url: HOST + "codes/delOrder",
                type: 'post',
                data: JSON.stringify(that.orderList),
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        layer.msg($i18np.prop("xiazhu.caozuo.success"));
                        that.orderList = [];
                        that.selectAllStatus = false;
                        that.pager.currpage = 1;
                        that.getBuyList();
                        window.parent.getCredit();
                        window.parent.reloadPrintTable();
                    } else {
                        layer.msg(res.msg);
                    }
                },
                fail: function (res) {
                    layer.closeAll();
                }
            })
        },
        tuima(){
            var that = this;
            this.buyList.forEach((item,idx)=>{
                if(item.checked){
                    if(!that.backCodeList.includes(item.id)){
                        that.backCodeList.push(item.id)
                    }
                }
            })
            if(that.backCodeList.length<1){
                layer.msg($i18np.prop("xiazhu.noCodeSelect"));
                return;
            }
            layer.msg($i18np.prop("xiazhu.chulizhong"), {
                icon: 16,shade: 0.3,time:-1
            });
            $.ajax({
                url:HOST+"draw/delUnbuyCode",
                type:'post',
                data:JSON.stringify({
                    idArr:that.backCodeList,
                    drawId:that.drawId
                }),
                success:function(res){
                    layer.closeAll();
                    that.selectAllStatus = false;
                    if(res.code==0){
                        that.pager.currpage=1;
                        that.backCodeList = [];
                        that.getBuyList();
                        window.parent.getCredit();
                        window.parent.reloadPrintTable();
                    }else{
                        layer.msg(res.msg);
                    }
                },
                fail:function (res) {
                    layer.closeAll();
                }
            })
            return false;
        },
        toPrint(){
            if(this.buyList.length>0){
                var title = "";
                if(this.searchParam.codeWinType==0){
                    if(this.isBillDetail){
                        title =  this.$t('message.page_curr')+this.searchParam.drawNo+this.$t('message.qi')+" "+this.$t('message.xiazhuMingxi.title')
                    }else{
                        title = this.$t('message.xiazhuMingxi.subTitle');
                    }
                }else{
                    if(this.isBillDetail){
                        title =  this.$t('message.page_curr')+this.searchParam.drawNo+this.$t('message.qi')+" "+this.$t('message.xiazhuMingxi.drawDetail')
                    }else{
                        title = this.$t('message.xiazhuMingxi.subTitle1');
                    }
                }
                const params = {
                    dataList:this.buyList,
                    hejiRow:this.heji,
                    dataTitle:title,
                    isWin:this.searchParam.codeWinType
                }
                //数据写入缓存
                sessionStorage.setItem("printParams",JSON.stringify(params));
                window.open("xiazhu-print.html")
            }else{
                layer.alert($i18np.prop("xiazhu.noDataPrint"),
                    {title:$i18np.prop("xiazhu.alertTitle"),
                        btn:[$i18np.prop("xiazhu.confirm")]
                    }
                    )
            }
        },
    }
})



vm.init(0);