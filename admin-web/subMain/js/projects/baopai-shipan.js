//包牌实盘页面脚本
var $ = layui.jquery;
var layer = layui.layer;
// var buyList = []; //购买记录
// var lmId = 4 ; //分类ID
// var settingList=[];
// var pager={
//     total:0,
//     pages:0,
//     curpage:1,
//     limit:100,
//     toPage:""
// }
// var settingCodesList=[]; //每个子类的号码
// var codesInfo = {
//     lsid:"",
//     lsName:"",
//     fuzhi:0,
//     zzvalue:0,
//     codesList:[], //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
// }

function popChild(tag){
    layer.open({
        type:2,
        title:'出货',
        content:'kuaida.html',
        area:['80%','650px']
    })
}

// var settingList = [];
// var codesList = [];
// var pageData=[];
// var selectedList=[];
// var rise_drop = 0;
// var lmId = 4;
// var fmName = "";
// var bpSettingList = [];
// var changeList = [];
// var lsId = 1;
// var currSetting = null;
// var bpId = null;
// var bpBuyList=[];

const vm = new Vue({
    el: '#handicap_reality_combo',
    data: {
        dataSortType:0, //0:号码，1：负值
        settingList:[],
        codesList:[], //子类的号码列表
        pageData:[],
        selectedList:[],
        rise_drop:0,
        lmId:4,
        fmName:"",
        bpSettingList:[],
        changeList:[],
        lsId:"",
        currSetting: {},
        bpId:"",
        bpBuyList:[],
        settingCodesList:[],
        noBuyMaxLoss:{},
        zzvalueMap:{},
        lsAvgOddsMap:{},
        lsList:[],
        drawId:'',
        distCode:'',
        nickName:'',
        userId:'',
        roleName:'',
        pager:{
            total:0,
            pages:0,
            curpage:1,
            limit:100,
            toPage:""
        },
        lines:0,
        oddsChange:{
            oddsChangeWay:0,
            odds:"",
            itemLimit:"",
            itemChangeWay:0,
            codesArr:[],
            codesInfoArr:[],
            isShow:false,
            comboId:0
        },
        batchSel:{
            size:0,
            lsId:""
        },
        currLs:{},//当前选中的分类
        loadOver:0, //数据加载完成标识
        oddsChangeList:[], //赔率变动记录
        oddsChangeComboList:[],
        currSettingCodes: {
            pageData:[]
        },
        zongtouList:[]
    },
    methods: {
        initDataList() {
            var _lmId = GetQueryString("lmId");
            if (_lmId == null) {
                this.lmId = 4;
            } else {
                this.lmId = _lmId;
            }
            this.getMethodList();
            //this.getBpSettingList();
        },
        changeMethodType(item) {
            var that = this;
            this.lmId = item.id;
            that.bpId = "";
            that.lsId = "";
            that.pager.curpage = 1;
            that.dataSortType = 0;
            this.settingList.forEach((lmItem,idx)=>{
                if(lmItem.id == item.id){
                    that.currSetting = lmItem.lotterySettingList[0];
                    that.lsId = that.currSetting.id;
                }
            })
            console.log("currSetting", that.currSetting)
            this.getBpSettingList();
        },
        getMethodList() {//大类数据
            const _this = this;
            layer.msg("数据加载中...", {time: -1, shade: 0.3, icon: 16})
            $.ajax({
                //url: HOST + 'admin/drawbuy/shipan/settings?lotteryType=1',
                url: HOST + 'lotteryMethod/getAll?lotteryType=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        var list = res.data;
                        list.forEach((item, idx)=>{
                            if(item.id >= 2 && item.id < 5){
                                _this.settingList.push(item);
                            }
                        })
                        _this.settingList.forEach((item, idx)=> {
                            item.fuzhi = 0;
                            item.lotterySettingList.forEach((it1, idx1)=> {
                                it1.fuzhi = 0;
                                it1.avgOdds = 0;
                                if (item.id == 2) {
                                    it1.zzvalue = 100;
                                } else if (item.id == 3) {
                                    it1.zzvalue = 1000;
                                } else if (item.id == 4) {
                                    it1.zzvalue = 10000
                                }
                            })
                            if(item.id==_this.lmId){
                                _this.changeMethodType(item);
                            }
                        })
                        _this.loadOver = 1;
                        $("#tbody").removeClass('hide');
                    } else {
                        layer.closeAll();
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误")
                }
            })
        },
        __checkSelectExist(__lsId,__bpId) {
            for(var i=0,len=this.changeList.length;i<len;i++){
                if(this.changeList[i].lotterySettingId==__lsId){
                    if(this.changeList[i].id==__bpId){
                        return i;
                    }
                }
            }
            return -1;
        },
        upLotteryMethodZongTou(lmId,fuzhi){
          //更新大类的总投
            var that = this;
            that.settingList.forEach((lm,idx)=>{
                if(lm.id==lmId){
                    that.settingList[idx].fuzhi=fuzhi;
                    //lm.fuzhi = lmFuzhi;
                }
            })
        },
        upLotterySettingZongTou(lmId,lsId,fuzhi){
            var that = this;
            that.bpSettingList.forEach((bpLs,idx)=>{
                if(bpLs.id==lsId){
                    that.bpSettingList[idx].fuzhi = fuzhi;
                    //bpLs.fuzhi = fuzhi;
                }
                if(that.currSetting.id == lsId){
                    that.currSetting.fuzhi = fuzhi;
                }
            })
        },
        upBpSettingLanHuo(lmId,lsId,bpId,bpLanMoney){
            var that = this;
            that.bpSettingList.forEach((bpLs,idx)=>{
                if(bpLs.id==lsId){
                    bpLs.bpSettingList.forEach((item,idx)=>{
                        if(item.id == bpId){
                            item.lanMoney = bpLanMoney;
                        }
                    })
                }
            })
        },
        getZongTou(){
            var that = this;
            $.ajax({
                url: HOST + 'admin/drawbuy/bpshipan/zongtou?lotteryType=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        that.zongtouList = res.data;
                        that.zongtouList.forEach((lm,idx)=>{
                            that.upLotteryMethodZongTou(lm.id,lm.fuzhi);
                            if(lm.lotterySettingList!=null){
                                lm.lotterySettingList.forEach((ls,idx1)=>{
                                    that.upLotterySettingZongTou(lm.id,ls.id,ls.fuzhi);
                                    // if(ls.bpSettingList!=null){
                                    //     ls.bpSettingList.forEach((bp,idx2)=>{
                                    //         that.upBpSettingLanHuo(lm.id,ls.id,bp.id,bp.lanMoney);
                                    //     })
                                    // }
                                })
                            }
                        })
                        console.log("===========settingList",that.settingList);
                    } else {
                        //layer.closeAll();
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误")
                }
            })
            //bpshipan/settings
        },
        addBpOddsChange () { //添加
            var that =this;
            if(that.changeList.length<1){
                layer.msg("未选择分类");
                return;
            }
            if (this.oddsChange.odds == "" && this.oddsChange.itemLimit == "") {
                layer.msg("未输入任何内容");
                return;
            }
            var odds = $("#set_odds").val();
            var itemLimit = $("#odds_item_limit").val();
            if(odds=="" && itemLimit==""){
                layer.msg("未填写内容");
                return;
            }
            // odds = this.oddsChange.odds;
            // limit = this.oddsChange.itemLimit;
            // var oddsChangeWay = $("input[name='rise_drop']:checked").val();
            // var itemLimitWay = $("input[name='islimit']:checked").val();
            // console.log("oddsChangeWay=",oddsChangeWay+",itemLimitWay=",itemLimitWay);
            for(var i=0,len=that.changeList.length;i<len;i++){
                that.changeList[i].oddsChangeType=that.oddsChange.oddsChangeWay;
                that.changeList[i].itemLimitType=that.oddsChange.itemChangeWay;
                that.changeList[i].peiRateChange=that.oddsChange.odds;
                that.changeList[i].oneItemLimit = that.oddsChange.itemLimit;
            }
                layer.msg("数据保存中...",{time:-1,shade:0.3,icon:16});
                $.ajax({
                    url:HOST+"bpSetting/change/saveChangeFromBpShiPan",
                    type:'post',
                    contentType:'application/json',
                    data:JSON.stringify(that.changeList),
                    success:function(res){
                        layer.closeAll();
                        if(res.code==200){
                            that.cancelOddsChange();
                            that.getBpSettingList();
                        }else{
                            layer.msg(res.msg);
                        }
                    },
                    error:function (e) {
                        layer.closeAll();
                    }
                })
                return false;
            },
        getBpSettingList() {//获取大类下所有小类的包牌设置
            var that = this;
            $.ajax({
                url: HOST + 'bpSetting/getList?lmId=' + that.lmId,
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        that.bpSettingList = res.data; //子类的包牌设置
                        that.bpSettingList.forEach((item, idx)=> {
                            item.fuzhi = 0;
                            item.selected = false;
                            if(that.lsId == "" && idx == 0){
                                that.lsId = item.id
                            }
                            item.bpSettingList.forEach((it1, idx1)=> {
                                it1.selected = false;
                                //it1.lanMoney = 0;
                                if (item.id == that.lsId) {
                                    if(that.bpId == "" && idx1 == 0){
                                        that.bpId = it1.id;
                                    }
                                }
                            })
                        })
                        that.getBuyList()
                        that.getZongTou();
                    } else {
                        //layer.closeAll();
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误")
                }
            })
        },
        getBuyList() {//获取购买记录
            const _this = this;
            $.ajax({
                //url: HOST + 'admin/drawbuy/bplist?bpId=' + _this.bpId,
                url: HOST + 'admin/shipan/bpListAll?bpId=' + _this.bpId,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        if(res.data.dataList!=null){
                            _this.bpBuyList = res.data.dataList;
                            //_this.zzvalueMap = res.data.zzvalueMap;
                            _this.noBuyMaxLoss = res.data.noBuyLossMap;
                            _this.lsAvgOddsMap = res.data.lsAvgOddsMap;
                        }
                        _this.createSettingCodes();
                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误")
                }
            })
        },
        getBuyCodeInfo(code, isXian) {
            var info = null;
            if (this.bpBuyList.length > 0) {
                for (var i = 0, len = this.bpBuyList.length; i < len; i++) {
                    var item = this.bpBuyList[i];
                    if (item.buyCode == code && isXian == item.isXian) {
                        return item;
                    }
                }
            }
            return info;
        },
        initOddsChangeWay(v) {
            this.oddsChange.oddsChangeWay = v;
        },
        initItemLimitChangeWay(v) {
            this.oddsChange.itemChangeWay = v;
        },
        cancelOddsChange() {
            this.oddsChange.oddsChangeWay = 0;
            this.oddsChange.itemChangeWay = 0;
            this.oddsChange.itemLimit = "";
            this.oddsChange.odds = "";
            this.changeList=[];
        },
        __updateCodesData(datalist, isXian, noBuyLoss, zzvalue, lsItem) { //从购买记录中找到号码的数据并回填
            var that = this;
            datalist.forEach((item, idx)=> {
                var info = that.getBuyCodeInfo(item.code, isXian);
                /**
                 * maxLoss:0,chuhuo:0,odds:0,odds_avg:0,package_chuhuo:0,
                 */
                if (null != info) {
                    item.maxLoss = info.maxLoss;
                    item.chuhuo = info.shihuoMoney; //此字段未使用
                    item.odds = info.peiRate;
                    item.odds_avg = info.avg_odds;
                    item.package_chuhuo = info.bpMoney
                } else {
                    item.maxLoss = noBuyLoss;
                    item.odds = lsItem.peiRate;
                }
            })
            datalist.sort(that.__sortByCode) //按负值排序
            var codesInfo = {
                lsid: lsItem.id,
                lsName: lsItem.bettingRule,
                fuzhi: lsItem.fuzhi,
                zzvalue: datalist.length - zzvalue,
                odds_avg: 0,
                codesList: datalist, //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
                pageData: []
            };
            return codesInfo;
        },
        createSettingCodes(){
            var _this = this;
            var buyList = this.bpBuyList;
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = _this.settingList[i];
                var __lmId = item.id;
                var lmFuzhi = 0;
                var dlist = item.lotterySettingList;
                item.lotterySettingList.forEach((it1, idx1)=> {
                    var lsFuzhi = 0;
                    buyList.forEach((cc, ii)=> {
                        if(cc.lotterySettingId == it1.id){
                            lsFuzhi = lsFuzhi + cc.bpMoney;
                            lmFuzhi = lmFuzhi + cc.bpMoney;
                        }
                    })
                    //回填小分类的负值数据
                    //_this.settingList[i].lotterySettingList[idx1].fuzhi = lsFuzhi;
                })
                _this.settingList[i].fuzhi = lmFuzhi;
                if (item.id == _this.lmId) {
                    _this.lsList = dlist;
                }
            }

            console.log("=======_this.lsList", _this.lsList);
            _this.settingCodesList = [];
            _this.lsList.forEach((item, idx)=> {
                var _lsId = item.id;
                var codesAmount = 0;
                //var zzvalue = _this.zzvalueMap[_lsId];
                var lsAvgOdds = _this.lsAvgOddsMap[_lsId];
                if (lsAvgOdds == null || lsAvgOdds == undefined) {
                    lsAvgOdds = 0;
                }
                item.avgOdds = lsAvgOdds;
                console.log("zzvalue == ", zzvalue, "lsId==" + _lsId)
                console.log("item == ", item)
                // if (zzvalue != null && zzvalue != undefined) {
                //     codesAmount = zzvalue;
                // }
                var noBuyLoss = _this.noBuyMaxLoss[_lsId];
                if (noBuyLoss == null || noBuyLoss == undefined) {
                    noBuyLoss = 0;
                }
                var dlist = createCodesForDing(_this.lmId, item.bettingRule, _lsId);

                var codesInfo = _this.__updateCodesData(dlist, 0, noBuyLoss, codesAmount, item);
                var zzvalue = 0;
                codesInfo.codesList.forEach((item,idx)=>{
                    if(item.maxLoss>=0){
                        zzvalue++;
                    }
                })
                codesInfo.zzvalue = zzvalue;
                if (_this.currSetting.id == _lsId) {
                    _this.currSetting.zzvalue = zzvalue;
                    _this.currSetting.avgOdds = lsAvgOdds;
                }
                _this.settingCodesList.push(codesInfo)

            });
            console.log("======settingCodesList ===", _this.settingCodesList);
             _this.getPagerData();
        },
        getPagerData(){
            var _this = this;
            if (this.settingCodesList.length == 1) {
                var codeList = this.settingCodesList[0].codesList;
                this.pager.total = codeList.length;
                //this.initPager();
                var st = (this.pager.curpage - 1) * this.pager.limit;
                var et = (this.pager.curpage) * this.pager.limit;
                console.log(st, et);
                if (et > codeList.length) {
                    et = codeList.length;
                }
                var pgData = codeList.slice(st, et);
                this.settingCodesList[0].pageData = pgData;
                this.batchSel.lsId = this.settingCodesList[0].lsid;
                this.lines = pgData.length % 10 == 0 ? parseInt(pgData.length / 10) : parseInt(pgData.length / 10) + 1;
                this.pager.pages = this.pager.total % this.pager.limit == 0 ?
                    parseInt(this.pager.total / this.pager.limit)
                    : parseInt(this.pager.total / this.pager.limit) + 1;
            } else {
                _this.pager.total = 0;
                var clsTotal = 0; //每个分类的总数量
                this.batchSel.lsId = "";
                this.settingCodesList.forEach((item, idx)=> {
                    var codeList = item.codesList;
                _this.pager.total = _this.pager.total + codeList.length;
                clsTotal = codeList.length;
                var st = (_this.pager.curpage - 1) * _this.pager.limit;
                var et = (_this.pager.curpage) * _this.pager.limit;
                if (et > codeList.length) {
                    et = codeList.length;
                }
                var pgData = codeList.slice(st, et);
                _this.settingCodesList[idx].pageData = pgData;
                _this.lines = pgData.length % 10 == 0 ? parseInt(pgData.length / 10) : parseInt(pgData.length / 10) + 1;
            })
                this.pager.pages = clsTotal % this.pager.limit == 0 ?
                    parseInt(clsTotal / this.pager.limit)
                    : parseInt(clsTotal / this.pager.limit) + 1;
                //this.initPager();
            }
        },
        toFirst(){//第一页
            console.log(("=======toFirst"))
            if (this.pager.curpage > 1) {
                this.pager.curpage = 1;
                this.getPagerData();
                // if(this.fullScreen==0){
                //     this.getPagerData();
                // }else{
                //     this.getPagerDataAtFullScreen();
                // }

            }
        },
        toPrev() {//上一页
            console.log(("=======toPre"))
            if (this.pager.curpage > 1) {
                this.pager.curpage--;
                this.getPagerData();
                // if(this.fullScreen==0){
                //     this.getPagerData();
                // }else{
                //     this.getPagerDataAtFullScreen();
                // }
            }
        }
        ,
        toNext() {//下一页
            console.log(("=======toNext"))
            if (this.pager.curpage < this.pager.pages) {
                this.pager.curpage++;
                this.getPagerData();
                // if(this.fullScreen==0){
                //     this.getPagerData();
                // }else{
                //     this.getPagerDataAtFullScreen();
                // }
            }
        }
        ,
        toLast(){//尾页
            console.log(("=======toLast"))
            if (this.pager.curpage < this.pager.pages) {
                this.pager.curpage = this.pager.pages;
                this.getPagerData();
                // if(this.fullScreen==0){
                //     this.getPagerData();
                // }else{
                //     this.getPagerDataAtFullScreen();
                // }
            }
        }
        ,
        toPage(){//指定页
            if (this.pager.toPage != "") {
                var topage = parseInt(this.pager.toPage);
                if (topage >= 1 && topage <= this.pager.pages) {
                    this.pager.curpage = topage;
                    this.getPagerData();
                    // if(this.fullScreen==0){
                    //     this.getPagerData();
                    // }else{
                    //     this.getPagerDataAtFullScreen();
                    // }
                }
            }
        },
        selCount(domId, size){
            this.clearSelectCodes();
            var $dom = $("#" + domId);//转jquery对象
            if ($dom.is('.sel-active')) {
                this.batchSel.size = 0;
                $dom.removeClass('sel-active')
            } else {
                $dom.addClass('sel-active')
                this.batchSel.size = size;
            }
            if (this.batchSel.lsId == "") {
                this.batchSel.lsId = this.lsId;
            }
            console.log("batchSel = ", this.batchSel);
            this.batchSelCode();
        },
        clearSelectCodes(){
            var _this = this;
            _this.oddsChange.codesInfoArr.forEach((item, idx)=> {
                var lsId = item.lsid;
                _this.settingCodesList.forEach((item1, idx1)=> {
                        if(item1.lsid == lsId){
                            var pgdata = item1.pageData;
                            for (var j = 0, len = pgdata.length; j < len; j++) {
                                if (item.code == pgdata[j].code) {
                                    _this.settingCodesList[idx1].pageData[j].selected = false;
                                }
                            }
                        }
                    })
                })
                if (this.showMode == 2) {
                    this.erdingBet.pageData.forEach((item, idx)=> {
                        item.selected = false;
                })
            }
            _this.oddsChange.codesInfoArr = [];
            _this.oddsChange.codesArr = [];
        } ,
        changeSelSettingId(item){
            console.log("changeSelSettingId=====", item.id);
            this.currSetting = item;
            this.batchSel.lsId = item.id;
            this.lsId = item.id;
            this.clearSelectCodes();
            this.batchSelCode();
            console.log("batchSel = ", this.batchSel);
            this.bpSettingList.forEach((bpLs,idx)=>{
                if(bpLs.id==item.id){
                    this.bpId = bpLs.bpSettingList[0].id;
                }
            })
            this.getBuyList();
        },
        batchSelCode(){
            var _this = this;
            if (this.batchSel.lsId != "" && this.batchSel.size > 0) {
                var _selLsId = this.batchSel.lsId;
                var _selSize = this.batchSel.size;
                var hasSelect = 0;
                this.settingCodesList.forEach((item, idx)=> {
                    if(item.lsid == _selLsId){
                        var pgdata = item.pageData;
                        if (_selSize > pgdata.length) {
                            _selSize = pgdata.length;
                        }
                        for (var j = 0; j < _selSize; j++) {
                            if (!_this.oddsChange.codesArr.includes(pgdata[j].code)) {
                                _this.settingCodesList[idx].pageData[j].ls
                                _this.settingCodesList[idx].pageData[j].selected = true;
                                _this.oddsChange.codesArr.push(pgdata[j].code);
                                _this.oddsChange.codesInfoArr.push(pgdata[j]);
                            }
                        }
                    }
                })
            } else {
                this.clearSelectCodes();
            }
        },
        selectAll(lsItem){
            lsItem.selected = !lsItem.selected;
            var that = this;
            that.bpSettingList.forEach((item, idx)=> {
                if(lsItem.id == item.id){
                    item.bpSettingList.forEach((it1, idx1)=> {
                        that.bpSettingList[idx].bpSettingList[idx1].selected = lsItem.selected;
                        var exist = that.__checkSelectExist(lsItem.id ,it1.id);
                        if(lsItem.selected){
                            if(exist==-1){
                                that.changeList.push({
                                    lotterySettingId:lsItem.id ,
                                    id:it1.id
                                })
                            }
                        }else{
                            if(exist!=-1){
                                that.changeList.splice(exist,1);
                            }
                        }
                    })
                }
            })
            console.log("========bpSettingList",that.bpSettingList);
            console.log("========selectAll",that.changeList);
        },
        selectOne(bpItem){
            var that = this;
            bpItem.selected = !bpItem.selected;
            console.log(bpItem,bpItem.selected);
            that.bpSettingList.forEach((item, idx)=> {
                if(bpItem.lotterySettingId == item.id){
                    item.bpSettingList.forEach((it1, idx1)=> {
                        if(bpItem.id==it1.id){
                            var exist = that.__checkSelectExist(bpItem.lotterySettingId,it1.id);
                            if(bpItem.selected){
                                if(exist==-1){
                                    that.changeList.push({
                                        lotterySettingId:bpItem.lotterySettingId,
                                        id:it1.id
                                    })
                                }
                            }else{
                                if(exist!=-1){
                                    that.changeList.splice(exist,1);
                                }
                            }
                        }

                    })
                }
            })
            console.log("========selectOne",that.changeList);
        },

        removeOneSelectCode(pageDateItem){//删除被选中的号码
            var _this = this;
            for (var j = 0, len = _this.oddsChange.codesInfoArr.length; j < len; j++) {
                var item = _this.oddsChange.codesInfoArr[j];
                if (pageDateItem.code == item.code) {
                    _this.oddsChange.codesInfoArr.splice(j, 1);
                    break;
                }
            }
        },

        // selectOne(pageDateItem){
        //     console.log("=============selectOne", pageDateItem)
        //     var _this = this;
        //     var _selLsId = pageDateItem.lsid;
        //     this.settingCodesList.forEach((item, idx)=> {
        //         if(item.lsid == _selLsId){
        //             var pgdata = item.pageData;
        //             for (var j = 0, len = pgdata.length; j < len; j++) {
        //                 if (pageDateItem.code == pgdata[j].code) {
        //                     var isSelect = _this.settingCodesList[idx].pageData[j].selected;
        //                     _this.settingCodesList[idx].pageData[j].selected = !isSelect;
        //                     if (!isSelect) {//被选中
        //                         if (!_this.oddsChange.codesArr.includes(pgdata[j].code)) {
        //                             _this.oddsChange.codesArr.push(pgdata[j].code);
        //                             _this.oddsChange.codesInfoArr.push(pgdata[j]);
        //                         }
        //                     } else {//取消选中
        //                         if (_this.oddsChange.codesArr.includes(pgdata[j].code)) {
        //                             var delIdx = _this.oddsChange.codesArr.indexOf(pgdata[j].code);
        //                             _this.oddsChange.codesArr.splice(delIdx, 1);
        //                             _this.removeOneSelectCode(pageDateItem);
        //                         }
        //                     }
        //                 }
        //             }
        //             if (_this.showMode == 2) {
        //                 _this.erdingBet.pageData = _this.settingCodesList[idx].pageData;
        //                 _this.erdingBet.pageData.sort(this.__sortByCode)
        //             }
        //         }
        //     })
        // },
        searchCode(){
            var _this = this;
            var isFind = false; //是否找到标识
            //号码搜索
            var dataList = this.settingCodesList;
            var dataLen = dataList.length;
            for (var i = 0; i < dataLen; i++) {
                var item = dataList[i];
                var dlist = item.codesList;
                for (var j = 0, len = dlist.length; j < len; j++) {
                    var item1 = dlist[j];
                    if (item1.code == _this.distCode) {
                        isFind = true;
                        var inPage = j % _this.pager.limit == 0 ? parseInt(j / _this.pager.limit) : parseInt(j / _this.pager.limit) + 1;
                        _this.pager.curpage = inPage;
                        _this.getPagerData();
                        $("td").each(function (idx) {
                            var $this = $(this);
                            var number = $this.attr("number");
                            if (number == _this.distCode) {
                                $this.addClass("selected")
                                setTimeout(function () {
                                    $this.removeClass("selected") //5秒后选中颜色消失
                                }, 5000)
                            }
                        });
                        break;
                    }
                }
                if (isFind) {
                    break;
                }
            }
            if (!isFind) {
                layer.msg("该号码不是当前类型")
            }
        },
        __sortByFuzhi(a1, a2){
            return a1.maxLoss - a2.maxLoss;
        },
        __sortByCode(a1, a2){
            return a1.code.localeCompare(a2.code);
        },
        sortBy(domObj,type){
            var that = this;
            this.dataSortType = type;
            this.settingCodesList.forEach((item,idx)=>{
                if(item.lsid == that.lsId){
                    if(type==0){
                        that.settingCodesList[idx].codesList.sort(that.__sortByCode);
                    }else{
                        that.settingCodesList[idx].codesList.sort(that.__sortByFuzhi);
                    }
                }
            })
            this.pager.curpage=1;
            this.getPagerData();

            //this.codesList = this.codesList.sortB
        }

    }

})
vm.initDataList();