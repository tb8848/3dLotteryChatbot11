const HOST="http://127.0.0.1:9192/bot"
// const HOST="https://vip.colourswin.com:9192/bot";
const SOCKET_HOST="http://127.0.0.1:9194/ws";
// const SOCKET_HOST="https://vip.colourswin.com:9194/ws";
//const WS_HOST="https://vip.colourswin.com:9194/ws";

//获取url参数
function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var a = decodeURI(window.location.search);
    var r = a.substr(1).match(reg);//search,查询？后面的参数，并匹配正则
    if (r != null) return unescape(r[2]);
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




function upOpenStatus(drawOpenStatus) {
    //开关盘状态监听方法
    //可以在页面上重写覆盖
    //console.log("=============================")
}

function changeLang(lang){
    //切换语言监听方法
    //可以在页面上重写覆盖
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

function accAdd(num1, num2) {
    var r1, r2, m, n
    try {
        r1 = num1.toString().split('.')[1].length
    } catch (e) {
        r1 = 0
    }
    try {
        r2 = num2.toString().split('.')[1].length
    } catch (e) {
        r2 = 0
    }
    const baseNum = Math.pow(10, Math.max(r1, r2));
    n = r1 >= r2 ? r1 : r2
    return ((num1 * baseNum + num2 * baseNum) / baseNum).toFixed(n);
}

function guid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0,
            v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}
