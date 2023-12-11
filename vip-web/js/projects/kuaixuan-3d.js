//脚本文件
//3d号码下注 通用方法定义
/**
 * 3D号码
 * @returns {[]}
 */


function create3DCodes() {
    var allCodesArr = [];
    for(var i=0;i<10;i++){
        for(var j=0;j<10;j++){
            for(var k=0;k<10;k++){
                var code = i+""+j+""+k;
                allCodesArr.push({
                    code:code,
                    sum:(i+j+k),
                    bai:i,
                    shi:j,
                    ge:k
                });
            }
        }
    }
    return allCodesArr;
}



function createCodesBy(bai,shi,ge) {
    var baiArr = bai.split("");
    var shiArr = shi.split("");
    var geArr = ge.split("");
    var allCodesArr = [];
    for(var i=0,len1=baiArr.length;i<len1;i++){
        for(var j=0,len2=shiArr.length;j<len2;j++){
            for(var k=0,len3=geArr.length;k<len3;k++){
                var code = baiArr[i]+""+shiArr[j]+""+geArr[k];
                allCodesArr.push({
                    code:code,
                    sum:(i+j+k),
                    bai:i,
                    shi:j,
                    ge:k
                });
            }
        }
    }
    return allCodesArr;
}


//组六号码
function z6CodeBy(nums) { //重号
    console.log("===============组6号码=="+nums);
    var codeList = []
    var arr = nums.split("");
    arr.sort();
    for(var i=0;i<arr.length-2;i++){
        for(var j=i+1;j<arr.length-1;j++){
            for(var k=j+1;k<arr.length;k++) {
                var code1 = arr[i] + "" + arr[j] + "" + arr[k];
                codeList.push({
                    code:code1,
                    sum:0,
                    bai:arr[i],
                    shi:arr[j],
                    ge:arr[k]
                });
            }
        }
    }
    return codeList;
}

//组三号码
function z3CodeBy(nums) { //重号
    var codeList = []
    var arr = nums.split("");
    arr.sort();
    for(var i=0;i<arr.length-1;i++){
        for(var j=i+1;j<arr.length;j++){
            var code1 = arr[i]+""+arr[i]+""+arr[j];
            codeList.push({
                code:code1,
                sum:0,
                bai:arr[i],
                shi:arr[i],
                ge:arr[j]
            });
            var code2 = arr[i]+""+arr[j]+""+arr[j];
            codeList.push({
                code:code2,
                sum:0,
                bai:arr[i],
                shi:arr[j],
                ge:arr[j]
            });
        }
    }
    return codeList;
}


//组三胆拖
function z3DtCode(dm,tm) { //胆码
    var codeList = []
    if(dm.length>0){
        var arr = tm.split("");
        arr.sort();
        for(var i=0;i<arr.length;i++){
            var narr = [dm,dm,arr[i]];
            narr.sort();
            var code1 = narr.join("");
            codeList.push({
                code:code1,
                sum:0,
                bai:narr[0],
                shi:narr[1],
                ge:narr[2]
            });
            narr = [dm,arr[i],arr[i]];
            narr.sort();
            var code2 = narr.join("");
            codeList.push({
                code:code2,
                sum:0,
                bai:narr[0],
                shi:narr[1],
                ge:narr[2]
            });
        }
    }else{
        var arr = tm.split("");
        arr.sort();
        for(var i=0;i<arr.length-1;i++){
            for(var j=i+1;j<arr.length;j++){
                var code1 = arr[i]+""+arr[i]+""+arr[j];
                var code2 = arr[i]+""+arr[j]+""+arr[j];
                codeList.push({
                    code:code1,
                    sum:0,
                    bai:arr[i],
                    shi:arr[i],
                    ge:arr[j]
                });
                codeList.push({
                    code:code2,
                    sum:0,
                    bai:arr[i],
                    shi:arr[j],
                    ge:arr[j]
                });
            }
        }
    }
    return codeList;
}


//组六胆拖
function z6DtCode(dm,tm) { //胆码
    var codeList = []
    if(dm.length==1){
        var arr = tm.split("");
        for(var i=0;i<arr.length-1;i++){
            for(var j=i+1;j<arr.length;j++) {
                var narr = [dm,arr[i],arr[j]];
                narr.sort();
                var code1 = narr.join("");
                codeList.push({
                    code:code1,
                    sum:0,
                    bai:narr[0],
                    shi:narr[1],
                    ge:narr[2]
                });
            }
        }
    }else if(dm.length==2){
        var dmArr = dm.split("");
        var arr = tm.split("");
        for(var i=0;i<arr.length;i++){
            var narr = [dmArr[0],dmArr[1],arr[i]];
            narr.sort();
            var code1 = narr.join("");
            codeList.push({
                code:code1,
                sum:0,
                bai:narr[0],
                shi:narr[1],
                ge:narr[2]
            });
        }
    }else{
        codeList = z6CodeBy(tm)
    }
    return codeList;
}

function ding1Code(bai,shi,ge) {
    var buyCodes = [];
    if(bai!=""){
        var arr = bai.split("");
        arr.forEach((cc,idx)=>{
            buyCodes.push({
                code:cc+"XX",
                sum:0,
                bai:cc,
                shi:"X",
                ge:"X"
            });
        })

    }
    if(shi!=""){
        var arr = shi.split("");
        arr.forEach((cc,idx)=>{
            buyCodes.push({
                code:"X"+cc+"X",
                sum:0,
                bai:"X",
                shi:cc,
                ge:"X"
            });
        })
    }
    if(ge!=""){
        var arr = ge.split("");
        arr.forEach((cc,idx)=>{
            buyCodes.push({
                code:"XX"+cc,
                sum:0,
                bai:"X",
                shi:"X",
                ge:cc
            });
        })
    }
    return buyCodes;
}



function ding2Code(bai,shi,ge) {
    var codeList = [];
    var codeArr = [];
    var baiArr = bai.split("");
    var shiArr = shi.split("");
    var geArr = ge.split("");
    if(baiArr.length>0 && shiArr.length>0 && geArr.length>0){
        for(var i=0;i<baiArr.length;i++){
            for(var j=0;j<shiArr.length;j++){
                for(var k=0;k<geArr.length;k++) {
                    var code1 = baiArr[i] + shiArr[j]+"X";
                    var code2 = baiArr[i] + "X"+geArr[k];
                    var code3 = "X" + shiArr[j]+geArr[k];
                    if(!codeArr.includes(code1)){
                        codeArr.push(code1);
                        codeList.push({
                            code:code1
                        });
                    }
                    if(!codeArr.includes(code2)){
                        codeArr.push(code2);
                        codeList.push({
                            code:code2
                        });
                    }
                    if(!codeArr.includes(code3)){
                        codeArr.push(code3);
                        codeList.push({
                            code:code3
                        });
                    }
                }
            }
        }
    }else if(baiArr.length>0 && shiArr.length>0){
        for(var i=0;i<baiArr.length;i++){
            for(var j=0;j<shiArr.length;j++){
                var code = baiArr[i]+shiArr[j]+"X";
                codeArr.push(code);
                codeList.push({
                    code:code
                });
            }
        }
    }else if(baiArr.length>0 && geArr.length>0){
        for(var i=0;i<baiArr.length;i++){
            for(var j=0;j<geArr.length;j++){
                var code = baiArr[i]+"X"+geArr[j];
                codeArr.push(code);
                codeList.push({
                    code:code
                });
            }
        }
    }else if(shiArr.length>0 && geArr.length>0){
        for(var i=0;i<shiArr.length;i++){
            for(var j=0;j<geArr.length;j++){
                var code = "X"+shiArr[i]+geArr[j];
                codeArr.push(code);
                codeList.push({
                    code:code
                });
            }
        }
    }
    console.log("==============codeArr==",codeArr);
    return codeList;


}


/**
 *
 * 猜2D号码，
 * 参考二字现
 * @param bai
 * @returns {[]}
 */
function createC2DCodes(bai) {
    var codeList = [];
    var codeArr = [];
    var baiArr = bai.split("");
    if(baiArr.length>0){
        for(var i=0;i<baiArr.length;i++){
            for(var j=0;j<baiArr.length;j++){
                var arr = [baiArr[i],baiArr[j]];
                arr.sort(function (n1,n2) {
                    return n1-n2;
                })
                var code = arr.join("");
                if(!codeArr.includes(code)){
                    codeArr.push(code);
                    codeList.push({
                        code:code
                    })
                }
            }
        }
    }
    return codeList;

}



function createB3Code(bai,shi,ge){
    var codeList = [];
    var baiArr = bai.split("");
    var shiArr = shi.split("");
    var geArr = ge.split("");
    if(baiArr.length>0 && shiArr.length>0 && geArr.length>0){
        baiArr.forEach((bv,i1)=>{
            shiArr.forEach((sv,i2)=>{
                geArr.forEach((gv,i3)=>{
                    if(bv==sv || sv==gv || bv==gv){
                        codeList.push({
                            code:bv+''+sv+''+gv
                        })
                    }
                })
            })
        })
    }
    return codeList;
}

function createB6Code(bai,shi,ge){
    var codeList = [];
    var baiArr = bai.split("");
    var shiArr = shi.split("");
    var geArr = ge.split("");
    if(baiArr.length>0 && shiArr.length>0 && geArr.length>0){
        baiArr.forEach((bv,i1)=>{
            shiArr.forEach((sv,i2)=>{
                geArr.forEach((gv,i3)=>{
                    if(bv!=sv && sv!=gv){
                        codeList.push({
                            code:bv+''+sv+''+gv
                        })
                    }
                })
            })
        })
    }
    return codeList;
}


function createZ6HzCode(sumArr) {
    var buyCodes = [];
    var exist = [];
    if(sumArr.length>0){
        var allCodesArr = create3DCodes();
        sumArr.forEach((hz,idx)=>{
            allCodesArr.forEach((obj,idx1)=>{
                //按三字现的号码规则，且三个号码各不相同
                var arr = [obj.bai,obj.shi,obj.ge];
                arr.sort();
                var code = arr.join("");
                if(arr[0]!=arr[1] && arr[1]!=arr[2]){
                    if(obj.sum == parseInt(hz) && !exist.includes(code)){
                        exist.push(code);
                        buyCodes.push({
                            code:code
                        });
                    }
                }
            })
        })
    }
    return buyCodes;
}


function createZ3HzCode(sumArr) {
    var buyCodes = [];
    var exist = [];
    if(sumArr.length>0){
        var allCodesArr = create3DCodes();
        sumArr.forEach((hz,idx)=>{
            for(var i=0,len=allCodesArr.length;i<len;i++){
                //按三字现的号码规则，取双重
                var obj = allCodesArr[i];
                var arr = [obj.bai,obj.shi,obj.ge];
                arr.sort();
                if(arr[0]==arr[1] && arr[1]==arr[2]){
                    continue;
                }
                var code = arr.join("");
                if(arr[0]==arr[1] || arr[1]==arr[2]) {
                    if (obj.sum == parseInt(hz) && !exist.includes(code)) {
                        exist.push(code);
                        buyCodes.push({
                            code:code
                        });

                    }
                }
            }
        })
    }
    return buyCodes;
}


function createZ3SFCode(sumArr) {
    var buyCodes = [];
    if(sumArr.length>0){
        sumArr.sort();
        var c1 = sumArr[0]+""+sumArr[0]+""+sumArr[0];
        var c2 = sumArr[0]+""+sumArr[0]+""+sumArr[1];
        var c3 = sumArr[0]+""+sumArr[1]+""+sumArr[1];
        var c4 = sumArr[1]+""+sumArr[1]+""+sumArr[1];
        buyCodes.push(c1);
        buyCodes.push(c2);
        buyCodes.push(c3);
        buyCodes.push(c4);
    }
    return buyCodes;
}

function createZ6SFCode(sumArr) {
    var buyCodes = [];
    if(sumArr.length>0){
        sumArr.sort();
        for(var i=0;i<10;i++){
            if(sumArr.includes(i)){
                continue;
            }
            var crr = [sumArr[0],sumArr[1],i];
            crr.sort();
            var c1 = crr.join("");
            buyCodes.push(c1);
        }
    }
    return buyCodes;
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
        case '13':
            lmShortName = "KD";
            break;
        case '14':
            lmShortName = "DD";
            break;
        case "100":
            lmShortName = "组三(组)";
            break;
        case "200":
            lmShortName = "组六(组)";
            break;
        case "300":
            lmShortName = "复式(组)";
            break;
        default:
            break;
    }
    if(null == lmShortName){
        return buyCodes;
    }
    return lmShortName + splitChar + buyCodes;

}



function changeDigitToChinese(dig,unit) {
    switch(dig){
        case 1:
        case "1":
            return "一"+unit;
        case 2:
        case "2":
            return "两"+unit;
        case 3:
        case "3":
            return "三"+unit;
        case 4:
        case "4":
            return "四"+unit;
        case 5:
        case "5":
            return "五"+unit;
        case 6:
        case "6":
            return "六"+unit;
        case 7:
        case "7":
            return "七"+unit;
        case 8:
        case "8":
            return "八"+unit;
        case 9:
        case "9":
            return "九"+unit;
        case 0:
        case "0":
            return "零"+unit;
        case 10:
        case "10":
            return "全包";
        default:
            return "";

    }
    return "";
}


/**
 * 删除号码中重复的数字，并返回剩余的内容
 * @param code
 */
function delRepeatNum(code){
    var arr1 = [];
    var arr = code.split("");
    for(cc in arr){
        if(!arr1.includes(arr[cc])){
            arr1.push(arr[cc])
        }
    }
    return arr1.join("");

}


