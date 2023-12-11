//实盘页面脚本
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

const vm = new Vue({
    el: '#main1',
    data: {
        zzvalueMap: {},
        noBuyLossMap: {},
        lsAvgOddsMap:{},
        maxLoss:0,//最大负值
        fullScreen:0,
        lmId:4,
        lsId:"",
        buyList:[],
        settingCodesList:[],
        settingList:[],
        lsList:[],
        drawId: "",
        distCode: "", //搜索号码
        nickName: "",
        userId: "",
        roleName: "",
        pager:{
            total:0,
            pages:0,
            curpage:1,
            limit:100,
            toPage:""
        },
        pageData:[],
        lines:0,
        oddsChange:{
            oddsChangeWay:0,
            odds:"",
            itemLimit:"",
            itemChangeWay:0,
            codesArr:[],
            codesInfoArr:[],
            isShow:false,
            comboId:0,
            comboName:''
        },
        batchSel:{
            size:0,
            lsId:""
        },
        currLs:{},//当前选中的分类
        showMode:0,  //0，普通模式；1：号码转模式;2:二字定
        dataFrom:0, //号码来源,0:分类所有；1：赔率变动
        loadOver:0, //数据加载完成标识
        codeZhuanList:[],
        oddsChangeList:[], //赔率变动记录
        oddsChangeComboList:[],
        currSettingCodes: {
            pageData:[]
        },
        zhuanParam:{
            maxFuzhi:0,
            minFuzhi:0
        },
        erdingBet:{ //与二定位相关的操作
            dataSet:{},
            pageData:[],
            selectLsId:'',
            end_money:'',
            start_money:'',
            lines:0,
            lsList:[],
            dingLoc1:'',
            dingLoc2:'',
            verSS:[], //纵向多选集合
            horSS:[], //横向多选集合
            locArr:[]
        },
        stopCodesList:[],
        optRange:1,
        maxOptRange:10,
        stopCodeChangeList:[]
    },
    methods: {
        toPrint(){
          window.parent.open("huizong-print.html?lmId="+this.lmId);
        },
        changeDataFrom(v) {
            this.dataFrom = v;
            if (this.fullScreen == 0){
                if (v == 0) {
                    this.pager.curpage = 1;
                    this.getPagerData();
                } else {
                    this.currSettingCodes.pageData = [];
                    this.lines = 0;
                    this.getOddsChangeCombo();
                }
            }else{
                if (v == 0) {
                    this.changeFullScreen(1,null);
                } else {
                    this.currSettingCodes.pageData = [];
                    this.lines = 0;
                    this.getOddsChangeCombo();
                    // this.currSettingCodes.pageData = [];
                    // this.lines = 0;
                }
            }
        },
        changeFullScreen(v,__lsId){
            var _this = this;
            this.fullScreen=v;
            this.dataFrom = 0;
            if(null!=__lsId){
                this.lsId = __lsId;
            }
            if(v==0){
                this.getPagerData();
            }else{
                this.pager.curpage = 1;
                this.getPagerDataAtFullScreen();
            }
        },
        getPagerDataAtFullScreen(){ //全屏模式下的分页
            var _this = this;
            this.settingCodesList.forEach((item,idex)=>{
                if(item.lsid == _this.lsId){
                    _this.currSettingCodes = item;
                    var codeList = item.codesList;
                    _this.pager.total = codeList.length;
                    var st = (_this.pager.curpage - 1) * _this.pager.limit;
                    var et = (_this.pager.curpage) * _this.pager.limit;
                    if (et > codeList.length) {
                        et = codeList.length;
                    }
                    var pgData = codeList.slice(st, et);
                    _this.currSettingCodes.pageData = pgData;
                    _this.lines = pgData.length % 8 == 0 ? parseInt(pgData.length / 8) : parseInt(pgData.length / 8) + 1;
                    _this.pager.pages = _this.pager.total % _this.pager.limit == 0 ?
                        parseInt(_this.pager.total / _this.pager.limit)
                        : parseInt(_this.pager.total / _this.pager.limit) + 1;
                }
            })
        },
        initDataList() {
            const _this = this;
            layer.msg("数据加载中...", {time: -1, shade: 0.3, icon: 16})
            $.ajax({
                //url: HOST + 'admin/drawbuy/shipan/settings?lotteryType=1',
                url:HOST +'lotteryMethod/getAll?lotteryType=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        _this.settingList = res.data;
                        _this.loadOver = 1;
                        $("#tbody").removeClass('hide');
                        _this.settingList.forEach((item,idx)=>{
                            item.lotterySettingList.forEach((it,idx1)=>{
                                it.avgOdds = 0;
                            })
                        })
                        _this.getDatas();
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
        getDatas(){
            var that = this;
            that.getStopCodes();
            setTimeout(function () {
                that.getBuyList();
            },500)
        },
        getBuyList(){
            const _this = this;
            $.ajax({
                //url: HOST + 'admin/drawbuy/xupan/list',
                url: HOST + 'admin/shipan/listAll?lmId=',
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        if(res.data.dataList!=null){
                            _this.buyList = res.data.dataList;
                            //_this.zzvalueMap = res.data.zzvalueMap;
                            _this.noBuyLossMap = res.data.noBuyLossMap;
                            _this.lsAvgOddsMap =res.data.lsAvgOddsMap;
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
        getStopCodes(){
            const _this = this;
            $.ajax({
                url: HOST + 'admin/stopBuyCodes/listAll',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        _this.stopCodesList = res.data;
                    }
                },
                error: function (e) {}
            })
        },
        __isStopCode(code,lsid){
            var exist = false;
            this.stopCodesList.forEach((item,idx)=>{
                if(item.codes==code && item.lotterySettingId==lsid){
                    exist = true;
                }
            })
            return exist;
        },
        getBuyCodeInfo(code,isXian){
            var info = null;
            if(this.buyList.length>0){
                console.log("===================")
                for(var i=0,len=this.buyList.length;i<len;i++){
                    var item = this.buyList[i];
                    if(item.buyCode == code && isXian == item.isXian){
                        console.log("===================",item.buyCode)
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
        addOddsChange() {//添加赔率
            var _this = this;
            var odds = "";
            var limit = "";
            var oddsChangeWay = 0;
            if(this.optRange==1){

            }
            if (this.oddsChange.codesInfoArr.length < 1) {
                layer.msg("未选择号码");
                return;
            }
            if (this.oddsChange.oddsChangeWay == 2) {
                if (this.oddsChange.odds == "" || this.oddsChange.itemLimit == "") {
                    layer.msg("请输入金额和赔率");
                    return;
                }
                odds = this.oddsChange.itemLimit;
                limit = this.oddsChange.odds;
            } else {
                if (this.oddsChange.odds == "" && this.oddsChange.itemLimit == "") {
                    layer.msg("未输入任何内容");
                    return;
                }
                odds = this.oddsChange.odds;
                limit = this.oddsChange.itemLimit;
            }
            layer.msg("添加中...", {time: -1, icon: 16, shade: 0.3});
            var datas = [];
            this.oddsChange.codesInfoArr.forEach((item, idx) => {
                datas.push({
                    hotNumber: item.code,
                    xianFlag: item.xian,
                    changePeiRate: odds,
                    prChangeWay: _this.oddsChange.oddsChangeWay,
                    oneItemUpperLimit: limit,
                    limitChangeWay: _this.oddsChange.itemChangeWay,
                    lotterySettingId: item.lsid,
                    isShow:_this.oddsChange.isShow==false?0:1
                })
            });

            $.ajax({
                url: HOST + "peiRateChange/shipan",
                type: 'post',
                data: JSON.stringify(datas),
                contentType: 'application/json',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        if (_this.oddsChange.oddsChangeWay == 2) {
                            //到下载页
                            window.location.href = "../shipan/download.html"
                        } else {
                            layer.msg("添加成功")
                            _this.cancelOddsChange();
                            _this.pager.curpage = 1;
                            _this.getPagerData();
                            //getBuyList();
                        }
                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误");
                }
            })
        },
        cancelOddsChange() {
            this.oddsChange.oddsChangeWay = 0;
            this.oddsChange.itemChangeWay = 0;
            this.oddsChange.itemLimit = "";
            this.oddsChange.odds = "";
        },
        changeMethodClass(obj, _lmId, pageSize) {
            this.fullScreen = 0;
            this.dataFrom = 0;
            this.showMode = 0;
            this.pager.limit = pageSize;
            this.pager.curpage = 1;
            $(obj).addClass('yellow');
            $(obj).siblings().removeClass('yellow');
            this.lmId = _lmId;
            if(this.lmId==3){
                this.maxOptRange = 5;
            }else if(this.lmId==4){
                this.maxOptRange = 10;
            }else if(this.lmId==7){
                this.maxOptRange = 3;
            }
            this.batchSel.size = 0;
            this.createSettingCodes();
        },

        __updateCodesData(datalist,isXian,noBuyLoss,zzvalue,lsItem){ //从购买记录中找到号码的数据并回填
            var that = this;
            datalist.forEach((item, idx) => {
                var info = that.getBuyCodeInfo(item.code,isXian);
                /**
                 * maxLoss:0,chuhuo:0,odds:0,odds_avg:0,package_chuhuo:0,
                 */
                if(null!=info){
                    console.log("=================chuhuo",info);
                    item.maxLoss = info.maxLoss;
                    item.chuhuo = info.chuhuoMoney;
                    item.odds = info.peiRate;
                    item.odds_avg = info.avg_odds;
                    item.package_chuhuo = info.bpMoney
                }else{
                    item.maxLoss = noBuyLoss;
                    var pr = lsItem.peiRate;
                    if(pr.indexOf("/")>-1){ //多重赔率分割
                        var prArr = pr.split("/");
                        switch(item.code.length){
                            case 2:
                                if(checkRepeatCodes(item.code,2)){
                                    pr = prArr[1];
                                }else{
                                    pr = prArr[0];
                                }
                                break;
                            case 3:
                                if(checkRepeatCodes(item.code,3)){
                                    pr = prArr[2];
                                }else if(checkRepeatCodes(item.code,2)){
                                    pr = prArr[1];
                                }else{
                                    pr = prArr[0];
                                }
                                break;
                            case 4:
                                if(checkRepeatCodes(item.code,4)){
                                    pr = prArr[3];
                                }else if(checkRepeatCodes(item.code,3)){
                                    pr = prArr[2];
                                }else if(checkRepeatCodes(item.code,2)){
                                    pr = prArr[1];
                                }else{
                                    pr = prArr[0];
                                }
                                break;
                        }

                    }
                    item.odds = pr;
                }
                var checkStopCode = that.__isStopCode(item.code,lsItem.id)
                item.selected = checkStopCode;
                item.isStopBuy = checkStopCode?1:0;
            })
            datalist.sort(that.__sortByFuzhi) //按负值排序
            var codesInfo = {
                lsid: lsItem.id,
                lmid: lsItem.lotteryMethodId,
                lsName: lsItem.bettingRule,
                fuzhi: lsItem.fuzhi,
                zzvalue: datalist.length - zzvalue,
                odds_avg: lsItem.avgOdds,
                codesList: datalist, //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
                pageData: []
            };
            return codesInfo;
        },

        createSettingCodes() {
            var _this = this;
            var buyList = this.buyList;
            console.log("buyList.length",buyList.length);
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = this.settingList[i];
                var __lmId = item.id;
                var lmFuzhi = 0;
                var dlist = item.lotterySettingList;
                item.lotterySettingList.forEach((it1,idx1)=>{
                    var lsFuzhi = 0;
                    buyList.forEach((cc,ii)=>{
                        if(cc.lotterySettingId==it1.id){
                            lsFuzhi = lsFuzhi + cc.chuhuoMoney;
                            lmFuzhi = lmFuzhi + cc.chuhuoMoney;
                        }
                    })
                    //回填小分类的负值数据
                    _this.settingList[i].lotterySettingList[idx1].fuzhi = lsFuzhi;
                })
                $("#lm-fz-" + __lmId).html(lmFuzhi);
                if (item.id == this.lmId) {
                    _this.lsList = dlist;
                }
            }
            this.settingCodesList = [];
            this.lsList.forEach((item, idx) => {
                var _lsId = item.id;
                var codesAmount = 0;
                // var zzvalue = _this.zzvalueMap[_lsId];
                // console.log("zzvalue == ",zzvalue,"lsId=="+_lsId)
                // if(zzvalue!=null && zzvalue!=undefined){
                //     codesAmount=zzvalue;
                // }
                var noBuyLoss = _this.noBuyLossMap[_lsId];
                if(noBuyLoss==null || noBuyLoss==undefined){
                    noBuyLoss=0;
                }
                var lsAvgOdds = _this.lsAvgOddsMap[_lsId];
                if(lsAvgOdds==null || lsAvgOdds==undefined){
                    lsAvgOdds=0;
                }
                item.avgOdds = lsAvgOdds;
                var dlist = [];
                var codesInfo = null;
                switch (_this.lmId) {
                    case 1:
                        dlist = createCodesForDing(1, item.bettingRule, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,0,noBuyLoss,codesAmount,item);
                        break;
                    case 2:
                        dlist = createCodesForDing(2, item.bettingRule, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,0,noBuyLoss,codesAmount,item);
                        break;
                    case 3:
                        dlist = createCodesForDing(3, item.bettingRule, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,0,noBuyLoss,codesAmount,item);
                        break;
                    case 4:
                        dlist = createCodesForDing(4, item.bettingRule, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,0,noBuyLoss,codesAmount,item);
                        break;
                    case 5:
                        dlist = createCodesForXian(2, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,1,noBuyLoss,codesAmount,item);
                        break;
                    case 6:
                        dlist = createCodesForXian(3, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,1,noBuyLoss,codesAmount,item);
                        break;
                    case 7:
                        dlist = createCodesForXian(4, _lsId);
                        codesInfo = _this.__updateCodesData(dlist,1,noBuyLoss,codesAmount,item);
                        break;
                }
                codesInfo.codesList.sort(_this.__sortByFuzhi);
                var zzvalue = 0;
                codesInfo.codesList.forEach((item,idx)=>{
                    if(item.maxLoss>=0){
                        zzvalue++;
                    }
                })
                codesInfo.zzvalue = zzvalue;
                console.log("codesList = ",codesInfo.codesList);
                _this.settingCodesList.push(codesInfo);
            })
            _this.getPagerData();
        },
        initStopCodesChangeList(dataList){
            dataList.forEach((item,idx)=>{

            })
        },
        getPagerData() {
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
                this.settingCodesList.forEach((item, idx) => {
                    if (idx == 0) {
                        _this.lsId = item.lsid;
                    }
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
        toFirst() {//第一页
            console.log(("=======toFirst"))
            if (this.pager.curpage > 1) {
                this.pager.curpage = 1;
                if(this.fullScreen==0){
                    this.getPagerData();
                }else{
                    this.getPagerDataAtFullScreen();
                }

            }
        },
        toPre() {//上一页
            console.log(("=======toPre"))
            if (this.pager.curpage > 1) {
                this.pager.curpage--;
                if(this.fullScreen==0){
                    this.getPagerData();
                }else{
                    this.getPagerDataAtFullScreen();
                }
            }
        },
        toNext() {//下一页
            console.log(("=======toNext"))
            if (this.pager.curpage < this.pager.pages) {
                this.pager.curpage++;
                if(this.fullScreen==0){
                    this.getPagerData();
                }else{
                    this.getPagerDataAtFullScreen();
                }
            }
        },
        toLast() {//尾页
            console.log(("=======toLast"))
            if (this.pager.curpage < this.pager.pages) {
                this.pager.curpage = this.pager.pages;
                if(this.fullScreen==0){
                    this.getPagerData();
                }else{
                    this.getPagerDataAtFullScreen();
                }
            }
        },
        toPage() {//指定页
            if (this.pager.toPage != "") {
                var topage = parseInt(this.pager.toPage);
                if (topage >= 1 && topage <= this.pager.pages) {
                    this.pager.curpage = topage;
                    if(this.fullScreen==0){
                        this.getPagerData();
                    }else{
                        this.getPagerDataAtFullScreen();
                    }
                }
            }
        },
        selCount(domId,size) {
            this.clearSelectCodes();
            var $dom = $("#"+domId);//转jquery对象
            if($dom.is('.sel-active')){
                this.batchSel.size = 0;
                $dom.removeClass('sel-active')
            }else{
                $dom.addClass('sel-active')
                this.batchSel.size = size;
            }
            if (this.batchSel.lsId == "") {
                this.batchSel.lsId = this.lsId;
            }
            console.log("batchSel = ", this.batchSel);
            this.batchSelCode();
        },
        clearSelectCodes() {
            var _this = this;
            _this.oddsChange.codesInfoArr.forEach((item, idx) => {
                var lsId = item.lsid;
                _this.settingCodesList.forEach((item1, idx1) => {
                    if (item1.lsid == lsId) {
                        var pgdata = item1.pageData;
                        for (var j = 0, len = pgdata.length; j < len; j++) {
                            if (item.code == pgdata[j].code) {
                                _this.settingCodesList[idx1].pageData[j].selected = false;
                            }
                        }
                    }
                })
            })
            if(this.showMode==2){
                this.erdingBet.pageData.forEach((item,idx)=>{
                    item.selected = false;
                })
            }
            _this.oddsChange.codesInfoArr = [];
            _this.oddsChange.codesArr = [];
        },
        changeSelSettingId(_lsid) {
            console.log("changeSelSettingId=====", _lsid);
            this.batchSel.lsId = _lsid;
            this.clearSelectCodes();
            this.batchSelCode();
            console.log("batchSel = ", this.batchSel);
        },
        batchSelCode() {
            var _this = this;
            if (this.batchSel.lsId != "" && this.batchSel.size > 0) {
                var _selLsId = this.batchSel.lsId;
                var _selSize = this.batchSel.size;
                var hasSelect = 0;
                this.settingCodesList.forEach((item, idx) => {
                    if (item.lsid == _selLsId) {
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

        removeOneSelectCode(pageDateItem) {//删除被选中的号码
            var _this = this;
            for (var j = 0, len = _this.oddsChange.codesInfoArr.length; j < len; j++) {
                var item = _this.oddsChange.codesInfoArr[j];
                if (pageDateItem.code == item.code) {
                    _this.oddsChange.codesInfoArr.splice(j, 1);
                    break;
                }
            }
        },

        selectOne(pageDateItem) {
            console.log("=============selectOne", pageDateItem)
            var _this = this;
            var _selLsId = pageDateItem.lsid;
            this.settingCodesList.forEach((item, idx) => {
                if (item.lsid == _selLsId) {
                    var pgdata = item.pageData;
                    for (var j = 0, len = pgdata.length; j < len; j++) {
                        if (pageDateItem.code == pgdata[j].code) {
                            var isSelect = _this.settingCodesList[idx].pageData[j].selected;
                            _this.settingCodesList[idx].pageData[j].selected = !isSelect;
                            if (!isSelect) {//被选中
                                if (!_this.oddsChange.codesArr.includes(pgdata[j].code)) {
                                    _this.oddsChange.codesArr.push(pgdata[j].code);
                                    _this.oddsChange.codesInfoArr.push(pgdata[j]);
                                }
                            } else {//取消选中
                                if (_this.oddsChange.codesArr.includes(pgdata[j].code)) {
                                    var delIdx = _this.oddsChange.codesArr.indexOf(pgdata[j].code);
                                    _this.oddsChange.codesArr.splice(delIdx, 1);
                                    _this.removeOneSelectCode(pageDateItem);
                                }
                            }
                        }
                    }
                    if(_this.showMode==2){
                        _this.erdingBet.pageData = _this.settingCodesList[idx].pageData;
                        _this.erdingBet.pageData.sort(this.__sortByCode)
                    }
                }
            })
        },
        selectOneAtZhuan(codeItem){ //号码转模式下的单个号码选中
            console.log("=============selectOneAtZhuan", codeItem)
            var _this = this;
            var _selLsId = codeItem.lsid;
            this.codeZhuanList.forEach((item, idx) => {
                if (item.lsid == _selLsId) {
                    var dataList = item.codesList;
                    dataList.forEach((item1,idx1)=>{
                        if (codeItem.code == item1.code) {
                            var isSelect = _this.codeZhuanList[idx].codesList[idx1].selected;
                            _this.codeZhuanList[idx].codesList[idx1].selected = !isSelect;
                            if (!isSelect) {//被选中
                                if (!_this.oddsChange.codesArr.includes(item1.code)) {
                                    _this.oddsChange.codesArr.push(item1.code);
                                    _this.oddsChange.codesInfoArr.push(item1);
                                }
                            } else {//取消选中
                                if (_this.oddsChange.codesArr.includes(item1.code)) {
                                    var delIdx = _this.oddsChange.codesArr.indexOf(item1.code);
                                    _this.oddsChange.codesArr.splice(delIdx, 1);
                                    _this.removeOneSelectCode(codeItem);
                                }
                            }
                        }
                    })
                }
            })
        },
        selectAllAtZhuan(dataItem){//号码转模式下的全选
            var _this = this;
            var _selLsId = dataItem.lsid;
            var checked = $("#zhuan-ls-"+_selLsId).prop("checked");
            console.log("==============_thisObj.checked",checked);
            _this.codeZhuanList.forEach((item, idx) => {
                if (item.lsid == _selLsId) {
                    var dataList = item.codesList;
                    dataList.forEach((item1,idx1)=>{
                        if(checked) {
                            _this.codeZhuanList[idx].codesList[idx1].selected = true;
                            if (!_this.oddsChange.codesArr.includes(item1.code)) {
                                _this.oddsChange.codesArr.push(item1.code);
                                _this.oddsChange.codesInfoArr.push(item1);
                            }
                        }else{
                            _this.codeZhuanList[idx].codesList[idx1].selected = false;
                            if (_this.oddsChange.codesArr.includes(item1.code)) {
                                var delIdx = _this.oddsChange.codesArr.indexOf(item1.code);
                                _this.oddsChange.codesArr.splice(delIdx, 1);
                                _this.removeOneSelectCode(item1);
                            }
                        }
                    })
                }
            })
        },
        searchCode() {
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
        zhuanCode(pageDataItem) {
            this.codeZhuanList = [];
            this.showMode = 1;
            this.zhuanParam.maxFuzhi = 0;
            this.zhuanParam.minFuzhi = 0;
            var code = pageDataItem.code;
            var numLocs = [];
            var arr = code.split("");
            arr.forEach((item, idx) => {
                if (item != 'X') {
                    numLocs.push({
                        idx: idx,
                        nums: item
                    })
                }
            })
            console.log(" this.numLocs ==", numLocs);
            var _this = this;
            var lmList = ["1","2","3","4"];

            lmList.forEach((ele, index) => {
                var lmId = ele;
                var lsArr = [];
                for (var i = 0, len = _this.settingList.length; i < len; i++) {
                    var item = _this.settingList[i];
                    if (item.id == lmId) {
                        lsArr = item.lotterySettingList;
                        break;
                    }
                }
                lsArr.forEach((lsItem, idx) => {
                    var noBuyLoss = _this.noBuyLossMap[lsItem.id];
                    if(noBuyLoss==null || noBuyLoss==undefined){
                        noBuyLoss=0;
                    }
                    var lsAvgOdds = _this.lsAvgOddsMap[lsItem.id];
                    if(lsAvgOdds==null || lsAvgOdds==undefined){
                        lsAvgOdds=0;
                    }
                    lsItem.avgOdds = lsAvgOdds;
                    var dlist = createCodesForDing(lmId, lsItem.bettingRule, lsItem.id);
                    var codesInfo  = _this.__updateCodesData(dlist,0,noBuyLoss,0,lsItem);
                    _this.__findMatchCodes(codesInfo.codesList,numLocs,lsItem.bettingRule,lsItem.id);
                })
            })
        },
        __findMatchCodes(dlist, numLocs, lsName,_lsId) {
            var that = this;
            var matchCodesList = [];
            dlist.forEach((item1, idx1) => {
                var cc = item1.code;
                var ccArr = cc.split("");
                var findOk = true;
                for (var i = 0; i < numLocs.length; i++) {
                    if (numLocs[i].nums != ccArr[numLocs[i].idx]) {//对应位置的号码不匹配
                        findOk = false;
                        break;
                    }
                }
                if (findOk) {//找到了转换后匹配的号码
                    if(item1.maxLoss<that.zhuanParam.minFuzhi){
                        that.zhuanParam.minFuzhi = item1.maxLoss
                    }
                    if(item1.maxLoss>that.zhuanParam.maxFuzhi){
                        that.zhuanParam.maxFuzhi = item1.maxLoss
                    }
                    matchCodesList.push(item1)
                }
            })
            if (matchCodesList.length > 0) {

                var lines = matchCodesList.length%10==0?parseInt( matchCodesList.length/10):parseInt( matchCodesList.length/10)+1;
                this.codeZhuanList.push({
                    lsName: lsName,
                    lsid:_lsId,
                    downPr:"",
                    itemLimit:"",
                    codesList: matchCodesList,
                    lines:lines
                })
            }
        },
        subOddsChangeAtZhuan(dataItem){//提交
            var _this = this;
            var _selLsId = dataItem.lsid;
            var datas = [];
            _this.oddsChange.codesInfoArr.forEach((item, idx) => {
                if(item.lsid == _selLsId){
                    _this.codeZhuanList.forEach((item1, idx1) => {
                        if(item.lsid == item1.lsid){
                            datas.push({
                                hotNumber: item.code,
                                xianFlag: item.xian,
                                changePeiRate: item1.downPr,
                                prChangeWay: 0,
                                oneItemUpperLimit: item1.itemLimit,
                                limitChangeWay: 1,
                                lotterySettingId: item.lsid
                            })
                        }
                    });
                }
            });
            if(datas.length<1){
                layer.msg("未选择任何号码");
                return;
            }
            layer.msg("添加中...", {time: -1, icon: 16, shade: 0.3});
            $.ajax({
                url: HOST + "peiRateChange/shipan",
                type: 'post',
                data: JSON.stringify(datas),
                contentType: 'application/json',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        //清除所有的号码选中状态
                        _this.__clearAllSelectColorAtZhuan()
                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误");
                }
            })
        },
        __clearAllSelectColorAtZhuan(){
            this.codeZhuanList.forEach((item,idx)=>{
                item.itemLimit = "";
                item.downPr="";
                item.codesList.forEach((item1,idx1)=>{
                    item1.selected = false;
                })
            });
        },
        getOddsChangeCombo(){
            var that = this;
            var params = "lotterySettingId="+this.lsId
            //赔率变动套餐
            $.ajax({
                url: HOST + "peiRateChange/comboList?"+params,
                type: 'get',
                success: function (res) {
                    if (res.code == 200) {
                        that.oddsChangeComboList = res.data;
                    }else{
                        that.oddsChangeComboList = [];
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误");
                }
            })
            this.getOddsChangeList();
        },
        getOddsChangeList(){ //获取赔率变动号码
            //layer.msg("",{time:-1,icon:16,shade:0.5});
            var _this = this;
            var comboId = "";
            _this.currSettingCodes.pageData=[];
            _this.oddsChange.comboName = "";
            if(this.oddsChange.comboId!=0){
                comboId = this.oddsChange.comboId;
                this.oddsChangeComboList.forEach((dd,idx)=>{
                    if(dd.id == comboId){
                        _this.oddsChange.comboName = dd.comboName
                    }
                })
            }
            var params = 'page='+this.pager.curpage+"&limit="+this.pager.limit+"&lotteryMethodId="+this.lmId+"&lotterySettingId="+this.lsId+"&comboId="+comboId;
            $.ajax({
                url: HOST + "peiRateChange/getPeiRateChangeByPage?"+params,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        _this.pager.total = res.count;
                        var resData = res.data.records;
                        //购买记录回填
                        if(resData.length>0){
                            resData.forEach((item,idx)=>{
                                var codeInfo = _this.getOneCodeInfo(item.hotNumber,item.lotterySettingId);
                                if(null!=codeInfo){
                                    _this.currSettingCodes.pageData.push(codeInfo);
                                }
                            })
                        }
                        var cellLen = 10;
                        var pgData = _this.currSettingCodes.pageData;
                        if(this.lsId<5){
                            cellLen = 8;
                        }
                        if(pgData.length>0){
                            _this.lines = pgData.length % cellLen == 0 ? parseInt(pgData.length / cellLen) : parseInt(pgData.length / cellLen) + 1;
                        }
                        _this.pager.pages = _this.pager.total % _this.pager.limit == 0 ?
                            parseInt(_this.pager.total / _this.pager.limit)
                            : parseInt(_this.pager.total / _this.pager.limit) + 1;
                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg("网络错误");
                }
            })
        },
        getOneCodeInfo(code,__lsId){
            var codeInfo = null;
            this.settingCodesList.forEach((item,idx)=>{
                if(item.lsid == __lsId){
                    item.codesList.forEach((item1,idx1)=>{
                        if(item1.code == code){
                            codeInfo = item1;
                        }
                    })
                }
            })
            return codeInfo;
        },
        __sortByFuzhi(a1,a2){
            return a1.maxLoss - a2.maxLoss;
        },
        __sortByCode(a1,a2){
            return a1.code>a2.code;
        },
        toErdingRealbet(__lsId){//切换到二字定模式
            $("input[type='button']").removeClass('active');
            this.clearSelectCodes();
            this.showMode=2;
            this.lsId = __lsId;
            var _this = this;
            this.settingList.forEach((item,idx)=>{
                if(item.id==2){
                    _this.lsList = item.lotterySettingList;
                }
            })
            this.initErdingData();
        },
        initErdingData(){//初始化二字定选中类别的数据
            var _this = this;
            _this.lsList.forEach((item,idx)=>{
                if(item.id==_this.lsId){
                    _this.currLs = item;
                }
            })
            this.settingCodesList.forEach((item,idx)=>{
                item.codesList = item.codesList.sort(_this.__sortByCode);
                if(item.lsid==_this.lsId){
                    _this.erdingBet.dataSet = item;
                    _this.erdingBet.pageData = item.codesList;
                }
            })
            var dataLen = _this.erdingBet.pageData.length;
            _this.erdingBet.lines = dataLen%10==0?parseInt(dataLen/10):parseInt(dataLen/10)+1;
            this.initCanshu()
        },
        changeOddsAtErding(){
            this.oddsChange.itemChangeWay=1;//上限值采用覆盖方式
            this.addOddsChange();//提交数据
        },
        changeCodesColor(){//号码变色
            var that = this;
            var startMoney = this.erdingBet.start_money;
            var endMoney = this.erdingBet.end_money;
            this.clearSelectCodes();
            if(startMoney!="" || endMoney!=""){
                this.erdingBet.pageData.forEach((item,idx)=>{
                    var isSatify1 = true; //是否满足变色条件
                    var isSatify2 = true; //是否满足变色条件
                    if(startMoney!=""){
                        if(item.chuhuo>=parseFloat(startMoney)){
                            isSatify1 = true;
                        }else{
                            isSatify1 = false;
                        }
                    }
                    if(endMoney!=""){
                        if(item.chuhuo<=parseFloat(endMoney)){
                            isSatify2 = true;
                        }else{
                            isSatify2 = false;
                        }
                    }
                    if(isSatify1 && isSatify2){
                       item.selected = true;
                    }
                })
            }
        },
        changeLsTypeAtErding(__lsId){//切换二字定各类别
            this.lsId = __lsId;
            this.initErdingData();
            this.clearSelectCodes();

        },
        initCanshu(){
            var locArr = ['仟','佰','拾','个'];
            var firstLocIndex = this.erdingBet.dataSet.lsName.indexOf("口");
            var lastLocIndex = this.erdingBet.dataSet.lsName.lastIndexOf("口");
            this.erdingBet.dingLoc1 = locArr[firstLocIndex];
            this.erdingBet.dingLoc2=locArr[lastLocIndex];
            //$("#loc1").html(locArr[firstLocIndex]);
            //$("#loc2").html(locArr[lastLocIndex]);
        },

        removeSelectCodeAtErding(code) {//删除被选中的号码
            var _this = this;
            for (var j = 0, len = _this.oddsChange.codesInfoArr.length; j < len; j++) {
                var item = _this.oddsChange.codesInfoArr[j];
                if (code == item.code) {
                    _this.oddsChange.codesInfoArr.splice(j, 1);
                    break;
                }
            }
            this.erdingBet.pageData.forEach((item,idx)=>{
                if(item.code == code){
                    _this.erdingBet.pageData[idx].selected = false;
                    exist = true;
                }
            })
        },
        addSelectCodeAtErding(code){
            var _this = this;
            var exist = false;
            var codeItem = null;
            for (var j = 0, len = _this.oddsChange.codesInfoArr.length; j < len; j++) {
                var item = _this.oddsChange.codesInfoArr[j];
                if (code == item.code) {
                    exist = true;
                    break;
                }
            }
            if(!exist){
                this.erdingBet.pageData.forEach((item,idx)=>{
                    if(item.code == code){
                        _this.erdingBet.pageData[idx].selected = true;
                        _this.oddsChange.codesInfoArr.push(item);
                    }
                })
            }
            console.log("===============",JSON.stringify(_this.oddsChange.codesInfoArr))
        },
        verticalSelect(domobj,endNum){//纵向多选
            var _this = this;
            var $this = $(domobj);
            $this.is(".active") ? $this.removeClass("active") : $this.addClass("active");
            var idx = endNum;
            $("td.options").each(function (it1) {
                var j = $(this).attr("_j");
                var i = $(this).attr("_i");
                var no = i+""+j;
                console.log("=========j="+j);
                if(j == idx){
                    var betNo = $(this).attr("bet_no");
                    if($(this).is(".active")){
                        if(_this.erdingBet.verSS.includes(no)){
                            _this.erdingBet.verSS.splice(_this.erdingBet.verSS.indexOf(no), 1)
                            if(!_this.erdingBet.horSS.includes(no)) {
                                $(this).removeClass("active");
                                _this.removeSelectCodeAtErding(betNo)
                            }
                        }
                    }else{
                        if (!_this.erdingBet.verSS.includes(no)) {
                            _this.erdingBet.verSS.push(no);
                            $(this).addClass("active")
                            _this.addSelectCodeAtErding(betNo);
                        }
                    }
                }
            })
        },
        hovorSelect(domobj,startNum){
            var _this = this;
            var $this = $(domobj);
            $this.is(".active") ? $this.removeClass("active") : $this.addClass("active");
            var idx = startNum;
            $("td.options").each(function (it1) {
                var j = $(this).attr("_j");
                var i = $(this).attr("_i");
                console.log("=========i="+i);
                var no = i+""+j;
                if(i == idx){
                    var betNo = $(this).attr("bet_no");
                    if($(this).is(".active")){
                        if(_this.erdingBet.horSS.includes(no)){
                            _this.erdingBet.horSS.splice(_this.erdingBet.horSS.indexOf(no), 1)
                            if(!_this.erdingBet.verSS.includes(no)){
                                $(this).removeClass("active")
                                _this.removeSelectCodeAtErding(betNo)
                            }
                        }
                    }else{
                        if (!_this.erdingBet.horSS.includes(no)) {
                            _this.erdingBet.horSS.push(no);
                            $(this).addClass("active")
                            _this.addSelectCodeAtErding(betNo);
                        }
                    }
                }
            })
        },

        ezdJ(domId,typeId,subId) {//定位置及合分操作事件
            var _this = this;
            console.log("====domId="+domId);
            var $domEle = $("#"+domId); //jquery对象
            $domEle.is(".active") ? $domEle.removeClass("active") : $domEle.addClass("active");
            var ct = [44, 55];
            var n = $domEle.attr("data");
            for(i=0;i<=9;i++) {
                for (j = 0; j <= 9; j++) {
                    var chCodeA = 0;
                    var no = i + "" + j;
                    var $ctt = $("#ct" + subId);
                    $ctt.is(".active") && (44 == subId && eval(i + j) % 2 == 1 ? chCodeA = 1 : 55 == subId && eval(i + j) % 2 != 1 && (chCodeA = 1))
                    var qCarChkA = 0,
                        qCarChkB = 0;
                    if (0 == chCodeA) {
                        $("#fixLoc1").find("input").each(function () {
                            if ($(this).is(".active")) {
                                var n = $(this).attr("data");
                                console.log("====================n1="+n);
                                n <= 9 && i == n ? qCarChkA = 1 : 10 == n && i % 2 == 1 ? qCarChkA = 1 : 11 == n && i % 2 == 0 ?
                                    qCarChkA = 1 : 12 == n && 5 <= i ? qCarChkA = 1 : 13 == n && i <= 4 && (qCarChkA = 1)
                            }
                        })
                        $("#fixLoc2").find("input").each(function () {
                            if ($(this).is(".active")) {
                                var n = $(this).attr("data");
                                console.log("====================n2="+n);
                                n <= 9 && j == n ? qCarChkB = 1 : 10 == n && j % 2 == 1 ? qCarChkB = 1 : 11 == n && j % 2 == 0 ? qCarChkB = 1 : 12 == n && 5 <= j ? qCarChkB = 1 : 13 == n && j <= 4 && (qCarChkB = 1)
                            }
                        })
                    }

                    if (1 == qCarChkA && 1 == qCarChkB) {
                        chCodeA = 1
                    }
                    if (0 == chCodeA) {
                        $("#erziding_hf0").find("input").each(function () {
                            if ($(this).is(".active")) {
                                var n = $(this).attr("data");
                                eval(i + j) % 10 == n && (chCodeA = 1)
                            }
                        });
                        $("#erziding_hf1").find("input").each(function () {
                            if ($(this).is(".active")) {
                                var n = $(this).attr("data");
                                eval(i + j) % 10 == n && (chCodeA = 1)
                            }
                        })
                    }
                    var $obj = $("#e" + no);
                    var code = $obj.attr("bet_no");

                    if (1 == chCodeA) {
                        console.log("==========================no="+no);
                        if (!_this.erdingBet.verSS.includes(no)) {
                            _this.erdingBet.verSS.push(no);
                            $obj.addClass("active")
                            _this.addSelectCodeAtErding(code)
                        }
                    } else {
                        //console.log("==========================no="+no);
                        if (_this.erdingBet.verSS.includes(no)) {
                            $obj.removeClass("active")
                            _this.erdingBet.verSS.splice(_this.erdingBet.verSS.indexOf(no), 1);
                            _this.removeSelectCodeAtErding(code)
                        }
                    }
                }
            }
        }

},


})
vm.initDataList();