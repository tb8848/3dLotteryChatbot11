/**
 此代码引入时需先引入layui、jquery.i18n.js
 var options = {
    defaultLang:'zh',
    module:'index',
    filePath:'',
    base:'js/'
}
 **/

var i18np = null;

function initLayui(options){
    layui.config(
        {base: 'js/'})
        // 继承treetable.js插件
        .extend({i18np: 'i18n'})
        .use([ 'i18np'], function () {
            //var $ = layui.$;
            i18np = layui.i18np;
            i18np.loadProperties(options.module);
        })
    console.log("======================1",i18np)
}


var $JQ = jQuery;
function initLangConfig(options){
    layui.data("langTab", { key: "langType", value: options.defaultLang });
    $JQ("[i18n]").i18n({
        defaultLang: options.defaultLang,
        lang:options.defaultLang,
        filePath: options.filePath,
        filePrefix: "i18n_",
        fileSuffix: "",
        forever: true,
        callback: function() {
            console.log("i18n is ready.");
            //initLayui(options);
        }
    });
    //initLayui(options);
    //_i18np.loadProperties(options.module);
    //return _i18np;
}


function reloadI18n(options){
    layui.data("langTab", { key: "langType", value: options.defaultLang });
    $JQ("[i18n]").i18n({
        defaultLang: options.defaultLang,
        lang:options.defaultLang,
        filePath: options.filePath,
    });
    //initLayui(options);
    //_i18np.loadProperties(options.module);
    //return _i18np;
}
