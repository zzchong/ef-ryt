<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<html>
<head>

    <script src="<c:url value='/scripts/jquery-2.1.3.min.js'/> "></script>

</head>
<body>
<form action="<c:url value="/app/uploadFile.do" />" id="thisForm" method="post" enctype="multipart/form-data">
    <input type="hidden" value="ih36t7ir18t05e6w" name="userId"/>
    <p>name:<input type="file" name="two"/></p>
    <p>gender:<input type="file" name="two"/></p>
    <p>photo:<input type="file" name="one"/></p>
    <p>photo:<input type="file" name="one"/></p>
    <p><input type="button" value="submit" onclick="sendForm();"></p>
</form>
<script type="text/javascript">
    function sendForm() {
        var formData = new FormData($("#thisForm")[0]);
        $.ajax({
            url: '<c:url value="/app/uploadFile.do"/>',
            type: 'POST',
            data: formData,
            async: false,
            cache: false,
            contentType: false,
            processData: false,
            success: function (data) {
                alert(data);
            },
            error: function (msg) {
                alert(msg);
            }
        });
    }
</script>
</body>
</html>
