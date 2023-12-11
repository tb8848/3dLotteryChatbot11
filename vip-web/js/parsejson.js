var json = (function(){
    var parse = function(obj) {
        for(var i in obj) {
            obj[i] = into(obj , obj , i)
        }
        return obj ;
    }

    var into = function(root , obj , k) {
        if(typeof(obj[k]) == "object") {
            for(var i in obj[k]) {
                if(i == "$ref") {
                    obj[k] = into(root , obj[k] , i)
                }else{
                    obj[k][i] = into(root , obj[k] , i)
                }
            }
            return obj[k]
        }
        if(typeof(obj[k]) == "string") {
            if(obj[k].startsWith("$.")) {
                var res = eval("root."+obj[k].substr(2))
                obj[k] = res
                return obj[k]
            }else if(obj[k].startsWith("$[")){
                var res = eval("root"+obj[k].substr(1))
                obj[k] = res
                return obj[k]
            }
            return obj[k]
        }
        return obj[k]
    }

    return {parse:parse} ;
})()