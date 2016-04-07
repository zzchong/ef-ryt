<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2015/7/22
  Time: 16:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<html>
<head>
    <title>项目详情</title>
</head>
<body>
<div class="am-cf am-padding">
    <div class="am-fl am-cf">
        <strong class="am-text-primary am-text-lg">审核项目-${object.title}-项目详情</strong>
    </div>
</div>

<div am-panel am-panel-default admin-sidebar-panel>
    <table class="am-table am-table-bordered am-table-radius am-table-striped">
        <tr>
            <td>项目标题</td>
            <td>${object.title}</td>
        </tr>
        <tr>
            <td>项目目标</td>
            <td>项目目标</td>
        </tr>
        <tr>
            <td>项目发起人</td>
            <td>${object.author.name}</td>
        </tr>
        <tr>
            <td>项目发起人联系方式</td>
            <td>联系方式</td>
        </tr>
        <tr>
            <td>项目介绍</td>
            <td>${object.brief}</td>
        </tr>
        <tr>
            <td>项目详情</td>
            <td>${object.description}</td>
        </tr>
    </table>
</div>
</body>
</html>
