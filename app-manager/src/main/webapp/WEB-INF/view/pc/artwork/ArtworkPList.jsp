<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2016/4/5
  Time: 17:00
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>

<html>
<head>
  <title></title>
  <script type="text/javascript" src="<c:url value='/scripts/jquery-1.11.1.min.js'/>"></script>
</head>
<body>
<table>
  <tr>
    <td width="20%">操作</td>
    <td width="15%">项目名称</td>
    <td width="10%">发起人</td>
    <td width="10%">当前状态</td>
    <td width="15%">融资金额(元)</td>
    <td width="10%">投资人数</td>
    <td width="5%">排序</td>
    <td width="15%">项目创建时间</td>
  </tr>

  <c:forEach items="${requestScope.pageInfo.list}" var="artwork">
    <tr>
      <td>${artwork.title}</td>
      <td>${artwork.author.name}</td>
      <td>${artwork.title}</td>
      <td>${artwork.author.name}</td>
      <td>${artwork.title}</td>
      <td>${artwork.author.name}</td>
      <td>${artwork.title}</td>
      <td>${artwork.createDatetime}</td>
    </tr>
  </c:forEach>
</table>
<div style="clear: both">
  <ming800:pcPageList bean="${requestScope.pageInfo.pageEntity}" url="/basic/xm.do">
    <ming800:pcPageParam name="qm" value="${requestScope.qm}"/>
    <ming800:pcPageParam name="conditions" value="${requestScope.conditions}"/>
  </ming800:pcPageList>
</div>

</body>
</html>

