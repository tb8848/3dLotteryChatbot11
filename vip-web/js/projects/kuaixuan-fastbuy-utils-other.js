
/**
 * 号码拆分（适用于两数和）
 * @param code
 * @return List<String>
 */
function splitCodes2(code){
    var tmpList = [];
    var codeArr = code.split("");
    var len = codeArr.length;
    for(var i=0;i<len-1;i++){
        for(var j=i+1;j<len;j++){
            tmpList.push(codeArr[i]+codeArr[j]);
        }
    }
    return tmpList;
}

/**
 * 号码拆分（适用于三数和）
 * @param code
 * @return List<String>
 */
function  splitCodes3(code){
    var tmpList = [];
    var codeArr = code.split("");
    var len = codeArr.length;
    for(var i=0;i<len-2;i++){
        for(var j=i+1;j<len-1;j++){
            for(var k=j+1;k<len;k++){
                tmpList.push(codeArr[i]+codeArr[j]+codeArr[k]);
            }
        }
    }
    return tmpList;
}

/**
 * 两数和计算
 * @param code，号码
 * @param resultStr 结果集合
 * @return boolean
 */
function sum2(code,resultStr){
    var len = code.length;
    if(len<2){
        return false;
    }
    var tmpList = splitCodes2(code);
    for(var i=0;i<tmpList.length;i++){
        var one = tmpList[i];
        var aa = one.split("");
        var num1 = parseInt(aa[0]);
        var num2 = parseInt(aa[1]);
        var result = (num1 + num2)%10;
        if(resultStr.indexOf(result+"")>-1){
            return true;
        }
    }
    return false;
}



/**
 * 三数和计算
 * @param code，号码
 * @param resultStr 结果集合
 * @return
 */
function sum3(code,resultStr){
    var len = code.length;
    if(len<3){
        return false;
    }
    var tmpList = splitCodes3(code);
    for(var i=0;i<tmpList.length;i++){
        var one = tmpList[i];
        var aa = one.split("");
        var num1 = parseInt(aa[0]);
        var num2 = parseInt(aa[1]);
        var num3 = parseInt(aa[2]);
        var result = (num1 + num2+num3)%10;
        if(resultStr.indexOf(result+"")>-1){
            return true;
        }
    }
    return false;
}


/**
 * 乘号位置判断
 * @param code，号码
 * @param locArr 结果集合
 * @return boolean
 */
function checkXLocation(code, locArr){
    if(code.length!=locArr.length){
        return false;
    }
    var aa = code.split("");
    for(var i=0,len=locArr.length;i<len;i++){
        if(locArr[i]==1 & aa[i]!=("X")){
            return false;
        }
    }
    return true;
}



/**
 * 生成复式号码
 * @param valueStr，号码范围
 * @param len 号码长度
 * @return Set<String> 复式号码集合
 */
function fushiCodes(valueStr,len){

    var codes = [];
    var arr = valueStr.split("");
    //去除重复的数字
    arr.forEach((item,idx)=>{
        if(!codes.includes(item)){
        codes.push(item);
    }
})

    var holderNums = [];
    var nums = [];
    var holders = [];
    var formatStr = "";
    var sb = "";
    //StringBuilder sb = new StringBuilder();
    var valueArr = codes;
    for(var i=0;i<len;i++) {
        holders[i] = "-";
    }
    for(var i=0;i<len;i++){
        if(i==0){
            for(var j=0,l1=valueArr.length;j<l1;j++){
                var h = holders;
                h[i]=valueArr[j]+"";
                var ncode = h.join("");
                if(!holderNums.includes(ncode)){
                    holderNums.push(ncode);
                }
                if(!nums.includes(ncode)){
                    nums.push(ncode);
                }
            }
        }else{
            for(var k=0,l2=holderNums.length;k<l2;k++){
                var nu = holderNums[k];
                if(nu.indexOf("-")>-1){
                    var loc = nu.indexOf("-");
                    for(var j=0,l1=valueArr.length;j<l1;j++){
                        sb = sb+nu;
                        var sbArr = sb.split("");
                        sbArr[loc]=valueArr[j]+"";
                        var ncode = sbArr.join("");
                        //holderNums.add(ncode);
                        if(!nums.includes(ncode)){
                            nums.push(ncode);
                        }
                        sb = "";
                    }
                }
            }
            nums.forEach((item,idx)=>{
                if(!holderNums.includes(item)){
                holderNums.push(item);
            }
        })
        }
    }

    var finalNums = [];
    //删除包含占位符的号码
    nums.forEach((item)=>{
        if(item.indexOf("-")<0){
        if(!finalNums.includes(item)){
            finalNums.push(item);
        }
    }
})
    nums = null;
    holderNums = null;
    console.log("=======复式号码=",finalNums);
    return finalNums;
}


/**
 * 全转号码
 * 从复式号码中删除重数号码
 * @param valueStr，号码范围
 * @param len 号码长度
 * @return Set<String> 复式号码集合
 */
function fullRotateCodes(valueStr,len){
    return fullRotate(valueStr,len);
}




/**
 * 全转号码
 * 从复式号码中删除重数号码
 * @param valueStr，号码范围
 * @param len 号码长度
 * @return Set<String> 复式号码集合
 */
function fullRotate(valueStr,len){
    var result = [];
    if(valueStr.length<len){
        return result;
    }
    return shangJiangCodes(valueStr,len);
}



/**
 * 创建指定长度的号码,不含任何定位符号
 * @param count
 * @param maxInt
 * @return
 */
function createCodes(count){
    var nums = [];
    if(count==1){
        for(var i=0;i<10;i++){
            nums.push(i+"");
        }
        return nums;
    }
    if(count==2){
        for(var i=0;i<10;i++){
            for(var j=0;j<10;j++){
                nums.push(i+""+j);
            }
        }
        return nums;
    }
    if(count==3){
        for(var i=0;i<10;i++){
            for(var j=0;j<10;j++){
                for(var k=0;k<10;k++) {
                    nums.push(i + "" + j+""+k);
                }
            }
        }
        return nums;
    }
    return nums;
}





/**
 * 上奖号码
 * @param valueStr，号码范围
 * @param len 号码长度
 * @return Set<String> 号码集合
 */
function shangJiangCodes(valueStr,len){
    var finalSet = [];
    var arr = valueStr.split("");
    var vlen = arr.length;
    if(vlen<len){
        var tmpSet = createCodes(len);
        var result = [];
        for(var i=0;i<tmpSet.length;i++) {
            var item = tmpSet[i];
            var allMatch = true;
            for (var j = 0; j < arr.length; j++) {
                var cc = arr[j];
                if (item.indexOf(cc)<0){
                    allMatch = false;
                    break;
                }
            }
            if(allMatch){
               !result.includes(item) && result.push(item);
            }
        }
        return result;
    }else{
        switch(len){
            case 2:
                for(var i=0;i<vlen;i++){
                for(var j=0;j<vlen;j++){
                    if(i==j){
                        continue;
                    }
                    var num = arr[i]+arr[j];
                    finalSet.push(num);
                }
            }
                break;
            case 3:
                for(var i=0;i<vlen;i++){
                for(var j=0;j<vlen;j++){
                    for(var k=0;k<vlen;k++){
                        if(i==j || j==k || i==k){
                            continue;
                        }
                        var num = arr[i]+arr[j]+arr[k];
                        finalSet.push(num);
                    }
                }
            }
                break;

        }
    }
    return finalSet;
}



/**
 * 排除号码，利用包含逻辑进行过滤
 * @oriCodesSet 原号码集合
 * @filterCodesSet 待排除的号码集合
 * @return Set<String>
 */
function excludeCodes(oriCodesSet,filterCodesSet){
    var finalSet = [];
    for(var i=0;i<oriCodesSet.length;i++){
        var oriCode = oriCodesSet[i];
        var needFilter = false;
        for(var j=0;j<filterCodesSet.length;j++){
            var fcodes = filterCodesSet[j];
            if(oriCode.includes(fcodes)){
                needFilter = true;
                break;
            }
        }
        if(!needFilter){
            finalSet.push(oriCode);
        }
    }
    return finalSet;
}

/**
 * 排除号码，利用包含逻辑进行过滤
 * @oriCodesSet 原号码集合
 * @filterCodesSet 待排除的号码集合
 * @return
 */
function excludeCodes1(oriCodesSet,filterCodesStr){
    var filterCodesSet = filterCodesStr.split("");
    return excludeCodes(oriCodesSet,filterCodesSet);
}

/**
 * 包含号码，利用包含逻辑进行过滤
 * @oriCodesSet 原号码集合
 * @filterCodesSet 待包含的号码集合
 * @return
 */
function containsCodes(oriCodesSet,filterCodesSet,isInclude){
    var finalSet =[];
    for(var i=0;i<oriCodesSet.length;i++){
        var needFilter = false;
        var oriCode = oriCodesSet[i];
        for(var j=0;j<filterCodesSet.length;j++){
            var fcodes = filterCodesSet[j];
            if(oriCode.indexOf(fcodes)>-1){
                needFilter = true;
                break;
            }
        }
        if(needFilter){
            finalSet.push(oriCode);
        }
    }
    if(isInclude){
        return finalSet
    }else{ //除
        var result = [];
        oriCodesSet.forEach((cc,idx)=>{
            if(!finalSet.includes(cc)){
              !result.includes(cc) && result.push(cc)
        }
        });
        return result;
    }
    return finalSet;

}


/**
 * 包含号码，利用包含逻辑进行过滤
 * @oriCodesSet 原号码集合
 * @filterCodesSet 待包含的号码集合
 * @return
 */
function getCodesByContain(oriCodesSet,containCodes){
    var finalSet = [];
    for(var i=0;i<oriCodesSet.length;i++){
        var oriCode = oriCodesSet[i];
        var needFilter = true;
        for(var j=0;j<containCodes.length;j++) {
            var it = containCodes[j];
            if (oriCode.indexOf(it) < 0) {
                needFilter = false;
                break;
            }
        }
        if(needFilter){
            finalSet.push(oriCode);
        }
    }
    return finalSet;
}


/**
 * 包含号码，利用包含逻辑进行过滤
 * @oriCodesSet 原号码集合
 * @filterStr 待包含的号码字符串
 * @return
 */
function containsCodes1(oriCodesSet,filterStr){
    var filterCodesSet = filterStr.split("");
    return containsCodes(oriCodesSet,filterCodesSet);
}

