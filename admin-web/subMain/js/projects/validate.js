
var $$$ = layui.jquery;

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