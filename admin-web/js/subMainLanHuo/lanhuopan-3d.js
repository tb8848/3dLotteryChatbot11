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
const messages = {
    zh: {
        di:"第",
        page:"页",
        total:"共",
        tiao:"条",
        firstPage:"首页",
        prePage:"上一页",
        nextPage:"下一页",
        lastPage:"尾页",
        fzph:"负值排行",
        zzgs:"正值个数",
        quanping:"全屏",
        lanhuopan:"拦货盘",
        lanhuoMoney:"拦货金额",
        submit:"提交",
    },
    th: {
        di:"หน้าที่",
        page:"หน้า",
        total:"ทั้งหมด",
        tiao:"รายการ",
        firstPage:"หน้าแรก",
        prePage:"หน้าก่อนหน้า",
        nextPage:"หน้าถัดไป",
        lastPage:"หน้าสุดท้าย",
        fzph:"การจัดอันดับค่าลบ",
        zzgs:"จำนวนค่าบวก",
        quanping:"เต็มจอ",
        lanhuopan:"ตะกร้าสินค้า",
        lanhuoMoney:"จำนวนเงินสำหรับการเลือกรับสินค้า",
        submit:"ส่ง",
    }
}

var moduleName = "lanhuo";
var defaultLang = layui.data('langTab').langType;
const i18n = new VueI18n({
    locale: defaultLang, // set locale
    messages, // set locale messages
});
var i18np = null;
initLangConfig({
    defaultLang:defaultLang,
    filePath: "../../js/i18n/"+moduleName+"/",
    module:moduleName,
    base:"../../js/"
})

function changeLang(lang) {
    defaultLang = lang;
    reloadI18n({
        defaultLang:lang,
        filePath: "../../js/i18n/"+moduleName+"/",
        module:moduleName,
    })
    i18np.loadProperties('subMain-'+moduleName);
    i18n.locale=lang;
    vm.initDataList();
}

layui.config({base: '../../js/'})
    // 继承treetable.js插件
    .extend({i18np: 'i18n'}).use([ 'i18np','jquery'], function () {
    i18np = layui.i18np;
    // changeLang(defaultLang);
    reloadI18n({
        defaultLang:defaultLang,
        filePath: "../../js/i18n/"+moduleName+"/",
        module:moduleName,
    })
    i18np.loadProperties('subMain-'+moduleName);
    vm.initDataList();
});

const worker = new Worker("../js/projects/siding-lanhuo-worker.js");
function popChild(tag){
    layer.open({
        type:2,
        title:i18np.prop("lanhuo.chuhuo"),
        content:'kuaida.html',
        area:['80%','500px']
    })
}

const vm = new Vue({
    el: '#lanhuopan',
    i18n,
    data: {
        maxLoss:0,//最大负值
        noBuyMaxLoss:0,
        fullScreen:1,
        lmId:2,
        lsId:"",
        buyList:[],
        settingCodesList:[],
        settingList:[],
        zzvalueMap:{},
        noBuyLossMap:{},
        lsFuzhiMap:{},
        lsList:[],
        drawId: "",
        distCode: "", //搜索号码
        nickName: "",
        userId: "",
        roleName: "",
        colspans:2,
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
            codesInfoArr:[]
        },
        batchSel:{
            size:0,
            lsId:""
        },
        showMode:0,  //0，普通模式；1：号码转模式
        dataFrom:0, //号码来源,0:分类所有；1：赔率变动
        loadOver:0, //数据加载完成标识
        codeZhuanList:[],
        oddsChangeList:[], //赔率变动记录
        currSettingCodes:null,
        zhuanParam:{

        },
        canSetLanHuo:false, //当前角色是否可以设置拦货
        vc:{display:'inline-block'},
        stopCodesList:[],
        drawCodeList:[]
    },
    methods: {
        setLanhuoMoney(obj,__lsid){
            var upLsMoney = $("#lanhuo-money-"+__lsid).val();
            if(upLsMoney==""){
                layer.msg(i18np.prop("lanhuo.noMoneyBuy"));
                return;
            }
            const _this = this;
            layer.msg(i18np.prop("lanhuo.chulizhong"), {time: -1, shade: 0.3, icon: 16})
            //$(obj).attr("disabled",true).val("提交中...");
            $.ajax({
                url:HOST+"drawRule/upLanhuoMoney",
                type:'post',
                data:JSON.stringify({
                    lotterySettingId:__lsid,
                    lanHuoUpperLimit:upLsMoney
                }),
                contentType:"application/json"
                ,success:function(res){
                    layer.closeAll();
                    //$(obj).attr("disabled",false).val("提交");
                    if(res.code==0){
                        layer.msg(i18np.prop("lanhuo.updateSuccess"))
                        //$("#lanhuo-money-"+__lsid).val("");

                        _this.lsList.forEach((item, idx) => {
                            if (item.id == __lsid) {
                                item.lanHuoAmount = upLsMoney;
                            }
                        })
                    }else{
                        layer.msg(res.msg);
                    }
                }
            })
            return false;
        },
        initDataList() {
            this.userId = GetQueryString("parentId");
            var userDetail = GetQueryString("userDetail");
            if (userDetail == 1) {
                this.vc.display='none'
            }
            const _this = this;
            console.log("==========",this.userId)

            //layer.msg("数据加载中...", {time: -1, shade: 0.3, icon: 16})
            $.ajax({
                //url: HOST + 'admin/drawbuy/shipan/settings?lotteryType=1',
                url:HOST +'lotteryMethod/getAll?lotteryType=1&type=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        _this.settingList = res.data;
                        $("#tbody").removeClass('hide')
                        _this.getZongLanhuo();
                        _this.getBuyList();
                    } else {
                        layer.closeAll();
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg(i18np.prop("lanhuo.netError"))
                }
            })
        },
        getZongLanhuo(){//每个大类的累计拦货金额
            var that = this;
            $.ajax({
                //url:HOST +'admin/drawbuy/lanhuopan/zongtou?userId='+that.userId,
                url:HOST +'admin/lanhuopan/zongtou?userId='+that.userId+'&lotteryType=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        var lhMap = res.data;
                        for (var i = 0, len = that.settingList.length; i < len; i++) {
                            var item = that.settingList[i];
                            var __lmId = item.id;
                            var lhMoney = lhMap[__lmId];
                            if(lhMoney==null || lhMoney==undefined){
                                item.fuzhi = 0;
                            }else{
                                item.fuzhi = lhMoney
                            }
                        }
                    }
                },
                error: function (e) {
                    //layer.closeAll();
                    layer.msg("网络错误")
                }
            })
        },
        getBuyList(){
            const _this = this;
            $.ajax({
                url: HOST + 'admin/lanhuopan/listAll?userId='+_this.userId+"&lmId="+_this.lmId,
                //url: HOST + 'admin/drawbuy/lanhuopan/list?userId='+_this.userId+"&lmId="+_this.lmId,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        _this.buyList = res.data.dataList;
                        _this.zzvalueMap = res.data.zzvalueMap;
                        _this.noBuyMaxLoss = res.data.noBuyLossMap;
                        _this.canSetLanHuo = res.data.canSetLanHuo;
                        _this.lsFuzhiMap = res.data.lsFuzhiMap;
                        _this.stopCodesList = res.data.stopBuyCodesList;
                        _this.drawCodeList = res.data.drawCodeList;
                        _this.loadOver = 1;
                        _this.createSettingCodes();

                    } else {
                        layer.msg(res.msg);
                    }
                },
                error: function (e) {
                    layer.closeAll();
                    layer.msg(i18np.prop("lanhuo.netError"))
                }
            })
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
        __isStopCode(code,lsid){
            var exist = false;
            if(this.stopCodesList!=null) {
                this.stopCodesList.forEach((item, idx) => {
                    if (item.codes == code && item.lotterySettingId == lsid) {
                        exist = true;
                    }
                })
            }
            return exist;
        },
        __isDrawCode(code,lmId,lsTypeId){
            var exist = false;
            if(this.drawCodeList!=null) {
                var codeList = this.drawCodeList[lmId+"-"+lsTypeId];
                if(codeList!=undefined && codeList.length>0 && codeList.includes(code)){
                    exist = true;
                }
            }
            return exist;
        },
        changeMethodClass(obj, _lmId, pageSize) {
            this.loadOver = 0;
            this.fullScreen = 1;
            this.dataFrom = 0;
            this.showMode = 0;
            this.pager.limit = pageSize;
            this.pager.curpage = 1;
            $(obj).addClass('yellow');
            $(obj).siblings().removeClass('yellow');
            this.lmId = parseInt(_lmId);
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = this.settingList[i];
                if(item.id == _lmId){
                    this.lsId = item.lotterySettingList[0].id;
                }
            }
            this.getBuyList();
        },
        createSettingCodes() {
            var _this = this;
            var buyList = this.buyList;
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = this.settingList[i];
                var __lmId = item.id;
                var dlist = item.lotterySettingList;
                item.lotterySettingList.forEach((it1,idx1)=>{
                    var lsFuzhi = 0;
                    buyList.forEach((cc,ii)=>{
                        if(cc.lotterySettingId==it1.id){
                            lsFuzhi = accAdd(lsFuzhi,cc.lanhuoMoney);
                        }
                    })
                    //回填小分类的负值数据
                    _this.settingList[i].lotterySettingList[idx1].fuzhi = lsFuzhi;
                })
                if (item.id == this.lmId) {
                    _this.lsList = dlist;
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
                if(_this.lsId==""){
                    _this.lsId = ls.id;
                }
                var _lsId = ls.id;
                var noBuyLoss = _this.noBuyMaxLoss[_lsId];
                if(noBuyLoss==null || noBuyLoss==undefined){
                    noBuyLoss=0;
                }
                var lsFuzhi = _this.lsFuzhiMap[_lsId];
                if(lsFuzhi==null || lsFuzhi==undefined){
                    lsFuzhi=0;
                }
                var dlist = [];
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
                dlist.forEach((item, idx) => {
                    var info = _this.getBuyCodeInfo(item.code,item.lsid);
                    /**
                     * maxLoss:0,chuhuo:0,odds:0,odds_avg:0,package_chuhuo:0,
                     */
                    var isStopBuy = _this.__isStopCode(item.code,item.lsid)
                    var isDraw = _this.__isDrawCode(item.code,_this.lmId,ls.typeId);
                    //var isDraw = _this.__isDrawCode(item.code,item.lsid);
                    if(null!=info){
                        item.maxLoss = info.maxLoss;
                        item.chuhuo = info.lanhuoMoney;
                        item.odds = info.peiRate;
                        item.odds_avg = info.avg_odds;
                        item.package_chuhuo = info.bpMoney;
                        item.selected = isStopBuy;
                    }else{
                        item.maxLoss = noBuyLoss;
                    }
                    item.hasDraw = isDraw;
                })
                dlist.sort(_this.__sortByFuzhi) //按负值排序
                var codesInfo = {
                    lsid: ls.id,
                    lsName: ls.bettingRule,
                    fuzhi: lsFuzhi,
                    zzvalue: 0,
                    odds_avg: 0,
                    codesList: dlist, //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
                    pageData: [],
                    lhAmount: ls.lanHuoAmount
                }
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
            var buyList = this.buyList;
            for (var i = 0, len = this.settingList.length; i < len; i++) {
                var item = this.settingList[i];
                var __lmId = item.id;
                var dlist = item.lotterySettingList;
                item.lotterySettingList.forEach((it1,idx1)=>{
                    var lsFuzhi = 0;
                    buyList.forEach((cc,ii)=>{
                        if(cc.lotterySettingId==it1.id){
                            lsFuzhi = accAdd(lsFuzhi,cc.lanhuoMoney);
                        }
                    })
                    //回填小分类的负值数据
                    _this.settingList[i].lotterySettingList[idx1].fuzhi = lsFuzhi;
                })
                if (item.id == this.lmId) {
                    _this.lsList = dlist;
                }
            }
            this.settingCodesList = [];
            this.lsList.forEach((item, idx) => {
                var _lsId = item.id;
                var noBuyLoss = _this.noBuyMaxLoss[_lsId];
                if(noBuyLoss==null || noBuyLoss==undefined){
                    noBuyLoss=0;
                }
                var lsFuzhi = _this.lsFuzhiMap[_lsId];
                if(lsFuzhi==null || lsFuzhi==undefined){
                    lsFuzhi=0;
                }
                var dlist = createCodesForDing(4, item.bettingRule, _lsId);
                var codesInfo = {
                    lsid: item.id,
                    lsName: item.bettingRule,
                    fuzhi: lsFuzhi,
                    zzvalue: 0,
                    odds_avg: 0,
                    codesList:[], //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked
                    pageData: [],
                    lhAmount: item.lanHuoAmount
                }
                _this.settingCodesList.push(codesInfo);
                var params = {
                    codesList:dlist,
                    buyList:_this.buyList,
                    noBuyLoss:noBuyLoss,
                    stopBuyList:_this.stopCodesList,
                    drawCodeList:_this.drawCodeList
                }
                worker.postMessage(JSON.stringify(params));
                worker.onmessage = (e)=>{
                    var clist = JSON.parse(e.data);
                    clist.sort(_this.__sortByFuzhi) //按负值排序
                    var zzvalue = 0;
                    clist.forEach((dd,idx1)=>{
                        if(dd.maxLoss>=0){
                            zzvalue++;
                        }
                    })
                    codesInfo.zzvalue = zzvalue;
                    codesInfo.codesList = clist;
                    _this.settingCodesList[0]=codesInfo;
                    _this.loadOver = 1;
                    _this.getPagerData();

                }
            })
        },
        changeFullScreen(v,__lsId){
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
            var tdCells = this.settingCodesList.length==1?10:8;
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
                    _this.lines =Math.ceil(pgData.length / tdCells);
                    _this.pager.pages = Math.ceil(_this.pager.total / _this.pager.limit);
                }
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
                if (et > codeList.length) {
                    et = codeList.length;
                }
                var pgData = codeList.slice(st, et);
                this.settingCodesList[0].pageData = pgData;
                //this.batchSel.lsId = this.settingCodesList[0].lsid;
                this.lines = Math.ceil(pgData.length / 10);
                this.pager.pages = Math.ceil(this.pager.total / this.pager.limit);
                console.log(this.lines,this.pager.pages);
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
            return a1.maxLoss-a2.maxLoss;
        }
    },


})
// vm.initDataList();