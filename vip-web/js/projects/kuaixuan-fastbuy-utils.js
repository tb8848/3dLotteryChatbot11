

/**
 * 直选号码
 * @return
 */
function zxCode(){
    var codeSet = [];
    for(var i=0;i<10;i++){
        for(var j=0;j<10;j++){
            for(var k=0;k<10;k++){
                var code = [i,j,k].join("");
                if(!codeSet.includes(code))
                codeSet.push(code);
            }
        }
    }
    codeSet.sort();
    return codeSet;
}

/**
 * 组三 号码
 * 按三字现规则生成号码
 * 存在两个相同的数字
 * 排除3重
 * @return
 */
function z3Code(){
    var codeSet = [];
    for(var i=0;i<10;i++){
        for(var j=0;j<10;j++){
            for(var k=0;k<10;k++){
                if(i==j && j==k){
                    continue;
                }
                var numArr = [i,j,k];
                numArr.sort();
                if(numArr[0]==numArr[1] || numArr[1]==numArr[2]){
                    var code = numArr.join("");
                    if(!codeSet.includes(code))
                    codeSet.push(code);
                }
            }
        }
    }
    codeSet.sort();
    return codeSet;
}


/**
 * 组六 号码
 * 按三字现规则生成号码
 * 三个数字各不相同
 * @return
 */
function z6Code(){
    var codeSet = [];
    for(var i=0;i<10;i++){
        for(var j=0;j<10;j++){
            for(var k=0;k<10;k++){
                var numArr = [i,j,k];
                numArr.sort();
                if(numArr[0]!=numArr[1] && numArr[1]!=numArr[2]){
                    var code = numArr.join("");
                    if(!codeSet.includes(code))
                        codeSet.push(code);
                }
            }
        }
    }
    codeSet.sort();
    return codeSet;
}


function d2Code(){
    var codeList = [];
    for(var i=0;i<10;i++){
        for(var j=0;j<10;j++){
            codeList.push([i,j].join(""));
        }
    }
    codeList.sort();
    return codeList;
}



function createCodesForDing(count){
    var codeList = [];
    if(count==1){
        for(var i=0;i<10;i++){
            codeList.push(i+"XX");
            codeList.push("X"+i+"X");
            codeList.push("XX"+i);
        }
    }else if(count==2){
        for(var i=0;i<10;i++){
            for(var j=0;j<10;j++){
                codeList.push([i,j,"X"].join(""));
                codeList.push([i,"X",j].join(""));
                codeList.push(["X",i,j].join(""));
            }
        }
    }
    codeList.sort();
    return codeList;
}



/**
 * 生成重数
 * @param len
 * @param maxInt
 * @return
 */
function createRepeatCodes(len){
    var finalNums =[];
    for(var j=0;j<10;j++){
        var values = "";
        for(var i=0;i<len;i++){
            values = values +j;
        }
        finalNums.push(values);
    }
    finalNums.sort();
    return finalNums;
}


function createPairsCodes(){
    var pairCodes = [];
    pairCodes.push("05");
    pairCodes.push("50");
    pairCodes.push("16");
    pairCodes.push("61");
    pairCodes.push("72");
    pairCodes.push("27");
    pairCodes.push("83");
    pairCodes.push("38");
    pairCodes.push("49");
    pairCodes.push("94");
    return pairCodes;
}

//
// /**
//  * 生成兄弟号码
//  * @param len
//  * @param maxInt
//  * @return
//  */
// function createBrotherCodes(len){
//     var finalNums = [];
//     var nextBrother = 0;
//     for(var j=0;j<10;j++){
//         var values = j+"";
//         nextBrother = j;
//         if(j==9){
//             for(var i=0;i<len-1;i++){
//                 values = values + i;
//             }
//         }else{
//             for(var i=1;i<len;i++){
//                 nextBrother = nextBrother+1;
//                 values = values + nextBrother;
//             }
//         }
//         var arr = values.split("");
//         values = arr.sort().join();
//         finalNums.push(values);
//     }
//     return finalNums;
// }
//



/**
 * 生成兄弟号码
 * @param len
 * @param maxInt
 * @return
 */
function createBrotherCodes(len){
    // var finalNums = [];
    // var nextBrother = 0;
    // for(var j=0;j<10;j++){
    //     var values = j+"";
    //     nextBrother = j;
    //     if(j==9){
    //         for(var i=0;i<len-1;i++){
    //             values = values + i;
    //         }
    //     }else{
    //         for(var i=1;i<len;i++){
    //             nextBrother = nextBrother+1;
    //             values = values + nextBrother;
    //         }
    //     }
    //     var arr = values.split("");
    //     values = arr.sort().join("");
    //     finalNums.push(values);
    // }

    if(len==2){
        return ["01","12","23","34","45","56","67","78","89","09"]
    }else if(len==3){
        return ["012","123","234","345","456","567","678","789","890","109"]
    }else{
        return [];
    }
    //return finalNums;
}



//定位置
function findCodeByLoc(oriNums,locIndex,locValue,isInclude){
    var finalNums = [];
    for(var i=0;i<oriNums.length;i++){
        var a = oriNums[i];
        var subStr = a.substring(locIndex,locIndex+1);
        if(isInclude){
            //取
            if(locValue.includes(subStr)){
                finalNums.push(a);
            }
        }else {
            //除
            if (!locValue.includes(subStr)) {
                finalNums.push(a);
            }
        }
    }
    return finalNums;
}


//重数
function findCodeByRepeat(oriNums,repeatsCount,isInclude){
    var oriRepeatSet = [];
    var repeatCodeSet = createRepeatCodes(repeatsCount);
    //console.log("======repeatCodeSet ==",repeatCodeSet)
    //console.log("======oriNums ==",oriNums)
    for(var i=0;i<oriNums.length;i++) {
        var a = oriNums[i];
        var arr = a.split(""); //对号码排序
        var a0 = arr.sort().join("");
        for(var j=0;j<repeatCodeSet.length;j++){
            if(a0.indexOf(repeatCodeSet[j])>-1){
                oriRepeatSet.push(a);
            }
        }
    }
    //console.log("======oriRepeatSet ==",oriRepeatSet)
    if(isInclude){
        //取
        return oriRepeatSet;
    }else{
        var set1 = [];
        for(var j=0;j<oriNums.length;j++){
            var cc = oriNums[j];
            if(!oriRepeatSet.includes(oriNums[j])){
                !set1.includes(cc) && set1.push(oriNums[j]);
            }
        }
        return set1;
    }
}


//
// function findCodeByBrother(oriNums,brotherCount,isInclude){
//     var oriBrotherSet = [];
//     var brotherCodeSet = createBrotherCodes(brotherCount);
//     for(var i=0;i<oriNums.length;i++) {
//         var a = oriNums[i]; //找出原始数据中包含兄弟的号码集合
//         var arr = a.split(""); //原始号码进行排序
//         var a0 = arr.sort().join();
//         for(var j=0;j<brotherCodeSet.length;j++){
//             if(a0.includes(brotherCodeSet[j])){
//                 !oriBrotherSet.includes(a) && oriBrotherSet.push(a);
//             }
//         }
//     }
//     brotherCodeSet = null;
//     if(isInclude){
//         //取
//         return oriBrotherSet;
//     }else{
//         var set1 = [];
//         for(var j=0;j<oriNums.length;j++){
//             var cc = oriNums[j];
//             if(!oriBrotherSet.includes(cc)){
//                 !set1.includes(cc) && set1.push(cc);
//             }
//         }
//         return set1;
//     }
// }
//


function findCodeByBrother(oriNums,brotherCount,isInclude){
    var oriBrotherSet = [];
    var brotherCodeSet = createBrotherCodes(brotherCount);
    console.log("===========",brotherCodeSet);
    for(var i=0;i<oriNums.length;i++) {
        var a = oriNums[i]; //找出原始数据中包含兄弟的号码集合
        var arr = a.split(""); //原始号码进行排序
        for(var j=0;j<brotherCodeSet.length;j++){
            var xdArr = brotherCodeSet[j].split("");
            if(brotherCount==2 && arr.includes(xdArr[0]) && arr.includes(xdArr[1])){
                !oriBrotherSet.includes(a) && oriBrotherSet.push(a);
            }else if(brotherCount==3 && arr.includes(xdArr[0]) && arr.includes(xdArr[1]) && arr.includes(xdArr[2])){
                !oriBrotherSet.includes(a) && oriBrotherSet.push(a);
            }
        }
        // var a0 = arr.sort().join("");
        // for(var j=0;j<brotherCodeSet.length;j++){
        //     if(a0.indexOf(brotherCodeSet[j])>-1){
        //         !oriBrotherSet.includes(a) && oriBrotherSet.push(a);
        //     }
        // }
    }
    brotherCodeSet = null;
    if(isInclude){
        //取
        return oriBrotherSet;
    }else{
        var set1 = [];
        for(var j=0;j<oriNums.length;j++){
            var cc = oriNums[j];
            if(!oriBrotherSet.includes(cc)){
                !set1.includes(cc) && set1.push(cc);
            }
        }
        return set1;
    }
}

//对数
function findCodeByPairs(oriNums,isInclude,targetPairs){
    var oriPairsSet = [];
    var pairsCodeSet = createPairsCodes();
    if(null!=targetPairs && targetPairs.length>0){
        pairsCodeSet = targetPairs;
    }

    for(var i=0;i<oriNums.length;i++) { //找出原始数据中包含对数的号码集合
        var a = oriNums[i];
        for(var j=0;j<pairsCodeSet.length;j++){
            var rcode = pairsCodeSet[j];
            var arr1 = rcode.split("");
            var matchResult = true;
            for(var k=0;k<arr1.length;k++){
                if(a.indexOf(arr1[k])<0){
                    matchResult = false;
                    break;
                }
            }
            if (matchResult) {
                oriPairsSet.push(a);
            }
        }
    }
    if(isInclude){
        //取
        return oriPairsSet;
    }else{

        for(var j=0;j<oriPairsSet.length;j++) {
            if(oriNums.indexOf(oriPairsSet[j])>-1){
                var loc = oriNums.indexOf(oriPairsSet[j]);
                oriNums.splice(loc, 1)
            }
        }
        oriPairsSet = null;
        return oriNums;
    }
}


/**
 * 根据大数、小数、单数、双数等规则找符合条件的号码
 * @param oriNums 号码集合
 * @param locIndex 位置,0到5之间的一个整数
 * @param isInclude true(取）,false(除)
 * @param numType 1(单数),2(双数),4(大数),3(小数)
 * @return
 */
function findCodeBy(oriNums,locIndex,isInclude,numType){
    var finalNums = [];
    for(var i=0;i<oriNums.length;i++){
        var a = oriNums[i];
        var subStr = a.substring(locIndex,locIndex+1);
        if(subStr=="X"){
            continue;
        }
        var locValue = parseInt(subStr);
        var isOk = false; //标记是否满足条件
        switch(numType){
            case 1: //单数
                isOk = locValue%2==1;
                break;
            case 2: //双数
                isOk = locValue%2==0;
                break;
            case 3: //小数
                isOk = locValue<5;
                break;
            case 4: //大数
                isOk = locValue>4;
                break;
            default:
                continue;
        }

        if(isInclude){
            //取
            if(isOk){
                finalNums.push(a);
            }
        }else{
            //除
            if(!isOk){
                finalNums.push(a);
            }
        }
    }
    return finalNums;
}



/**
 * 重数规则匹配
 * @param oriNums
 * @param repeatRule
 * @return
 */
function repeatMatch(oriNums,rulesRepeat){
    var isInclude = false;
    if(rulesRepeat.included==1){
        isInclude = true;
    }else if(rulesRepeat.excluded==1){
        isInclude = false;
    }
    if(rulesRepeat.nums>0){
        return findCodeByRepeat(oriNums,rulesRepeat.nums,isInclude);
    }
    return oriNums;
}



function brotherRuleMatch(oriNums, brotherRules){
    var isInclude = false;

    if(brotherRules.included==1){
        isInclude = true;
    }else if(brotherRules.excluded==1){
        isInclude = false;
    }
    if(brotherRules.nums>0){
        oriNums = findCodeByBrother(oriNums,brotherRules.nums,isInclude);
    }

    return oriNums;
}

// function pairsRuleMatch(oriNums, pairsRule){
//     var isInclude = false;
//
//     if(pairsRule.included==1){
//         isInclude = true;
//     }else if(pairsRule.excluded==1){
//         isInclude = false;
//     }
//     var targetPairs =[];
//
//     if(pairsRule.pair1!=null && pairsRule.pair1.length>0){
//         targetPairs.push(pairsRule.pair1);
//     }
//     if(pairsRule.pair2!=null && pairsRule.pair2.length>0){
//         targetPairs.push(pairsRule.pair2);
//     }
//     if(pairsRule.pair3!=null && pairsRule.pair3.length>0){
//         targetPairs.push(pairsRule.pair3);
//     }
//     oriNums = findCodeByPairs(oriNums,isInclude,targetPairs);
//     return oriNums;
// }


function pairsRuleMatch(oriNums, pairsRule){
    var isInclude = false;

    if(pairsRule.included==1){
        isInclude = true;
    }else if(pairsRule.excluded==1){
        isInclude = false;
    }
    var targetPairs =[];

    if((pairsRule.pair1==null || pairsRule.pair1.length==0)
        && (pairsRule.pair2==null || pairsRule.pair2.length==0) || (pairsRule.pair3==null && pairsRule.pair3.length==0)
    ){
        targetPairs.push("05");
        targetPairs.push("16");
        targetPairs.push("27");
        targetPairs.push("38");
        targetPairs.push("49");
    }else{
        if(pairsRule.pair1!=null && pairsRule.pair1.length>0){
            targetPairs.push(pairsRule.pair1);
        }
        if(pairsRule.pair2!=null && pairsRule.pair2.length>0){
            targetPairs.push(pairsRule.pair2);
        }
        if(pairsRule.pair3!=null && pairsRule.pair3.length>0){
            targetPairs.push(pairsRule.pair3);
        }
    }

    console.log("======targetParis===",targetPairs);
    oriNums = findCodeByPairs(oriNums,isInclude,targetPairs);
    return oriNums;
}


// function oneCodeRuleMatch(oriNums,locArr,isInclude,numType){
//     for(var i=0;i<locArr.length;i++){
//         if(locArr[i]>0){
//             oriNums = findCodeBy(oriNums,i,isInclude,numType);
//         }
//     }
//     return oriNums;
// }


function oneCodeRuleMatch(oriNums,locArr,isInclude,numType){
    var arrCopy = [];
    for (i = 0; i < oriNums.length; i++) {
        arrCopy[i] = oriNums[i];
    }
    for(var i=0;i<locArr.length;i++){
        if(locArr[i]>0){
            oriNums = findCodeBy(oriNums,i,true,numType);
        }
    }

    if(isInclude){
        return oriNums;
    }

    var excludesArr = [];
    for (i = 0; i < arrCopy.length; i++) {
        var code = arrCopy[i];
        if(!oriNums.includes(code)){
            !excludesArr.includes(code) && excludesArr.push(code);
        }
    }
    return excludesArr;
}

//双数
function doubleNumRuleMatch(oriNums,doubleNumRule){
    var isInclude = false;

    if(doubleNumRule.included==1){
        isInclude = true;
    }else if(doubleNumRule.excluded==1){
        isInclude = false;
    }
    return oneCodeRuleMatch(oriNums,doubleNumRule.locArr,isInclude,2);

}

//单数
function singleNumRuleMatch(oriNums,singleNumRule){
    var isInclude = false;

    if(singleNumRule.included==1){
        isInclude = true;
    }else if(singleNumRule.excluded==1){
        isInclude = false;
    }
    return oneCodeRuleMatch(oriNums,singleNumRule.locArr,isInclude,1);
}

//大数规则
function bigNumRuleMatch(oriNums,bigNumRule){
    var isInclude = false;

    if(bigNumRule.included==1){
        isInclude = true;
    }else if(bigNumRule.excluded==1){
        isInclude = false;
    }
    return oneCodeRuleMatch(oriNums,bigNumRule.locArr,isInclude,4);
}

//小数规则
function smallNumRuleMatch(oriNums,smallNumRule){
    var isInclude = false;

    if(smallNumRule.included==1){
        isInclude = true;
    }else if(smallNumRule.excluded==1){
        isInclude = false;
    }
    return oneCodeRuleMatch(oriNums,smallNumRule.locArr,isInclude,3);
}

//和数规则
function sum2RuleMatch(oriNums, sum2Rule){
    var finalSet = [];
    if(sum2Rule.sumType==2 && sum2Rule.sum!=null && sum2Rule.sum!=""){//两数和
        for(var i=0;i<oriNums.length;i++) {
            var code = oriNums[i];
            if (sum2(code, sum2Rule.sum)) {
                finalSet.push(code);
            }
        }
        return finalSet;
    }else if(sum2Rule.sumType==3 && sum2Rule.sum!=null && sum2Rule.sum!=""){//三数和
        for(var i=0;i<oriNums.length;i++) {
            var code = oriNums[i];
            if (sum3(code, sum2Rule.sum)) {
                finalSet.push(code);
            }
        }
        return finalSet;
    }
    return oriNums;
}

//全转规则
function fullRotateMatch(oriNums,othersRule,count){
    //全转
    var valueStr = othersRule.fullChange;
    var rotateCodesSet = fullRotateCodes(valueStr,count);
    //console.log(rotateCodesSet);
    var finalSet = [];
    for(var i=0;i<oriNums.length;i++){
        var oriCode = oriNums[i];
        for(var j=0;j<rotateCodesSet.length;j++){
            var fcodes = rotateCodesSet[j];
            if(oriCode.includes(fcodes)){
                finalSet.push(oriCode);
            }
        }
    }
    return finalSet;
}

//复式规则
function fushiMatch(oriNums,fushiRule,count){

    var tmpSet = null;
    var finalSet = [];
    var isInclude = fushiRule.included==1;

    var containsStr = fushiRule.contains;
    if(containsStr!=""){
        oriNums = containsCodes(oriNums,containsStr,isInclude);
    }

    var combines = fushiRule.combines;
    if(combines!=""){
        tmpSet = fushiCodes(combines,count);
    }

    if(null == tmpSet){
        return oriNums;
    }
    if(isInclude){
        var result = [];
        for(var i=0;i<oriNums.length;i++){
            var oriCode = oriNums[i];
            if(tmpSet.includes(oriCode)){
                !result.includes(oriCode) && result.push(oriCode);
            }
        }
        return result;
    }else{
        var result = [];
        for(var i=0;i<oriNums.length;i++){
            var oriCode = oriNums[i];
            if(!tmpSet.includes(oriCode)){
                !result.includes(oriCode) && result.push(oriCode);
            }
        }
        return result;
    }

}




/**
 *
 * @param rules
 * @param lotterySettingList
 * @return
 */
function createPeirateSettingCode(rules, lmId,lotteryType){

    var buyDesc = [];

    var isDing = true;
    if(lmId=="3" || lmId=="4"){
        isDing = false;
    }
    var count = 1;
    var randomCodes = null;

    var includesFushi = false;
    plvSettingList = [];
    //List<CodesVO> plvSettingList = new ArrayList<CodesVO>();
    var lotteryType=-1;
    switch(lmId){
        case "1":
        case 1:
        case 2:
        case "2":
            lotteryType=1;
            count=3;
            if(rules.codes.length>0){
                randomCodes = zxCodeBy(rules.codes);
            }else{
                randomCodes = zxCode();
            }

            break;

        case "3":
        case 3:
            lotteryType=1;
            count=3;
            if(rules.codes.length>0){
                randomCodes = z3CodeBy(rules.codes);
            }else{
                randomCodes = zxCode();
            }
            break;
        case "4":
        case 4:
            lotteryType=1;
            count=3;
            if(rules.codes.length>0){
                randomCodes = z6CodeBy(rules.codes);
            }else{
                randomCodes = zxCode();
            }
            break;
        case "6":
        case 6:
            lotteryType=1;
            count=1;
            randomCodes = createCodesForDing(1);
            break;
        case "7":
        case 7:
            lotteryType=1;
            count=2;
            randomCodes = d2Code();
            break;
    }

    console.log("====codes",randomCodes);

    var othersRule = rules.others;
    if(lmId=="1" || lmId=="3" || lmId=="4" || lmId == "7" || lmId=="2"){
        if(othersRule!=null && othersRule.fullChange!=null && othersRule.fullChange!=""){
            buyDesc.push("全转："+othersRule.fullChange)

            //全转
            randomCodes = fullRotateMatch(randomCodes,othersRule,count);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        //上奖
        if(othersRule!=null && othersRule.shangJiang!=null && othersRule.shangJiang!=""){
            buyDesc.push("上奖："+othersRule.shangJiang);
            randomCodes = shangJiangRuleMatch(randomCodes,othersRule,count);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }

        //值范围
        if(null!=othersRule && null!=othersRule.valueRange){
            var needCheck = othersRule.valueRange[0]!="" || othersRule.valueRange[1]!="";
            if(needCheck){
                var ss ="值范围：["+(othersRule.valueRange[0]!=""?othersRule.valueRange[0]:"")+","+(othersRule.valueRange[1]!=""?othersRule.valueRange[1]:"")+"]";
                buyDesc.push(ss);
                randomCodes = valueRangeRuleMatch(randomCodes,othersRule);
                if(randomCodes.length==0){
                    return plvSettingList;
                }
            }
        }

        //排除
        if(othersRule!=null && null!=othersRule.excludes && othersRule.excludes!=""){

            buyDesc.push("排除："+othersRule.excludes);
            randomCodes = excludeCodes(randomCodes,othersRule.excludes);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }

    }

    //复式
    var fushiRule = rules.fushi;
    if(null!=fushiRule && (fushiRule.included==1 || fushiRule.excluded==1) && (
        fushiRule.combines!="" || fushiRule.contains!="")){
        var qu = fushiRule.included==1?"取":"除";
        var han = fushiRule.contains!=""?"含"+fushiRule.contains:"";
        var fs = fushiRule.combines!=""?"复式"+fushiRule.combines:"";
        buyDesc.push(qu+han+fs);
        randomCodes = fushiMatch(randomCodes,fushiRule,count);
        if(randomCodes.length==0){
            return plvSettingList;
        }
    }

    //重数
    var repeatRuleComp = rules.repeat;
    if(null!=repeatRuleComp){
        if(null !=repeatRuleComp.double1 && ""!=repeatRuleComp.double1){//双重
            var rule = repeatRuleComp.double1;
            if(rule.included==1 || rule.excluded==1) {
                var qu = rule.included==1?"取":"除";
                buyDesc.push(qu+"双重");
                randomCodes = repeatMatch(randomCodes, rule);
            }
        }
        if(null !=repeatRuleComp.triple && ""!=repeatRuleComp.triple){//三重
            var rule = repeatRuleComp.triple;
            if(rule.included==1 || rule.excluded==1){
                var qu = rule.included==1?"取":"除";
                buyDesc.push(qu+"三重");
                randomCodes = repeatMatch(randomCodes, rule);
            }
        }
        if(null==randomCodes || randomCodes.length==0){
            return plvSettingList;
        }
    }

    //兄弟
    var brotherRuleComp = rules.brothers;
    if(null!=brotherRuleComp){
        if(null !=brotherRuleComp.br2){
            var rule = brotherRuleComp.br2;
            if(rule.included==1 || rule.excluded==1){
                var qu = rule.included==1?"取":"除";
                buyDesc.push(qu+"二兄弟");
                randomCodes = brotherRuleMatch(randomCodes,rule);
            }
        }
        if(null !=brotherRuleComp.br3){
            var rule = brotherRuleComp.br3;
            if(rule.included==1 || rule.excluded==1){
                var qu = rule.included==1?"取":"除";
                buyDesc.push(qu+"三兄弟");
                randomCodes = brotherRuleMatch(randomCodes,rule);
            }
        }
    }

    //对数
    var pairsRule = rules.pairs;
    if(null !=pairsRule && (pairsRule.included==1 || pairsRule.excluded==1)){
        // if(pairsRule.pair1!="" || pairsRule.pair2!=""
        //     || pairsRule.pair3!=""){
            var pair1 = pairsRule.pair1?pairsRule.pair1+",":"";
            var pair1 = pairsRule.pair1?pairsRule.pair1+",":"";
            var pair2 = pairsRule.pair2?pairsRule.pair2+",":"";
            var pair3 = pairsRule.pair3?pairsRule.pair3+",":"";
            var qu = pairsRule.included==1?"取":"除";
            buyDesc.push(qu+"对数："+pair1+pair2+pair3);
            randomCodes = pairsRuleMatch(randomCodes,pairsRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        //}
    }

    //和数
    var sum2Rule = rules.sum2;
    if(null!=sum2Rule && (sum2Rule.included==1)){
        randomCodes = sum2RuleMatch(randomCodes,sum2Rule);
        var qu = "取";
        buyDesc.push(qu+(sum2Rule.sumType==2?"两数和：":"三数和：")+sum2Rule.sum);
        if(randomCodes.length==0){
            return plvSettingList;
        }
    }





    //以下代码跟号码的位置有关
    var finalSet = [];
    var tmpSet = [];
    var tmpList = [];
    if(lmId=="1" || lmId=="6" || lmId=="7" || lmId=="2"){

        if(lmId=="7"){ //2d号码，回填X
            var result = [];
            randomCodes.forEach((code,idx)=>{
                var arr = code.split("");
                result.push(["X",arr[0],arr[1]].join(""));
                result.push([arr[0],"X",arr[1]].join(""));
                result.push([arr[0],arr[1],"X"].join(""));

            });
            randomCodes = result;
        }
        //定位置
        var fixCodeRule = rules.fixCode;
        if(null !=fixCodeRule && (fixCodeRule.included==1 || fixCodeRule.excluded==1)){
            randomCodes = fixCodeMatch(randomCodes,fixCodeRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }

        if(lmId=="6" || lmId=="7") {
            //乘号位置
            if (othersRule != null && null != othersRule.locArr) {
                var needCheck = othersRule.locArr[0] == 1 || othersRule.locArr[1] == 1 || othersRule.locArr[2] == 1
                if (needCheck) {
                    randomCodes = xLocationRuleMatch(randomCodes, othersRule);
                    if (randomCodes.length == 0) {
                        return plvSettingList;
                    }
                }
            }
        }

        //合分规则
        var hefenRule = rules.hefen;
        if(null!=hefenRule && (hefenRule.included==1 || hefenRule.excluded==1)){
            randomCodes = hefenRuleMatch(randomCodes,hefenRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }


        var singleNumRule = rules.singleNum;
        if(null !=singleNumRule && (singleNumRule.included==1 || singleNumRule.excluded==1)){
            randomCodes = singleNumRuleMatch(randomCodes,singleNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var doubleNumRule = rules.doubleNum;
        if(null !=doubleNumRule && (doubleNumRule.included==1 || doubleNumRule.excluded==1)){
            randomCodes = doubleNumRuleMatch(randomCodes,doubleNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var smallNumRule = rules.smallNum;
        if(null !=smallNumRule && (smallNumRule.included==1 || smallNumRule.excluded==1)){
            randomCodes = smallNumRuleMatch(randomCodes,smallNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var bigNumRule = rules.bigNum;
        if(null !=bigNumRule && (bigNumRule.included==1 || bigNumRule.excluded==1)){
            randomCodes = bigNumRuleMatch(randomCodes,bigNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }

        randomCodes.sort();

        for(var i=0,len=randomCodes.length;i<len;i++){
            var codes = randomCodes[i];
            plvSettingList.push({
                lotteryMethodId:lmId,
                lotteryType:lotteryType,
                buyCode:codes
            });

        }


    }else{
        var singleNumRule = rules.singleNum;
        if(null !=singleNumRule && (singleNumRule.included==1 || singleNumRule.excluded==1)){
            randomCodes = singleNumRuleMatch(randomCodes,singleNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var doubleNumRule = rules.doubleNum;
        if(null !=doubleNumRule && (doubleNumRule.included==1 || doubleNumRule.excluded==1)){
            randomCodes = doubleNumRuleMatch(randomCodes,doubleNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var smallNumRule = rules.smallNum;
        if(null !=smallNumRule && (smallNumRule.included==1 || smallNumRule.excluded==1)){
            randomCodes = smallNumRuleMatch(randomCodes,smallNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var bigNumRule = rules.bigNum;
        if(null !=bigNumRule && (bigNumRule.included==1 || bigNumRule.excluded==1)){
            randomCodes = bigNumRuleMatch(randomCodes,bigNumRule);
            if(randomCodes.length==0){
                return plvSettingList;
            }
        }
        var setArr = [];
        var validCodes = [];
        if(lmId=="3"){
            validCodes = z3Code();
        }else if(lmId=="4"){
            validCodes = z6Code();
        }else{
            return plvSettingList;
        }
        for(var i=0,len=randomCodes.length;i<len;i++){
            var codes = randomCodes[i];
            var a0 = codes.split("").sort().join("");
            if(validCodes.includes(a0)){
                !setArr.includes(a0) && setArr.push(a0) && plvSettingList.push({
                    lotteryMethodId:lmId,
                    lotteryType:lotteryType,
                    buyCode:a0
                });
            }
        }
    }

    return {
        "data":plvSettingList,
        "buyDesc":buyDesc
    }

    //return plvSettingList;
}

//上奖
function shangJiangRuleMatch(randomCodes, othersRule, count) {
    var shangJiang = othersRule.shangJiang;
    var valueLen = shangJiang.length();
    var tmpSet = [];
    var sjcode = [];
    sjcode =shangJiangCodes(shangJiang,count);
    randomCodes.sort();
    for(var i=0,len=randomCodes.length;i<len;i++){
        var codes = randomCodes[i];
        for(var j=0,len1=sjcode.length;j<len1;j++){
            var c1 = shangJiangCodes[j];
            if(codes.includes(c1)){
                tmpSet.push(codes);
            }
        }
    }
    return tmpSet;

}


function valueRangeRuleMatch(randomCodes, othersRule) {
    var finalSet = [];
    var valueRange = othersRule.valueRange;
    var min = 0;
    var max = 0;
    if(valueRange[0] && valueRange[1]){
        min = parseInt(valueRange[0]);
        max = parseInt(valueRange[1]);
    }else if(valueRange[0]){
        min = max = parseInt(valueRange[0]);
    }else{
        min = max = parseInt(valueRange[1]);
    }
    for(var i=0,len=randomCodes.length;i<len;i++){
        var code = randomCodes[i];
        var arr = code.split("");
        var sum = 0;
        arr.forEach((n,ii)=>{
            sum+=parseInt(n);
        });
        if(sum>=min && sum<=max){
            finalSet.push(code);
        }
    }
    return finalSet;
}

function hefenRuleMatch(randomCodes,hefenRule) {
    var list = hefenRule.binds;
    for(var i=0;i<list.length;i++){
        var itemRule = list[i];
        var needCheck = itemRule.locArr.includes(1);
        if(itemRule.value && needCheck){
            randomCodes = hefenItemRuleMatch(randomCodes,itemRule,hefenRule.included==1);
        }
    }
    return randomCodes;
}


//合分配置项的匹配
function hefenItemRuleMatch(randomCodes, hefenItemRule,isInclude) {
    var finalSet = [];
    var values = hefenItemRule.value.split("");
    var locArr = hefenItemRule.locArr;
    var len = locArr.length;
    for(var i=0,ll=randomCodes.length;i<ll;i++){
        var code  = randomCodes[i];
        var arr = code.split("");
        var sum = 0;
        var skip = false;
        for(var ii=0;ii<len;ii++){
            if(locArr[ii]==1){
                if(arr[ii].toUpperCase()=="X"){
                    skip = true;
                    break;
                }else{
                    sum = sum + parseInt(arr[ii]);
                }
            }
        }
        if(skip){
            continue;
        }
        var hf = sum%10+"";
        if(values.includes(hf)){
            finalSet.push(code);
        }
    }

    if(isInclude){
        return finalSet;
    }else{
        var resultSet = [];
        randomCodes.forEach((cc,idx) => {
            if(!finalSet.includes(cc)){
                resultSet.push(cc);
            }
        })
        return resultSet;
    }

}

//乘号规则
function xLocationRuleMatch(randomCodes, othersRule) {
    var finalSet = [];
    var locArr = othersRule.locArr;
    randomCodes.forEach((oriCode,idx) => {
        if(checkXLocation(oriCode,locArr)){
            finalSet.push(oriCode);
        }
    });

    return finalSet;
}



/**
 * 根据定位规则找出符合条件的号码
 * @param oriNums
 * @param fixCodeRule
 * @return Set<String>
 */
function fixCodeMatch(oriNums,fixCodeRule){
    var isInclude = false;
    if(fixCodeRule.included==1){
        isInclude = true;
    }else if(fixCodeRule.excluded==1){
        isInclude = false;
    }
    if(fixCodeRule.loc1){
        oriNums = findCodeByLoc(oriNums,0,fixCodeRule.loc1,isInclude);
    }
    if(fixCodeRule.loc2){
        oriNums = findCodeByLoc(oriNums,1,fixCodeRule.loc2,isInclude);
    }
    if(fixCodeRule.loc3){
        oriNums = findCodeByLoc(oriNums,2,fixCodeRule.loc3,isInclude);
    }
    return oriNums;
}



/**
 * 组三 号码
 * 按三字现规则生成号码
 * 存在两个相同的数字
 * 排除3重
 * @return
 */
function z3CodeBy(nums){
    var arr = nums.split("");
    arr.sort();
    var len = arr.length;
    var codeSet = [];
    for(var i=0;i<len;i++){
        for(var j=0;j<len;j++){
            for(var k=0;k<len;k++){
                if(arr[i]==arr[j] && arr[j]==arr[k]){
                    continue;
                }
                var numArr = [arr[i],arr[j],arr[k]];
                numArr.sort();
                if(numArr[0]==numArr[1] || numArr[1]==numArr[2]){
                    var code = numArr.join("");
                    if(!codeSet.includes(code))
                        codeSet.push(code);
                }
            }
        }
    }
    codeSet.sort();
    return codeSet;
}

/**
 * 组六 号码
 * 按三字现规则生成号码
 * 三个数字各不相同
 * @return
 */
function z6CodeBy(nums){
    var arr = nums.split("");
    arr.sort();
    var len = arr.length;
    var codeSet = [];
    for(var i=0;i<len;i++){
        for(var j=0;j<len;j++){
            for(var k=0;k<len;k++){
                var numArr = [arr[i],arr[j],arr[k]];
                numArr.sort();
                if(numArr[0]!=numArr[1] && numArr[1]!=numArr[2]){
                    var code = numArr.join("");
                    if(!codeSet.includes(code))
                        codeSet.push(code);
                }
            }
        }
    }
    codeSet.sort();
    return codeSet;
}

/**
 * 直选号码
 * @return
 */
function zxCodeBy(nums){
    var codeSet = [];
    var arr = nums.split("");
    arr.sort();
    var len = arr.length;
    for(var i=0;i<len;i++){
        for(var j=0;j<len;j++){
            for(var k=0;k<len;k++){
                var code = [arr[i],arr[j],arr[k]].join("");
                if(!codeSet.includes(code))
                    codeSet.push(code);
            }
        }
    }
    codeSet.sort();
    return codeSet;
}





















