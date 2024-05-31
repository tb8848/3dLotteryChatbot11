var $ = layui.jquery;
const vm = new Vue({
    el:'#kuaixuan',
    i18n,
    data:{
        rules:{
            "isXian":0,
            "count":1,
            "lotteryMethodId":'',
            "fixCode":{
                "name":'定位置',
                "included":1,
                "excluded":0,
                "loc1":"",
                "loc2":"",
                "loc3":"",
                "loc4":"",
                "isShow":true
            },
            "matchCodes":{
                "name":'配数',
                "included":0,
                "excluded":0,
                "loc1":"",
                "loc2":"",
                "loc3":"",
                "loc4":"",
                "isXian":0,
                "isShow":false
            },
            "repeat":{
                "name":'重数',
                "double1":{
                    "name":"双重",
                    "included":0,
                    "excluded":0,
                    "nums":2,
                },
                "triple":{
                    "name":"三重",
                    "included":0,
                    "excluded":0,
                    "nums":3,
                },
                "double2":{
                    "name":"四重",
                    "included":0,
                    "excluded":0,
                    "nums":4,
                },
                "doubledouble":{
                    "name":"双双重",
                    "included":0,
                    "excluded":0,
                    "nums":22,
                }
            },
            "brothers":{
                "name":"兄弟",
                "br2":{
                    "name":"两兄弟",
                    "included":0,
                    "excluded":0,
                    "nums":2,
                },
                "br3":{
                    "name":"三兄弟",
                    "included":0,
                    "excluded":0,
                    "nums":3,
                },
                "br4":{
                    "name":"四兄弟",
                    "included":0,
                    "excluded":0,
                    "nums":4,
                }
            },
            "pairs":{
                "name":"对数",
                "included":0,
                "excluded":0,
                "pair1":"",
                "pair2":"",
                "pair3":"",
                "pair4":"",
                "pair5":""
            },
            "singleNum":{
                "name":"单数",
                "included":0,
                "excluded":0,
                "locArr":[0,0,0,0],
            },
            "doubleNum":{
                "name":"双数",
                "included":0,
                "excluded":0,
                "locArr":[0,0,0,0],
            },
            "bigNum":{
                "name":"大数",
                "included":0,
                "excluded":0,
                "locArr":[0,0,0,0],
            },
            "smallNum":{
                "name":"小数",
                "included":0, //1:取
                "excluded":0, //1：除
                "locArr":[0,0,0,0],//位置,

            },
            "sum2":{
                "name":"两(或三)数合",
                "included":0, //1:取
                "excluded":0, //1：除
                "sum":"",
                "sumType":0, //2：两数和；3：三数和
            },
            "others":{
                "name":'其他规则',
                "fullChange":'', //全转
                "shangJiang":'', //上奖
                'excludes':'', //排除
                'locArr':[0,0,0,0],//乘号规则
                "valueRange":["",""] //值范围
            },
            "fushi":{
                "name":'复式规则',
                "included":1,
                "excluded":0,
                "contains":'', //含
                "combines":'' //复式
            },
            "hefen":{
                "name":'合分',
                "included":1,
                "excluded":0,
                "binds":[
                    {
                        "name":'',
                        "value":'',
                        "locArr":[0,0,0,0]
                    },
                    {
                        "name":'',
                        "value":'',
                        "locArr":[0,0,0,0]
                    },
                    {
                        "name":'',
                        "value":'',
                        "locArr":[0,0,0,0]
                    },
                    {
                        "name":'',
                        "value":'',
                        "locArr":[0,0,0,0]
                    }
                ]
            }
        },
        ruleName:'',
        lotteryMethodId:"",
        lotteryMethodList:[],
        ruleItemIndex:0,
        isXian:0,
        codesNum:0,
        codesList:[],
        showLines:0,
        emptyRules:{},
        reCreate:0,
        buyMoney:'',
        buyAmount:0,
        totalMoney:0,
        quickSelectSetting:{

        },
        openStatus:1,
        isStopBuy:0, //是否停止下注
        openStatusDesc:'',
        recreateId:'',
        printOdds:'',
        currLm:'',
        created:false,
        isPhone:false,
        tableData:{
            currpage:1,
            limit:1000,
            dataList:[]
        },
        tableBox:null,
        tableDataHtml:'',
        xzuuid:''

    },
    methods:{
        loadTableData(e){
            let el = e.target;
            var currpage = this.tableData.currpage;
            let totalPages = Math.ceil(this.codesList.length/this.tableData.limit);
            const scrollDistance = el.scrollHeight - el.scrollTop - el.clientHeight;
            if(scrollDistance<=10){
                if(currpage<totalPages){
                    currpage++;
                    var st = (currpage-1)*this.tableData.limit;
                    var et = (currpage)*this.tableData.limit;
                    if(et>this.codesList.length){
                        et = this.codesList.length;
                    }
                    var pageData = this.codesList.slice(st,et);
                    //this.tableData.dataList = this.codesList.slice(0,(currpage)*this.tableData.limit);
                    //this.showLines = Math.ceil(this.tableData.dataList.length/8);
                    //console.log("============showLines ==",this.showLines)
                    this.tableData.currpage=currpage;
                    this.appendTableData(pageData)
                }
            }
        },
        initLotteryMethodI18nName(){
            this.openStatusTrans();
            const that = this;
            that.lotteryMethodList.forEach((item,idx)=>{
                var betMethodName = item.bettingMethod;
                switch(item.id){
                    case "1":
                        betMethodName = that.$t("message.yiding");
                        break;
                    case "2":
                        betMethodName = that.$t("message.erding");
                        break;
                    case "3":
                        betMethodName = that.$t("message.sanding");
                        break;
                    case "4":
                        betMethodName = that.$t("message.siding");
                        break;
                    case "5":
                        betMethodName = that.$t("message.erxian");
                        break;
                    case "6":
                        betMethodName = that.$t("message.sanxian");
                        break;
                    case "7":
                        betMethodName = that.$t("message.sixian");
                        break;
                }
                if(that.currLm.id==item.id){
                    that.ruleName = betMethodName;
                }
                item.bettingMethod = betMethodName;
            })
        },
        appendTableData(pageData){
            var newHtml = "";
            var tr = "";
            var td = "";
            for(var i=0,len=pageData.length;i<len;i++){
                if(parseInt(i%8)==0 && i>0){
                    tr = '<tr>'+td+'</tr>';
                    newHtml = newHtml + tr;
                    td = '';
                }
                td = td +'<td style="width:12.5%">'+pageData[i].buyCode+'</td>';
            }

            tr = '<tr>'+td+'</tr>';
            newHtml = newHtml + tr;
            this.tableDataHtml = this.tableDataHtml + newHtml;

            //$("#tableData").append(newHtml);
        },
        setOpenStatus(msg){
            this.openStatus = msg.openStatus;
            if(msg.quickOrTxtCloseFlag==0) {//快选/导入栏目禁止下注标识
                this.isStopBuy = 1;
                this.openStatusDesc = $i18np.prop("kuaixuan.stopBuy"); //停止下注
            }else{
                this.isStopBuy = 0;
                this.openStatusDesc = "";
            }
            if(this.openStatus==3){
                this.openStatusDesc = $i18np.prop("kuaixuan.drawOpening"); //"正在开盘中";
            }else if(this.openStatus==2){
                this.openStatusDesc = $i18np.prop("kuaixuan.drawUnopen"); //"未开盘";
            }else if(this.openStatus==0){
                this.openStatusDesc = $i18np.prop("kuaixuan.drawClose"); //"已封盘";
            }else{
                this.openStatusDesc = "";
            }
        },
        openStatusTrans(){
            if(this.openStatus==3){
                this.openStatusDesc = $i18np.prop("kuaixuan.drawOpening"); //"正在开盘中";
            }else if(this.openStatus==2){
                this.openStatusDesc = $i18np.prop("kuaixuan.drawUnopen"); //"未开盘";
            }else if(this.openStatus==0){
                this.openStatusDesc = $i18np.prop("kuaixuan.drawClose"); //"已封盘";
            }else{
                this.openStatusDesc = "";
            }
        },
        init(){
            const that = this;
            this.emptyRules = JSON.stringify(this.rules);
            this.recreateId = GetQueryString("recreateId");
            this.getLotteryMethods();
            if ((navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i))) {
               this.isPhone=true;
            }
            this.getuuid();
        },
        getuuid(){
            const that = this;
            $.ajax({
                url:HOST+'getUUid',
                type:'get',
                success:function(res){
                    if(res.code==0){
                        that.xzuuid = res.data.saveXZ;
                    }
                },
                error:function (e) {

                }
            });
        },
        getDrawInfo(){
            var that = this;
            $.ajax({
                url:HOST+'code/drawInfo',
                type:'get',
                success:function(res){
                    if(res.code==0){
                        that.setOpenStatus(res.data);
                        if(res.data.openStatus==1){
                            if(that.recreateId!=null){
                                that.recreateCodes();
                            }
                        }
                        that.initParams();
                        that.getQuickSelectSetting();
                    }
                }
            });
        },
        recreateCodes(){
            var that = this;
            $.ajax({
                url:HOST+'logsPC/getOne?logId='+that.recreateId,
                type:'get',
                success:function(res){
                    that.created = true;
                    if(res.code==0 && res.data!=null){
                        var rulesData = res.data.contentJson;
                        that.rules = JSON.parse(rulesData);
                        that.lotteryMethodId = that.rules.lotteryMethodId;
                        that.initParams();
                        that.getQuickSelectSetting();
                        setTimeout(function (){
                            that.createCodes();
                        },500)

                    }else{
                        layer.msg(res.msg);
                    }
                }
            })
        },
        getLotteryMethods(){
            var that = this;
            $.ajax({
                url:HOST+'draw/lotteryMethods?lotterType=1',
                type:'get',
                success:function(res){
                    if(res.code==0){
                        that.lotteryMethodList = res.data;
                        that.initLotteryMethodI18nName();
                        that.currLm = that.lotteryMethodList[1];
                        that.lotteryMethodId =that.currLm.id;
                        that.rules.lotteryMethodId = that.currLm.id;
                        that.ruleName = that.currLm.bettingMethod;
                        that.getDrawInfo();
                    }
                }
            })
        },
        getQuickSelectSetting(){
            var that = this;
            $.ajax({
                url:HOST+'basicsetting/quick',
                type:'get',
                success:function(res){
                    if(res.code==0){
                        that.quickSelectSetting = res.data;
                    }
                }
            })
        },
        checkCreateCondition(){
            var hasSetCondi = false;
            if(this.rules.fixCode.included==1 || this.rules.fixCode.excluded==1){
                if(this.rules.fixCode.loc1!="" || this.rules.fixCode.loc2!="" || this.rules.fixCode.loc3!="" || this.rules.fixCode.loc4!=""){
                    return true;
                }
            }
            if(this.rules.matchCodes.included==1 || this.rules.matchCodes.excluded==1){
                if(this.rules.matchCodes.loc1!="" || this.rules.matchCodes.loc2!="" || this.rules.matchCodes.loc3!="" || this.rules.matchCodes.loc4!=""){
                    return true;
                }
            }
            if(this.rules.brothers.br2.included==1 || this.rules.brothers.br2.excluded==1){
                return true;
            }
            if(this.rules.brothers.br3.included==1 || this.rules.brothers.br3.excluded==1){
                return true;
            }
            if(this.rules.brothers.br4.included==1 || this.rules.brothers.br4.excluded==1){
                return true;
            }
            if(this.rules.repeat.double1.included==1 || this.rules.repeat.double1.excluded==1){
                return true;
            }
            if(this.rules.repeat.double2.included==1 || this.rules.repeat.double2.excluded==1){
                return true;
            }
            if(this.rules.repeat.triple.included==1 || this.rules.repeat.triple.excluded==1){
                return true;
            }
            if(this.rules.repeat.doubledouble.included==1 || this.rules.repeat.doubledouble.excluded==1){
                return true;
            }
            if(this.rules.sum2.sumType==2 || this.rules.sum2.sumType==3){
                if(this.rules.sum2.sum!=""){
                    return true;
                }
            }
            if(this.rules.others.fullChange!="" || this.rules.others.excludes!="" || this.rules.others.shangJiang!=""
                || this.rules.others.locArr[0]==1 || this.rules.others.locArr[1]==1  || this.rules.others.locArr[2]==1
                || this.rules.others.locArr[3]==1  || (this.rules.others.valueRange[0]!="" || this.rules.others.valueRange[1]!="")){
                    return true;
            }
            if(this.rules.singleNum.included==1 || this.rules.singleNum.excluded==1){
                if(this.rules.singleNum.locArr[0]==1 || this.rules.singleNum.locArr[1]==1
                    || this.rules.singleNum.locArr[2]==1 || this.rules.singleNum.locArr[3]==1  ) {
                    return true;
                }
            }
            if(this.rules.doubleNum.included==1 || this.rules.doubleNum.excluded==1){
                if(this.rules.doubleNum.locArr[0]==1 || this.rules.doubleNum.locArr[1]==1
                    || this.rules.doubleNum.locArr[2]==1 || this.rules.doubleNum.locArr[3]==1  ) {
                    return true;
                }
            }
            if(this.rules.bigNum.included==1 || this.rules.bigNum.excluded==1){
                if(this.rules.bigNum.locArr[0]==1 || this.rules.bigNum.locArr[1]==1
                    || this.rules.bigNum.locArr[2]==1 || this.rules.bigNum.locArr[3]==1  ) {
                    return true;
                }
            }
            if(this.rules.smallNum.included==1 || this.rules.smallNum.excluded==1){
                if(this.rules.smallNum.locArr[0]==1 || this.rules.smallNum.locArr[1]==1
                    || this.rules.smallNum.locArr[2]==1 || this.rules.smallNum.locArr[3]==1  ) {
                    return true;
                }
            }
            if(this.rules.fushi.included==1 || this.rules.fushi.excluded==1){
                if(this.rules.fushi.contains!="" || this.rules.fushi.combines!=""){
                    return true;
                }
            }
            if(this.rules.pairs.included==1 || this.rules.pairs.excluded==1){
                if(this.rules.pairs.pair1!=""){
                    if(this.rules.pairs.pair1.length!=2){ //kuaixuan.tipTitle
                        layer.alert($i18np.prop("kuaixuan.duishi.tip"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                        return false;
                    }
                    var arr = this.rules.pairs.pair1.split("");
                    var sum = accSub(arr[0],arr[1]);
                    if(sum!=5 && sum!=-5){
                        layer.alert($i18np.prop("kuaixuan.duishi.tip"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                        return false;
                    }
                }
                if(this.rules.pairs.pair2!=""){
                    if(this.rules.pairs.pair2.length!=2){
                        layer.alert($i18np.prop("kuaixuan.duishi.tip"),
                            {title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                        return false;
                    }
                    var arr = this.rules.pairs.pair2.split("");
                    var sum = accSub(arr[0],arr[1]);
                    if(sum!=5 && sum!=-5){
                        layer.alert($i18np.prop("kuaixuan.duishi.tip"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                        return false;
                    }
                }
                if(this.rules.pairs.pair3!=""){
                    if(this.rules.pairs.pair3.length!=2){
                        layer.alert($i18np.prop("kuaixuan.duishi.tip"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                        return false;
                    }
                    var arr = this.rules.pairs.pair3.split("");
                    var sum = accSub(arr[0],arr[1]);
                    if(sum!=5 && sum!=-5){
                        layer.alert($i18np.prop("kuaixuan.duishi.tip"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                        return false;
                    }
                }
                hasSetCondi = true;
            }

            if(this.rules.hefen.included==1 || this.rules.hefen.excluded==1){
                for(var i=0;i<4;i++){
                    if(this.rules.hefen.binds[i].locArr[i]==1 ){
                        if(this.rules.hefen.binds[i].value!=""){
                            return true;
                        }
                    }
                }
            }

            if(!hasSetCondi){
                layer.alert($i18np.prop("kuaixuan.createTip"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]});
                return false;
            }
            return true;
        },

        createCodes() {
            var that = this;
            var hasSetCondi = this.checkCreateCondition();
            if(hasSetCondi){
                if(that.codesList.length>0){
                    layer.confirm($i18np.prop("kuaixuan.noCreateCondition"),
                        {title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm"),$i18np.prop("kuaixuan.printOdds.cancel")] },
                        function (index) {
                            that.doCreateCode();
                        },function (index) {
                            layer.close(index);
                        })
                }else{
                    that.doCreateCode();
                }
            }
        },
        doCreateCode(){
            this.tableDataHtml="";
            const that = this;
            layer.msg($i18np.prop("kuaixuan.chulizhong"), {
                icon: 16
                , shade: 0.3
                , time: -1
            });
            $.ajax({
                url: HOST + "code/create",
                type: 'post',
                contentType: 'application/json',
                data: JSON.stringify(that.rules),
                success: function (res) {
                    layer.closeAll();
                    that.created = true;
                    if (res.code == 0) {
                        if (res.count < 1) {
                            that.codesList =[];
                            that.codesNum='';
                            that.tableData.dataList=[];
                            //layer.alert("未找到符合条件的号码");
                            //$("#showselectnumber").html(initHtml());
                        } else {
                            that.codesList = res.data;
                            that.codesNum = res.count;
                            that.tableData.dataList = that.codesList.slice(0,(that.tableData.currpage)*that.tableData.limit);
                            that.appendTableData(that.tableData.dataList)
                            that.showLines = Math.ceil(that.tableData.dataList.length/8);
                            console.log("============showLines ==",that.showLines)
                        }
                    }
                },
                error: function () {
                    layer.closeAll();
                }
            })
            return false;
        },
        resetXuan(){
            this.clearInput();
            if(this.lotteryMethodList.length>0){
                this.lotteryMethodId = this.lotteryMethodList[1].id;
                this.rules.lotteryMethodId = this.lotteryMethodList[1].id;
                this.ruleName = this.lotteryMethodList[1].bettingMethod;
                this.initParams();
            }else{
                this.getLotteryMethods();
            }
        },
        clearInput() {
            this.codesList = [];
            this.totalMoney = 0;
            this.buyMoney = '';
            this.codesNum = 0;
            this.rules = JSON.parse(this.emptyRules);
        },
        initParams(){
            var lmId = parseInt(this.lotteryMethodId);
            if(lmId>4){
                this.ruleItemIndex = lmId-3;
                this.isXian = 1;
                this.rules.isXian=1;
                this.rules.matchCodes.included=1;
                this.rules.matchCodes.excluded=0;
                this.rules.matchCodes.isXian=1;
            }else{
                this.ruleItemIndex = lmId;
                this.isXian = 0;
                this.rules.isXian=0;
                this.rules.matchCodes.included=0;
                this.rules.matchCodes.excluded=0;
                this.rules.matchCodes.isXian=0;
            }
            this.rules.lotteryMethodId = this.lotteryMethodId;
            this.rules.count = lmId;
        },
        changeLotteryMethod(item){
            this.currLm = item;
            this.ruleName = item.bettingMethod;
            this.created = false;
            if(this.lotteryMethodId!=item.id){
                this.clearInput();
            }
            this.lotteryMethodId = item.id;
            this.initParams();

        },
        showChkResult(my,ruleTypeName,itemIdx) {
            switch(ruleTypeName){
                case 'bigNum':
                    this.rules.bigNum.locArr[itemIdx]= this.rules.bigNum.locArr[itemIdx]==1?0:1;
                    break;
                case 'smallNum':
                    this.rules.smallNum.locArr[itemIdx]=this.rules.smallNum.locArr[itemIdx]==1?0:1;
                    break;
                case 'doubleNum':
                    this.rules.doubleNum.locArr[itemIdx]=this.rules.doubleNum.locArr[itemIdx]==1?0:1;
                    break;
                case 'singleNum':
                    this.rules.singleNum.locArr[itemIdx]=this.rules.singleNum.locArr[itemIdx]==1?0:1;
                    break;
                case 'xlocArr':
                    this.rules.others.locArr[itemIdx]=this.rules.others.locArr[itemIdx]==1?0:1;
                    break;
                case 'hefenItem1':
                    this.rules.hefen.binds[0].locArr[itemIdx]=this.rules.hefen.binds[0].locArr[itemIdx]==1?0:1;
                    break;
                case 'hefenItem2':
                    this.rules.hefen.binds[1].locArr[itemIdx]=this.rules.hefen.binds[1].locArr[itemIdx]==1?0:1;
                    break;
                case 'hefenItem3':
                    this.rules.hefen.binds[2].locArr[itemIdx]=this.rules.hefen.binds[2].locArr[itemIdx]==1?0:1;
                    break;
                case 'hefenItem4':
                    this.rules.hefen.binds[3].locArr[itemIdx]=this.rules.hefen.binds[3].locArr[itemIdx]==1?0:1;
                    break;
            }
        },
        showdis(my, to,ruleTypeName,ruleName,included) {
            switch(ruleTypeName){
                case 'fushi':
                    if(included==0){
                        this.rules.fushi.excluded=this.rules.fushi.excluded==1?0:1;
                        this.rules.fushi.included=0;
                    }else if(included==1){
                        this.rules.fushi.excluded=0;
                        this.rules.fushi.included=this.rules.fushi.included==1?0:1;
                    }else{
                        this.rules.fushi.excluded=0;
                        this.rules.fushi.included=0;
                    }
                    break;
                case 'hefen':
                    if(included==0){
                        this.rules.hefen.excluded=this.rules.hefen.excluded==1?0:1;
                        this.rules.hefen.included=0;
                    }else if(included==1){
                        this.rules.hefen.excluded=0;
                        this.rules.hefen.included=this.rules.hefen.included==1?0:1;
                    }else{
                        this.rules.hefen.excluded=0;
                        this.rules.hefen.included=0;
                    }
                    break;
                case 'fixLoc':
                    this.rules.matchCodes.excluded = 0;
                    this.rules.matchCodes.included = 0;
                    this.rules.matchCodes.isXian=0;
                    if(included==0){
                        this.rules.fixCode.excluded=this.rules.fixCode.excluded==1?0:1;
                        this.rules.fixCode.included=0;
                    }else if(included==1){
                        this.rules.fixCode.excluded=0;
                        this.rules.fixCode.included=this.rules.fixCode.included==1?0:1;
                    }
                    if(this.rules.fixCode.excluded==0 && this.rules.fixCode.included==0){
                        this.rules.fixCode.loc1='';
                        this.rules.fixCode.loc2='';
                        this.rules.fixCode.loc3='';
                        this.rules.fixCode.loc4='';
                    }
                    break;
                case 'match1': //配数全转
                    this.rules.fixCode.excluded = 0;
                    this.rules.fixCode.included = 0;
                    if(included==0){
                        this.rules.matchCodes.excluded=this.rules.matchCodes.excluded==1?0:1;
                        this.rules.matchCodes.included=0;
                    }else if(included==1){
                        this.rules.matchCodes.excluded=0;
                        this.rules.matchCodes.included=this.rules.matchCodes.included==1?0:1;
                    }
                    if(this.rules.matchCodes.excluded==0 && this.rules.matchCodes.included==0){
                        this.rules.matchCodes.loc1='';
                        this.rules.matchCodes.loc2='';
                        this.rules.matchCodes.loc3='';
                        this.rules.matchCodes.loc4='';
                    }
                    this.rules.matchCodes.isXian=0;
                    break;
                case 'match2': //配数
                    if(included==0){
                        this.rules.matchCodes.excluded=this.rules.matchCodes.excluded==1?0:1;
                        this.rules.matchCodes.included=0;
                    }else if(included==1){
                        this.rules.matchCodes.excluded=0;
                        this.rules.matchCodes.included=this.rules.matchCodes.included==1?0:1;
                    }
                    if(this.rules.matchCodes.excluded==0 && this.rules.matchCodes.included==0){
                        this.rules.matchCodes.loc1='';
                        this.rules.matchCodes.loc2='';
                        this.rules.matchCodes.loc3='';
                        this.rules.matchCodes.loc4='';
                    }
                    this.rules.matchCodes.isXian=1;
                    break;
                case 'repeat':
                    this.initRepeatRule(ruleName,included,my.checked)
                    break;
                case 'xiongdi':
                    this.initXiongdiRule(ruleName,included,my.checked)
                    break;
                case 'bigNum':
                    if(included==0){
                        this.rules.bigNum.excluded=this.rules.bigNum.excluded==1?0:1;
                        this.rules.bigNum.included=0;
                    }else if(included==1){
                        this.rules.bigNum.excluded=0;
                        this.rules.bigNum.included=this.rules.bigNum.included==1?0:1;
                    }
                    if(this.rules.bigNum.excluded==0 && this.rules.bigNum.included==0){
                        this.rules.bigNum.locArr=[0,0,0,0]
                    }
                    break;
                case 'smallNum':
                    if(included==0){
                        this.rules.smallNum.excluded=this.rules.smallNum.excluded==1?0:1;
                        this.rules.smallNum.included=0;
                    }else if(included==1){
                        this.rules.smallNum.excluded=0;
                        this.rules.smallNum.included=this.rules.smallNum.included==1?0:1;
                    }
                    if(this.rules.smallNum.excluded==0 && this.rules.smallNum.included==0){
                        this.rules.smallNum.locArr=[0,0,0,0]
                    }
                    break;
                case 'doubleNum':
                    if(included==0 ){
                        this. rules.doubleNum.excluded=this.rules.doubleNum.excluded==1?0:1;
                        this. rules.doubleNum.included=0;
                    }else if(included==1){
                        this.rules.doubleNum.excluded=0;
                        this.rules.doubleNum.included=this.rules.doubleNum.included==1?0:1;
                    }
                    if(this.rules.doubleNum.excluded==0 && this. rules.doubleNum.included==0){
                        this.rules.doubleNum.locArr=[0,0,0,0]
                    }
                    break;
                case 'singleNum':
                    if(included==0){
                        this.rules.singleNum.excluded=this.rules.singleNum.excluded==1?0:1;
                        this.rules.singleNum.included=0;
                    }else if(included==1){
                        this.rules.singleNum.excluded=0;
                        this.rules.singleNum.included=this.rules.singleNum.included==1?0:1;
                    }
                    if(this.rules.singleNum.excluded==0 && this.rules.singleNum.included==0){
                        this.rules.singleNum.locArr=[0,0,0,0]
                    }
                    break;
                case 'bdwhefen':
                    if(ruleName==='sum2'){
                        this.rules.sum2.sumType=this.rules.sum2.sumType==2?0:2;
                        if(this.rules.sum2.sumType==2){
                            this.rules.sum2.included=1;
                        }else{
                            this.rules.sum2.included=0;
                            this.rules.sum2.sum='';
                        }
                    }else if(ruleName==='sum3'){
                        this.rules.sum2.sumType=this.rules.sum2.sumType==3?0:3;
                        if(this.rules.sum2.sumType==3){
                            this.rules.sum2.included=1;
                        }else{
                            this.rules.sum2.included=0;
                            this.rules.sum2.sum='';
                        }
                    }
                    break;
                case 'duishu':
                    if(included==0){
                        this.rules.pairs.excluded=this.rules.pairs.excluded==1?0:1;
                        this.rules.pairs.included=0;
                    }else if(included==1){
                        this.rules.pairs.excluded=0;
                        this.rules.pairs.included=this.rules.pairs.included==1?0:1;
                    }
                    if(this.rules.pairs.excluded==0 && this.rules.pairs.included==0){
                        this.rules.pairs.pair1="";
                        this.rules.pairs.pair2="";
                        this.rules.pairs.pair3="";
                        this.rules.pairs.pair4="";
                        this.rules.pairs.pair5="";
                    }
                    break;
            }
        },
        initRepeatRule(ruleName,included,checked){
            switch(ruleName){
                case 'repeat1':
                    if(included==0){
                        this.rules.repeat.double1.excluded=this.rules.repeat.double1.excluded==1?0:1;
                        this.rules.repeat.double1.included=0;
                    }else if(included==1){
                        this.rules.repeat.double1.excluded=0;
                        this.rules.repeat.double1.included=this.rules.repeat.double1.included==1?0:1;
                    }else{
                        this.rules.repeat.double1.excluded=0;
                        this.rules.repeat.double1.included=0;
                    }
                    break;
                case 'repeat22': //双双重
                    if(included==0){
                        this.rules.repeat.doubledouble.excluded=this.rules.repeat.doubledouble.excluded==1?0:1;
                        this.rules.repeat.doubledouble.included=0;
                    }else if(included==1){
                        this.rules.repeat.doubledouble.excluded=0;
                        this.rules.repeat.doubledouble.included=this.rules.repeat.doubledouble.included==1?0:1;
                    }else{
                        this.rules.repeat.doubledouble.excluded=0;
                        this.rules.repeat.doubledouble.included=0;
                    }
                    break;
                case 'repeat3':
                    if(included==0){
                        this.rules.repeat.triple.excluded=this.rules.repeat.triple.excluded==1?0:1;
                        this.rules.repeat.triple.included=0;
                    }else if(included==1){
                        this.rules.repeat.triple.excluded=0;
                        this.rules.repeat.triple.included=this.rules.repeat.triple.included==1?0:1;
                    }else{
                        this.rules.repeat.triple.excluded=0;
                        this.rules.repeat.triple.included=0;
                    }
                    break;
                case 'repeat4':
                    if(included==0){
                        this.rules.repeat.double2.excluded=this.rules.repeat.double2.excluded==1?0:1;
                        this.rules.repeat.double2.included=0;
                    }else if(included==1){
                        this.rules.repeat.double2.excluded=0;
                        this.rules.repeat.double2.included=this.rules.repeat.double2.included==1?0:1;
                    }else{
                        this.rules.repeat.double2.excluded=0;
                        this.rules.repeat.double2.included=0;
                    }
                    break;

            }
        },
        delSameNum(v){//删除重复的数字
             var n = v;
             var arr = n.split("");
             arr = Array.from(new Set(arr));
             v = arr.join("");
             v = v.replace(/[^0-9]/g, "");
             return v;
        },
        limitRepeatNums(t,ruleTypeName,itemIdx) {
            switch (ruleTypeName) {
                case 'sum2':
                    this.rules.sum2.sum = this.delSameNum(this.rules.sum2.sum);
                    break;
                case 'duishu':
                    this.pairCodeRuleValues(itemIdx, t.value);
                    break;
                case 'peishu':
                    this.matchRuleValues(itemIdx, t.value);
                    break;
                case 'fixCode':
                    this.fixCodeRuleValues(itemIdx, t.value);
                    break;
                case 'hefenItem1':
                    this.rules.hefen.binds[0].value = this.delSameNum(this.rules.hefen.binds[0].value);
                    break;
                case 'hefenItem2':
                    this.rules.hefen.binds[1].value = this.delSameNum(this.rules.hefen.binds[1].value);
                    break;
                case 'hefenItem3':
                    this.rules.hefen.binds[2].value = this.delSameNum(this.rules.hefen.binds[2].value);
                    break;
                case 'hefenItem4':
                    this.rules.hefen.binds[3].value = this.delSameNum(this.rules.hefen.binds[3].value);
                    break;
                case 'fushi':
                    this.rules.fushi.combines = this.delSameNum(this.rules.fushi.combines);
                    break;
                case 'quanzhuan':
                    //this.rules.others.fullChange = this.delSameNum(this.rules.others.fullChange);
                    break;
                case 'paichu':
                    //this.rules.others.excludes = this.delSameNum(this.rules.others.excludes);
                    break;
                case 'includes':
                    this.rules.fushi.contains = this.delSameNum(this.rules.others.contains);
                    break;
                case "shangjiang":
                    //this.rules.others.shangJiang = this.delSameNum(this.rules.others.shangJiang);
                    break;
                case "valueRange":
                    //this.rules.others.valueRange[itemIdx] = this.delSameNum(this.rules.others.valueRange[itemIdx]);
                    break;
             }
        },
        initXiongdiRule(ruleName,included,checked){
            switch(ruleName){
                case 'xiongdi1':
                    if(included==0){
                        this.rules.brothers.br2.excluded=this.rules.brothers.br2.excluded==1?0:1;
                        this.rules.brothers.br2.included=0;
                    }else if(included==1){
                        this.rules.brothers.br2.excluded=0;
                        this.rules.brothers.br2.included= this.rules.brothers.br2.included==1?0:1;
                    }else{
                        this.rules.brothers.br2.excluded=0;
                        this.rules.brothers.br2.included=0;
                    }
                    break;
                case 'xiongdi2':
                    if(included==0){
                        this.rules.brothers.br3.excluded=this.rules.brothers.br3.excluded==1?0:1;
                        this.rules.brothers.br3.included=0;
                    }else if(included==1){
                        this.rules.brothers.br3.excluded=0;
                        this.rules.brothers.br3.included=this.rules.brothers.br3.included==1?0:1;
                    }else{
                        this.rules.brothers.br3.excluded=0;
                        this.rules.brothers.br3.included=0;
                    }
                    break;
                case 'xiongdi3':
                    if(included==0){
                        this.rules.brothers.br4.excluded=this.rules.brothers.br4.excluded==1?0:1;
                        this.rules.brothers.br4.included=0;
                    }else if(included==1){
                        this.rules.brothers.br4.excluded=0;
                        this.rules.brothers.br4.included=this.rules.brothers.br4.included==1?0:1;
                    }else{
                        this.rules.brothers.br4.excluded=0;
                        this.rules.brothers.br4.included=0;
                    }
                    break;

            }
        },
        toBuy(){//下注
            var that = this;
            if(this.buyMoney==""){
                layer.alert($i18np.prop("kuaixuan.moneyNoInput"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]}) //请输入金额
                return;
            }
            if(this.codesNum>0){
                layer.msg($i18np.prop("kuaixuan.chulizhong"), {icon: 16,shade: 0.3 ,time:-1 });
                $.ajax({
                    url:HOST+'codes/v1/batchBuy?xzuuid='+that.xzuuid,
                    type:'post',
                    contentType:'application/json',
                    data:JSON.stringify({
                        "batchCodesList":that.codesList,
                        "lotteryMethodId":that.lotteryMethodId,
                        "isXian":that.isXian,
                        "totalMoney":"",
                        "buyMoney":that.buyMoney,
                        "rules":that.rules,
                        "codesFrom":4
                    }),
                    success:function (res) {
                        layer.closeAll();
                        if(res.code==0){
                            window.parent.toPage("kuaida");
                            window.parent.getCredit();
                            window.parent.reloadPrintTable();
                        }else{
                            that.getuuid();
                            layer.alert(res.msg,{title:$i18np.prop("kuaixuan.tipTitle"),
                                btn:[$i18np.prop("kuaixuan.printOdds.confirm")]});
                        }
                    }
                })
                return false;
            }

        },
        moneyInput(){//金额输入
            if((this.buyMoney>='0' && this.buyMoney<='9') || this.buyMoney=="."){

            }else{
                this.buyMoney = this.buyMoney.substr(0,this.buyMoney.length-1);
                if(this.buyMoney.startsWith(".")){
                    this.buyMoney = this.buyMoney.substr(1,this.buyMoney.length-1);
                }
            }
            if(this.buyMoney!="" && this.codesNum>0){
                this.totalMoney =  parseFloat(this.buyMoney)*this.codesNum;
            }else{
                this.totalMoney='';
            }
            return false;
        },
        doPreBuy(){
            layer.closeAll();
            if(this.buyMoney==""){
                layer.alert($i18np.prop("kuaixuan.moneyNoInput"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                return;
            }
            if(this.printOdds!=""){
              var max = this.lotteryMethodId=="2"?100:this.lotteryMethodId=="3"?1000:this.lotteryMethodId==4?10000:'';
              if(max!=""){
                  if(accSub(this.printOdds,max)>0){
                      const params = [{name:'XXX',value:max}]
                      layer.alert($i18np.propByPlaceholder("kuaixuan.moneyNoInput",params),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]});//"打印赔率不能大于:"+max
                      return;
                  }
              }
            }
            var that = this;
            if(this.codesNum>0){
                layer.msg($i18np.prop("kuaixuan.chulizhong"), {icon: 16,shade: 0.3,time:-1});
                $.ajax({
                    url:HOST+'prebuy/batchBuy?xzuuid='+that.xzuuid,
                    type:'post',
                    contentType:'application/json',
                    data:JSON.stringify({
                        "batchCodesList":that.codesList,
                        "lotteryMethodId":that.lotteryMethodId,
                        "isXian":that.isXian,
                        "totalMoney":"",
                        "buyMoney":that.buyMoney,
                        "rules":that.rules,
                        "codesFrom":4,
                        "printPeiRate":that.printOdds
                    }),
                    success:function (res) {
                        layer.closeAll();
                        if(res.code==0){
                            if(that.lotteryMethodId=="2"){
                                window.parent.toPage('erdPackage');
                            }else if(that.lotteryMethodId=="3"){
                                window.parent.toPage('sadPackage');
                            }else if(that.lotteryMethodId=="4"){
                                window.parent.toPage('sidPackage');
                            }
                        }else{
                            that.getuuid();
                            layer.alert(res.msg,{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]});
                        }
                    }
                })
                return false;
            }
        },
        cancelPreBuy(){
          layer.closeAll();
        },
        toPreBuy(){//录入汇总表
            if(this.buyMoney==""){
                layer.alert($i18np.prop("kuaixuan.moneyNoInput"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})//"请输入金额"
                return;
            }
            if(this.codesNum=="" || this.codesNum<1){
                layer.alert($i18np.prop("kuaixuan.noCodesSelect"),{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]})
                return;
            }
            layer.open({
                type:1,
                content:$("#print-odds-dialog"),
                area:['450px','155px'],
                title:$i18np.prop("kuaixuan.printOdds.title")
            });

        },
        toBaoPai(){
            if(this.buyMoney==""){
                layer.msg($i18np.prop("kuaixuan.moneyNoInput"))
                return;
            }
            var that = this;
            if(this.codesNum>0){
                layer.msg($i18np.prop("kuaixuan.chulizhong"), {icon: 16,shade: 0.3,time:-1});
                $.ajax({
                    url:HOST+'baopai/batchBuy?xzuuid='+that.xzuuid,
                    type:'post',
                    contentType:'application/json',
                    data:JSON.stringify({
                        "batchCodesList":that.codesList,
                        "lotteryMethodId":that.lotteryMethodId,
                        "isXian":that.isXian,
                        "totalMoney":"",
                        "buyMoney":that.buyMoney,
                        "rules":that.rules,
                        "codesFrom":4
                    }),
                    success:function (res) {
                        layer.closeAll();
                        if(res.code==0){
                            window.parent.toPage("kuaida");
                            window.parent.getCredit();
                            window.parent.reloadPrintTable();
                        }else{
                            that.getuuid();
                            layer.alert(res.msg,{title:$i18np.prop("kuaixuan.tipTitle"),btn:[$i18np.prop("kuaixuan.printOdds.confirm")]});
                        }
                    }
                })
                return false;
            }
        },
        matchRuleValues(locIdx,val){
            switch(locIdx){
                case 0:
                    this.rules.matchCodes.loc1=this.delSameNum(this.rules.matchCodes.loc1);
                    break;
                case 1:
                    this.rules.matchCodes.loc2=this.delSameNum(this.rules.matchCodes.loc2);
                    break;
                case 2:
                    this.rules.matchCodes.loc3=this.delSameNum(this.rules.matchCodes.loc3);
                    break;
                case 3:
                    this.rules.matchCodes.loc4=this.delSameNum(this.rules.matchCodes.loc4);
                    break;
            }
        },
        fixCodeRuleValues(locIdx,val){
            switch(locIdx){
                case 0:
                    this.rules.fixCode.loc1=this.delSameNum(this.rules.fixCode.loc1);
                    break;
                case 1:
                    this.rules.fixCode.loc2=this.delSameNum(this.rules.fixCode.loc2);
                    break;
                case 2:
                    this.rules.fixCode.loc3=this.delSameNum(this.rules.fixCode.loc3);
                    break;
                case 3:
                    this.rules.fixCode.loc4=this.delSameNum(this.rules.fixCode.loc4);
                    break;
            }
        },

        pairCodeRuleValues(locIdx,val){
            switch(locIdx){
                case 0:
                    this.rules.pairs.pair1=this.delSameNum(this.rules.pairs.pair1);
                    break;
                case 1:
                    this.rules.pairs.pair2=this.delSameNum(this.rules.pairs.pair2);
                    break;
                case 2:
                    this.rules.pairs.pair3=this.delSameNum(this.rules.pairs.pair3);
                    break;
                case 3:
                    this.rules.pairs.pair4=this.delSameNum(this.rules.pairs.pair4);
                    break;
                case 4:
                    this.rules.pairs.pair5=this.delSameNum(this.rules.pairs.pair5);
                    break;
            }
        }
    }
});
//vm.init();

