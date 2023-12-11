

function createCodes(numStr,len,xianFlag){
    var codes = [];
    var arr = numStr.split("");
    //去除重复的数字
    arr.forEach((item,idx)=>{
        if(!codes.includes(item)){
            codes.push(item);
        }
    })


    switch(len){
        case 1:
          for(var i=0;i<10;i++){
              var n = i+"";
              if(numStr.indexOf(n)>-1){
                  if(!codes.includes(n)){
                      codes.push(n);
                  }
              }
          }
          break;
        case 2:
            for(var i=0;i<10;i++){
                for(var j=0;j<10;j++){
                    var n = i+""+j;
                    if(numStr.indexOf(i+"")>-1 || numStr.indexOf(j+"")){
                        if(!codes.includes(n)){
                            codes.push(n);
                        }
                    }
                }

            }
    }
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
    console.log("=======finalNums=",finalNums);
    return finalNums;
}

//fushiCodes("0026",4);


/**
 * 生成定字号码
 * @param len 长度
 * @param fmName 格式，如12xx,34xx,xx45
 */
function createCodesForDing(len,fmName,_lsId) {
    var codesList = [];
    if(len == 4){
        for (var i = 0; i < 10; i++) {
            for (var j = 0; j < 10; j++) {
                for (var k = 0; k < 10; k++) {
                    for (var m = 0; m < 10; m++) {
                        var code = i + "" + j + "" + k + "" + m;
                        codesList.push({
                            code:code,
                            xian:0,
                            maxLoss:0,
                            chuhuo:0,
                            odds:0,
                            odds_avg:0,
                            package_chuhuo:0,
                            selected:false,
                            lsid:_lsId,
                            sameCombo:[],
                            isStopBuy:0,
                            hasDraw:false,
                            isSearchCode:false
                        })
                    }
                }
            }
        }
        return codesList;
    }else if(len==3){
        for (var i = 0; i < 10; i++) {
            for (var j = 0; j < 10; j++) {
                for (var k = 0; k < 10; k++) {
                    var code = i + "" + j + "" + k;
                    codesList.push({
                        code:code,
                        xian:0,
                        maxLoss:0,
                        chuhuo:0,
                        odds:0,
                        odds_avg:0,
                        package_chuhuo:0,
                        selected:false,
                        lsid:_lsId,
                        sameCombo:[],
                        isStopBuy:0,
                        hasDraw:false,
                        isSearchCode:false
                    })
                }
            }
        }
        var clist = createFixFormatCodes(codesList,fmName);
        // for(var i=0,len=clist.length;i<len;i++){
        //     var cc = clist[i];
        //     clist[i]={
        //         code:cc,
        //         xian:0,
        //         maxLoss:0,
        //         chuhuo:0,
        //         odds:0,
        //         odds_avg:0,
        //         package_chuhuo:0,
        //         selected:false,
        //         lsid:_lsId,
        //         sameCombo:[]
        //     }
        // }
        return clist;

    }else if(len==2){
        for (var i = 0; i < 10; i++) {
            for (var j = 0; j < 10; j++) {
                var code = i + "" + j;
                codesList.push({
                    i:i,
                    j:j,
                    oriCode:code,
                    code:code,
                    xian:0,
                    maxLoss:0,
                    chuhuo:0,
                    odds:0,
                    odds_avg:0,
                    package_chuhuo:0,
                    selected:false,
                    lsid:_lsId,
                    sameCombo:[],
                    isStopBuy:0,
                    hasDraw:false,
                    isSearchCode:false
                })
            }
        }
        var clist = createFixFormatCodes(codesList,fmName);
        // for(var i=0,len=clist.length;i<len;i++){
        //     var cc = clist[i];
        //     clist[i]={
        //         code:cc,
        //         xian:0,
        //         maxLoss:0,
        //         chuhuo:0,
        //         odds:0,
        //         odds_avg:0,
        //         package_chuhuo:0,
        //         selected:false,
        //         lsid:_lsId,
        //         sameCombo:[]
        //     }
        // }
        return clist;
    }else if(len==1){
        for (var i = 0; i < 10; i++) {
            var code = i+"";
            codesList.push({
                oriCode:code,
                code:code,
                xian:0,
                maxLoss:0,
                chuhuo:0,
                odds:0,
                odds_avg:0,
                package_chuhuo:0,
                selected:false,
                lsid:_lsId,
                sameCombo:[],
                isStopBuy:0,
                hasDraw:false,
                isSearchCode:false
            })
        }
        var clist = createFixFormatCodes(codesList,fmName);
        //console.log("============clist == ",clist);
        return clist;
    }

}



/**
 * 生成现字号码
 * @param len 长度
 */
function createCodesForXian(len,_lsId) {
    var codesList = [];
    if(len == 4){
        for (var i = 0; i < 10; i++) {
            for (var j = 0; j < 10; j++) {
                for (var k = 0; k < 10; k++) {
                    for (var m = 0; m < 10; m++) {
                        var code = i + "" + j + "" + k + "" + m;
                        var sameCombo = [
                            code,i+""+j+""+m+""+k,i+""+k+""+j+""+m,i+""+k+""+m+""+j,i+""+m+""+k+""+j,i+""+m+""+j+""+k,
                            j+""+i+""+m+""+k,j+""+i+""+k+""+m,j+""+k+""+i+""+m,j+""+k+""+m+""+i,j+""+m+""+k+""+i,j+""+m+""+i+""+k,
                            k+""+i+""+j+""+m,k+""+i+""+m+""+j,k+""+j+""+i+""+m,k+""+j+""+m+""+i,k+""+m+""+j+""+i,k+""+m+""+i+""+j,
                            m+""+i+""+j+""+k,m+""+i+""+k+""+j,m+""+j+""+i+""+k,m+""+j+""+k+""+i,m+""+k+""+j+""+i,m+""+k+""+i+""+j
                        ];
                        var arr = code.split("");
                        arr.sort(_sortByUpOrder);
                        var cc = arr.join("");
                        var isSameCombo = checkSameCombo(codesList,cc);
                        if(!isSameCombo) {
                            codesList.push({
                                code: cc,
                                xian: 1,
                                maxLoss: 0,
                                chuhuo: 0,
                                odds: 0,
                                odds_avg: 0,
                                package_chuhuo: 0,
                                selected: false,
                                lsid: _lsId,
                                sameCombo:sameCombo, //相同组合的号码
                                isStopBuy:0,
                                hasDraw:false,
                                isSearchCode:false
                            })
                        }
                    }
                }
            }
        }
        return codesList;
    }else if(len==3){
        codesList=[];
        for (var i = 0; i < 10; i++) {
            for (var j = 0; j < 10; j++) {
                for (var k = 0; k < 10; k++) {
                    var code = i + "" + j + "" + k;
                    var sameCombo = [code,i+""+k+""+j,j+""+i+""+k,j+""+k+""+i,k+""+i+""+j,k+""+j+""+i];
                    var arr = code.split("");
                    arr.sort(_sortByUpOrder);
                    var cc = arr.join("");
                    var isSameCombo = checkSameCombo(codesList,cc);
                    if(!isSameCombo){
                        codesList.push({
                            code:cc,
                            xian:1,
                            maxLoss:0,
                            chuhuo:0,
                            odds:0,
                            odds_avg:0,
                            package_chuhuo:0,
                            selected:false,
                            lsid:_lsId,
                            sameCombo:sameCombo, //相同组合的号码
                            isStopBuy:0,
                            hasDraw:false,
                            isSearchCode:false
                        })
                    }
                }
            }
        }
        return codesList;

    }else if(len==2){
        codesList = [];
        for (var i = 0; i < 10; i++) {
            for (var j = 0; j < 10; j++) {
                var code = i + "" + j;
                var sameCombo = [code,j+""+i];
                var arr = code.split("");
                arr.sort(_sortByUpOrder);
                var cc = arr.join("");
                var isSameCombo = checkSameCombo(codesList,cc);
                if(!isSameCombo){
                    codesList.push({
                        code:cc,
                        xian:1,
                        maxLoss:0,
                        chuhuo:0,
                        odds:0,
                        odds_avg:0,
                        package_chuhuo:0,
                        selected:false,
                        lsid:_lsId,
                        sameCombo:sameCombo, //相同组合的号码
                        isStopBuy:0,
                        hasDraw:false,
                        isSearchCode:false
                    })
                }
            }
        }
        return codesList;
    }

}


function  checkSameCombo(codeList,cc) {
    for(var i=0,len=codeList.length;i<len;i++){
        var item = codeList[i];
        if(item.code==cc){
            return true;
        }
    }
    return false;
}


function createFixFormatCodes(codes,ruleName){
    var finalSet = [];
    codes.forEach((item,idx)=>{
        var nu = item.code;
        var rn = ruleName;
        var dings = rn.split("");
        var chars = nu.split("");
        for(var i=0,len=chars.length;i<len;i++){
            var c = chars[i];
            var idx = rn.indexOf("口");
            if(idx>-1){
                dings[idx]=c;
            }
            rn = dings.join("");
        }
        var di = dings.join("");
        item.code=di;
        // if(!finalSet.includes(di)){
        //     finalSet.push(di)
        // }
    })
    //return finalSet;
    return codes
}


function checkRepeatCodes(oriCode,repeatType){
    var repeatCodes = [];
    if(repeatType==2){
        repeatCodes = [];
        for(var i=0;i<10;i++){
            repeatCodes.push(i+""+i);
        }
        //双重
    }else if(repeatType==3){
        //三重
        repeatCodes = [];
        for(var i=0;i<10;i++){
            repeatCodes.push(i+""+i+""+i);
        }
    }else if(repeatType==4){
        //四重
        repeatCodes = [];
        for(var i=0;i<10;i++){
            repeatCodes.push(i+""+i+""+i+""+i);
        }
    }
    //console.log("repeatCodes,",repeatCodes);
    for(var i=0,len=repeatCodes.length;i<len;i++){
        if(oriCode.indexOf(repeatCodes[i])>-1){
            return true;
        }
    }
    return false;
}

/**
 * 升序排列
 * @param a1
 * @param a2
 * @returns {boolean}
 */
function _sortByUpOrder(a1,a2){
    return a1-a2;
}
