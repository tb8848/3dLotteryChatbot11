var $ = layui.jquery;
var layer = layui.layer;
var defaultLang = layui.data('langTab').langType;

const i18n = new VueI18n({
    locale: defaultLang, // set locale
    messages, // set locale messages
})


const vm = new Vue({
    el: '#main',
    i18n,
    data: {
        totalHs:0,
        totalMoney:0,
        userName:"",
        roleName:"",
        dataTypeList:[{
            value:"0",
            text:'全部'
        }],
        dataType:"0",
        drawList:[],
        buyList:[],
        userId:'',
        pid:'',
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
            colType:"1",
            isXian:0,
            minValue:'',
            maxValue:'',
            lotterySettingsId:'',
            lmId:'',
            drawNo:'',
            buyType:0,
            codeWinType:0
        },
        heji:{
            amount:0,
            money:0,
            huishui:0,
            earn:0,
            drawMoney:''
        }
    },
    methods: {
        goBack(){
            window.location.href="../userList2.html?id="+this.pid+"&isBack=1";
            //history.back();
        },
        init(detailType) {
            this.dataTypeList=[];
            this.searchParam.codeWinType = detailType;
            this.userName = window.sessionStorage.getItem("userName");
            this.roleName = window.sessionStorage.getItem("roleName");
            this.userId = GetQueryString("vipId");
            this.pid = GetQueryString("pid");
            var drawNo = GetQueryString("drawNo");
            if(null!=drawNo){
                this.searchParam.drawNo = drawNo;
            }
            this.getTypeList();
            this.getDrawList();
            this.getBuyList();
        },
        getTypeList() {
            this.dataTypeList = [];
            var _this = this;
            $.ajax({
                url: HOST + "lotteryMethod/getAll?lotteryType=1"
                , type: "GET"
                , contentType: 'application/json'
                , success: function (res) {
                    _this.dataTypeList.push({
                        value:"0",
                        text:_this.$t('message.all')
                    });
                    $.each(res.data, function (index, item) {
                        var lsList = item.lotterySettingList;
                        var betMethod = item.bettingMethod;
                        // switch(item.id){
                        //     case '1':
                        //         betMethod = _this.$t('message.yiding');
                        //         break;
                        //     case '2':
                        //         betMethod = _this.$t('message.erding');
                        //         break;
                        //     case '3':
                        //         betMethod = _this.$t('message.sanding');
                        //         break;
                        //     default:
                        //         break;
                        // }
                        if (lsList.length > 1) {
                            _this.dataTypeList.push({
                                value: item.id + "-00",
                                text: betMethod,
                                type: 0
                            })
                            //$('#dataType').append(new Option(item.bettingMethod, item.id+"-00"));// 下拉菜单里添加元素
                            $.each(item.lotterySettingList, function (idx, it1) {
                                _this.dataTypeList.push({
                                    value: it1.id + "-01",
                                    text: "--"+it1.bettingRule,
                                    type: 1
                                })
                                // $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                            });
                        } else {
                            $.each(item.lotterySettingList, function (idx, it1) {
                                var betRule = it1.bettingRule;
                                // switch(item.id){
                                //     case '4':
                                //         betRule = _this.$t('message.siding');
                                //         break;
                                //     case '5':
                                //         betRule = _this.$t('message.erxian');
                                //         break;
                                //     case '6':
                                //         betRule = _this.$t('message.sanxian');
                                //         break;
                                //     case '7':
                                //         betRule = _this.$t('message.sixian');
                                //         break;
                                // }
                                _this.dataTypeList.push({
                                    value: it1.id + "-01",
                                    text: betRule,
                                    type: 1
                                })
                                // $('#dataType').append(new Option(it1.bettingRule, it1.id+"-01"));
                            });
                        }
                    });


                    // _this.dataTypeList.push({
                    //     value: "2", text: _this.$t('message.kuaida'), type: 0
                    // })
                    // _this.dataTypeList.push({
                    //     value: "3", text: _this.$t('message.kuaixuan'), type: 0
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
            var qs = "buyCodes=" + this.searchParam.buyCodes + "&dataType=" + this.searchParam.dataType + "&colType=" + this.searchParam.colType +
                "&isXian=" + (this.searchParam.isXian?1:0) + "&minValue=" + this.searchParam.minValue + "&maxValue=" + this.searchParam.maxValue +
                "&lotterySettingsId=" + this.searchParam.lotterySettingsId + "&buyType=" + this.searchParam.buyType
                + "&page=" + this.pager.currpage + "&limit=" + this.pager.limit + "&drawNo=" + this.searchParam.drawNo
                + "&vipId=" + this.userId+"&codeWinType="+this.searchParam.codeWinType+"&lmId="+this.searchParam.lmId;
            $.ajax({
                url: HOST + "admin/drawbuy/listByPage?" + qs
                , type: "GET"
                , success: function (res) {
                    if (res.code == 0) {
                        _this.pager.pages = res.data.pages;
                        _this.pager.total = res.data.total;
                        _this.buyList = res.data.dataList;
                        _this.heji.amount = _this.buyList.length;
                        _this.heji.earn = res.data.heji.totalEarn;
                        _this.heji.money = res.data.heji.totalMoney;
                        _this.heji.huishui = res.data.heji.totalHs;
                        _this.heji.drawMoney = res.data.heji.totalDraw;

                    } else {

                    }
                    // $("#dataType").append(new Option('快打',2));
                    // $("#dataType").append(new Option('快选',3));
                    // $("#dataType").append(new Option('txt导入',5));
                    // $("#dataType").append(new Option('二定',6));
                    // $("#dataType").append(new Option('汇总表',7));
                    //layui.form.render("select");
                }
            });
        },
        toSearch() {

            var lsId = "";
            var lmId = "";
            if (this.dataType.indexOf("-") > -1) {
                var arr = this.dataType.split("-");
                if (arr[1] == '00') {
                    lmId = arr[0];
                    this.searchParam.lmId = lmId;
                    this.searchParam.dataType = "-1";
                } else if (arr[1] == "01") {
                    lsId = arr[0];
                    this.searchParam.lmId = "";
                    this.searchParam.lotterySettingsId = lsId;
                    this.searchParam.dataType = "-1";
                }
            }else{
                this.searchParam.dataType = this.dataType;
                this.searchParam.lmId = "";
                this.searchParam.lotterySettingsId = "";

            }
            this.pager.total = 0;
            this.pager.currpage = 1;
            this.getBuyList();
        },

        firstPage() {
            if (this.pager.currpage != 1) {
                this.pager.currpage = 1;
                this.getBuyList();
            }
        },
        nextPage() {
            if (this.pager.currpage < this.pager.pages) {
                this.pager.currpage++;
                this.getBuyList();
            }
        },
        prePage() {
            if (this.pager.currpage > 1) {
                this.pager.currpage--;
                this.getBuyList();
            }
        },
        lastPage() {
            if (this.pager.currpage < this.pager.pages) {
                this.pager.currpage = this.pager.pages;
                this.getBuyList();
            }
        },
        goPage() {
            if(this.pager.toPage!="") {
                const toPage = parseInt(this.pager.toPage);
                if (toPage >= 1 && toPage <= this.pager.pages && toPage != this.pager.currpage) {
                    this.pager.currpage = toPage;
                    this.getBuyList();
                }
            }
        },
        changeDrawId() {
            this.buyList=[];
            this.pager.total=0
            this.pager.currpage=1;
            this.searchParam.drawNo = $("#sel_period_no").val();
            this.getBuyList();
        },
        bpcodesDetail(item) {
            var rid = item.id;
            sessionStorage.setItem("bptitle", item.huizongName);
            sessionStorage.setItem("bprid", rid);
            sessionStorage.setItem("drawNo",this.searchParam.drawNo)
            sessionStorage.setItem("vipId", this.userId);
            window.parent.open("baopaiDetail.html");
        }
    }
})
//vm.init();
