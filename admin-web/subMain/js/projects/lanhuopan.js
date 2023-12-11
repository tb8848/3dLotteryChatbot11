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
const worker = new Worker("../js/projects/siding-lanhuo-worker.js");
function popChild(tag){
    layer.open({
        type:2,
        title:'出货',
        content:'kuaida.html',
        area:['80%','500px']
    })
}

const vm = new Vue({
    el: '#lanhuopan',
    data: {
        maxLoss:0,//最大负值
        noBuyMaxLoss:0,
        fullScreen:0,
        lmId:4,
        lsId:"",
        buyList:[],
        settingCodesList:[],
        settingList:[],
        zzvalueMap:{},
        noBuyLossMap:{},
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
        canSetLanHuo:false,
        zhuanParam:{

        },
        vc:{display:'inline-block'}
    },
    methods: {
        setLanhuoMoney(obj,__lsid){
            var upLsMoney = $("#lanhuo-money-"+__lsid).val();
            if(upLsMoney==""){
                layer.msg("请填写拦货金额");
                return;
            }

            $(obj).attr("disabled",true).val("提交中...");
            $.ajax({
                url:HOST+"drawRule/upLanhuoMoney",
                type:'post',
                data:JSON.stringify({
                    lotterySettingId:__lsid,
                    lanHuoUpperLimit:upLsMoney
                })
                ,contentType:'application/json'
                ,success:function(res){
                    $(obj).attr("disabled",false).val("提交");
                    if(res.code==0){
                        layer.msg("操作成功");
                    }else{
                        layer.msg(res.msg);
                    }
                }
            })
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
                    this.getOddsChangeList();
                }
            }else{
                if (v == 0) {
                    this.changeFullScreen(1,null);
                } else {
                    this.currSettingCodes.pageData = [];
                    this.lines = 0;
                    this.getOddsChangeList();
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
            this.userId = GetQueryString("parentId");
            var userDetail = GetQueryString("userDetail");
            if (userDetail=="1") {
                //$("input[name='hold_money']").hide()
                this.vc.display='none'
            }
            console.log(this.vc);
            const _this = this;
            //layer.msg("数据加载中...", {time: -1, shade: 0.3, icon: 16})
            $.ajax({
                //url: HOST + 'admin/drawbuy/shipan/settings?lotteryType=1',
                url:HOST +'lotteryMethod/getAll?lotteryType=1&type=1',
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        _this.settingList = res.data;
                        $("#tbody").removeClass('hide')
                        _this.getBuyList()
                        _this.getZongLanhuo();
                    } else {
                        this.loadOver=1;
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
        getBuyList(){
            const _this = this;
            $.ajax({
                url: HOST + 'admin/drawbuy/lanhuopan/list?userId='+_this.userId+"&lmId="+_this.lmId,
                type: 'get',
                success: function (res) {
                    layer.closeAll();
                    if (res.code == 0) {
                        _this.buyList = res.data.dataList;
                        //_this.noBuyMaxLoss = res.data.totalEarn;
                        _this.zzvalueMap = res.data.zzvalueMap;
                        _this.noBuyMaxLoss = res.data.noBuyLossMap;
                        if(_this.lmId==4){
                            _this.createSettingCodes4();
                        }else{
                            _this.loadOver = 1;
                            _this.createSettingCodes();
                        }
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
        getBuyCodeInfo(code,isXian){
            var info = null;
            if(this.buyList.length>0){
                for(var i=0,len=this.buyList.length;i<len;i++){
                    var item = this.buyList[i];
                    if(item.buyCode == code && isXian == item.isXian){
                        return item;
                    }
                }
            }
            return info;
        },
        changeMethodClass(obj, _lmId, pageSize) {
            this.loadOver = 0;
            this.fullScreen = 0;
            this.dataFrom = 0;
            this.showMode = 0;
            this.pager.limit = pageSize;
            this.pager.curpage = 1;
            $(obj).addClass('yellow');
            $(obj).siblings().removeClass('yellow');
            this.lmId = _lmId;
            this.batchSel.size = 0;
            this.getBuyList();
        },
        getZongLanhuo(){//每个大类的累计拦货金额
            var that = this;
            $.ajax({
                url:HOST +'admin/drawbuy/lanhuopan/zongtou?userId='+that.userId,
                type: 'get',
                success: function (res) {
                    if (res.code == 0) {
                        var lhMap = res.data;
                        for (var i = 0, len = that.settingList.length; i < len; i++) {
                            var item = that.settingList[i];
                            var __lmId = item.id;
                            var lhMoney = lhMap[__lmId];
                            if(lhMoney==null || lhMoney==undefined){
                                $("#lm-fz-" + __lmId).html(0);
                            }else{
                                $("#lm-fz-" + __lmId).html(lhMoney);
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
                            lsFuzhi = lsFuzhi + cc.lanhuoMoney;
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
                var dlist = [];
                switch (_this.lmId) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        dlist = createCodesForDing(_this.lmId, item.bettingRule, _lsId);
                        break;
                    case 5:
                        dlist = createCodesForXian(2, _lsId);
                        break;
                    case 6:
                        dlist = createCodesForXian(3, _lsId);
                        break;
                    case 7:
                        dlist = createCodesForXian(4, _lsId);
                        break;
                }

                dlist.forEach((item, idx) => {
                    var info = _this.getBuyCodeInfo(item.code,item.xian);
                    if(null!=info){
                        item.maxLoss = info.maxLoss;
                        item.chuhuo = info.buyMoney;
                        item.odds = info.peiRate;
                        item.odds_avg = info.avg_odds;
                        item.package_chuhuo = info.bpMoney;
                        item.selected = true;
                    }else{
                        item.maxLoss = noBuyLoss;
                    }
                })
                dlist.sort(_this.__sortByFuzhi) //按负值排序
                var zzvalue = 0;
                dlist.forEach((dd,idx1)=>{
                    if(dd.maxLoss>=0){
                        zzvalue++;
                    }
                })
                var codesInfo = {
                    lsid: item.id,
                    lsName: item.bettingRule,
                    fuzhi: item.fuzhi,
                    odds_avg: 0,
                    zzvalue: zzvalue,
                    codesList: dlist, //元素为对象，包含属性 code,odds,odds_avg,chuhuo,maxLoss,lsid,package_chuhuo,checked,
                    pageData: [],
                    lhAmount: item.lanHuoAmount
                }
                _this.settingCodesList.push(codesInfo)
            })
            _this.getPagerData();
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
                var dlist = createCodesForDing(4, item.bettingRule, _lsId);
                var codesInfo = {
                    lsid: item.id,
                    lsName: item.bettingRule,
                    fuzhi: item.fuzhi,
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
                    noBuyLoss:noBuyLoss
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
        initPager() {

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
        selCount(obj,size) {
            this.clearSelectCodes();
            if($(obj).hasClass('sel-active')){
                this.batchSel.size = 0;
            }else{
                this.batchSel.size = size;
            }
            if (this.batchSel.lsId == "") {
                this.batchSel.lsId = this.lsId;
            }
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
                }
            })
        },
        selectOneAtZhuan(codeItem){ //号码转模式下的单个号码选中
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
            this.showMode = 1;
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
            var _this = this;
            var lmList = [2, 3,4];
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
                lsArr.forEach((item, idx) => {
                    var dlist = createCodesForDing(lmId, item.bettingRule, item.id);
                    dlist.forEach((item1, idx1) => {
                        item1.lsid = item.id;
                    })
                    _this.__findMatchCodes(dlist,numLocs,item.bettingRule,item.id);
                })
            })
        },
        __findMatchCodes(dlist, numLocs, lsName,_lsId) {
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
        getOddsChangeList(){ //获取赔率变动号码
            layer.msg("",{time:-1,icon:16,shade:0.5});
            var _this = this;
            var params = 'page='+this.pager.curpage+"&limit="+this.pager.limit+"&lotteryMethodId="+this.lmId+"&lotterySettingId="+this.lsId+"&comboId=";
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
                        if(lsId<5){
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
            return a1.maxLoss-a2.maxLoss;
        }
    },


})
//vm.initDataList();