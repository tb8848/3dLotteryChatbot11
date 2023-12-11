
/**
 * 验证指定ID绑定的内容，是否符合typeName指定的规则
 * @param $id jquery风格的ID，如#cc
 * @param typeName money(金额规则),digit(数字规则)
 * @returns {boolean}
 */
function validateValue(value,typeName){
    var pass = true;
    if(typeName=='money'){
        var amtreg=/^\d+(\.\d{1,2})?$/;
        if(!amtreg.test(value)){
            return false;
        }
    }else if(typeName=='digit'){
        var amtreg=/^\d+$/;
        if(!amtreg.test(value)){
            return false;
        }
    }
    return pass;
}


function validateMoney(money) {
    var moneyReg=/^(([1-9][0-9]*)|(([0]\.\d{1}|[1-9][0-9]*\.\d{1})))$/;
    return moneyReg.test(money)
}

function validateCode(code) {
    var codeReg = /[0-9xX]{2,4}$/;
    return codeReg.test(code)
}

function checkPhoneBrowser() {
    var isPhone = false
    if ((navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i))) {
        isPhone=true;
    }
    return isPhone
}