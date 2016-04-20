<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2016/4/5
  Time: 18:40
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
  <title>虚拟计划</title>
  <script src="<c:url value='/scripts/function.js'/>"></script>
</head>
<body  style="height: auto">
<div class="am-cf am-padding">
  <div class="am-fl am-cf">
    <strong class="am-text-primary am-text-lg">计划列表</strong>
  </div>
</div>
<jsp:include page="/layouts/myConfirm.jsp"/>
<jsp:include page="/layouts/myReject.jsp"/>

<table class="am-table am-table-bordered am-table-radius am-table-striped">
  <tr style="text-align:left">
    <td>操作</td>
    <td>序列号</td>
    <td>计划名称</td>
    <td>计划类型</td>
    <td>状态</td>
    <td>开始日期</td>
    <td>结束日期</td>
    <td>开始时间</td>
    <td>结束时间</td>
    <td>创建时间</td>
  </tr>

  <c:forEach items="${requestScope.pageInfo.list}" var="plan">
    <tr style="text-align: left">
      <td>
        <div class="am-btn-toolbar">
          <div class="am-btn-group am-btn-group-xs" style="width: 100%;" >
            操作
            <%--<button onclick="myConfirm('<c:url value="/checkProject/remove.do?id=${artwork.id}"/>', 'D')"--%>
                    <%--&lt;%&ndash;onclick="window.location.href='<c:url value="/checkProject/remove.do?id=${artwork.id}"/>'"&ndash;%&gt;--%>
                    <%--class="am-btn am-btn-default am-btn-xs am-text-danger am-hide-sm-only">删除</button>--%>
            <%--<button onclick="window.location.href='<c:url value="/basic/xm.do?qm=viewCheckArtwork&checkProject=checkProject&id=${artwork.id}"/>'"--%>
                    <%--class="am-btn am-btn-default am-btn-xs am-hide-sm-only">查看</button>--%>
            <%--<c:if test="${artwork.step == '10'}">--%>
              <%--<button onclick="window.location.href='<c:url value="/checkProject/checkPass.do?id=${artwork.id}&type=${artwork.step}&resultPage=L"/>'"--%>
                      <%--class="am-btn am-btn-default am-btn-xs am-hide-sm-only">标记</button>--%>
            <%--</c:if>--%>
            <%--<c:if test="${artwork.step == '11'}">--%>

              <%--<button onclick="window.location.href='<c:url value="/checkProject/checkPass.do?id=${artwork.id}&type=${artwork.step}&resultPage=L"/>'"--%>
                      <%--class="am-btn am-btn-default am-btn-xs am-hide-sm-only">通过</button>--%>
              <%--<button onclick="myReject('<c:url value="/checkProject/checkReject.do?id=${artwork.id}&resultPage=L"/>')"--%>
                      <%--&lt;%&ndash;onclick="window.location.href='<c:url value="/checkProject/checkReject.do?id=${artwork.id}&resultPage=L"/>'"&ndash;%&gt;--%>
                      <%--class="am-btn am-btn-default am-btn-xs am-hide-sm-only">驳回</button>--%>
            <%--</c:if>--%>
            <%--<c:if test="${artwork.step == '12'}">--%>
              <%--<button onclick="window.location.href='<c:url value=""/>'"--%>
                      <%--class="am-btn am-btn-default am-btn-xs am-hide-sm-only">撤销通过</button>--%>
            <%--</c:if>--%>
          </div>
        </div>
      </td>
      <td>${plan.serial}</td>
      <td>${plan.description}</td>
      <td>${plan.planType}</td>
      <td>
        <ming800:status name="status" dataType="appVirtualPlan.status" checkedValue="${plan.status}" type="normal"/>
      </td>
      <td><%--起始日期--%>${plan.startDate}
        <%--<fmt:formatDate value="${plan.startDate}" pattern="yyyy-MM-dd"/>--%>
      </td>
      <td><%--终止日期--%>${plan.endDate}
          <%--<fmt:formatDate value="${plan.endDate}" pattern="yyyy-MM-dd"/>--%>
      </td>
      <td><%--起始时间--%>${plan.startTime}
          <%--<fmt:formatDate value="${plan.startTime}" pattern="HH:mm:ss"/>--%>
      </td>
      <td><%--终止时间--%>${plan.endTime}
          <%--<fmt:formatDate value="${plan.endTime}" pattern="HH:mm:ss"/>--%>
      </td>
      <td>
        <fmt:formatDate value="${plan.createDatetime}" pattern="yyyy-MM-dd HH:mm:ss"/>
      </td>
    </tr>
  </c:forEach>
</table>
<div style="clear: both">
  <c:url value="/basic/xm.do" var="url" />
  <ming800:pcPageList bean="${requestScope.pageInfo.pageEntity}" url="${url}">
    <ming800:pcPageParam name="qm" value="${requestScope.qm}"/>
    <ming800:pcPageParam name="conditions" value="${requestScope.conditions}"/>
  </ming800:pcPageList>
</div>

</body>
</html>
