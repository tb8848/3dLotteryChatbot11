const messages = {
    zh: {
        all:"全选",
        group:"组",
        zongtou:"总投"
    },
    th: {
        all:"เลือกทั้งหมด",
        group:"กลุ่ม",
        zongtou:"การลงทุนทั้งหมด"
    }
}

//包牌实盘页面脚本
var $ = layui.jquery;
var layer = layui.layer;

var moduleName = "shipan";
var defaultLang = layui.data('langTab').langType;
const i18n = new VueI18n({
    locale: defaultLang, // set locale
    messages, // set locale messages
});
initLangConfig({
    defaultLang:defaultLang,
    filePath: "../js/i18n/"+moduleName+"/",
    module:moduleName,
    base:"../js/"
})
var i18np = null;
function changeLang(lang) {
    defaultLang = lang;
    reloadI18n({
        defaultLang:lang,
        filePath: "../js/i18n/"+moduleName+"/",
        module:moduleName,
    })
    i18np.loadProperties(moduleName);
    i18n.locale=lang;
}

layui.config({base: '../js/'})
    // 继承treetable.js插件
    .extend({i18np: 'i18n'}).use([ 'i18np','jquery'], function () {
    i18np = layui.i18np;
    // changeLang(defaultLang);
    reloadI18n({
        defaultLang:defaultLang,
        filePath: "../js/i18n/"+moduleName+"/",
        module:moduleName,
    })
    i18np.loadProperties(moduleName);

    vm.initDataList();
});
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
const worker = new Worker("siding-worker.js?v2");
function popChild(tag){
    layer.open({
        type:2,
        // title:'出货',
        title:i18np.prop("shipan.menu.chuhuo"),
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
    i18n,
    data: {
        fillOk:false,
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
        zongtouList:[],
        lsParamMap:{},
        currSettingFuzhi:0,
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
        },
        changeSelBpGroupId(it1){
            var that = this;
            this.bpId = it1.id;
            this.lsId = it1.lotterySettingId;
            this.settingList.forEach((lmItem,idx)=>{
                if(lmItem.id == that.lmId){
                    lmItem.lotterySettingList.forEach((lsItem,lsIdx)=>{
                        if(lsItem.id == it1.lotterySettingId){
                            that.currSetting = lsItem;
                        }
                    })
                    //that.currSetting = lmItem.lotterySettingList[0];
                    //that.lsId = that.currSetting.id;
                }
            })
            this.pager.curpage=1;
            this.getBuyList();
        },
        changeMethodType(item) {//切换大分类
            var that = this;
            this.lmId = item.id;
            that.bpId = "";
            that.lsId = "";
            that.pager.curpage = 1;
            that.dataSortType = 0;
            that.fillOk = false;
            this.lines = 0;
            this.settingList.forEach((lmItem,idx)=>{
                if(lmItem.id == item.id){
                    that.currSetting = lmItem.lotterySettingList[0];
                    that.lsId = that.currSetting.id;
                }
            })
            this.bpSettingList=[];
            this.getBpSettingList();
        },
        getMethodList() {//大类数据
            const _this = this;
            layer.msg(i18np.prop("shipan.msg.data.loading"), {time: -1, shade: 0.3, icon: 16})
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
                    layer.msg(i18np.prop("shipan.msg.request.error"));
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
                }
                if(that.currSetting.id == lsId){
                    that.currSetting.fuzhi = fuzhi;
                }
            })
        },
        upBpSettingLanHuo(lmId,lsId,bpId,bpLanMoney){//更新拦货
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
                    layer.msg(i18np.prop("shipan.msg.request.error"));
                }
            })
            //bpshipan/settings
        },
        addBpOddsChange () { //添加
            var that=this;
            console.log("===========that.changeList",that.changeList);

            if(that.changeList.length<1){
                layer.msg(i18np.prop("shipan.msg.no.selected.type"));
                return;
            }
            if (this.oddsChange.odds == "" && this.oddsChange.itemLimit == "") {
                layer.msg(i18np.prop("shipan.msg.no.fill.anything"));
                return;
            }
            var odds = $("#set_odds").val();
            var itemLimit = $("#odds_item_limit").val();
            if(odds=="" && itemLimit==""){
                layer.msg(i18np.prop("shipan.msg.no.fill.anything"));
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
                layer.msg(i18np.prop("shipan.msg.data.saved"),{time:-1,shade:0.3,icon:16});
                $.ajax({
                    url:HOST+"bpSetting/change/saveChangeFromBpShiPan",
                    type:'post',
                    contentType:'application/json',
                    data:JSON.stringify(that.changeList),
                    success:function(res){
                        layer.closeAll();
                        if(res.code==200){
                            layer.msg("修改成功")
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
                            item.selected = false;
                            if(that.lsId == "" && idx == 0){
                                that.lsId = item.id
                            }
                            item.bpSettingList.forEach((it1, idx1)=> {
                                it1.selected = false;
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
                    layer.msg(i18np.prop("shipan.msg.request.error"));
                }
            })
        },
        getBuyList() {//获取购买记录
            const _this = this;
            $.ajax({
                //url: HOST + 'admin/drawbuy/bplist?bpId=' + _this.bpId,
                url: HOST + 'admin/shipan/bpListAll?bpId=' + _this.bpId+"&lsId="+_this.lsId,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        if(res.data.dataList!=null){
                            _this.bpBuyList = res.data.dataList;
                            _this.noBuyMaxLoss = res.data.noBuyLossMap;
                            _this.lsAvgOddsMap = res.data.lsAvgOddsMap;
                            _this.lsParamMap = res.data.lsParamMap;
                        }else{
                            _this.bpBuyList=[];
                            _this.noBuyMaxLoss={};
                            _this.lsAvgOddsMap={};
                            _this.lsParamMap={};
                            _this.currSettingFuzhi=0;
                        }
                        if(_this.lmId==4){
                            _this.createSettingCodes4();
                        }else{
                            _this.createSettingCodes();
                        }

                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg(i18np.prop("shipan.msg.request.error"));
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
                    item.chuhuo = info.buyMoney; //此字段未使用
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
        __updateCodesData4(datalist,isXian,lsItem) { //从购买记录中找到号码的数据并回填
            var that = this;
            var noBuyLoss = 0;
            var lsParam = this.lsParamMap[lsItem.id];
            if(lsParam!=null && lsParam!=undefined){
                noBuyLoss = lsParam.noBuyLoss;
            }
            datalist.forEach((item, idx)=> {

                var info = that.getBuyCodeInfo(item.code, isXian);
                /**
                 * maxLoss:0,chuhuo:0,odds:0,odds_avg:0,package_chuhuo:0,
                 */
                if (null != info) {
                    item.maxLoss = info.maxLoss;
                    item.chuhuo = info.buyMoney; //此字段未使用
                    item.odds = info.peiRate;
                    item.odds_avg = info.avg_odds;
                    item.package_chuhuo = info.bpMoney
                } else {
                    item.maxLoss = noBuyLoss;
                    item.odds = lsItem.peiRate;
                }
            })
            datalist.sort(that.__sortByCode) //按负值排序
            return datalist;
        },
        createSettingCodes(){
            var _this = this;
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = _this.settingList[i];
                var dlist = item.lotterySettingList;
                if (item.id == _this.lmId) {
                    _this.lsList = dlist;
                }
            }
            _this.settingCodesList = [];
            _this.lsList.forEach((item, idx)=> {
                var _lsId = item.id;
                var codesAmount = 0;
                var lsParam = _this.lsParamMap[_lsId];
                var lsAvgOdds = 0;
                var noBuyLoss = 0;
                var fuzhi = 0;
                if(lsParam!=null && lsParam!=undefined){
                    lsAvgOdds = lsParam.avgOdds;
                    noBuyLoss = lsParam.noBuyLoss;
                    fuzhi = lsParam.fuzhi;
                }

                item.avgOdds = lsAvgOdds;
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
                    _this.currSettingFuzhi = fuzhi;
                }
                _this.settingCodesList.push(codesInfo)
            });
            _this.getPagerData();
        },

        createSettingCodes4(){
            console.log("=====================siding");
            var _this = this;
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = _this.settingList[i];
                var dlist = item.lotterySettingList;
                if (item.id == _this.lmId) {
                    _this.lsList = dlist;
                }
            }
            _this.settingCodesList = [];
            _this.lsList.forEach((item, idx)=> {
                var _lsId = item.id;
                var lsAvgOdds = 0;
                var noBuyLoss = 0;
                var fuzhi = 0;
                var lsParam = _this.lsParamMap[_lsId];
                if(lsParam!=null && lsParam!=undefined){
                    lsAvgOdds = lsParam.avgOdds;
                    noBuyLoss = lsParam.noBuyLoss;
                    fuzhi = lsParam.fuzhi;
                }
                var fzvalue = 0;
                //从购买记录中统计损失为负数的数量
                _this.bpBuyList.forEach((bc,ii)=>{
                    if(bc.maxLoss<0){
                        fzvalue++;
                    }
                })
                var zzvalue = accSub(10000,fzvalue);
                item.avgOdds = lsAvgOdds;
                var dlist = createCodesForDing(_this.lmId, item.bettingRule, _lsId);
                var codesInfo = {
                    lsid: item.id,
                    lsName: item.bettingRule,
                    fuzhi: item.fuzhi,
                    zzvalue: zzvalue,
                    odds_avg: item.avgOdds,
                    codesList: dlist, //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
                    pageData: []
                };
                //var codesInfo = _this.__updateCodesData(dlist, 0, noBuyLoss, codesAmount, item);
                if (_this.currSetting.id == _lsId) {
                    _this.currSetting.zzvalue = zzvalue;
                    _this.currSetting.avgOdds = lsAvgOdds;
                    _this.currSettingFuzhi = fuzhi;
                }
                _this.settingCodesList.push(codesInfo)
                _this.getPagerData4();

            });
            _this.fillParams4();
        },

        fillParams4(){//创建worker方式回填数据
            const that = this;
            var codeList = this.settingCodesList[0].codesList;
            var noBuyLoss = 0;
            var lsParam = this.lsParamMap[that.currSetting.id];
            if(lsParam!=null && lsParam!=undefined){
                noBuyLoss = lsParam.noBuyLoss;
            }
            var params = {
                noBuyLoss:noBuyLoss,
                lsItem:that.currSetting,
                bpBuyList:that.bpBuyList,
                codesList: codeList
            }
            worker.postMessage(JSON.stringify(params));
            worker.onmessage = (e)=>{
                var clist = JSON.parse(e.data);
                that.settingCodesList[0].codesList = clist;
                that.fillOk = true;
            }
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
                this.lines = Math.ceil(pgData.length / 10);
                this.pager.pages = Math.ceil(this.pager.total / this.pager.limit);
            } else {
                _this.pager.total = 0;
                var clsTotal = 0; //每个分类的总数量
                this.batchSel.lsId = "";
                this.settingCodesList.forEach((item, idx)=> {
                    if(_this.currSetting.id == item.lsid){
                        var codeList = item.codesList;
                        _this.pager.total = codeList.length;
                        clsTotal = codeList.length;
                        var st = (_this.pager.curpage - 1) * _this.pager.limit;
                        var et = (_this.pager.curpage) * _this.pager.limit;
                        if (et > codeList.length) {
                            et = codeList.length;
                        }
                        var pgData = codeList.slice(st, et);
                        _this.settingCodesList[idx].pageData = pgData;
                        _this.lines = Math.ceil(pgData.length / 10);
                        _this.pager.pages = Math.ceil(this.pager.total / this.pager.limit);
                    }
                })

            }
        },

        getPagerData4(){
            var _this = this;
            if (this.settingCodesList.length == 1) {
                var codeList = this.settingCodesList[0].codesList;
                this.pager.total = codeList.length;
                var st = (this.pager.curpage - 1) * this.pager.limit;
                var et = (this.pager.curpage) * this.pager.limit;
                console.log(st, et);
                if (et > codeList.length) {
                    et = codeList.length;
                }
                var pgData = codeList.slice(st, et);
                if(!_this.fillOk){
                    pgData = this.__updateCodesData4(pgData,0,_this.currSetting)
                }

                this.settingCodesList[0].pageData = pgData;
                this.batchSel.lsId = this.settingCodesList[0].lsid;
                this.lines = Math.ceil(pgData.length / 10);
                this.pager.pages = Math.ceil(this.pager.total / this.pager.limit);
            }
        },
        toFirst(){//第一页
            if (this.pager.curpage > 1) {
                this.pager.curpage = 1;
                if(this.lmId==4){
                    this.getPagerData4()
                }else {
                    this.getPagerData();
                }
            }
        },
        toPrev() {//上一页
            if (this.pager.curpage > 1) {
                this.pager.curpage--;
                if(this.lmId==4){
                    this.getPagerData4()
                }else {
                    this.getPagerData();
                }
            }
        }
        ,
        toNext() {//下一页
            if (this.pager.curpage < this.pager.pages) {
                this.pager.curpage++;
                if(this.lmId==4){
                    this.getPagerData4()
                }else {
                    this.getPagerData();
                }
            }
        }
        ,
        toLast(){//尾页
            if (this.pager.curpage < this.pager.pages) {
                this.pager.curpage = this.pager.pages;
                if(this.lmId==4){
                    this.getPagerData4()
                }else {
                    this.getPagerData();
                }
            }
        }
        ,
        toPage(){//指定页
            if (this.pager.toPage != "") {
                var topage = parseInt(this.pager.toPage);
                if (topage >= 1 && topage <= this.pager.pages) {
                    this.pager.curpage = topage;
                    if(this.lmId==4){
                        this.getPagerData4()
                    }else {
                        this.getPagerData();
                    }
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
        changeSelSettingId(item){//切换小分类
            console.log("changeSelSettingId=====", item.id);
            this.currSetting = item;
            this.batchSel.lsId = item.id;
            this.lsId = item.id;
            this.clearSelectCodes();
            this.batchSelCode();
            console.log("batchSel = ", this.batchSel);
            this.bpId="";
            // this.bpSettingList.forEach((bpLs,idx)=>{
            //     if(bpLs.id==item.id){
            //         this.bpId = bpLs.bpSettingList[0].id;
            //     }
            // })
            this.pager.curpage=1;
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
        },
        selectOne(bpItem){
            var that = this;
            that.bpSettingList.forEach((item, idx)=> {
                if(bpItem.lotterySettingId == item.id){
                    var counts = item.bpSettingList.length;
                    item.bpSettingList.forEach((it1, idx1)=> {
                        if(bpItem.id==it1.id){
                            it1.selected = !it1.selected;
                            var exist = that.__checkSelectExist(bpItem.lotterySettingId,it1.id);
                            if(it1.selected){
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
                    var hasSelected = 0;
                    that.changeList.forEach((cc,ii)=>{
                        if(cc.lotterySettingId==bpItem.lotterySettingId){
                            hasSelected++;
                        }
                    })
                    if(hasSelected==counts){
                        item.selected = true;
                    }else{
                        item.selected = false;
                    }
                }
            })

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
                layer.msg(i18np.prop("shipan.msg.number.not.current.type"));
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
// vm.initDataList();