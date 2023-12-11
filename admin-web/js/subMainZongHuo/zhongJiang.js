function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var a = decodeURI(window.location.search);
    var r = a.substr(1).match(reg);//search,查询？后面的参数，并匹配正则
    if (r != null) return unescape(r[2]);
    return null;
}

const messages = {
    zh: {
        select: {
            all: '全部',
            oneFixed:'一定位',
            twoFixed:'二定位',
            threeFixed:'三定位',
            fourFixed:'四定位',
            twoShow:'二字现',
            threeShow:'三字现',
            fourShow:'四字现',
            quickHit:'快打',
            quickSelect:'快选',
            txtImport:'txt导入',
            ed:'二定',
            summary:'汇总表',
            bulkCargo:'散货',
            baoPai:'包牌',
            peiRate:'赔率',
            amount:'金额',
            backCode:'退码',
            xian:"现",
            tui:"退",
            total:"合计",
            noData:"暂无数据!",
        }
    },
    th: {
        select: {
            all: 'ทั้งหมด',
            oneFixed:'เลือกตำแหน่งคงที่ 1',
            twoFixed:'เลือกตำแหน่งคงที่ 2',
            threeFixed:'เลือกตำแหน่งคงที่ 3',
            fourFixed:'เลือกตำแหน่งคงที่ 4',
            twoShow:'เลือก 2 ตัว',
            threeShow:'เลือก 3 ตัว',
            fourShow:'เลือก 4 ตัว',
            quickHit:'เล่นเร็ว',
            quickSelect:'เลือกเร็ว',
            txtImport:'นำเข้าไฟล์ txt',
            ed:'เลือกตำแหน่งคงที่ 2',
            summary:'สรุปผล',
            bulkCargo:'สินค้าประเภทกระจาย',
            baoPai:'สินค้าประเภทผู้เล่นจ่ายเงิน',
            peiRate:'อัตราการจ่ายเงินรางวัล',
            amount:'จำนวนเงิน',
            backCode:'การยกเลิกหมายเลข',
            xian:"เอ็กซ์เบอร์",
            tui:"คืน",
            total:"รวม",
            noData:"ไม่มีข้อมูล!",
        }
    }
}

var moduleName = "zongHuo";
var defaultLang = layui.data('langTab').langType;
if (defaultLang == undefined || defaultLang == null || defaultLang == "") {
    defaultLang = "zh";
    layui.data('langTab',{key:'langType',value:"zh"});
}
const i18n = new VueI18n({
    locale: defaultLang, // set locale
    messages, // set locale messages
});
var i18np=null
layui.config(
    {base: '../../js/'})
    .extend({i18np: 'i18n'})
    .use([ 'i18np'], function () {
        i18np = layui.i18np;
        reloadI18n({
            defaultLang:defaultLang,
            filePath: "../../js/i18n/"+moduleName+"/",
            module:moduleName,
        })
        i18np.loadProperties('subMain-'+moduleName);
        vm.initData(1);
    })

initLangConfig({
    defaultLang:defaultLang,
    filePath: "../../js/i18n/"+moduleName+"/",
    module:moduleName,
    base:"../../js/"
})

function changeLang(lang){
    reloadI18n({
        defaultLang:lang,
        filePath: "../../js/i18n/"+moduleName+"/",
        module:moduleName,
    })
    i18np.loadProperties('subMain-'+moduleName);
    i18n.locale=lang;
}

var $ = layui.jquery;
$(function () {
    var flag = GetQueryString("userDetail")
    if (flag == 1) {
        $("#top").hide();
        $("#downLoad").hide();
        $("#btnPrint").hide();
    }
})

function popChild(tag) {
    layer.open({
        type: 2,
        title: '出货',
        content: 'kuaida.html',
        area: ['80%', '650px']
    })
}

const vm = new Vue({
    el: '#mainzj',
    i18n,
    data: {
        dataList: [],
        backList: [],
        count: 0,        //总条数
        countPage: 1,    //总页数
        currentPage: 1,  //当前页数
        inputPage: '',   //文本框输入页数
        total: "",
        totalAmount: "",
        totalDrawAmount: "",
        ssXx: "",
        totalReturnWater: "",
        username: "",
        buyCodes: "",
        xianFlag: 0,
        lieType: 0,
        start: "",
        end: "",
        lotterySettingType: "",
        buyType: "",
        drawId: "",
        selectAllCheck: false,
        parentUserId: "",
    },
    methods: {
        initData(page) {
            this.drawId = GetQueryString("drawId");
            this.parentUserId = GetQueryString("parentId");
            $.ajax({
                url: HOST + "draw/getDrawList68",
                success: function (res) {
                    if (res.code == 200) {
                        for (var i in res.data) {
                            $("#draw").append("<option value='" + res.data[i].drawId + "'>" + res.data[i].drawIdStr + "</option>")
                        }
                    }
                }
            });
            $.ajax({
                url: HOST + "company/selectListCompany",
                success: function (res) {
                    if (res.code == 0) {
                        $("#company").append("<option value=''>全部</option>")
                        for (var i in res.data) {
                            $("#company").append("<option value='" + res.data[i].id + "'>" + res.data[i].companyName + "</option>")
                        }
                    }
                }
            });
            this.initDataList(page);
        },
        initDataList(page) {
            const _this = this;
            $.ajax({
                url: HOST + 'totalCargo/agentTotalCargoDetailList',
                type: "get",
                data: {
                    page: page,
                    username: _this.username,
                    buyCodes: _this.buyCodes,
                    xianFlag: _this.xianFlag,
                    lieType: _this.lieType,
                    start: _this.start,
                    end: _this.end,
                    lotterySettingType: _this.lotterySettingType,
                    type: _this.buyType,
                    drawId: _this.drawId,
                    drawStatus: "1"
                },
                success: function (res) {
                    if (res.code == 0) {
                        _this.dataList = res.data.dataList
                        _this.total = res.data.total
                        _this.totalAmount = res.data.totalAmount
                        _this.totalDrawAmount = res.data.totalDrawAmount
                        _this.ssXx = res.data.ssxx
                        _this.totalReturnWater = res.data.totalReturnWater
                        _this.count = res.count;
                        _this.currentPage = page;
                        let pageNum = res.count % 40 == 0 ? parseInt(res.count / 40) : Math.ceil(res.count / 40);
                        _this.countPage = pageNum;
                    }
                }
            });
        },
        submitQuery(page) {
            this.username = $("#member_account").val();
            this.buyCodes = $("#bet_no").val();
            const isXian = $("input[name='show_xian']").prop("checked");
            if (isXian) {
                this.xianFlag = 1;
            } else {
                this.xianFlag = 0;
            }
            this.lieType = $("#range_type").val();
            this.start = $("#range_min").val();
            this.end = $("#range_max").val();
            this.lotterySettingType = $("#category").val();
            this.buyType = $("#package_id").val();
            this.drawId = $("#draw").val();
            this.initDataList(page);
        },
        firstPage() {
            if (this.currentPage != 1) {
                if (this.select == 2) {
                    this.initData(1);
                } else {
                    this.submitQuery(1);
                }
            }
        },
        prePage() {
            if (this.currentPage != 1) {
                if (this.select == 2) {
                    this.initData(this.currentPage * 1 - 1);
                } else {
                    this.submitQuery(this.currentPage * 1 - 1);
                }
            }
        },
        nextPage() {
            if (this.currentPage * 1 < this.countPage * 1) {
                if (this.select == 2) {
                    this.initData(this.currentPage * 1 + 1);
                } else {
                    this.submitQuery(this.currentPage * 1 + 1);
                }
            }
        },
        lastPage() {
            if (this.currentPage * 1 < this.countPage * 1) {
                if (this.select == 2) {
                    this.initData(this.countPage);
                } else {
                    this.submitQuery(this.countPage);
                }
            }
        },
        go() {
            if (this.inputPage == null || this.inputPage == "" || this.inputPage == undefined) {
                layer.msg(i18np.prop('zh.page.msg.inputPageNo'));
                return false;
            }
            if (this.currentPage * 1 == this.inputPage * 1) {
                console.log("当前页不查询");
            } else if (this.inputPage * 1 < 1 || this.inputPage * 1 > this.countPage * 1) {
                if (this.select == 2) {
                    this.initData(1);
                } else {
                    this.submitQuery(1);
                }
            } else {
                if (this.select == 2) {
                    this.initData(this.inputPage);
                } else {
                    this.submitQuery(this.inputPage);
                }
            }
        },
        changeDrawId() {
            this.username = $("#member_account").val();
            this.buyCodes = $("#bet_no").val();
            const isXian = $("input[name='show_xian']").prop("checked");
            if (isXian) {
                this.xianFlag = 1;
            } else {
                this.xianFlag = 0;
            }
            this.lieType = $("#range_type").val();
            this.start = $("#range_min").val();
            this.end = $("#range_max").val();
            this.lotterySettingType = $("#category").val();
            this.buyType = $("#package_id").val();
            this.drawId = $("#draw").val();
            this.companyId = $("#company").val();
            this.initDataList(1);
        },
        changeCompany() {
            this.username = $("#member_account").val();
            this.buyCodes = $("#bet_no").val();
            const isXian = $("input[name='show_xian']").prop("checked");
            if (isXian) {
                this.xianFlag = 1;
            } else {
                this.xianFlag = 0;
            }
            this.lieType = $("#range_type").val();
            this.start = $("#range_min").val();
            this.end = $("#range_max").val();
            this.lotterySettingType = $("#category").val();
            this.buyType = $("#package_id").val();
            this.drawId = $("#draw").val();
            this.companyId = $("#company").val();
            this.initDataList(1);
        },
        print() {
            var d = $("#draw").val();
            window.open('../../zonghuo/printZhongjiang.html?drawId=' + d);
        }
    }
});
// vm.initData(1);