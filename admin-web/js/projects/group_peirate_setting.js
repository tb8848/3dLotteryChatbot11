/**
 * 分批赔率和包牌分批赔率的脚本
 * 页面调用方法传递类型参数，控制不同页面的交互逻辑
 * @type {jQuery|HTMLElement}
 */
const messages = {
    zh: {
        addBatchOdds:"新增分批赔率",
        dpdxsx:"定盘单项上限",
        plsx:"赔率上限",
        plxx:"赔率下限",
        dypfpts:"第一批分批条数",
        dypdytjzjef:"第一批第一条截止金额放",
        dypjzjexhdz:"第一批截止金额循环递增",
        dyppvsxxhdj:"第一批赔率上限循环递减",
        dyfpjzjew:"第一分批截止金额为",
        pljd:"赔率降到",
        depfpts:"第二批分批条数",
        depdytjzjef:"第二批第一条截止金额放",
        depjzjexhdz:"第二批截止金额循环递增",
        deppvsxxhdj:"第二批赔率上限循环递减",
        defpjzjew:"第二分批截止金额为",
        dspfpts:"第三批分批条数",
        dspdytjzjef:"第三批第一条截止金额放",
        dspjzjexhdz:"第三批截止金额循环递增",
        dsppvsxxhdj:"第三批赔率上限循环递减",
        dsfpjzjew:"第三分批截止金额为",
        submit:"提交",
        delete:"删除",
        yes:"是",
        no:"否",
        tongbu:"同步"
    },
    th: {
        addBatchOdds:"การจ่ายเงินแบบชุดใหม่",
        dpdxsx:"ขีดจำกัดการเดิมพันเดี่ยว",
        plsx:"ขีดจำกัดอัตราต่อรอง",
        plxx:"ขีดจำกัดอัตราต่อรองต่ำสุด",
        dypfpts:"จำนวนการเดิมพันแบบชุดแรก",
        dypdytjzjef:"จำนวนเงินสิ้นสุดของรายการแรกของชุดแรก",
        dypjzjexhdz:"การเพิ่มจำนวนเงินสิ้นสุดของชุดแรกเป็นลำดับเรียง",
        dyppvsxxhdj:"การลดค่าอัตราการจ่ายเงินของชุดแรกเป็นลำดับเรียง",
        dyfpjzjew:"ขีดจำกัดการเดิมพันแบบชุดแรกเป็น",
        pljd:"การลดอัตราการจ่ายเงินเป็น",
        depfpts:"จำนวนการเดิมพันแบบชุดที่สอง",
        depdytjzjef:"จำนวนเงินสิ้นสุดของรายการแรกของชุดที่สอง",
        depjzjexhdz:"การเพิ่มจำนวนเงินสิ้นสุดของชุดที่สองเป็นลำดับเรียง",
        deppvsxxhdj:"การลดค่าอัตราการจ่ายเงินของชุดที่สองเป็นลำดับเรียง",
        defpjzjew:"ขีดจำกัดการเดิมพันแบบชุดที่สองเป็น",
        dspfpts:"จำนวนการเดิมพันแบบชุดที่สาม",
        dspdytjzjef:"จำนวนเงินสิ้นสุดของรายการแรกของชุดที่สาม",
        dspjzjexhdz:"การเพิ่มจำนวนเงินสิ้นสุดของชุดที่สามเป็นลำดับเรียง",
        dsppvsxxhdj:"การลดค่าอัตราการจ่ายเงินของชุดที่สามเป็นลำดับเรียง",
        dsfpjzjew:"ขีดจำกัดการเดิมพันแบบชุดที่สามเป็น",
        submit:"ส่ง",
        delete:"ลบ",
        yes:"ใช่",
        no:"ไม่",
        tongbu:"ซิงค์ข้อมูล"
    }
}

var $ = layui.jquery;

var moduleName = "setting";
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

    vm.init(0);
}

var layer = layui.layer;
window.parent.navSelect("setting");
const vm = new Vue({
    el: '#st_batchodds',
    i18n,
    data: {
        oddsType:0, //类型，0：分批赔率；1：包牌分批赔率
        lsInfo:{
            lsName:'',
            maxNumberTypeCount:'',
            peiRate:'',
            peiRateLowerLimit:''
        },
        selectAllSync:false,
        selectAll:false,
        lsId:null,
        lmId:null,
        editError:false,
        delList:[],
        dataList: [],
        similarList:[],
        syncList:[],
        editList:[],
        upLsId: [],
        addError:false,
        dl:{
            lotterySettingId:'',
            firstItem:{},
            secondItem:{},
            thirdItem:{},
            group1:{
                batch_count:'',
                first_batch_end_money:'',
                increment_money:'',
                odds_gap:'',
                total_money:'',
                total_odds:''
            },
            group2:{
                batch_count:'',
                first_batch_end_money:'',
                increment_money:'',
                odds_gap:'',
                total_money:'',
                total_odds:''
            },
            group3:{
                batch_count:'',
                first_batch_end_money:'',
                increment_money:'',
                odds_gap:'',
                total_money:'',
                total_odds:''
            },
            syncList:[]
        },
        batchAddAreaShow:false,
        syncAreaShow:false
    },
    methods: {
        init(type){
            this.oddsType = type;
            this.lsId = GetQueryString("id");
            this.lmId = GetQueryString("lmId");
            $("#lotterySettingId").val(this.lsId);
            layer.msg(i18np.prop("setting.msg.data.loading"),{time:-1,shade:0.3,icon:16})
            this.getDingPanInfo();
            //this.getSimilarTypes();
            //this.getDatas();
        },
        getDingPanInfo(){
            var that = this;
            $.ajax({
                url: HOST+"dingPan/getPeiRate?lotterySettingId=" + that.lsId
                , type: "get"
                , success: function (res) {
                    if (res.code == 200) {
                        // $("#dpSingleItemUpperLimit").html(res.data.maxNumberTypeCount);
                        // $("#peiRateUpperLimit").val(res.data.peiRate);
                        // $("#peiRateLowerLimit").val(res.data.peiRateLowerLimit);
                        // $("#settingName").html("《"+res.data.bettingRule+"》分批赔率")
                        // $("#ruleName2").html("《"+res.data.bettingRule+"》");
                        that.lsName = res.data.bettingRule;
                        that.lsInfo.lsName = res.data.bettingRule;
                        that.lsInfo.maxNumberTypeCount = res.data.maxNumberTypeCount;
                        that.lsInfo.peiRate = res.data.peiRate;
                        that.lsInfo.peiRateLowerLimit = res.data.peiRateLowerLimit;
                        that.getSimilarTypes();
                        that.getDatas();

                    }else{
                        layer.closeAll();
                        layer.msg(res.msg);
                    }
                }
                , error: function () {
                    layer.closeAll();
                    console.log("ajax error");
                }
            });
        },
        showBatchAddOdds(){
            this.batchAddAreaShow = true;
        },
        setOutFlag(item,idx){
            this.dataList[idx].isOut = !item.isOut;
        },
        saveEdit(){//保存修改
            //判断截止金额修改
            var lastEndAmount = 0;
            var outNum = 0;
            var that = this;
            for(var i=0,len=that.dataList.length;i<len;i++){
                var item = that.dataList[i];
                if(item.checked){
                    outNum++;
                }
                if(parseFloat(item.peiRateUpperLimit)>parseFloat(this.lsInfo.peiRate)){
                    layer.msg(i18np.prop("setting.four.odds")+"["+item.peiRateUpperLimit+"]"+i18np.prop("setting.bndyplsx")+"："+this.lsInfo.peiRate);
                    return;
                }
                if(parseFloat(item.peiRateUpperLimit)<parseFloat(this.lsInfo.peiRateLowerLimit)){
                    layer.msg(i18np.prop("setting.four.odds")+"["+item.peiRateUpperLimit+"]"+i18np.prop("setting.bnxyplxx")+"："+this.lsInfo.peiRateLowerLimit);
                    return;
                }
                //item.isOut = item.checked?1:0;
                if(parseFloat(item.endAmount)<=0){
                    layer.msg(i18np.prop("setting.jzjebnxydy"));
                    return;
                }
                if(i==0){
                    lastEndAmount = item.endAmount;
                }else{
                    if(parseFloat(item.endAmount)<=lastEndAmount){
                        layer.msg(i18np.prop("setting.jzje")+item.endAmount+i18np.prop("setting.bnxydysypdjzje")+lastEndAmount);
                        return;
                    }
                    if(item.startAmount!=lastEndAmount){
                        item.startAmount = lastEndAmount;
                    }
                    lastEndAmount = item.endAmount;
                }
            }
            if(outNum==0){
                layer.msg(i18np.prop("setting.zsfcytjl"))
                return;
            }
            // console.log("===============dataList==",that.dataList);
            layer.msg(i18np.prop("setting.zzclz"), {icon: 16,shade: 0.3,time:-1 });
            var syncList = [];
            this.similarList.forEach((item,idx)=>{//同步列表，不包含当前选中的
                if(item.checked && item.id!=that.lsId){
                    syncList.push(item.id)
                }
            })
            var editList =[];
            that.dataList.forEach((item,idx)=>{
                var it = item;
                it.isOut = it.checked?1:0;
                editList.push(it);
            })

            var url = "";
            var params = {};
            if(this.oddsType == 0){
                url = HOST+'fpPeiRate/update';
                params = {
                    editList:editList,
                    syncList:syncList,
                }
            }else{
                url = HOST+'bpGroupPeiRate/update';
                params = {
                    bpEditList:editList,
                    syncList:syncList,
                }
            }

            $.ajax({
                url:url,
                type:'post',
                contentType:'application/json',
                data:JSON.stringify(params),
                success:function (res) {
                    layer.closeAll();
                    if(res.code==0){
                        layer.confirm(i18np.prop("setting.edit.success"), {
                            title:i18np.prop("setting.five.tips"),
                            btn: [i18np.prop("setting.determine")] //可以无限个按钮
                        }, function(index, layero){
                            that.clearSyncCheck();
                            that.getDatas();
                        });
                        // that.clearSyncCheck();
                        // that.getDatas();
                    }else{
                        layer.confirm(res.msg, {
                            title:i18np.prop("setting.five.tips"),
                            btn: [i18np.prop("setting.determine")] //可以无限个按钮
                        }, function(index, layero){
                            that.getDatas();
                        });
                        // layer.msg(res.msg);
                        // that.getDatas();
                    }
                },
                error:function (e) {
                    // layer.closeAll();
                }
            })
            return false;
        },

        saveDel(){//删除
            var that = this;
            var drawOpenStatus = window.localStorage.getItem("drawOpenStatus");
            if(drawOpenStatus==1 || drawOpenStatus=="1"){
                layer.msg(i18np.prop("setting.kphbnscfppl"));
                return;
            }
            var delList = [];
            this.dataList.forEach((item,idx)=>{
                if(item.checked){
                    delList.push(item.id);
                }
            })
            if(delList.length<1){
                layer.msg(i18np.prop("setting.no.select.record"));
                return;
            }
            var url = "";
            if(this.oddsType == 0){
                url = HOST+'fpPeiRate/del'
            }else{
                url = HOST+'bpGroupPeiRate/del'
            }

            layer.msg(i18np.prop("setting.zzclz"), {icon: 16,shade: 0.3,time:-1 });
            $.ajax({
                url:url,
                type:'post',
                contentType:'application/json',
                data:JSON.stringify({
                    dlist:delList,
                    lotterySettingId:that.lsId
                }),
                success:function (res) {
                    layer.closeAll();
                    if(res.code==0){
                        that.batchAddAreaShow = false;
                        that.getDatas();
                    }else{
                        layer.msg(res.msg);
                    }
                },
                error:function (e) {
                    layer.closeAll();
                }
            })
        },
        saveAdd(){
            var that = this;
            if(this.addError){
                // layer.alert("分批设置参数有错误，请核查");
                layer.msg(i18np.prop("setting.fpszcsycwqhc"));
                return;
            }
            var firstEndAmount2 = this.dl.group1.total_money;
            if(firstEndAmount2==null || firstEndAmount2==''){
                // layer.alert("第1批次的截止金额不能为空");
                layer.msg(i18np.prop("setting.dypcdjzjebnwk"));
                return;
            }

            var secondEndAmount = this.dl.group2.total_money;
            if(secondEndAmount!=null && secondEndAmount!=""){
                if(parseFloat(secondEndAmount)<parseFloat(firstEndAmount2)){
                    layer.msg(i18np.prop("setting.depcdjzje")+":"+secondEndAmount+" "+i18np.prop("setting.bxdydypcdzdjzje")+":"+firstEndAmount2+"。");
                    return;
                }
            }

            var thirdEndAmount = this.dl.group3.total_money;
            if(thirdEndAmount!=null && thirdEndAmount!=""){
                var secondEndAmount2 = this.dl.group2.total_money;
                if(parseFloat(thirdEndAmount)<parseFloat(secondEndAmount2)){
                    layer.msg(i18np.prop("setting.dspcdjzje")+":"+thirdEndAmount+" "+i18np.prop("setting.bxdydepcdzdjzje")+":"+secondEndAmount2+"。");
                    return;
                }
            }

            layer.msg(i18np.prop("setting.zzclz"), {icon: 16,shade: 0.3,time:-1});
            var syncList = [];
            this.similarList.forEach((item,idx)=>{//同步列表，不包含当前选中的
                if(item.checked && item.id!=that.lsId){
                    syncList.push(item.id)
                }
            })

            this.dl.lotterySettingId = that.lsId;
            this.dl.syncList = syncList;

            var url = "";
            if(this.oddsType == 0){
                url = HOST+'fpPeiRate/save'
            }else{
                url = HOST+'bpGroupPeiRate/save'
            }

            $.ajax({
                url:url,
                type:'post',
                contentType:'application/json',
                data:JSON.stringify(that.dl),
                success:function (res) {
                    layer.closeAll();
                    that.clearSyncCheck();
                    if(res.code==0) {
                        that.batchAddAreaShow = false;
                        that.clearAddParams();
                        that.getDatas();
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
        clearAddParams(){
            this.dl.firstItem = this.dl.secondItem = this.dl.thirdItem = {};
            this.dl.group1 = this.dl.group2 = this.dl.group3 = {
                batch_count:'',
                first_batch_end_money:'',
                increment_money:'',
                odds_gap:'',
                total_money:'',
                total_odds:''
            }
        },
        getSimilarTypes(){
            var that = this;
            $.ajax({
                url: HOST+"lotterySetting/getList?lmId="+that.lmId
                , type: "get"
                , success: function (res) {
                    if (res.code == 0) {
                        that.similarList = res.data;
                        that.similarList.forEach((item,idx)=>{
                            item.checked =false;
                        })
                        if(that.similarList.length>1){
                            that.syncAreaShow = true;
                        }else{
                            that.syncAreaShow = false;
                        }
                    }else{
                        layer.msg(res.msg);
                    }
                }
                , error: function () {
                    console.log("ajax error");
                }
            });
        },
        getDatas(){
            var that = this;
            var url = "";
            console.log("oddsType="+this.oddsType+" lsId="+that.lsId);
            if(this.oddsType == 0) {
                url = HOST + 'fpPeiRate/listAll?lotterySettingId=' + that.lsId;
            }else {
                url = HOST + 'bpGroupPeiRate/listAll?lotterySettingId=' + that.lsId;
            }
            $.ajax({
                url:url,
                type:'get',
                success:function(res){
                    layer.closeAll();
                    if(res.code==0){
                        that.dataList = res.data;
                        for(var i=0,len=that.dataList.length;i<len;i++){
                            that.dataList[i].initStartAmount = that.dataList[i].startAmount;
                            that.dataList[i].initEndAmount=that.dataList[i].endAmount;
                            that.dataList[i].initPeiRateUpperLimit=that.dataList[i].peiRateUpperLimit;
                            that.dataList[i].checked = that.dataList[i].isOut==1?true:false;
                        }
                        console.log("=======dataList",that.dataList);
                        // dataList.forEach((item,idx)=>{
                        //     item.initStartAmount = item.startAmount;
                        //     item.initEndAmount=item.endAmount;
                        //     item.initPeiRateUpperLimit=item.peiRateUpperLimit;
                        // })
                        //renderTable()
                    }else{
                        layer.msg(res.msg);
                    }
                },
                error:function (e) {
                    layer.closeAll();
                }
            })
        },
        setGroup1(){
            var pass = false;
            var that = this;
            var firstGroupCount = this.dl.group1.batch_count;
            if(firstGroupCount != null && firstGroupCount != ""){
                pass = validateValue(firstGroupCount,"digit");
                if(!pass){
                    layer.msg(i18np.prop("setting.tsqzznwsz"));
                    this.addError = true;
                    return;
                }
            }
            var firstEndAmount = this.dl.group1.first_batch_end_money;
            var firstEndAmountLoopAdd = this.dl.group1.increment_money;
            var firstPeiRateUpperLimitLoopSubtract = this.dl.group1.odds_gap;
            var peiRateUpperLimit = this.lsInfo.peiRate;
            var peiRateLowerLimit = this.lsInfo.peiRateLowerLimit;
            if (firstGroupCount != null && firstGroupCount != "" &&
                firstEndAmount != null && firstEndAmount != "" &&
                firstEndAmountLoopAdd != null && firstEndAmountLoopAdd != "" &&
                firstPeiRateUpperLimitLoopSubtract != null && firstPeiRateUpperLimitLoopSubtract != "") {
                try{
                    var totalMoney = parseInt(firstEndAmount) + ((parseInt(firstGroupCount) * parseInt(firstEndAmountLoopAdd)) - parseInt(firstEndAmountLoopAdd));
                    var isMutiOdds = false;
                    if(peiRateUpperLimit.indexOf("/")>-1){
                        isMutiOdds = true;
                    }
                    var leftOdds = 0;
                    var totalOdds = (parseInt(firstGroupCount)-1) * parseFloat(firstPeiRateUpperLimitLoopSubtract);
                    if(!isMutiOdds){
                        leftOdds = accSub(parseFloat(peiRateUpperLimit),totalOdds);
                        if(leftOdds<parseFloat(peiRateLowerLimit)){
                            leftOdds = peiRateLowerLimit;
                        }
                    }else{
                        var prUpperArr = peiRateUpperLimit.split("/");
                        var prLowerArr = peiRateLowerLimit.split("/");
                        var leftOddsArr = [];
                        prUpperArr.forEach((item,idx)=>{
                            var odds = accSub(parseFloat(item),totalOdds);
                            if(odds<parseFloat(prLowerArr[idx])){
                                odds = prLowerArr[idx];
                            }
                            leftOddsArr.push(odds);
                        })
                        leftOdds = leftOddsArr.join("/");
                    }

                    this.dl.group1.total_money = totalMoney;
                    this.dl.group1.total_odds = leftOdds;
                    this.dl.firstItem = {
                        endAmount:firstEndAmount,
                        groupCount:firstGroupCount,
                        amountLoopAdd:firstEndAmountLoopAdd,
                        peiRateLoopSubstract:firstPeiRateUpperLimitLoopSubtract,
                        maxEndAmount:totalMoney
                    }
                    this.addError = false;
                }catch (e) {
                    this.addError = true;
                }

            }
        },
        setGroup2() {
            var isMutiOdds = false;
            var secondGroupCount = this.dl.group2.batch_count;
            var secondEndAmount = this.dl.group2.first_batch_end_money;
            var secondEndAmountLoopAdd = this.dl.group2.increment_money;;
            var secondPeiRateUpperLimitLoopSubtract = this.dl.group2.odds_gap;
            var totalMoney = this.dl.group1.total_money;
            var totalOdds = this.dl.group1.total_odds;
            var peiRateUpperLimit = this.lsInfo.peiRate;
            var peiRateLowerLimit = this.lsInfo.peiRateLowerLimit;
            if (totalMoney == null || totalMoney == "" || totalOdds == null || totalOdds == "") {
                this.addError = true;
                layer.msg(i18np.prop("setting.qxtxdypfptssj"));
                return;
            }
            if (secondGroupCount != null && secondGroupCount != "" &&
                secondEndAmount != null && secondEndAmount != "" &&
                secondEndAmountLoopAdd != null && secondEndAmountLoopAdd != "" &&
                secondPeiRateUpperLimitLoopSubtract != null && secondPeiRateUpperLimitLoopSubtract != "") {
                if(parseFloat(secondEndAmount)<parseFloat(totalMoney)){
                    this.addError = true;
                    return
                }
                var totalMoney2 = parseFloat(secondEndAmount) + parseFloat(secondEndAmountLoopAdd) * (parseInt(secondGroupCount)-1);
                if(peiRateUpperLimit.indexOf("/")>-1){
                    isMutiOdds = true;
                }
                var leftOdds = 0;
                if(!isMutiOdds){
                    leftOdds = accSub(totalOdds,(secondGroupCount * secondPeiRateUpperLimitLoopSubtract));
                    if(leftOdds<parseFloat(peiRateLowerLimit)){
                        leftOdds = peiRateLowerLimit;
                    }
                }else{
                    var prUpperArr = peiRateUpperLimit.split("/");
                    var prLowerArr = peiRateLowerLimit.split("/");
                    var totalOddsArr = totalOdds.split("/");
                    var leftOddsArr = [];
                    totalOddsArr.forEach((item,idx)=>{
                        var odds = accSub(parseFloat(item),(secondGroupCount * secondPeiRateUpperLimitLoopSubtract));
                        if(odds<parseFloat(prLowerArr[idx])){
                            odds = prLowerArr[idx];
                        }
                        leftOddsArr.push(odds);
                    })
                    leftOdds = leftOddsArr.join("/");
                }
                //
                // var totalOdds2 = totalOdds - (secondGroupCount * secondPeiRateUpperLimitLoopSubtract);
                // if(totalOdds2<parseFloat(peiRateLowerLimit)){
                //     totalOdds2 = peiRateLowerLimit;
                // }
                this.dl.group2.total_money = totalMoney2;
                this.dl.group2.total_odds = leftOdds;
                // $("input[name='secondEndAmount2']").val(totalMoney2);
                // $("input[name='secondPeiRateLower']").val(totalOdds2);
                this.dl.secondItem = {
                    endAmount:secondEndAmount,
                    groupCount:secondGroupCount,
                    amountLoopAdd:secondEndAmountLoopAdd,
                    peiRateLoopSubstract:secondPeiRateUpperLimitLoopSubtract,
                    maxEndAmount:totalMoney
                }
                this.addError = false;
            }
        },
        setGroup3(){
            var isMutiOdds = false;
            var thirdGroupCount =this.dl.group3.batch_count;
            var thirdEndAmount = this.dl.group3.first_batch_end_money;
            var thirdEndAmountLoopAdd = this.dl.group3.increment_money;
            var thirdPeiRateUpperLimitLoopSubtract = this.dl.group3.odds_gap;
            var totalMoney =this.dl.group2.total_money;
            var totalOdds = this.dl.group2.total_odds;
            var peiRateUpperLimit = this.lsInfo.peiRate;
            var peiRateLowerLimit = this.lsInfo.peiRateLowerLimit;
            if (totalMoney == null || totalMoney == "" || totalOdds == null || totalOdds == "") {
                this.addError = true;
                layer.msg(i18np.prop("setting.qxtxdepfptssj"));
                return;
            }
            if (thirdGroupCount != null && thirdGroupCount != "" &&
                thirdEndAmount != null && thirdEndAmount != "" &&
                thirdEndAmountLoopAdd != null && thirdEndAmountLoopAdd != "" &&
                thirdPeiRateUpperLimitLoopSubtract != null && thirdPeiRateUpperLimitLoopSubtract != "") {
                if(parseFloat(thirdEndAmount)<parseFloat(totalMoney)){
                    this.addError = true;
                    return
                }
                var totalMoney2 = parseFloat(thirdEndAmount) + parseFloat(thirdEndAmountLoopAdd) * (parseInt(thirdGroupCount)-1);
                if(peiRateUpperLimit.indexOf("/")>-1){
                    isMutiOdds = true;
                }
                var leftOdds = 0;
                if(!isMutiOdds){
                    leftOdds = accSub(totalOdds,(thirdGroupCount * thirdPeiRateUpperLimitLoopSubtract));
                    if(leftOdds<parseFloat(peiRateLowerLimit)){
                        leftOdds = peiRateLowerLimit;
                    }
                }else{
                    var prLowerArr = peiRateLowerLimit.split("/");
                    var totalOddsArr = totalOdds.split("/");
                    var leftOddsArr = [];
                    totalOddsArr.forEach((item,idx)=>{
                        var odds = accSub(parseFloat(item),(thirdGroupCount * thirdPeiRateUpperLimitLoopSubtract));
                        if(odds<parseFloat(prLowerArr[idx])){
                            odds = prLowerArr[idx];
                        }
                        leftOddsArr.push(odds);
                    })
                    leftOdds = leftOddsArr.join("/");
                }
                // var totalOdds2 = totalOdds - (thirdGroupCount * thirdPeiRateUpperLimitLoopSubtract);
                // if(totalOdds2<parseFloat(peiRateLowerLimit)){
                //     totalOdds2 = peiRateLowerLimit;
                // }
                this.dl.group3.total_money = totalMoney2;
                this.dl.group3.total_odds = leftOdds;
                this.dl.thirdItem = {
                    endAmount:thirdEndAmount,
                    groupCount:thirdGroupCount,
                    amountLoopAdd:thirdEndAmountLoopAdd,
                    peiRateLoopSubstract:thirdPeiRateUpperLimitLoopSubtract,
                    maxEndAmount:totalMoney
                }
                this.addError =false;
            }
        },
        clearSyncCheck(){
            this.selectAllSync = false;
            this.similarList.forEach((item,idx)=>{
                item.checked =false;
            })
        },
        changeSyncAllCheck(){
            this.selectAllSync = !this.selectAllSync;
            this.similarList.forEach((item,idex)=>{
                item.checked = this.selectAllSync;
            })
        },
        changeSyncOneCheck(one){
            var hasCheckedNum = 0;
            this.similarList.forEach((item,idex)=>{
                if(one.id == item.id){
                    item.checked = !item.checked;
                }
                if(item.checked){
                    hasCheckedNum++;
                }
            })
            if(hasCheckedNum == this.similarList.length){
                this.selectAllSync = true;
            }else{
                this.selectAllSync = false;
            }
        },
        changeSelectOne(one){//数据列表，单选
            var hasCheckedNum = 0;
            this.dataList.forEach((item,idex)=>{
                if(one.id == item.id){
                    item.checked = !item.checked;
                }
                if(item.checked){
                    hasCheckedNum++;
                }
            })
            if(hasCheckedNum == this.dataList.length){
                this.selectAll = true;
            }else{
                this.selectAll = false;
            }
        },
        changeSelectAll(){
            this.selectAll = !this.selectAll;
            this.dataList.forEach((item,idx)=>{
                item.checked =this.selectAll;
            })
        },
        editDataItem(oneItem,idx){
            var oriDataItem = this.dataList[idx];
            //判断输入的赔率上下限
            if(oriDataItem.endAmount!=oneItem.endAmount){
                this.dataList[idx].endAmount = oneItem.endAmount;
                this.editError = false;
            }
            if(oriDataItem.peiRateUpperLimit != oneItem.peiRateUpperLimit){
                this.dataList[idx].peiRateUpperLimit = oneItem.peiRateUpperLimit;
                this.editError = false;

            }
        }
    }
});

//vm.init();