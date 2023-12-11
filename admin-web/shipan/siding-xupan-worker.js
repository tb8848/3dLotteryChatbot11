self.onmessage = (ev) => {
    const params = JSON.parse(ev.data);
    var codeMap = params.codeMap; //购买记录
    var oddsChangeList = params.oddsChangeList; //小分类对象
    var codeList = params.codeArr;
    for(var i=0,len=codeList.length;i<len;i++){
        var item = codeList[i];
        var tMaxLoss =0;
        const key = item.code+"-"+item.xian;
        var info = codeMap[key];
        if(null==info || undefined==info){

        }else{
            item = info;
        }
        var relateCodesList = item.relateCodes;
        for(var j=0,len1=relateCodesList.length;j<len1;j++){
            var rc = relateCodesList[j];
            const key1 = rc.code+"-"+rc.xian;
            var info1 = codeMap[key1];
            if(null==info1 || undefined==info1){

            }else{
                rc = info1;
                tMaxLoss = accAdd(tMaxLoss,info1.maxLoss);
            }
        }
        item.maxLoss = tMaxLoss;
    }
    var result = JSON.stringify(codeList);
    self.postMessage(result);
}