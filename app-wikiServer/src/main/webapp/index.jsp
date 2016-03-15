<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<html>
<head>

    <script src="<c:url value='/scripts/jquery-2.1.3.min.js'/> "></script>

</head>
<body>
<form name="form1" id="form1">
    <p>name:<input type="text" name="name" ></p>
    <p>gender:<input type="radio" name="gender" value="1">male <input type="radio" name="gender" value="2">female</p>
    <p>photo:<input type="file" name="headPortrait" id="headPortrait"></p>
    <p><input type="button" name="b1" value="submit" onclick="fsubmit()"></p>
</form>
<div id="result"></div>


<script type="text/javascript">

    function fsubmit(){
        var data = new FormData($('#form1')[0]);
        $.ajax({
            url: "<c:url value='/app/completeUserInfo.do'/>",
            type: 'POST',
            data: data,
            dataType: 'JSON',
            cache: false,
            processData: false,
            contentType: false
        }).done(function(ret){
            if(ret['isSuccess']){
                var result = '';
                result += 'name=' + ret['name'] + '<br>';
                result += 'gender=' + ret['gender'] + '<br>';
                result += '<img src="' + ret['photo']  + '" width="100">';
                $('#result').html(result);
            }else{
                alert('提交失敗');
            }
        });
        return false;
    }
    $(document).ready(function(){
        transdate(1457951615632);
    });

    function transdate(endTime){
        var mytime=new Array();
        var timestamp = Date.parse(new Date());
        var oldTime = parseInt(endTime);
        var intervalTime = (timestamp - oldTime)/1000;

       if((intervalTime/60/60/24)>=1){//>=1 day
           var day = parseInt(intervalTime/60/60/24);
           var hour = parseInt((intervalTime-(day*60*60*24))/60/60);
           var min =parseInt((intervalTime-(day*60*60*24)-(hour*60*60))/60);
           var sec = parseInt(intervalTime-(day*60*60*24)-(hour*60*60)-min*60);
           mytime[0] =day;
           mytime[1] =hour;
           mytime[2] =min;
           mytime[2] =sec;
           alert (mytime[0]+" "+mytime[1]+" "+mytime[2]+" "+mytime[3]);
           return mytime;
       }else if((intervalTime/60/60/24)<1 && (intervalTime/60/60)>=1){
           var hour = parseInt((intervalTime)/60/60);
           var min =parseInt((intervalTime-(hour*60*60))/60);
           var sec = parseInt(intervalTime-(hour*60*60)-min*60);
           mytime[0] =hour;
           mytime[1] =min;
           mytime[2] =sec;
           alert (mytime[0]+" "+mytime[1]+" "+mytime[2]);
           return mytime;
       }else if((intervalTime/60/60)<1 && (intervalTime/60)>=1){
           var min =parseInt((intervalTime)/60);
           var sec = parseInt(intervalTime-min*60);
           mytime[0] ="";
           mytime[1] =min;
           mytime[2] =sec;
           alert (mytime[0]+" "+mytime[1]+" "+mytime[2]);
           return mytime;
       }else if((intervalTime/60)<1 && intervalTime>0){
           var sec = intervalTime;
           mytime[0] ="";
           mytime[1] ="";
           mytime[2] =sec;
           alert (mytime[0]+" "+mytime[1]+" "+mytime[2]);
           return mytime;
       }

    }
</script>

</body>
</html>
