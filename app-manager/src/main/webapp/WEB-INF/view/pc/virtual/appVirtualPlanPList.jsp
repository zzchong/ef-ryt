<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2015/11/25
  Time: 17:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<html>
<head>
    <title>虚拟数据批次</title>
</head>
<body style="height: auto">
<div style="text-align: left;margin-left: 10px;">
    <input onclick="window.location.href='<c:url value="/basic/xm.do?qm=formVirtualPlan&virtual=virtual"/>'"
           type="button" class="am-btn am-btn-default am-btn-xs"
           style="margin-top: 4px;margin-bottom: 6px;margin-left:2px;height: 35px;"
           value="新建虚拟数据批次"/>
</div>
<div>
    <table class="am-table am-table-bordered am-table-radius am-table-striped">
        <tr style="text-align:left">
            <td>操作</td>
            <td>批次编号</td>
            <td>批次名称</td>
            <%--<td>起始日期</td>--%>
            <%--<td>终止日期</td>--%>
            <%--<td>起始时间</td>--%>
            <%--<td>终止时间</td>--%>
            <td>任务状态</td>
            <td>任务进度</td>
            <td>创建时间</td>
        </tr>
        <c:forEach items="${requestScope.pageInfo.list}" var="plan">
            <tr>
                <td>
                    <div class="am-btn-toolbar">
                        <div class="am-btn-group am-btn-group-xs" style="width: 100%;">
                            <c:if test="${plan.status == '1' || plan.status == '2'}">
                                <button onclick="window.location.href='<c:url
                                        value="/basic/xm.do?qm=formVirtualPlan&virtual=virtual&id=${plan.id}"/>'"
                                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"><span
                                        class="am-icon-edit"></span> 编辑
                                </button>
                                <button onclick="window.location.href='<c:url
                                        value="/virtualPlan/removeVirtualPlan.do?id=${plan.id}"/>'"
                                        class="am-btn am-btn-default am-btn-xs am-text-danger am-hide-sm-only"><span
                                        class="am-icon-trash-o"></span> 删除
                                </button>
                            </c:if>
                            <c:if test="${plan.status == '3' && plan.planType != 'user'}">
                                <button onclick="window.location.href='<c:url
                                        value="/virtualPlan/pausePlan2.do?id=${plan.id}&resultPage=/basic/xm.do?qm=plistVirtualPlan_default"/>'"
                                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"><span
                                        class="am-icon-edit"></span> 暂停任务
                                </button>
                            </c:if>
                            <c:if test="${plan.status == '2' || plan.status == '5'}">
                                <button onclick="window.location.href='<c:url
                                        value="/virtualPlan/startPlan.do?id=${plan.id}&resultPage=/basic/xm.do?qm=plistVirtualPlan_default"/>'"
                                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"><span
                                        class="am-icon-trash-o"></span> 开始任务
                                </button>
                            </c:if>
                            <span id="lastHit">
                                <button onclick="javascript:lastHit('${plan.id}')"
                                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"><span
                                        class="am-icon-edit"></span> 补刀
                                </button>
                            </span>
                            <span id="cancelLastHit">
                                <button onclick="javascript:cancelLastHit('${plan.id}')"
                                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"><span
                                        class="am-icon-edit"></span> 终止补刀
                                </button>
                            </span>
                        </div>
                    </div>
                </td>
                <td>
                    <a href="<c:url value="/virtualPlan/getTypeObjectView.do?virtual=virtual&id=${plan.id}&type=${plan.planType}"/>">${plan.serial}</a>
                </td>
                <td><%--名称--%>${plan.description}</td>
                <%--<td>&lt;%&ndash;起始日期&ndash;%&gt;${plan.startDate}--%>
                        <%--&lt;%&ndash;<fmt:formatDate value="${plan.startDate}" pattern="yyyy-MM-dd"/>&ndash;%&gt;--%>
                <%--</td>--%>
                <%--<td>&lt;%&ndash;终止日期&ndash;%&gt;${plan.endDate}--%>
                        <%--&lt;%&ndash;<fmt:formatDate value="${plan.endDate}" pattern="yyyy-MM-dd"/>&ndash;%&gt;--%>
                <%--</td>--%>
                <%--<td>&lt;%&ndash;起始时间&ndash;%&gt;${plan.startTime}--%>
                        <%--&lt;%&ndash;<fmt:formatDate value="${plan.startTime}" pattern="HH:mm:ss"/>&ndash;%&gt;--%>
                <%--</td>--%>
                <%--<td>&lt;%&ndash;终止时间&ndash;%&gt;${plan.endTime}--%>
                        <%--&lt;%&ndash;<fmt:formatDate value="${plan.endTime}" pattern="HH:mm:ss"/>&ndash;%&gt;--%>
                <%--</td>--%>
                <td>
                    <ming800:status name="status" dataType="appVirtualPlan.status" checkedValue="${plan.status}"
                                    type="normal"/>
                </td>
                <td>
                    <c:if test="${not empty plan.implementClass}">
                        <c:set value="${0}" var="currentMoney"/>
                        <c:forEach var="invest" items="${plan.virtualArtwork.artwork.artworkInvests}">
                            <c:set var="currentMoney" value="${currentMoney + invest.price}"/>
                        </c:forEach>
                        ${currentMoney * 100 / plan.virtualArtwork.artwork.investGoalMoney }%
                    </c:if>
                </td>
                <td><fmt:formatDate value="${plan.createDatetime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
        </c:forEach>
    </table>
</div>
<div style="clear: both">
    <c:url value="/basic/xm.do" var="url"/>
    <ming800:pcPageList bean="${requestScope.pageInfo.pageEntity}" url="${url}">
        <ming800:pcPageParam name="qm" value="${requestScope.qm}"/>
        <ming800:pcPageParam name="conditions" value="${requestScope.conditions}"/>
    </ming800:pcPageList>
</div>
<script type="application/javascript">

    function lastHit(id) {
        $.ajax({
            type: 'post',
            async: false,
            url: '<c:url value="/virtualPlan/finishPlan.do?id="/>' + id,
            success: function (data) {
                if (data == "true") {
                    alert("success");
                }else{
                    alert("falied");
                }
            },
            error: function (a, b, c) {
                alert(a + b + c);
            }
        });
    }

    function cancelLastHit(id) {
        $.ajax({
            type: 'post',
            async: false,
            url: '<c:url value="/virtualPlan/pausePlan.do?id="/>' + id,
            success: function (data) {
                if (data == "true") {
                    alert("success");
                }else{
                    alert("falied");
                }
            },
            error: function (a, b, c) {
                alert(a + b + c);
            }
        });
    }
</script>
</body>
</html>
