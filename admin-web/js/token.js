var API_SERVER='/api';
var IMAGE_SERVER='/images';
var UPLOAD_SERVER='/upload';

var $ = layui.$;

$.ajaxSetup({
    //所有的ajax请求前会执行这个代码
    beforeSend:function(xhr){
        var token = layui.data('local_store').token;
        var lang = layui.data('langTab').langType;
        // console.log('send_token:'+token);
        // console.log('send_lang:'+lang);
        xhr.setRequestHeader('token',token);
        xhr.setRequestHeader('lang',lang);
        xhr.setRequestHeader('from','pc');
        xhr.setRequestHeader('Content-Type',"application/json;charset=utf-8");
    },

    //所有ajax响应后，会先执行此代码
    complete:function(xhr){
        //接收服务器传过来的令牌
        var server_token = xhr.getResponseHeader("token");

        //console.log('server_token:'+server_token);
        //判断令牌是否有效
        if (server_token==null || xhr.status==401 )
        {
            //layer.msg('拒绝访问',{icon:2,time:1000});
            return false;
        }
        //成功则保存
        layui.data('local_store',{key:'token',value:server_token});
    }

});