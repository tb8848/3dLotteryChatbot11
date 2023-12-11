/**
 * 实盘四定位号码参数的回填
 */
var lmOddsChangeList = [];
var stopBuyList = [];
self.onmessage = (ev) => {
    const params = JSON.parse(ev.data);
    var buyList = params.bpBuyList; //购买记录
    stopBuyList = params.stopBuyList;
    var lsItem = params.lsItem; //小分类对象
    var noBuyLoss = params.noBuyLoss;
    var codeList = params.codesList;
    lmOddsChangeList = params.lmOddsChangeList;
    var isXian = 0;
    for(var i=0,len=codeList.length;i<len;i++){
        var item = codeList[i];
        var code = item.code;
        var info = null;
        if (buyList.length > 0) {
            for (var ii = 0, len1 = buyList.length; ii < len1; ii++) {
                var bpbuy = buyList[ii];
                if (bpbuy.buyCode == code && isXian == bpbuy.isXian) {
                    info = bpbuy;
                    break;
                }
            }
        }
        var lsPr = getLmPeiRate(lsItem,code);
        var codeOddsChange = getOddsChange(item);

        if(null!=info){
            item.maxLoss = info.maxLoss;
            item.chuhuo = info.chuhuoMoney;
            if(null!=codeOddsChange){
                item.odds = codeOddsChange.changePeiRate;
            }else{
                item.odds = info.peiRate;
            }
            item.odds_avg = info.avg_odds;
            item.package_chuhuo = info.bpMoney
        }else{
            item.maxLoss = noBuyLoss;
            if(null!=codeOddsChange){
                item.odds = codeOddsChange.changePeiRate;
            }else{
                item.odds = lsPr;
            }
        }
        var checkStopCode = isStopCode(item.code,lsItem.id)
        item.selected = checkStopCode;
        item.isStopBuy = checkStopCode?1:0;
    }
    var result = JSON.stringify(codeList);
    self.postMessage(result);
}

function isStopCode(buyCode,lsId) {
    var exist = false;
    stopBuyList.forEach((item,idx)=>{
        if(item.codes==buyCode && item.lotterySettingId==lsId){
            exist = true;
        }
    })
    return exist;
}

function getOddsChange(buyItem) {
    var oddsChange = null;
    if(lmOddsChangeList.length>0){
        for(var i=0,len=lmOddsChangeList.length;i<len;i++){
            var item = lmOddsChangeList[i];
            if(item.hotNumber == buyItem.code){
                oddsChange= item;
                break;
            }
        }
    }
    return oddsChange;
}

function getLmPeiRate(lsItem,code) {
    var pr = lsItem.peiRate;
    if(pr.indexOf("/")>-1){ //多重赔率分割
        var prArr = pr.split("/");
        var isRepeat = false;
        for(var i=code.length;i>=2;i--){
            isRepeat = checkRepeatCodes(code,i)
            if(isRepeat){
                pr = prArr[i-1];
                break;
            }
        }
        if(!isRepeat){
            pr = prArr[0];
        }
    }
    return pr;
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