<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>介绍页</title>
    <link rel="stylesheet" href="<c:url value="/scripts/shop2016/css/app_page.css?t=20160413"/>">
</head>
<body class="app_page">

<div class="container">
    <div class="sleft">
        <h1 class="icon">融易投</h1>
        <ul class="info">
            <li><i></i>创新的艺术投资玩法，2元“玩”进艺术圈</li>
            <li><i></i>看得见的创作过程：作品创作过程及时分享，现场图片、视频记录精彩瞬间，满足你的好奇心与求知欲。</li>
            <li><i></i>平台不断汇聚精致有底蕴的文化美物，让美触手可及。</li>
        </ul>
        <div class="btns">
            <a class="ios" href="javascript:" title="ios下载"></a>
            <a class="android" href="javascript:" title="Android下载"></a>
        </div>
    </div>
    <!--//End--sleft-->
    <div class="sright">
        <i class="preview"></i>
    </div>
    <!--//End--sright-->

</div>

</body>
<%--<head>--%>
    <%--<script src="<c:url value='/scripts/jquery-2.1.3.min.js'/> "></script>--%>
    <%--<script src="<c:url value='/scripts/audio/js/main.js'/>"></script>--%>
    <%--<script src="<c:url value='/scripts/audio/js/audioplayer.js'/>"></script>--%>
    <%--<link rel="stylesheet" href="<c:url value='/scripts/audio/css/style.css'/>">--%>

    <%--<link rel="stylesheet" type="text/css" href="<c:url value="/scripts/simditor-2.3.6/font-awesome-4.5.0/css/font-awesome.css"/>">--%>
    <%--<link rel="stylesheet" type="text/css" href="<c:url value="/scripts/simditor-2.3.6/styles/simditor.css"/>">--%>
    <%--<script type="text/javascript" src="<c:url value="/scripts/simditor-2.3.6/scripts/module.js"/>"></script>--%>
    <%--<script type="text/javascript" src="<c:url value="/scripts/simditor-2.3.6/simple-hotkeys-master/lib/hotkeys.js"/>"></script>--%>
    <%--<script type="text/javascript" src="<c:url value="/scripts/simditor-2.3.6/scripts/uploader.js"/>"></script>--%>
    <%--<script type="text/javascript" src="<c:url value="/scripts/simditor-2.3.6/scripts/simditor.js"/>"></script>--%>

<%--</head>--%>
<%--<body>--%>
    <%--<input type="hidden" value="ih36t7ir18t05e6w" name="userId"/>--%>
    <%--<p>name:<input type="file" name="two"/></p>--%>
    <%--<p>gender:<input type="file" name="two"/></p>--%>
    <%--<p>photo:<input type="file" name="one"/></p>--%>
    <%--<p>photo:<input type="file" name="one"/></p>--%>
    <%--<p><input type="button" value="submit" onclick="sendForm();"></p>--%>
    <%--<audio class="audioplayer" src="<c:url value="/scripts/audio/audio.mp3"/>" preload="auto" controls >--%>
        <%--<source src="audio.wav" />--%>
        <%--<source src="audio.mp3" />--%>
        <%--<source src="audio.ogg" />--%>
    <%--</audio>--%>

    <%--<textarea id="editor" placeholder="这里输入内容" autofocus></textarea>--%>
    <%--<form action="<c:url value="/app/initNewArtWork2.do" />" method="post" enctype="multipart/form-data">--%>
        <%--<input type="file" name="file">--%>
        <%--<input type="file" name="file">--%>
        <%--<input type="file" name="file">--%>
        <%--<input type="text" value="项目说明" name="description">--%>
        <%--<input type="text" value="制作说明" name="make_instru">--%>
        <%--<input type="text" value="融资答疑" name="financing_aq">--%>
        <%--<input type="hidden" value="add" name="actions">--%>
        <%--<input type="hidden" value="add" name="actions">--%>
        <%--<input type="hidden" value="add" name="actions">--%>
        <%--<input type="hidden" value="" name="attachmentIds">--%>
        <%--<input type="hidden" value="" name="attachmentIds">--%>
        <%--<input type="hidden" value="" name="attachmentIds">--%>
        <%--<input type="hidden" value="qydeyugqqiugd2" name="artworkId">--%>
        <%--<input type="submit" value="测试编辑项目">--%>
    <%--</form>--%>

<%--<script type="text/javascript">--%>
    <%--$(function(){--%>
        <%--$("audio").audioPlayer(--%>
                <%--{--%>
                    <%--classPrefix: 'audioplayer',--%>
                    <%--strPlay: '播放',--%>
                    <%--strPause: '暂停',--%>
                    <%--strVolume: '音量'--%>
                <%--}--%>
        <%--);--%>
        <%--$(".audioplayer-stopped").css({"width":"20%"});--%>

        <%--var editor = new Simditor({--%>
            <%--textarea: $('#editor') ,--%>
            <%--toolbar:[--%>
                <%--'title',--%>
                <%--'bold',--%>
                <%--'italic',--%>
                <%--'underline',--%>
                <%--'strikethrough',--%>
                <%--'fontScale',--%>
                <%--'color',--%>
                <%--'ol',--%>
        <%--'ul',--%>
        <%--'blockquote',--%>
        <%--'code',--%>
        <%--'table',--%>
        <%--'link',--%>
        <%--'image',--%>
        <%--'hr',--%>
        <%--'indent',--%>
        <%--'outdent',--%>
        <%--'alignment'--%>
            <%--],--%>
            <%--upload:true,--%>
            <%--pasteImage:true--%>
        <%--});--%>
    <%--});--%>
    <%--function sendForm() {--%>
        <%--var formData = new FormData($("#thisForm")[0]);--%>
        <%--$.ajax({--%>
            <%--url: '<c:url value="/app/uploadFile.do"/>',--%>
            <%--type: 'POST',--%>
            <%--data: formData,--%>
            <%--async: false,--%>
            <%--cache: false,--%>
            <%--contentType: false,--%>
            <%--processData: false,--%>
            <%--success: function (data) {--%>
                <%--alert(data);--%>
            <%--},--%>
            <%--error: function (msg) {--%>
                <%--alert(msg);--%>
            <%--}--%>
        <%--});--%>
    <%--}--%>
<%--</script>--%>
<%--</body>--%>
</html>
