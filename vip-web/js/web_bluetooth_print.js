
function fillSpace(loc, content, totalLen) {//填充空格
    var clen = getStrLen(content);
    var newContent = "";
    if (totalLen > clen) {
        newContent = content;
        for (var i = 0; i < (totalLen - clen); i++) {
            if (loc == 0) {//左填充空格
                newContent = " " + newContent;
            } else if (loc == 1) {//右边填充空格
                newContent = newContent + " ";
            }
        }
        return newContent;
    } else {
        return content.substr(0, totalLen + 1);
    }
}

function getStrLen(str){
    var realLength = 0, len = str.length, charCode = -1;
    for (var i = 0; i < len; i++) {
        charCode = str.charCodeAt(i);
        if(defaultLang=='tai'){
            realLength += 1;
        }else{
            if (charCode >= 0 && charCode <= 128)
                realLength += 1;
            else
                realLength += 2;
        }
    }
    return realLength;
}


function str2utf8(text) {
    const code = encodeURIComponent(text);
    const bytes = [];
    for (var i = 0; i < code.length; i++) {
        const c = code.charAt(i);
        if (c === '%') {
            const hex = code.charAt(i + 1) + code.charAt(i + 2);
            const hexVal = parseInt(hex, 16);
            bytes.push(hexVal);
            i += 2;
        } else bytes.push(c.charCodeAt(0));
    }
    return bytes;
}

function getCodePrintName(lmId,splitChar,buyCodes) {
    if(splitChar==''){
        splitChar = "-";
    }
    let lmShortName = null;
    switch(lmId){
        case '1':
            lmShortName = "ZX";
            break;
        case '2':
            lmShortName = "TX";
            break;
        case '3':
            lmShortName = "Z3";
            break;
        case '4':
            lmShortName = "Z6";
            break;
        case '5':
            lmShortName = "HS";
            break;
        case '6':
            lmShortName = "1D";
            break;
        case '7':
            lmShortName = "2D";
            break;
        case '8':
            lmShortName = "BX";
            break;
        default:
            break;
    }
    if(null == lmShortName){
        return buyCodes;
    }
    return lmShortName + splitChar + buyCodes;

}


function createPrintText(lang,printInfo,uname,currpage,totalPage,drawNo,$i18np){

    if(lang == 'th'){
        var emptyLine = fillSpace(0,'',31)+"\r\n";
        var content = "";
        var printTimes = $i18np.prop("main.lotteryName");
        var headerStr = fillSpace(1,printTimes+" ส่วนที่("+printInfo.printCount+")การพิมพ์รอง",31)+"\r\n";
        content = content+headerStr;
        content = content+emptyLine;
        var printTimeLbl = $i18np.prop("main.time");
        content = content+fillSpace(1,printTimeLbl+":"+printInfo.printTime,31)+"\r\n";
        var memberLbl = $i18np.prop("main.vip");
        content = content+fillSpace(1,memberLbl+":"+uname,31)+"\r\n";
        //content = content+emptyLine;
        content = content+ fillChars("*",31)+"\r\n";
        var codeLbl = $i18np.prop("main.buyCode");
        var oddsLbl = $i18np.prop("main.odds");
        var moneyLbl = $i18np.prop("main.buyMoney");
        //content = content+this.fillSpace(1,"号码",10)+this.fillSpace(1,"赔率",10)+this.fillSpace(1,"金额",9)+"\r\n";
        content = content+printThreeData(codeLbl,oddsLbl,moneyLbl+"\n",lang)

        printInfo.dataList.forEach((item,idx)=>{
            var codes = item.buyCodes;
            var pcname = getCodePrintName(item.lotteryMethodId,"-",codes);
            var plv = item.param3;
            var bm = item.buyMoney;
            //content = content+this.fillSpace(1,codes.toUpperCase(),10)+this.fillSpace(1,plv,10)+this.fillSpace(1,bm,9)+"\r\n";
            content = content+printThreeData(pcname,plv,bm+'\n',this.printLang);
        });
        content = content+ fillChars("*",31)+"\r\n";
        var bishu = $i18np.prop("main.totalAmount")+":"+printInfo.dataList.length+ $i18np.prop("main.totalMoney")+":"+printInfo.totalMoney;
        content = content+fillSpace(1,bishu,31)+"\r\n";
        content = content+ fillChars("*",31)+"\r\n";
        var drawValidInfo = $i18np.propByPlaceholder("main.validInfo",[{"name":'XXX',"value":drawNo},{"name":'YYY',"value":3}]);
        content = content+drawValidInfo+"\r\n";
        if(totalPage>1){
            content = content+fillSpace(1,$i18np.prop("main.di")+currpage+"/"+totalPage+$i18np.prop("main.ye"),31)+"\r\n";
        }
        content = content+emptyLine;
        return content;
    }else{
        var emptyLine = fillSpace(0,'',31)+"\r\n";
        var content = "";
        var headerStr = fillSpace(1,"福彩3D 第("+printInfo.printCount+")次打印",31)+"\r\n";
        content = content+headerStr;
        content = content+emptyLine;
        content = content+fillSpace(1,"时间:"+printInfo.printTime,31)+"\r\n";
        content = content+fillSpace(1,"会员:"+uname,31)+"\r\n";
        //content = content+emptyLine;
        content = content+ fillChars("*",31)+"\r\n";
        //content = content+this.fillSpace(1,"号码",10)+this.fillSpace(1,"赔率",10)+this.fillSpace(1,"金额",9)+"\r\n";
        content = content+printThreeData("号码","赔率","金额\n",defaultLang)

        printInfo.dataList.forEach((item,idx)=>{
            var codes = item.buyCodes;
            var pcname = getCodePrintName(item.lotteryMethodId,"-",codes);
            var plv = item.param3;
            var bm = item.buyMoney;
            //content = content+this.fillSpace(1,codes.toUpperCase(),10)+this.fillSpace(1,plv,10)+this.fillSpace(1,bm,9)+"\r\n";
            content = content+printThreeData(pcname,plv,bm+'\n',this.printLang);
        });
        content = content+ fillChars("*",31)+"\r\n";
        var bishu = "笔数:"+printInfo.dataList.length+" 总金额:"+printInfo.totalMoney;
        content = content+this.fillSpace(1,bishu,31)+"\r\n";
        content = content+ fillChars("*",31)+"\r\n";
        content = content+this.fillSpace(1,"第"+drawNo+"期 3天内有效",31)+"\r\n";
        if(totalPage>1){
            content = content+this.fillSpace(1,"第"+currpage+"/"+totalPage+"页",31)+"\r\n";
        }
        content = content+emptyLine;
        return content;
    }




}


function fillChars(charCode,len) {
    var str = "";
    for (var i = 0; i < len; i++) {
        str = str + charCode;
    }
    return str;
}

function hexToBytes(hex) {
    for (var bytes = [], c = 0; c < hex.length; c += 2)
        bytes.push(parseInt(hex.substr(c, 2), 16));
    return bytes;
}


function stringToByte(str) {
    var len, c;
    len = str.length;
    var bytes = [];
    for(var i = 0; i < len; i++) {
        c = str.charCodeAt(i);
        if(c >= 0x010000 && c <= 0x10FFFF) {
            bytes.push(((c >> 18) & 0x07) | 0xF0);
            bytes.push(((c >> 12) & 0x3F) | 0x80);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if(c >= 0x000800 && c <= 0x00FFFF) {
            bytes.push(((c >> 12) & 0x0F) | 0xE0);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if(c >= 0x000080 && c <= 0x0007FF) {
            bytes.push(((c >> 6) & 0x1F) | 0xC0);
            bytes.push((c & 0x3F) | 0x80);
        } else {
            bytes.push(c & 0xFF);
        }
    }
    return new Int8Array(bytes);
}




/*
	content:打印内容
*/
/**

 * 打印纸一行最大的字节

 */

const LINE_BYTE_SIZE = 32;

/**

 * 打印三列时，中间一列的中心线距离打印纸左侧的距离

 */

const LEFT_LENGTH = 10;

const MID_LENGTH = 10;

/**

 * 打印三列时，中间一列的中心线距离打印纸右侧的距离

 */

const RIGHT_LENGTH = 10;

const MAX_LINE_LENGTH = 30

/**

 * 打印三列时，第一列汉字最多显示几个文字

 */

const LEFT_TEXT_MAX_LENGTH = 7;

/**
 * 打印三列
 *
 * @param leftText 左侧文字
 * @param middleText 中间文字
 * @param rightText 右侧文字
 * @return
 */

function printThreeData(leftText, middleText, rightText,lang) {
    var sb = "";
    // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
    if (leftText.length > LEFT_TEXT_MAX_LENGTH) {
        leftText = leftText.substr(0, LEFT_TEXT_MAX_LENGTH);
    }
    const leftTextLength = getStrLen(leftText,lang);
    const middleTextLength = getStrLen(middleText,lang);
    const rightTextLength = getStrLen(rightText,lang);
    sb = sb + leftText;
    const leftSpace = LEFT_LENGTH - leftTextLength;
    for (var i = 0; i < leftSpace; i++) {
        sb+=" ";
    }
    sb+=middleText;
    const midSpace = MID_LENGTH-middleTextLength;
    for (var i = 0; i < midSpace; i++) {
        sb+=" ";
    }
    const rightSpace = RIGHT_LENGTH - rightTextLength;
    for (var i = 0; i < rightSpace; i++) {
        sb+=" ";
    }
    sb+=rightText
    return sb;
}


function printOneData(leftText,lang) {
    var sb = "";
    // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
    if (leftText.length >MAX_LINE_LENGTH) {
        leftText = leftText.substr(0, MAX_LINE_LENGTH);
    }
    const leftTextLength = getStrLen(leftText,lang);
    sb = sb + leftText;
    const leftSpace = MAX_LINE_LENGTH - leftTextLength;
    for (var i = 0; i < leftSpace; i++) {
        sb+=" ";
    }
    return sb;
}


let table

function initGbkTable() {
    const ranges = [
        [0xA1, 0xA9,  0xA1, 0xFE],
        [0xB0, 0xF7,  0xA1, 0xFE],
        [0x81, 0xA0,  0x40, 0xFE],
        [0xAA, 0xFE,  0x40, 0xA0],
        [0xA8, 0xA9,  0x40, 0xA0],
        [0xAA, 0xAF,  0xA1, 0xFE],
        [0xF8, 0xFE,  0xA1, 0xFE],
        [0xA1, 0xA7,  0x40, 0xA0],
    ]
    const codes = new Uint16Array(23940)
    let i = 0

    for (const [b1Begin, b1End, b2Begin, b2End] of ranges) {
        for (let b2 = b2Begin; b2 <= b2End; b2++) {
            if (b2 !== 0x7F) {
                for (let b1 = b1Begin; b1 <= b1End; b1++) {
                    codes[i++] = b2 << 8 | b1
                }
            }
        }
    }
    table = new Uint16Array(65536)
    table.fill(0xFFFF)

    const str = new TextDecoder('gbk').decode(codes)
    for (let i = 0; i < str.length; i++) {
        table[str.charCodeAt(i)] = codes[i]
    }
}

const defaultOnAlloc = (len) => new Uint8Array(len)

const defaultOnError = () => 63   // '?'


function str2gbk(str, opt = {}) {
    if (!table) {
        initGbkTable()
    }
    const onAlloc = opt.onAlloc || defaultOnAlloc
    const onError = opt.onError || defaultOnError

    const buf = onAlloc(str.length * 2)
    let n = 0

    for (let i = 0; i < str.length; i++) {
        const code = str.charCodeAt(i)
        if (code < 0x80) {
            buf[n++] = code
            continue
        }
        const gbk = table[code]

        if (gbk !== 0xFFFF) {
            buf[n++] = gbk
            buf[n++] = gbk >> 8
        } else if (code === 8364) {
            // 8364 == '€'.charCodeAt(0)
            // Code Page 936 has a single-byte euro sign at 0x80
            buf[n++] = 0x80
        } else {
            const ret = onError(i, str)
            if (ret === -1) {
                break
            }
            if (ret > 0xFF) {
                buf[n++] = ret
                buf[n++] = ret >> 8
            } else {
                buf[n++] = ret
            }
        }
    }
    return buf.subarray(0, n)
}
