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

</script>

</body>
</html>
