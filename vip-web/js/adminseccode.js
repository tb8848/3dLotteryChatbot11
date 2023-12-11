/*
app 弹出验证码
*/

function getQueryTimeid()
{
    var date = new Date();
    return date.getTime();
}
var loginTimes = 1;
var logingetr = '';
var loginwap = '';
var login_but="#seccodelogin";
function onLogin(getr,wap,posturl,postdata)
{
    logingetr=getr;
    loginwap=wap;

    if($(login_but).attr("disabled")=="disabled")return false;
    $(login_but).val('Loading..');
    $(login_but).attr({"disabled":"disabled"});

    var ssvv = "seccodeverify";
    var seccodeverify = $("input[name='"+ssvv+"']").val();
    //var encryptkey = $.jCryption.createKey();

    //var getseccode =  $.jCryption.str2hex(Base64_2.encode($.jCryption.encrypt(seccodeverify, encryptkey)));
    var getseccode =  seccodeverify;
    var logininfo = {
        seccodeverify: getseccode
        //useragent: navigator.userAgent.toLowerCase()
    };
    for(var i=0;i<seccodeWin.postdata.length;i++) {
        logininfo[i] = seccodeWin.postdata[i];
    }
    $.ajax({
        url: posturl,
        type: 'POST',
        dataType: 'json',
        data: logininfo,
        //async:true,
        //timeout:6000,
        complete: function (xhr, status)
        {
            //alert(status);
        },
        error: function(jqXHR, textStatus, errorThrown)
        {
            smessagebox('代号:'+jqXHR.status+' ,连接失败, 请重新操作！');
        },
        success: seccodeWin.huidiao
    });


    return false;
}

function smessagebox ( msg)
{
    $(login_but).val('提交');$(login_but).removeAttr("disabled");
    alert(msg)
}

function updateseccode() {
    //alert('dd'))
    var w = seccodeWin.w;
    var h = seccodeWin.h;

    type = seccodeWin.t;
    var rand = Math.random();

    if(type == 2) {
        $('#seccodeimage').html('<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0" width="' + w + '" height="' + h + '" align="middle">'
            + '<param name="allowScriptAccess" value="sameDomain" /><param name="movie" value="seccode.php?update=' + rand + '" /><param name="quality" value="high" /><param name="wmode" value="transparent" /><param name="bgcolor" value="#ffffff" />'
            + '<embed src="seccode.php?update=' + rand + '" quality="high" wmode="transparent" bgcolor="#ffffff" width="' + w + '" height="' + h + '" align="middle" allowScriptAccess="sameDomain" type="application/x-shockwave-flash" pluginspage="http://www.macromedia.com/go/getflashplayer" /></object>');
    } else {

        $('#seccodeimage').html('<img id="seccode" onclick="updateseccode()" width="' + w + '" height="' + h + '" src="seccode.php?update=' + rand + '" align="absmiddle" alt="" />') ;
        $('#seccodeverify').val('');
        $('#seccodeverify').focus().select();
        seccodeWin.setKey();
        //alert(type+"w"+seccodedata[0]+"q"+seccodedata[1]+"w"+rand)
    }
}

var seccodeWin={
    getrand:0,
    wap:0,
    w:0,
    h:0,
    t:0,
    huidiao:0,

    show:function (){

        $(".bgseccodemain").show();
        //$(".bgseccodedx").center()
        updateseccode();
    },
    hide:function (){
        $(".bgseccodemain").hide();
    },
    arrSort:function (){
        var arr=[];
        for(var i=0;i<10;i++){
            arr[i]=i;
        }
        arr.sort(function(){ return 0.5 - Math.random() })
        return arr;
    },
    setKey:function (){
        var arr = this.arrSort();
        $('#mainkey td').each(function(i){
            $(this).text(arr[i]);
        });
    },
    //$seccodedata['width']=80;
    //	$seccodedata['height']=21;  $seccodedata['type']
    //只要设置高度宽度和回调
    postdata:'',
    showHtml:function (json,huidiao){
        seccodeWin.w=json.w;
        seccodeWin.h=json.h;
        seccodeWin.t=json.t;
        seccodeWin.posturl=json.securl;
        seccodeWin.postdata=json.secdata;
        seccodeWin.huidiao=huidiao;
        if(json.secstat==2)smessagebox('您输入的答案不正确。')
        var html='';



        $("#seccodemain").empty();
        html='	<div class="bgseccodemain">'
            +'<div class="bgseccode"></div>'
            +'<div class="bgseccodedx">'
            +'	<div class="bgseccodedxtit"><div class="bgseccoderight" id="loginx">X</div><div class="bgseccodeleft">请回答</div></div><div class="clear"></div>'
            +'   <ul>'
            +'       <li ><span id="seccodeimage"  title="看不清楚，请点击"></span></li>'
            +'       <li >= <input type="text" onfocus="this.onfocus = null" id="seccodeverify" name="seccodeverify" maxlength="4" style="width: '+seccodeWin.w+'px;height: '+(seccodeWin.h-5)+'px;line-height: '+(seccodeWin.h-6)+'px;font-size: '+(seccodeWin.h-16)+'px;vertical-align:middle;border: solid 1px #a0a0a0;" readonly="readonly"/></li>'
            +'       <li><a href="####" onclick="JavaScript:updateseccode();return false;">换题</a>'
            +'       	<script type="text/javascript">var seccodedata = ['+seccodeWin.w+', '+seccodeWin.h+', '+seccodeWin.t+'];</script>'
            +'      </li>'
            +'    </ul>'
            +'    <ul>'
            +'        <li class="mainkey">'
            +'	<table id="mainkey"  class="mainkey" cellpadding="2" cellspacing="0" align="center" border="2" bordercolor="#FFFFFF">'
            +'	<tbody>'
            +'	<tr>'
            +'		<td></td><td></td><td></td><td></td><td></td>'
            +'	</tr>'
            +'	<tr> '
            +'		<td></td><td></td><td></td><td></td><td></td>'
            +'	</tr>'
            +'	<tbody>'
            +'	</table>   '
            +'        </li>'
            +'    </ul>'
            +'    <ul>'
            +'       <li><input type="button" id="seccodelogin" name="seccodelogin" value=" 提交 "></li>'
            +'   </ul>'
            +'</div>'
            +'</div>'

        $("#seccodemain").html(html);
        seccodeWin.show();
        seccodeWin.int(0,0);
        //return html;
    },
    int:function (r,wap){
        this.getrand = r;
        this.wap = wap;
        this.setKey();
        $("#loginx").on("click", function(){
            seccodeWin.hide();
        });
        $("#seccodelogin").on("click", function(){
            var v = $('#seccodeverify').val();
            if(!v){
                alert('请填写答案。');
                $('#seccodeverify').focus().select();
            }else{
                onLogin(''+seccodeWin.getrand+'',seccodeWin.wap,seccodeWin.posturl,seccodeWin.postdata);
            }

        });
        $('#seccodeverify').keypress(function(event){
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if(event.keyCode ==13){
                $("#seccodelogin").click();
            }
        });
        $('#seccodeverify').on('click', function(){
            $('#seccodeverify').val('');
        });

        $('#mainkey').on('click', 'td', function(e){
            e.preventDefault();
            $('#seccodeverify').val($('#seccodeverify').val()+''+$(this).text());
        });
    }
};

$(window).load(function(){

    $("body").prepend('<div id="seccodemain" style="z-index: 9999999;"></div>');

});