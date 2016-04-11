<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2016/4/8
  Time: 14:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<html>
<head>
    <title></title>
</head>
<body>
<jsp:include page="/layouts/myConfirm.jsp"/>
<jsp:include page="/layouts/myReject.jsp"/>
<div>项目名称:${object.title}</div>
<div>发起人:${object.author.name}</div>
<div>目标金额:${object.investGoalMoney}</div>
<div>当前节点:<ming800:status name="orderType" dataType="Artwork.type"
                          checkedValue="${object.type}"
                          type="normal"/></div>
<div>项目创建时间:${object.investEndDatetime}</div>
<%--融资情况--%>
<table>
    <tr>
        <td width="20%" colspan="5">
            <div id="financing_status"></div>
        </td>
    </tr>
    <tr>
        <td width="15%">操作</td>
        <td width="10%">融资金额</td>
        <td width="10%">项目进度</td>
        <td width="10%">投资人数</td>
        <td width="5%">截止时间</td>
    </tr>
    <tr>
        <td width="15%">
            <button onclick="window.location.href = '/basic/xm.do?qm=viewArtwork&id=${artwork.id}';"
                    class="am-btn am-btn-default am-btn-xs am-hide-sm-only"> 查看所有投资者
            </button>
        </td>
        <td width="10%">${object.investsMoney}</td>
        <td width="10%">
            <div id="financing_progress"></div>
        </td>
        <td width="10%">
            <c:set var="count" value="0"/>
            <c:forEach items="${object.artworkInvests}" var="invests">
                <c:set var="count" value="${count + 1}"/>
            </c:forEach>
            ${count}
        </td>
        <td width="5%">${object.investEndDatetime}</td>
    </tr>
</table>
<%--制作动态--%>
<table>
    <tr>
        <td width="20%" colspan="5">
            <div id="work_status"></div>
        </td>
    </tr>
    <tr>
        <td width="15%">操作</td>
        <td width="10%">排序</td>
        <td width="10%">动态图片</td>
        <td width="10%">文字说明</td>
        <td width="5%">更新时间</td>
    </tr>
    <c:forEach items="${object.artworkMessages}" var="artworkMessage" varStatus="x">
        <tr>
            <td width="15%">
                <button onclick="window.location.href = '/basic/xm.do?qm=remove';"
                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"> 删除动态
                </button>
            </td>
            <td width="10%">${x.index + 1}</td>
            <td width="10%"><img src="${object.picture_url}@!tanent-details-view"></td>
            <td width="10%">${artworkMessage.content}</td>
            <td width="5%">${artworkMessage.createDatetime}</td>
        </tr>
    </c:forEach>
</table>
<%--拍卖情况--%>
<table>
    <tr>
        <td width="20%" colspan="5">
            <div id="auction_status"></div>
        </td>
    </tr>
    <tr>
        <td width="15%">操作</td>
        <td width="10%">拍卖情况</td>
        <td width="10%">拍品得主</td>
        <td width="10%">联系方式</td>
        <td width="5%">起拍价</td>
        <td width="5%">成交价</td>
        <td width="5%">参拍人数</td>
    </tr>
        <tr>
            <td width="15%">
                <button onclick="window.location.href = '/basic/xm.do?qm=view';"
                        class="am-btn am-btn-default am-btn-xs am-hide-sm-only"> 查看拍品得主信息
                </button>
            </td>
            <td width="10%"><ming800:status name="orderType" dataType="Artwork.step"
                                            checkedValue="${object.step}"
                                            type="normal"/></td>
            <td width="10%">${object.winner.name}</td>
            <td width="10%">${object.winner.username}</td>
            <td width="5%">${object.newBidingPrice}</td>
            <td width="5%">${object.newBidingPrice}</td>
            <td width="5%">${object.newBidingPrice}</td>
        </tr>
</table>
<%--开奖情况--%>
<%--确认完成--%>
<div class="am-btn-toolbar">
    <div class="am-btn-group am-btn-group-xs" style="width: 100%;">
        <button onclick="myConfirm('/basic/', 'F')"
                class="am-btn am-btn-default am-btn-xs am-hide-sm-only"> 确认完成
        </button>
        <button onclick="myReject('23')"
                class="am-btn am-btn-default am-btn-xs am-hide-sm-only"> 驳回
        </button>
    </div>
</div>
<%--<script src="<c:url value="/scripts/jquery-1.11.1.min.js"/>" ></script>--%>
<script src="<c:url value="/scripts/function.js"/>" > </script>
<script type="application/javascript">
    $("#financing_status").html("融资情况 ${object.investStartDatetime} - " + new Date().toLocaleString());
    $("#work_status").html("制作动态 ${object.investEndDatetime} - " + new Date().toLocaleString());
    $("#auction_status").html("拍卖情况 ${object.auctionStartDatetime} - " + new Date().toLocaleString());

    var progress =
    ${object.investsMoney} / ${object.investGoalMoney} *
    100;
    $("#financing_progress").html(progress + "%");
</script>
</body>
</html>
