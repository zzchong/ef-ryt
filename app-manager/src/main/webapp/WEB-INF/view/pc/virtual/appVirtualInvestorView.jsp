<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2015/11/26
  Time: 13:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>虚拟数据批次信息</title>
    <script src="<c:url value="/META-INF/resources/resources/plugins/My97DatePicker/WdatePicker.js"/>"></script>
</head>
<body>
<div>
    <table class="am-table am-table-bordered am-table-radius am-table-striped">
        <tr style="text-align:left">
            <td>昵称</td>
            <td>头像</td>
            <td>签名</td>
            <td>所属组</td>
            <%--<td>出资上限</td>--%>
        </tr>
        <c:forEach items="${object.virtualUserList}" var="virtualUser">
            <tr>
                <td>${virtualUser.userBrief.user.name}</td>
                <td><img src="http://rongyitou2.efeiyi.com/${virtualUser.userBrief.user.pictureUrl}@!ryt_head_portrait"></td>
                <td>${virtualUser.userBrief.signer}</td>
                    <td>${object.group}</td>
                    <%--<td>${investorPlan.investCeilAmount}</td>--%>
            </tr>
        </c:forEach>
    </table>
</div>
<script>
    function objectChange(obj) {
        var val = obj.value;
        $("#planType").val(val);
    }
    function startLessThanEnd(val) {
        if (afterSubmitForm2zero(val) && dateCheck() && timeCheck()) {
            return true;
        }
        return false;
    }
    function dateCheck() {
        var sd = $("#startDate").val();
        var ed = $("#endDate").val();
        if (ed <= sd) {
            alert("任务开始日期应该小于结束日期");
            return false;
        }
        return true;
    }
    function timeCheck() {
        var st = $("#startTime").val();
        var et = $("#endTime").val();
        if (et <= st) {
            alert("任务运行开始时间应该小于结束时间");
            return false;
        }
        return true;
    }
    function afterSubmitForm2zero(formId) {
        var form2 = document.getElementById(formId);
        var a = form2.elements.length;//所有的控件个数
        for (var j = 0; j < a; j++) {
            if (form2.elements[j].required) {
                if (form2.elements[j].value == "" || form2.elements[j].value == null) {
                    alert(form2.elements[j].title + "不能为空");
                    form2.elements[j].focus();
                    return false;
                }
            }
        }
        return true;
    }
</script>

</body>
</html>
