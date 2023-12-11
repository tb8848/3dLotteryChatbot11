self.onmessage = (ev) => {
    const params = JSON.parse(ev.data);
    var buyList = params.buyList; //购买记录
    var noBuyLoss = params.noBuyLoss;
    var codeList = params.codesList;
    var drawCodeList = params.drawCodeList;
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
        var isDraw = false;
        if (drawCodeList.length > 0) {
            for (var ii = 0, len1 = drawCodeList.length; ii < len1; ii++) {
                var drawCode = drawCodeList[ii];
                if (drawCode == code) {
                    isDraw = true;
                    break;
                }
            }
        }

        if(null!=info){
            item.maxLoss = info.maxLoss;
            item.chuhuo = info.lanhuoMoney;
            item.odds = info.peiRate;
            item.odds_avg = info.avg_odds;
            item.package_chuhuo = info.bpMoney;
            //item.selected = true;
        }else{
            item.maxLoss = noBuyLoss;
        }
        item.hasDraw = isDraw;
    }
    var result = JSON.stringify(codeList);
    self.postMessage(result);
}