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
  <script type="text/javascript" src="<c:url value='/scripts/recommended.js'/>"></script>
</head>
<body>
<jsp:include page="/do/generateTabs.do?qm=${requestScope.qm}&conditions=${requestScope.conditions}"/>
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
      <td>
        <button onclick="showConfirm('提示','删除项目将会关联删除一切相关记录，确定删除吗',function(){removeProject('${artwork.id}')})"
                class="am-btn am-btn-default am-btn-xs am-text-danger am-hide-sm-only"><span
                class="am-icon-trash-o"></span> 删除
        </button>
      </td>
      <td>${artwork.title}</td>
      <td>${artwork.author.name}</td>
      <td> <ming800:status name="orderType" dataType="Artwork.step"
                           checkedValue="${artwork.step}"
                           type="normal"/></td>
      <td>${artwork.investsMoney}/${artwork.investGoalMoney}</td>
      <td>${artwork.auctionNum}</td>
      <td>${artwork.sorts}</td>
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
<script>
  window.onload = function(){

    <% if (request.getParameter("message") != null && !"".equalsIgnoreCase(request.getParameter("message")))
     {
    %>
    alert("<%=request.getParameter("message")%>");
    <% } %>
  }
  function removeProject(id){
    $.ajax({
      type: "post",
      url: '<c:url value="/product/project/removeProject.do"/>',
      cache: false,
      dataType: "json",
      data:{id:id},
      success: function (data) {
        console.log(data);
        $("#"+data).remove();
      }
    });
  }
  function changeStatus(obj,id){
    var status = $(obj).attr("status");
    $.ajax({
      type: "get",
      url: '<c:url value="/product/project/updateStatus.do"/>',
      cache: false,
      dataType: "json",
      data:{id:id,status:status},
      success: function (data) {
        $(obj).attr("status",data);
        if(status=="1"){
          $(obj).find("span").text("隐藏");
          $(obj).attr("status","2");
        }
        if(status=="2"){
          $(obj).find("span").text("显示");
          $(obj).attr("status","1");
        }
      }
    });
  }

</script>
</body>
</html>

