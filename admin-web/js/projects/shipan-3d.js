const messages = {
    zh: {
        ranking:"负值排行",
        batches:"[A分批]",
        number:"正值个数",
        oddsChange:"[赔率变动]",
        averageOdds:"平均赔率",
        returnShipan:"[返回实盘]",
        thali:"套餐",
        all:"全部",
        quanping:"全屏",
        zhuan:"转",
        baopai:"包牌",
        ljzdfz:"累计最大负值",
        renovate:"刷新",
        selectAll:"全选",
        eachDrop:"各降",
        topLimit:"上限",
        submit:"提交",
        firmOffer:"实盘",
        type:"类别",
        negative:"负值",
        moneyLess:"金额小于",
        moneyGreate:"金额大于",
        changeColor:"号码变色",
        haoma:"号码",
        odds:"赔率",
        fixedPosition:"定位置",
        hefen:"合分",
        dan:"单",
        shuang:"双",
        da:"大",
        xiao:"小",
        fixedOdds:"定盘赔率",
        noShow:"前台不显示",
        sheng:"各升",
        jiang:"各降",
        shangxian:"上限",
        add:"添加",
        cancel:"取消",
        qian:'仟',
        bai:'佰',
        shi:'拾',
        ge:'个',
        position:"位置",
        zjkp:"总监控盘",
        shihuoDetail:"实货明细",
        winDetail:"中奖明细",
        numberSearch:"号码搜索",
        operationStopNumber:"操作停押号码",
        thisPage:"本页",
        front:"前",
        page:"页",
    },
    th: {
        ranking:"การจัดอันดับค่าลบ",
        batches:"[A ส่วน]",
        number:"จำนวนค่าบวก",
        oddsChange:"[การเปลี่ยนแปลงอัตราต่อรอง]",
        averageOdds:"อัตราต่อรองเฉลี่ย",
        returnShipan:"[กลับไปยังตลาดจริง]",
        thali:"แพคเกจ",
        all:"ทั้งหมด",
        quanping:"เต็มหน้าจอ",
        zhuan:"เปลี่ยน",
        baopai:"ขายแพ็คเกจ",
        ljzdfz:"ค่าลบสูงสุดรวม",
        renovate:"รีเฟรช",
        selectAll:"เลือกทั้งหมด",
        eachDrop:"ลง",
        topLimit:"ขีดจำกัดสูงสุด",
        submit:"ส่ง",
        firmOffer:"ตลาดจริง",
        type:"ประเภท",
        negative:"ค่าลบ",
        moneyLess:"จำนวนเงินน้อยกว่า",
        moneyGreate:"จำนวนเงินมากกว่า",
        changeColor:"เปลี่ยนสีหมายเลข",
        haoma:"หมายเลข",
        odds:"อัตราต่อรอง",
        fixedPosition:"ตำแหน่งแน่นอน",
        hefen:"รวม",
        dan:"เดี่ยว",
        shuang:"คู่",
        da:"ใหญ่",
        xiao:"เล็ก",
        fixedOdds:"อัตราต่อรองในตำแหน่งแน่นอน",
        noShow:"ไม่แสดงบนหน้าตู้",
        sheng:"ขึ้น",
        jiang:"ลง",
        shangxian:"ขีดจำกัดสูงสุด",
        add:"เพิ่ม",
        cancel:"ยกเลิก",
        qian:'พัน',
        bai:'ร้อย',
        shi:'สิบ',
        ge:'หลักเดียว',
        position:"ตำแหน่ง",
        zjkp:"ตำแหน่งผู้ควบคุมทั้งหมด",
        shihuoDetail:"รายละเอียดสินค้าจริง",
        winDetail:"รายละเอียดการชนะของสินค้าจริง",
        numberSearch:"ค้นหาหมายเลข",
        operationStopNumber:"หมายเลขหยุดการเดิมพัน",
        thisPage:"หน้านี้",
        front:"หน้า",
        page:"หน้า",
    }
}

//实盘页面脚本
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
const worker = new Worker("siding-shipan-worker.js?t="+Math.random());

const vm = new Vue({
    el: '#main1',
    i18n,
    data: {
        colspans:1,
        lmSettingList:[],
        fillOk:false, //标识号码的参数是否回填完成
        sidingZzvalue:10000,
        sidingAvgOdds:'0',
        sidingFuzhi:0,
        sidingEarn:0,
        startLoading:false,
        isFind:false,
        zzvalueMap: {},
        noBuyLossMap: {},
        lsAvgOddsMap:{},
        maxLoss:0,//最大负值
        fullScreen:1,
        lmId:2,
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
        stopCodeChangeList:[],
        lmOddsChangeList:[],
        lsParamMap:{}
    },
    methods: {
        changeStopCodeRange(v){
            this.optRange=v;
            this.findStopBuyCodes();
        },
        findStopBuyCodes(){
            this.stopCodeChangeList = [];
            var _this = this;
            if (this.settingCodesList.length == 1) {
                var codeList = this.settingCodesList[0].codesList;
                var st = (this.pager.curpage - 1) * this.pager.limit;
                var et = (this.pager.curpage) * this.pager.limit;
                console.log(st, et);
                if (et > codeList.length) {
                    et = codeList.length;
                }
                var pgData = codeList.slice(st, et);
                var filterStopBuyCodeList = [];
                if(_this.optRange==1){
                    filterStopBuyCodeList = pgData;
                }else{
                    var st = 0;
                    var et = _this.optRange * this.pager.limit;
                    filterStopBuyCodeList=codeList.slice(st, et);
                }
                if(filterStopBuyCodeList.length>0){
                    filterStopBuyCodeList.forEach((cc,ii)=>{
                        if(cc.isStopBuy && cc.selected){
                            //本页停押号码
                            _this.stopCodeChangeList.push(cc);
                            _this.oddsChange.codesArr.push(cc.code);
                        }
                    })
                }
            } else {
                this.settingCodesList.forEach((item, idx) => {
                    var codeList = item.codesList;
                    var st = (_this.pager.curpage - 1) * _this.pager.limit;
                    var et = (_this.pager.curpage) * _this.pager.limit;
                    if (et > codeList.length) {
                        et = codeList.length;
                    }
                    var pgData = codeList.slice(st, et);
                    var filterStopBuyCodeList = [];
                    if(_this.optRange==1){
                        filterStopBuyCodeList = pgData;
                    }else{
                        var st = 0;
                        var et = _this.optRange * this.pager.limit;
                        filterStopBuyCodeList=codeList.slice(st, et);
                    }
                    if(filterStopBuyCodeList.length>0){
                        filterStopBuyCodeList.forEach((cc,ii)=>{
                            if(cc.isStopBuy && cc.selected){
                                _this.stopCodeChangeList.push(cc);
                                _this.oddsChange.codesArr.push(cc.code);
                            }
                        })
                    }
                })
            }
        },
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
            this.lsList.forEach((ls,idx)=>{
                if(ls.id==_this.lsId){
                    _this.currLs = ls;
                }
            })
            if(v==0){
                this.getPagerData();
            }else{
                this.pager.curpage = 1;
                this.getPagerDataAtFullScreen();
            }

        },
        getPagerDataAtFullScreen(){ //全屏模式下的分页
            var _this = this;
            var tdCells = this.settingCodesList.length==1?10:8;
            this.settingCodesList.forEach((item,idex)=>{
                if(item.lsid == _this.lsId){
                    let findIdx = -1;
                    _this.currSettingCodes = item;
                    var codeList = item.codesList;
                    _this.pager.total = codeList.length;
                    var st = (_this.pager.curpage - 1) * _this.pager.limit;
                    var et = (_this.pager.curpage) * _this.pager.limit;
                    if (et > codeList.length) {
                        et = codeList.length;
                    }
                    var pgData = codeList.slice(st, et);
                    pgData.forEach((dd,idx)=>{
                        if(dd.code==_this.distCode){
                            findIdx=idx;
                            dd.isSearchCode=true;
                        }
                    })
                    _this.currSettingCodes.pageData = pgData;
                    _this.stopCodeChangeList=[]
                    pgData.forEach((cc,ii)=>{
                        if(cc.isStopBuy==1){
                            _this.stopCodeChangeList.push(cc);
                            _this.oddsChange.codesArr.push(cc.code);
                            _this.oddsChange.codesInfoArr.push(cc);
                        }
                    });
                    _this.lines = Math.ceil(pgData.length / tdCells);
                    _this.pager.pages = Math.ceil(_this.pager.total / _this.pager.limit);
                    if(_this.isFind){
                        setTimeout(function () {
                            if(findIdx>=0){
                                _this.currSettingCodes.pageData[findIdx].isSearchCode=false;
                                _this.isFind = false;
                            }
                        }, 5000)
                    }
                }
            })
        },
        initDataList() {
            this.startLoading = true;
            const _this = this;
            $.ajax({
                url:HOST +'admin/shipan/3d/lmList?lotteryType=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        _this.settingList = res.data;
                        _this.lmSettingList = res.data;
                        _this.loadOver = 1;
                        $("#tbody").removeClass('hide');
                        _this.getDatas();
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
        getDatas(){
            var that = this;
            that.getBuyList();
        },
        getBuyList4(){
            const _this = this;
            $.ajax({
                url: HOST + 'admin/shipan/listAll4?lmId='+_this.lmId+"&page="+_this.pager.curpage+"&limit="+_this.pager.limit,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    _this.startLoading = false;
                    if (res.code == 0) {
                        if(res.data.dataList!=null){
                            _this.buyList = res.data.dataList;
                            _this.sidingZzvalue = res.data.zzvalue;
                            _this.sidingFuzhi = res.data.totalFuzhi;
                            _this.sidingAvgOdds = res.data.avgOdds;
                            _this.sidingEarn = res.data.noBuyLoss;
                            _this.stopCodesList = res.data.stopBuyCodesList;
                            _this.buyList.forEach((item,idx)=>{
                                item.selected = false;
                                item.hasDraw=item.isDraw==1?true:false;
                            })
                        }
                        _this.createSettingCodes4();
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
        getBuyList(){
            const _this = this;
            $.ajax({
                url: HOST + 'admin/shipan/3d/listAll?lmId='+_this.lmId,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    _this.startLoading = false;
                    if (res.code == 0) {
                        if(res.data.dataList!=null){
                            _this.buyList = res.data.dataList;
                            _this.lsAvgOddsMap =res.data.lsAvgOddsMap;
                            _this.lmOddsChangeList = res.data.prChangeList;
                            _this.lsParamMap = res.data.lsParamMap;
                            _this.stopCodesList = res.data.stopBuyCodesList;
                            _this.buyList.forEach((item,idx)=>{
                                item.selected = false;
                                item.hasDraw=item.isDraw==1?true:false;
                            })
                        }
                        _this.createSettingCodes();
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
        getBuyCodeInfo(code,lsid){
            var info = null;
            if(this.buyList.length>0){
                for(var i=0,len=this.buyList.length;i<len;i++){
                    var item = this.buyList[i];
                    if(item.buyCode == code && lsid == item.lotterySettingId){
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
        //添加赔率
        addOddsChange() {
            var _this = this;
            var odds = "";
            var limit = "";
            if(this.stopCodeChangeList.length>0){//有停押号码
                //操作本页
                if (this.oddsChange.codesInfoArr.length < 1) {
                    this.oddsChange.codesInfoArr = this.stopCodeChangeList;
                }else{
                    //去除重复的数据
                    var newArr = [];
                    _this.stopCodeChangeList.forEach((d1,i1)=>{
                        var exist = false;
                        _this.oddsChange.codesInfoArr.forEach((cc,idx)=>{
                            if(cc.code==d1.code && cc.lsid==d1.lsid){
                                exist = true;
                            }
                        })
                        if(!exist){
                            newArr.push(d1);
                        }
                    })

                    newArr.forEach((cc,idx)=>{
                        _this.oddsChange.codesInfoArr.push(cc)
                    })
                }
            }
            if (this.oddsChange.codesInfoArr.length < 1) {
                // layer.msg("未选择号码");
                layer.msg(i18np.prop("shipan.msg.no.selected.number"));
                return;
            }
            if (this.oddsChange.oddsChangeWay == 2) {
                //出货
                if (this.oddsChange.odds == "" || this.oddsChange.itemLimit == "") {
                    // layer.msg("请输入金额和赔率");
                    layer.msg(i18np.prop("shipan.msg.input.money.odds"));
                    return;
                }
                var lsOdds = currLs.peiRate;
                if(lsOdds.indexOf("/")>-1 && this.oddsChange.odds.indexOf("/")<0){
                    layer.msg("出货赔率:"+this.oddsChange.odds+"格式不正确，请用斜杠(/)分隔");
                    return;
                }
                if(lsOdds.indexOf("/")<0 && this.oddsChange.odds.indexOf("/")>-1){
                    layer.msg("出货赔率:"+this.oddsChange.odds+"格式不正确");
                    return;
                }
                if(lsOdds.indexOf("/")>-1 && this.oddsChange.odds.indexOf("/")>-1){
                    let len1 = lsOdds.indexOf("/").length;
                    let len2 = this.oddsChange.odds.indexOf("/").length;
                    if(len1 != len2){
                        layer.msg("出货赔率:"+this.oddsChange.odds+"格式不正确");
                        return;
                    }
                }
                odds = this.oddsChange.itemLimit;
                limit = this.oddsChange.odds;
            } else {
                if (this.oddsChange.odds == "" && this.oddsChange.itemLimit == "") {
                    // layer.msg("未输入任何内容");
                    layer.msg(i18np.prop("shipan.msg.no.fill.anything"));
                    return;
                }
                odds = this.oddsChange.odds;
                limit = this.oddsChange.itemLimit;
            }
            layer.msg(i18np.prop("shipan.msg.adding"), {time: -1, icon: 16, shade: 0.3});
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
                    isShow:1,
                    lotteryMethodId:_this.lmId
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
                            //window.location.href = "../shipan/download.html"
                            _this.initDataList();
                        } else {
                            var newOdds = res.data;
                            // layer.msg("添加成功")
                            layer.msg(i18np.prop("shipan.msg.add.success"));
                            _this.cancelOddsChange();
                            _this.pager.curpage = 1;
                            setTimeout(function () {
                                if(_this.lmId==2){
                                    if(newOdds.length>0){
                                        _this.upOddsOfCodes4(newOdds)
                                    }else{
                                        //_this.getBuyList4();
                                        _this.getBuyList();
                                    }
                                }else{
                                    _this.getBuyList();
                                }
                            },500);
                        }
                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    // layer.msg("网络错误");
                    layer.msg(i18np.prop("shipan.msg.request.error"));
                }
            })
        },

        //更新页面号码的最新赔率
        upOddsOfCodes4(newOdds){
            var pageData = this.currSettingCodes.pageData
            var pageData1 = this.settingCodesList[0].pageData
            newOdds.forEach((odds,idx)=>{
                pageData.forEach((cc,idx1)=>{
                    if(odds.hotNumber == cc.code){
                        cc.odds = odds.changePeiRate
                    }
                })
                pageData1.forEach((cc,idx1)=>{
                    if(odds.hotNumber == cc.code){
                        cc.odds = odds.changePeiRate
                    }
                })
            })
            this.currSettingCodes.pageData = pageData;
            this.settingCodesList[0].pageData = pageData1;
        },
        cancelOddsChange() {
            this.oddsChange.oddsChangeWay = 0;
            this.oddsChange.itemChangeWay = 0;
            this.oddsChange.itemLimit = "";
            this.oddsChange.odds = "";
            this.clearSelectCodes();
            // if(this.lmId==4){
            //     this.clearSelectCodes4();
            // }else{
            //     this.clearSelectCodes();
            // }
        },
        changeMethodClass(obj, _lmId, pageSize) {
            this.fullScreen = 1;
            this.dataFrom = 0;
            this.showMode = 0;
            this.pager.limit = pageSize;
            this.pager.curpage = 1;
            // $(obj).addClass('yellow');
            // $(obj).siblings().removeClass('yellow');
            this.lmId = parseInt(_lmId);
            this.lsId = '';
            console.log("=================lmId==",this.lmId);
            if(this.lmId==3){
                this.maxOptRange = 5;
            }else if(this.lmId==4){
                this.maxOptRange = 10;
            }else if(this.lmId==7){
                this.maxOptRange = 3;
            }
            this.batchSel.size = 0;
            this.startLoading = true;
            this.getBuyList();
        },

        getCodeChangeOdds(code,lsid){
            var info = null;
            if(this.lmOddsChangeList.length>0){
                for(var i=0,len=this.lmOddsChangeList.length;i<len;i++){
                    var item = this.lmOddsChangeList[i];
                    if(item.hotNumber == code && lsid == item.lotterySettingId){
                        return item;
                    }
                }
            }
            return info;
        },

        __updateCodesData(datalist,isXian,noBuyLoss,zzvalue,lsItem){
            //从购买记录中找到号码的数据并回填
            //号码的赔率取变动赔率或定盘上限赔率
            var that = this;
            datalist.forEach((item, idx) => {
                var pr = lsItem.peiRate;
                var info = that.getBuyCodeInfo(item.code,item.lsid);
                var codeOddsChange = that.getCodeChangeOdds(item.code,item.lsid);
                if(null!=info){
                    item.maxLoss = info.maxLoss;
                    item.chuhuo = info.chuhuoMoney;
                    item.odds = info.peiRate;
                    item.odds_avg = info.avg_odds;
                    item.package_chuhuo = info.bpMoney
                    item.hasDraw = info.isDraw==1?true:false;
                }else{
                    item.maxLoss = noBuyLoss;
                    if(null!=codeOddsChange){
                        item.odds = codeOddsChange.changePeiRate;
                    }else{
                        item.odds = pr;
                    }
                    item.hasDraw = false;
                }
                var checkStopCode = that.__isStopCode(item.code,lsItem.id)
                item.selected = checkStopCode;
                item.isStopBuy = checkStopCode?1:0;
                item.isSearchCode = false;
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
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = this.settingList[i];
                var dlist = item.lotterySettingList;
                item.lotterySettingList.forEach((it1,idx1)=>{
                    var lsFuzhi = 0;
                    var lsParam = _this.lsParamMap[it1.id];
                    if(lsParam!=null && lsParam!=undefined) {
                        lsFuzhi = lsFuzhi + lsParam.fuzhi;
                    }
                    //回填小分类的负值数据
                    _this.settingList[i].lotterySettingList[idx1].fuzhi = lsFuzhi;
                })
                //$("#lm-fz-" + __lmId).html(lmFuzhi);
                if (item.id == this.lmId) {
                    _this.lsList = dlist;
                    break;
                }
            }
            if(_this.lsList.length==2){
                _this.colspans = 8;
            }else if(_this.lsList.length==3){
                _this.colspans = 6;
            }else if(_this.lsList.length>4){
                _this.colspans = 2;
            }else{
                _this.colspans = 1;
            }
            this.settingCodesList = [];
            this.lsList.forEach((ls, idx) => {
                var _lsId = ls.id;
                var codesAmount = 0;
                var lsParam = _this.lsParamMap[_lsId];
                var lsAvgOdds = 0;
                var noBuyLoss = 0;
                if(lsParam!=null && lsParam!=undefined){
                    lsAvgOdds = lsParam.avgOdds;
                    noBuyLoss = lsParam.noBuyLoss;
                }
                ls.avgOdds = lsAvgOdds;
                var dlist = [];
                var codesInfo = null;
                if(_this.lsId==""){
                    _this.lsId = _lsId;
                }
                if(_this.lsId==_lsId){
                    _this.currLs = ls;
                }

                switch(_this.lmId){
                    case 1: //直选
                    case 2: //通选
                        dlist = ding3Code(_lsId);
                        break;
                    case 3: //组三
                        dlist = z3Code(_lsId);
                        break;
                    case 4: //组六
                        dlist = z6Code(_lsId);
                        break;
                    case 5: //和数
                        var brList = ls.bettingRule.split("/");
                        dlist = initCodeInfo(brList,_lsId);
                        break;
                    case 9:
                        var clist = ["大","小"];
                        dlist = initCodeInfo(clist,_lsId);
                        break;
                    case 10:
                        var clist = ["奇","偶"];
                        dlist = initCodeInfo(clist,_lsId);
                        break;
                    case 11:
                        var clist = ["三同号"];
                        dlist = initCodeInfo(clist,_lsId);
                        break;
                    case 12:
                        var clist = [ls.bettingRule];
                        dlist = initCodeInfo(clist,_lsId);
                        break;
                    case 6: //1D
                        if(ls.typeId==1){//1定位
                            dlist = ding1Code(_lsId);
                        }else{
                            //1字现
                            var numList = [];
                            for(var i=0;i<=9;i++){
                                numList.push(i+"");
                            }
                            dlist = initCodeInfo(numList,_lsId);
                        }
                        break;
                    case 7: //2D
                        if(ls.typeId==1){//2定位
                            dlist = ding2Code(_lsId);
                        }else{
                            //2字现
                            dlist=c2dCode(_lsId);
                        }
                        break;
                    case 8: //包选
                        if(ls.typeId==1){
                            dlist = b3Code(_lsId);
                        }else{
                            //2字现
                            dlist=b6Code(_lsId);
                        }
                        break;
                }
                codesInfo = _this.__updateCodesData(dlist,0,noBuyLoss,codesAmount,ls);
                codesInfo.codesList.sort(_this.__sortByFuzhi);
                var zzvalue = 0;
                codesInfo.codesList.forEach((item,idx)=>{
                    if(item.maxLoss>=0){
                        zzvalue++;
                    }
                })
                codesInfo.zzvalue = zzvalue;
                _this.settingCodesList.push(codesInfo);
            })
            _this.getPagerDataAtFullScreen();
        },
        createSettingCodes4() {
            var _this = this;
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = _this.settingList[i];
                if(item.id == this.lmId){
                    var dlist = item.lotterySettingList;
                    _this.lsList = dlist;
                    break;
                }
            }
            _this.settingCodesList = [];
            _this.lsList.forEach((item, idx)=> {
                var _lsId = item.id;
                item.avgOdds = _this.sidingAvgOdds;
                var dlist = createCodesForDing(_this.lmId, item.bettingRule, _lsId);
                var codesInfo = {
                    lsid: item.id,
                    lsName: item.bettingRule,
                    fuzhi: _this.sidingFuzhi,
                    zzvalue: _this.sidingZzvalue,
                    odds_avg: _this.sidingAvgOdds,
                    codesList: dlist, //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
                    pageData: _this.buyList
                };
                _this.settingCodesList.push(codesInfo)
                _this.getPagerData4();
            })
            //_this.fillParams4();
        },

        fillParams4(){//创建worker方式回填数据
            const that = this;
            var codeList = this.settingCodesList[0].codesList;
            var noBuyLoss = 0;
            var lsParam = this.lsParamMap[that.lsList[0].id];
            if(lsParam!=null && lsParam!=undefined){
                noBuyLoss = lsParam.noBuyLoss;
            }
            var params = {
                noBuyLoss:noBuyLoss,
                lsItem:that.lsList[0],
                bpBuyList:that.buyList,
                codesList: codeList,
                stopBuyList:that.stopCodesList,
                lmOddsChangeList:that.lmOddsChangeList
            }
            worker.postMessage(JSON.stringify(params));
            worker.onmessage = (e)=>{
                var clist = JSON.parse(e.data);
                clist.sort(that.__sortByFuzhi);
                that.settingCodesList[0].codesList = clist;
                that.getPagerData();
                that.startLoading = false;
                //that.fillOk = true;
            }
        },

        getPagerData4(){
            var _this = this;
            _this.stopCodeChangeList = [];
            var findIdx = -1;
            if (this.settingCodesList.length == 1) {
                //var codeList = this.settingCodesList[0].codesList;
                this.pager.total = 10000;
                var pgData =this.settingCodesList[0].pageData;
                pgData.forEach((dd,idx)=>{
                    dd.lsid = dd.lotterySettingId;
                    if(dd.maxLoss==0){
                        dd.maxLoss = _this.sidingEarn;
                    }
                    var isStop = _this.__isStopCode(dd.code,dd.lotterySettingId)
                    dd.selected = isStop;
                    dd.isStopBuy = isStop?1:0;
                    dd.hasDraw = dd.isDraw==1?true:false;
                    if(dd.code==_this.distCode){
                        findIdx=idx;
                        dd.isSearchCode=true;
                    }

                })
                this.settingCodesList[0].pageData = pgData;
                this.batchSel.lsId = this.settingCodesList[0].lsid;
                this.lines = Math.ceil(pgData.length/10);
                this.pager.pages = Math.ceil(this.pager.total / this.pager.limit);
                if(_this.isFind){
                    setTimeout(function () {
                        if(findIdx>=0){
                            _this.settingCodesList[0].pageData[findIdx].isSearchCode=false;
                            _this.isFind = false;
                        }
                    }, 5000)
                }
                var filterStopBuyCodeList = [];
                if(_this.optRange==1){
                    filterStopBuyCodeList = pgData;
                }else{
                    var st = 0;
                    var et = _this.optRange * this.pager.limit;
                    filterStopBuyCodeList=codeList.slice(st, et);
                }
                if(filterStopBuyCodeList.length>0){
                    filterStopBuyCodeList.forEach((cc,ii)=>{
                        if(cc.isStopBuy && cc.selected){
                            //本页停押号码
                            _this.stopCodeChangeList.push(cc);
                            _this.oddsChange.codesArr.push(cc.code);
                        }
                    })
                }
            }
        },

        __updateCodesData4(datalist,isXian,lsItem) { //从购买记录中找到号码的数据并回填
            var that = this;
            var noBuyLoss = 0;
            var lsParam = this.lsParamMap[lsItem.id];
            if(lsParam!=null && lsParam!=undefined){
                noBuyLoss = lsParam.noBuyLoss;
            }
            datalist.forEach((item, idx) => {
                var pr = lsItem.peiRate;
                if(pr.indexOf("/")>-1){ //多重赔率分割
                    var prArr = pr.split("/");
                    var isRepeat = false;
                    for(var i=item.code.length;i>=2;i--){
                        isRepeat = checkRepeatCodes(item.code,i)
                        if(isRepeat){
                            pr = prArr[i-1];
                            break;
                        }
                    }
                    if(!isRepeat){
                        pr = prArr[0];
                    }
                }
                var info = that.getBuyCodeInfo(item.code,isXian);
                var codeOddsChange = that.getCodeChangeOdds(item.code,isXian);
                /**
                 * maxLoss:0,chuhuo:0,odds:0,odds_avg:0,package_chuhuo:0,
                 */
                if(null!=info){
                    item.maxLoss = info.maxLoss;
                    item.chuhuo = info.chuhuoMoney;
                    if(null!=codeOddsChange){
                        item.odds = codeOddsChange.changePeiRate;
                    }else{
                        item.odds = info.peiRate;
                    }
                    item.odds_avg = info.avg_odds;
                    item.package_chuhuo = info.bpMoney
                }else{
                    item.maxLoss = noBuyLoss;
                    if(null!=codeOddsChange){
                        item.odds = codeOddsChange.changePeiRate;
                    }else{
                        item.odds = pr;
                    }
                }
                var checkStopCode = that.__isStopCode(item.code,lsItem.id)
                item.selected = checkStopCode;
                item.isStopBuy = checkStopCode?1:0;
            })
            datalist.sort(that.__sortByCode) //按负值排序
            return datalist;
        },

        initStopCodesChangeList(dataList){
            dataList.forEach((item,idx)=>{

            })
        },

        getPagerData() {
            var _this = this;
            _this.stopCodeChangeList = [];
            if (this.settingCodesList.length == 1) {
                var findIdx = -1;
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
                pgData.forEach((dd,idx)=>{
                    if(dd.code==_this.distCode){
                        findIdx=idx;
                        dd.isSearchCode=true;
                    }
                })
                this.settingCodesList[0].pageData = pgData;
                this.batchSel.lsId = this.settingCodesList[0].lsid;
                this.lines = Math.ceil(pgData.length / 10);
                this.pager.pages = Math.ceil(this.pager.total / this.pager.limit) ;
                if(_this.isFind){
                    setTimeout(function () {
                        if(findIdx>=0){
                            _this.settingCodesList[0].pageData[findIdx].isSearchCode=false;
                            _this.isFind = false;
                        }
                    }, 5000)
                }
                var filterStopBuyCodeList = [];
                if(_this.optRange==1){
                    filterStopBuyCodeList = pgData;
                }else{
                    var st = 0;
                    var et = _this.optRange * this.pager.limit;
                    filterStopBuyCodeList=codeList.slice(st, et);
                }
                if(filterStopBuyCodeList.length>0){
                    filterStopBuyCodeList.forEach((cc,ii)=>{
                        if(cc.isStopBuy && cc.selected){
                            //本页停押号码
                            _this.stopCodeChangeList.push(cc);
                            _this.oddsChange.codesArr.push(cc.code);
                        }
                    })
                }
                //console.log("============停押1==",_this.stopCodeChangeList)
            } else {
                _this.pager.total = 0;
                var clsTotal = 0; //每个分类的总数量
                this.batchSel.lsId = "";
                var pageIdx = -1;
                var findIdx=-1;
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
                    pgData.forEach((dd,idx1)=>{
                        if(dd.code==_this.distCode){
                            pgData[idx1].isSearchCode=true;
                            findIdx=idx1;
                            pageIdx = idx;
                        }
                    })
                    _this.settingCodesList[idx].pageData = pgData;
                    _this.lines = pgData.length % 10 == 0 ? parseInt(pgData.length / 10) : parseInt(pgData.length / 10) + 1;

                    var filterStopBuyCodeList = [];
                    if(_this.optRange==1){
                        filterStopBuyCodeList = pgData;
                    }else{
                        var st = 0;
                        var et = _this.optRange * this.pager.limit;
                        filterStopBuyCodeList=codeList.slice(st, et);
                    }
                    if(filterStopBuyCodeList.length>0){
                        filterStopBuyCodeList.forEach((cc,ii)=>{
                            if(cc.isStopBuy && cc.selected){
                                _this.stopCodeChangeList.push(cc);
                                _this.oddsChange.codesArr.push(cc.code);
                            }
                        })
                    }

                    console.log("============停押2==",_this.stopCodeChangeList)

                })
                this.pager.pages = clsTotal % this.pager.limit == 0 ?
                    parseInt(clsTotal / this.pager.limit)
                    : parseInt(clsTotal / this.pager.limit) + 1;
                if(_this.isFind){
                    setTimeout(function () {
                        if(pageIdx>=0 && findIdx>=0){
                            _this.settingCodesList[pageIdx].pageData[findIdx].isSearchCode=false;
                            _this.isFind = false;
                        }
                    }, 5000)
                }
            }
        },
        toFirst() {//第一页
            console.log(("=======toFirst"))
            if (this.pager.curpage > 1) {
                this.pager.curpage = 1;
                if(this.fullScreen==0){
                    if(this.lmId==4){
                       this.getBuyList4();
                    }else{
                        this.getPagerData();
                    }
                    //this.getPagerData();
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
                    if(this.lmId==4){
                        this.getBuyList4();
                    }else{
                        this.getPagerData();
                    }
                    //this.getPagerData();
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
                    if(this.lmId==4){
                        this.getBuyList4();
                    }else{
                        this.getPagerData();
                    }
                    //this.getPagerData();
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
                    if(this.lmId==4){
                        this.getBuyList4();
                    }else{
                        this.getPagerData();
                    }
                    //this.getPagerData();
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
                        if(this.lmId==4){
                            this.getBuyList4();
                        }else{
                            this.getPagerData();
                        }
                        //this.getPagerData();
                    }else{
                        this.getPagerDataAtFullScreen();
                    }
                }
            }
        },
        selCount(domId,size) {
            this.clearSelectCodes();
            //this.clearSelectCodes();
            // if(this.lmId==4){
            //     this.clearSelectCodes4();
            // }else{
            //     this.clearSelectCodes();
            // }
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
        clearSelectCodes4() {
            var _this = this;
            //console.log("===============",_this.oddsChange.codesInfoArr);
            _this.oddsChange.codesInfoArr.forEach((item, idx) => {
                var lsId = item.lotterySettingId;
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
            _this.oddsChange.codesInfoArr = [];
            _this.oddsChange.codesArr = [];
        },
        changeSelSettingId(_lsid) {
            this.batchSel.lsId = _lsid;
            this.clearSelectCodes();
            this.batchSelCode();
        },
        batchSelCode() {
            var _this = this;
            if (this.batchSel.lsId != "" && this.batchSel.size > 0) {
                var _selLsId = this.batchSel.lsId;
                var _selSize = this.batchSel.size;
                this.settingCodesList.forEach((item, idx) => {
                    if (item.lsid == _selLsId) {
                        var pgdata = item.pageData;
                        if (_selSize > pgdata.length) {
                            _selSize = pgdata.length;
                        }
                        for (var j = 0; j < _selSize; j++) {
                            if (!_this.oddsChange.codesArr.includes(pgdata[j].code)) {
                                //_this.settingCodesList[idx].pageData[j].ls
                                _this.settingCodesList[idx].pageData[j].selected = true;
                                _this.oddsChange.codesArr.push(pgdata[j].code);
                                _this.oddsChange.codesInfoArr.push(pgdata[j]);
                            }
                        }
                    }
                })
            } else {
                // if(this.lmId==4){
                //     this.clearSelectCodes4();
                // }else{
                //     this.clearSelectCodes();
                // }
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

            for (var j = 0, len = _this.stopCodeChangeList.length; j < len; j++) {
                var item = _this.stopCodeChangeList[j];
                if (pageDateItem.code == item.code) {
                    _this.stopCodeChangeList.splice(j, 1);
                    break;
                }
            }

            console.log("============停押3==",_this.stopCodeChangeList)
        },

        selectOne(pageDateItem) {
            var _this = this;
            var _selLsId = pageDateItem.lsid;
            // if(_this.lmId==4){
            //     _selLsId = pageDateItem.lotterySettingId;
            // }
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
            // if(this.lmId==4) {
            //     this.pager.curpage = 1;
            //     this.searchCode4();
            //     return;
            // }
            var _this = this;
            var isFind = false; //是否找到标识
            //号码搜索
            var dataList = this.settingCodesList;
            var dataLen = dataList.length;
            for (var i = 0; i < dataLen; i++) {
                var item = dataList[i];
                if(item.lsid == _this.lsId){
                    var dlist = item.codesList;
                    for (var j = 0, len = dlist.length; j < len; j++) {
                        var item1 = dlist[j];
                        if (item1.code == _this.distCode) {
                            isFind = true;
                            _this.isFind = true;
                            console.log("==============find j = ",j);
                            var inPage = (j+1) % _this.pager.limit == 0 ? parseInt((j+1) / _this.pager.limit) : parseInt((j+1) / _this.pager.limit) + 1;
                            _this.pager.curpage = inPage;
                            _this.getPagerDataAtFullScreen();
                            break;
                        }
                    }
                    if (isFind) {
                        break;
                    }
                }
            }
            if (!isFind) {
                _this.isFind = false;
                // layer.msg("该号码不是当前类型")
                layer.msg(i18np.prop("shipan.msg.number.not.current.type"));
            }
        },

        searchCode4() {
            var _this = this;
            $.ajax({
                url:HOST+"admin/shipan/search4?page="+_this.pager.curpage+"&limit="+_this.pager.limit+"&searchCode="+_this.distCode,
                type:'get',
                success:function(res){
                    if(res.code==0){
                        var isFind = res.data.isFind;
                        var total = res.data.searchTotal;
                        if(isFind==1){
                            _this.isFind = true;
                            _this.settingCodesList[0].pageData = res.data.dataList;
                            _this.pager.curpage = res.data.page;
                            _this.getPagerData4(total);
                        }else{
                            _this.isFind = false;
                            layer.msg(i18np.prop("shipan.msg.number.not.current.type"));
                            // _this.pager.curpage=1;
                            // _this.getPagerData4(total);
                            // _this.settingCodesList[0].pageData = res.data.dataList;
                            //
                            //layer.msg("该号码不是当前类型")
                        }
                    }
                }
            })
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
                    var lsParam = _this.lsParamMap[lsItem.id];
                    var lsAvgOdds = 0;
                    var noBuyLoss = 0;
                    if(lsParam!=null && lsParam!=undefined){
                        lsAvgOdds = lsParam.avgOdds;
                        noBuyLoss = lsParam.noBuyLoss;
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
                // layer.msg("未选择任何号码");
                layer.msg(i18np.prop("shipan.msg.no.selected.number"));
                return;
            }
            layer.msg(i18np.prop("shipan.msg.adding"), {time: -1, icon: 16, shade: 0.3});
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
                    // layer.msg("网络错误");
                    layer.msg(i18np.prop("shipan.msg.request.error"));
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
                    // layer.msg("网络错误");
                    layer.msg(i18np.prop("shipan.msg.request.error"));
                }
            })
            this.getOddsChangeList();
        },
        getOddsChangeList(){ //获取赔率变动号码
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
                    // layer.msg("网络错误");
                    layer.msg(i18np.prop("shipan.msg.request.error"));
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
            return a1.code.localeCompare(a2.code);
            //return a1.code>a2.code;
        },
        toErdingRealbet(__lsId){//切换到二字定模式
            var that = this;
            $("input[type='button']").removeClass('active');
            this.clearSelectCodes();
            this.showMode=2;
            this.lsId = __lsId;
            this.settingList.forEach((item,idx)=>{
                if(item.id==that.lmId){
                    that.lsList = item.lotterySettingList;
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
                item.codesList.sort(_this.__sortByCode);
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
            var locArr = [this.$t('qian'),this.$t('bai'),this.$t('shi'),this.$t('ge')];
            var firstLocIndex = this.erdingBet.dataSet.lsName.indexOf("口");
            var lastLocIndex = this.erdingBet.dataSet.lsName.lastIndexOf("口");
            this.erdingBet.dingLoc1 = locArr[firstLocIndex];
            this.erdingBet.dingLoc2=locArr[lastLocIndex];
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
            //var n = $domEle.attr("data");
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
// vm.initDataList();