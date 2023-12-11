const HOST="https://zz.3d11aa.com:9091/";
//const HOST='http://45.195.145.20:9091/'
//const HOST='http://127.0.0.1:9091/';

//const WS_HOST = 'http://127.0.0.1:8086/ws';
const WS_HOST="https://zz.3d11aa.com:9094/ws";
//const WS_HOST = 'http://45.195.145.20:9094/ws';

function checkpwd(value){ //value：表单的值、item：表单的DOM对象
    value = value.toLowerCase();

    if (value.indexOf("qwe") > -1 || value.indexOf("asd") > -1
        || value.indexOf("zxc") > -1 || value.indexOf("qaz") > -1 || value.indexOf("wsx") > -1) {
        return "不能包含键盘常用字串qwe、asd、zxc、qaz、wsx";
    }

    if (!new RegExp("^[a-zA-Z0-9]{8,16}").test(value)) {
        return '必须是8-16位的大小写字母及数字组合';
    }

    if (/([0-9])\1{2}/.test(value)) {
        return '不可相连3位以上相同数字';
    }
    if (/([a-zA-Z])\1{2}/.test(value)) {
        return '不可相连3位以上相同字母';
    }
    if (checkKeyboardContinuousChar(value)) {
        return '不可相连3位以上连续数字或字母';
    }

    var uname = localStorage.getItem("uname");
    if (value == uname) {
        return '密码不可包含帐号'
    }
    var prefix = uname.substr(0, 2);
    if (value.indexOf(prefix) == 0) {
        return '密码开头2位不能和帐号相同'
    }
    return null;
}


/**
 * 判断字符串是否键盘三连（横着、竖着）
 * @param {String} str
 * @returns boolean 是否满足键盘3连键
 */
function checkKeyboardContinuousChar(str) {
    const c1 = [
        ['!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+'],
        ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '{', '}', '|'],
        ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ':', '"'],
        ['z', 'x', 'c', 'v', 'b', 'n', 'm', '<', '>', '?']
    ]
    const c2 = [
        ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '='],
        ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '[', ']', '\\'],
        ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';', '\''],
        ['z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/']
    ]
    str = str.toLowerCase().split('')
    // 获取坐标位置
    const y = []
    const x = []
    for (let c = 0; c < str.length; c++) {
        y[c] = 0// 当做~`键处理
        x[c] = -1
        for (let i = 0; i < c1.length; i++) {
            for (let j = 0; j < c1[i].length; j++) {
                if (str[c] == c1[i][j]) {
                    y[c] = i
                    x[c] = j
                }
            }
        }
        if (x[c] != -1) continue
        for (let i = 0; i < c2.length; i++) {
            for (let j = 0; j < c2[i].length; j++) {
                if (str[c] == c2[i][j]) {
                    y[c] = i
                    x[c] = j
                }
            }
        }
    }
    // 匹配坐标连线
    for (let c = 1; c < str.length - 1; c++) {
        // 横着同一行
        if (y[c - 1] == y[c] && y[c] == y[c + 1]) {
            // 从左往右或者从右往左一排
            if ((x[c - 1] + 1 == x[c] && x[c] + 1 == x[c + 1]) || (x[c + 1] + 1 == x[c] && x[c] + 1 == x[c - 1])) {
                return true
            }
        }
        // 竖着同一列
        if (x[c - 1] == x[c] && x[c] == x[c + 1]) {
            // 从下往上或者从下往下同一列
            if ((y[c - 1] + 1 == y[c] && y[c] + 1 == y[c + 1]) || (y[c + 1] + 1 == y[c] && y[c] + 1 == y[c - 1])) {
                return true
            }
        }
        // 竖着同一列（类似/而不是\的一列）
        if ((x[c - 1] + 1 == x[c] && x[c] + 1 == x[c + 1]) || (x[c - 1] - 1 == x[c] && x[c] - 1 == x[c + 1])) {
            // 从下往上或者从下往下同一列
            if ((y[c - 1] + 1 == y[c] && y[c] + 1 == y[c + 1]) || (y[c + 1] + 1 == y[c] && y[c] + 1 == y[c - 1])) {
                return true
            }
        }
    }
    return false
}



//获取url参数
function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var a = decodeURI(window.location.search);
    var r = a.substr(1).match(reg);//search,查询？后面的参数，并匹配正则
    if (r != null) return unescape(r[2]);
    return null;
}


function accSub(arg1, arg2){
    var r1, r2, m, n
    try {
        r1 = arg1.toString().split('.')[1].length
    } catch (e) {
        r1 = 0
    }
    try {
        r2 = arg2.toString().split('.')[1].length
    } catch (e) {
        r2 = 0
    }
    m = Math.pow(10, Math.max(r1, r2)) // last modify by deeka //动态控制精度长度
    n = r1 >= r2 ? r1 : r2
    return ((arg1 * m - arg2 * m) / m).toFixed(n)
}

var $ = layui.$;
$(function () {
    $("div.fl").find("a").eq(0).on('click',function (f) {
        const url = window.sessionStorage.getItem("homeUrl")
        $(this).attr("href", url+"/home.html");
    })
})