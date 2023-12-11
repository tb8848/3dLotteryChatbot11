const messages = {
    zh: {
        odds:"赔率",
        money:"金额",
        decode:"退码",
        all:"全部",
        oneFixed:"一定位",
        twoFixed:"二定位",
        threeFixed:"三定位",
        fourFixed:"四定位",
        twoShow:"二字现",
        threeShow:"三字现",
        fourShow:"四字现",
        quickHit:"快打",
        quickSelect:"快选",
        txtImport:"txt导入",
        ed:"二定",
        summary:"汇总表",
        bulkCargo:"散货",
        package:"包牌",
        total:"合计",
        notData:"暂无数据!",
    },
    th: {
        odds:"อัตราต่อรอง",
        money:"จำนวนเงิน",
        decode:"ยกเลิกการเดิมพัน",
        all:"ทั้งหมด",
        oneFixed:"ตำแหน่งแน่นอนที่หนึ่ง",
        twoFixed:"ตำแหน่งแน่นอนที่สอง",
        threeFixed:"ตำแหน่งแน่นอนที่สาม",
        fourFixed:"ตำแหน่งแน่นอนที่สี่",
        twoShow:"เดิมพันสองตัวพิเศษ",
        threeShow:"เดิมพันสามตัวพิเศษ",
        fourShow:"สี่ตัวแสดงผล",
        quickHit:"เล่นเร็ว",
        quickSelect:"เลือกเร็ว",
        txtImport:"นำเข้าไฟล์ txt",
        ed:"เลือกตำแหน่งคงที่ 2",
        summary:"สรุปผล",
        bulkCargo:"สินค้าประเภทกระจาย",
        package:"การซื้อเดิมพันทั้งหมด",
        total:"รวม",
        notData:"ไม่มีข้อมูล!",
    }
}

function GetQueryString(name){
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var a = decodeURI(window.location.search);
    var r = a.substr(1).match(reg);//search,查询？后面的参数，并匹配正则
    if(r!=null)return  unescape(r[2]); return null;
}

var moduleName = "lanhuo";
var defaultLang = layui.data('langTab').langType;
if (defaultLang == undefined || defaultLang == null || defaultLang == "") {
    defaultLang = "zh";
    layui.data('langTab',{key:'langType',value:"zh"});
}
const i18n = new VueI18n({
    locale: defaultLang, // set locale
    messages, // set locale messages
});
var i18np=null;

initLangConfig({
    defaultLang:defaultLang,
    filePath: "../js/i18n/"+moduleName+"/",
    module:moduleName,
    base:"../js/"
})

var $ = layui.jquery;
$(function () {
    var flag = GetQueryString("userDetail")
    if (flag == 1) {
        $("#top").hide();
        $("#downLoad").hide();
    }
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

const vm = new Vue({
    el: '#mainLH',
    i18n,
    data: {},
    methods: {
        init() {
            console.log("初始化vue了")
        }
    }
})

layui.config(
    {base: '../../js/'})
    .extend({i18np: 'i18n'})
    .use(['form' , 'jquery' , 'table' , 'i18np'], function(){

        var form = layui.form;
        var table = layui.table;

        i18np = layui.i18np;
        reloadI18n({
            defaultLang:defaultLang,
            filePath: "../../js/i18n/"+moduleName+"/",
            module:moduleName,
        })
        i18np.loadProperties('subMain-'+moduleName);
        vm.init();

        var parentUserId = GetQueryString("parentId");
        table.render({
            elem: '#t',
            id: 'tt',
            totalRow: true, // 开启合计行
            url: HOST+'lanHuo/getLanHuoList',
            where: {drawStatus: "", parentUserId: parentUserId},
            limit: 40,
            cols: [
                [
                    // {title: '序号',type:'numbers',width:80 ,align: 'center'},
                    {field: 'printId',title: i18np.prop('lanhuo.table.head.printId'),align: 'center',totalRowText: i18np.prop('lanhuo.table.total'), width: 180},
                    {field: 'vipName',title: i18np.prop('lanhuo.table.head.member'),align: 'center', width: 130},
                    {field: 'createTime',title: i18np.prop('lanhuo.table.head.orderTime'),align: 'center',width: 180},
                    {field: 'buyCodes',title: i18np.prop('lanhuo.table.head.codes'),align: 'center',width: 130, templet: function (d) {
                            if (d.hasOneFlag == 1) {
                                return "<span style='font-weight: bold'>" +  d.buyCodes + "</span>" +
                                    "<span span style='color:red; font-weight: bold' i18n='lanhuo.query.xian'>"+i18n.prop("lanhuo.query.xian")+"</span>";
                            }else {
                                return "<span style='font-weight: bold'>" + d.buyCodes + "</span>";
                            }
                        }},
                    {field: 'buyMoney',title: i18np.prop('lanhuo.table.head.betAmount'),align: 'center', totalRow: true},
                    {field: 'zhanCheng',title: i18np.prop('lanhuo.table.head.zhancheng'),align: 'center'},
                    {field: 'shMoney',title: i18np.prop('lanhuo.table.head.shihuoMoney'),align: 'center'},
                    {field: 'param3',title: i18np.prop('lanhuo.table.head.peiRate'),align: 'center'},
                    {field: 'returnWater',title: i18np.prop('lanhuo.table.head.huishui'),align: 'center', totalRow: true},
                    {field: 'drawMoney',title: i18np.prop('lanhuo.table.head.winLottery'),align: 'center', totalRow: true, templet: function (d) {
                            if (d.drawStatus == 0 || d.drawStatus == null || d.drawStatus == "") {
                                return "--"
                            }else {
                                return d.drawMoney
                            }
                        }},
                    {field: 'profitLossMoney',title: i18np.prop('lanhuo.table.head.yinkui'),align: 'center', totalRow: true},
                    {field: 'peiRateUpper',title: i18np.prop('lanhuo.table.head.peiRateUpperLimit'),align: 'center'},
                    {field: 'huizongName',title: "下注说明",align: 'center', width: 220}
                ]
            ],
            page: true
            ,page: {
                curr: layui.data("t_page").index
            },
            text:{none: i18np.prop('lanhuo.table.noData')},
            done: (res, curr, count) => {
                form.render();
                layui.data("t_page", {
                    key: 'index',
                    value: curr
                });

                var backCodeList = [];
                var data = res.data;
                for (var i = 0; i < res.data.length; i++) {
                    if (data[i].backCodeFlag == 1) {
                        backCodeList.push(i);
                    }
                }
                Layui_SetDataTableRowColor("table_div", backCodeList,'#c3c3c3', 'red');
            }
        });

        function Layui_SetDataTableRowColor(DivId, RowArray, Color, fontColor) {
            try {
                var div = document.getElementById(DivId);
                if (div != null) {
                    var table_main = div.getElementsByClassName('layui-table-main');
                    if (table_main != null && table_main.length > 0) {
                        var table = table_main[0].getElementsByClassName('layui-table');
                        if (table != null && table.length > 0) {
                            var trs = table[0].querySelectorAll("tr");
                            for (var RowIndex = 0; RowIndex < RowArray.length; RowIndex++) {
                                if (trs != null && trs.length > 0) {
                                    trs[RowArray[RowIndex]].style.color = fontColor;
                                    trs[RowArray[RowIndex]].style.backgroundColor = "#DEDEBC";
                                }
                            }
                        }
                    }
                }
            } catch (e) {
                console.log(e.message);
            }
        }

        $.ajax({
            url:HOST+"company/selectListCompany" ,
            success:function(res){
                if(res.code == 0) {
                    $("#company").append("<option value='' i18n='lanhuo.query.select.all'>"+i18np.prop("lanhuo.query.select.all")+"</option>")
                    for(var i in res.data) {
                        $("#company").append("<option value='"+res.data[i].id+"'>"+res.data[i].companyName+"</option>")
                    }
                    form.render()
                }
            }
        });

        $.ajax({
            url:HOST+"draw/getDrawList68" ,
            success:function(res){
                if(res.code == 200) {
                    for(var i in res.data) {
                        $("#draw").append("<option value='"+res.data[i].drawId+"'>"+res.data[i].drawIdStr+"</option>")
                    }
                    form.render()
                }
            }
        });

        var username = "";
        var buyCodes = "";
        var xianFlag = 0;
        var lieType = 0;
        var start = "";
        var end = "";
        var lotterySettingType = "";
        var buyType = "";
        var company = "";
        var drawId = "";

        //搜索操作
        form.on('submit(search-submit)', function (data) {
            username = $("#member_account").val();
            buyCodes = $("#bet_no").val();
            var isXian = $("input[name='show_xian']").prop("checked");
            if (isXian) {
                xianFlag = 1;
            }else {
                xianFlag = 0;
            }
            lieType = $("#range_type").val();
            start = $("#range_min").val();
            end = $("#range_max").val();
            lotterySettingType = $("#category").val();
            buyType = $("#package_id").val();
            company = $("#company").val();
            drawId = $("#draw").val();
            table.reload('tt', {
                where: {"username": username,"buyCodes":buyCodes, "xianFlag": xianFlag, "lieType": lieType, "start": start, "end": end, "lotterySettingType": lotterySettingType, "buyType": buyType, "companyId": company, "drawId": drawId}
                , page: {
                    curr: 1
                }
            });
            return false;
        });

        $("#company").change(function() {
            username = $("#member_account").val();
            buyCodes = $("#bet_no").val();
            var isXian = $("input[name='show_xian']").prop("checked");
            if (isXian) {
                xianFlag = 1;
            }else {
                xianFlag = 0;
            }
            lieType = $("#range_type").val();
            start = $("#range_min").val();
            end = $("#range_max").val();
            lotterySettingType = $("#category").val();
            buyType = $("#package_id").val();
            company = $("#company").val();
            drawId = $("#draw").val();
            table.reload('tt', {
                where: {"username": username,"buyCodes":buyCodes, "xianFlag": xianFlag, "lieType": lieType, "start": start, "end": end, "lotterySettingType": lotterySettingType, "buyType": buyType, "companyId": company, "drawId": drawId}
                , page: {
                    curr: 1
                }
            });
        });

        $("#draw").change(function() {
            username = $("#member_account").val();
            buyCodes = $("#bet_no").val();
            var isXian = $("input[name='show_xian']").prop("checked");
            if (isXian) {
                xianFlag = 1;
            }else {
                xianFlag = 0;
            }
            lieType = $("#range_type").val();
            start = $("#range_min").val();
            end = $("#range_max").val();
            lotterySettingType = $("#category").val();
            buyType = $("#package_id").val();
            company = $("#company").val();
            drawId = $("#draw").val();
            table.reload('tt', {
                where: {"username": username,"buyCodes":buyCodes, "xianFlag": xianFlag, "lieType": lieType, "start": start, "end": end, "lotterySettingType": lotterySettingType, "buyType": buyType, "companyId": company, "drawId": drawId}
                , page: {
                    curr: 1
                }
            });
        });

        $("#btnPrint").on('click', function () {
            var d = $("#draw").val();
            window.open('printZonghuo.html?drawId=' + d);
        })
    });