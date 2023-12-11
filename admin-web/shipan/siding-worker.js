/**
 * 包牌实盘四定位号码参数的回填
 */

self.onmessage = (ev) => {
    const params = JSON.parse(ev.data);
    var bpBuyList = params.bpBuyList; //购买记录
    var lsItem = params.lsItem; //小分类对象
    var noBuyLoss = params.noBuyLoss;
    var codeList = params.codesList;
    var isXian = 0;
    for(var i=0,len=codeList.length;i<len;i++){
        var item = codeList[i];
        var code = item.code;
        var info = null;
        if (bpBuyList.length > 0) {
            for (var ii = 0, len1 = bpBuyList.length; ii < len1; ii++) {
                var bpbuy = bpBuyList[ii];
                if (bpbuy.buyCode == code && isXian == bpbuy.isXian) {
                    info = bpbuy;
                    break;
                }
            }
        }
        if (null != info) {
            item.maxLoss = info.maxLoss;
            item.chuhuo = info.buyMoney; //此字段未使用
            item.odds = info.peiRate;
            item.odds_avg = info.avg_odds;
            item.package_chuhuo = info.bpMoney
        } else {
            item.maxLoss = noBuyLoss;
            item.odds = lsItem.peiRate;
        }
    }
    var result = JSON.stringify(codeList);
    self.postMessage(result);
}