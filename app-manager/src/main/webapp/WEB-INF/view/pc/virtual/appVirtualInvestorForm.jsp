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
    <script src="<c:url value="/resources/plugins/My97DatePicker/WdatePicker.js"/>"></script>
</head>
<body>
<div class="am-cf am-padding">
    <div class="am-fl am-cf">
        <strong class="am-text-primary am-text-lg">虚拟融资用户组详情</strong>
    </div>
</div>
<hr/>
<div class="am-g">
    <form id="vPlanForm" onsubmit="return startLessThanEnd('vPlanForm')"
          action="<c:url value='/basic/xm.do?qm=saveOrUpdateVirtualPlan'/>"
          method="post" enctype="multipart/form-data" class="am-form am-form-horizontal">
        <input type="hidden" name="id" value="${object.id}">
        <%--<input type="hidden" name="serial" value="${object.serial}"/>--%>
        <%--<input type="hidden" name="status" value="${object.status}"/>--%>

        <div class="am-form-group">
            <label for="group" class="am-u-sm-3 am-form-label">虚拟融资用户组别
                <small>*</small>
            </label>

            <div class="am-u-sm-9">
                <input type="text" name="group" id="group"
                       title="虚拟融资用户组别" placeholder="虚拟融资用户组别"
                       value="${object.group}" required="true">
            </div>
        </div>

        <div class="am-form-group">
            <label class="am-u-sm-3 am-form-label">选择虚拟用户
                <small>*</small>
            </label>

            <div class="am-u-sm-9">
                <input type="text" name="userNameList" id="userNameList"
                       placeholder="用户" data-am-modal="{target: '#userBriefModal'}" value="${userNameList}">
            </div>
        </div>

        <div class="am-form-group">
            <label for="investFloorAmount" class="am-u-sm-3 am-form-label">出资下限
                <small>*</small>
            </label>

            <div class="am-u-sm-9" style="margin-top: 8px">
                <input type="text" name="investFloorAmount" id="investFloorAmount"
                       title="出资下限" placeholder="出资下限"
                       value="${object.investFloorAmount}" required="true">
            </div>
        </div>

        <div class="am-form-group">
            <label for="investCeilAmount" class="am-u-sm-3 am-form-label">出资下限
                <small>*</small>
            </label>

            <div class="am-u-sm-9" style="margin-top: 8px">
                <input type="text" name="investCeilAmount" id="investCeilAmount"
                       title="出资上限" placeholder="出资上限"
                       value="${object.investCeilAmount}" required="true">
            </div>
        </div>

        <div class="am-form-group">
            <div class="am-u-sm-9 am-u-sm-push-3">
                <input type="submit" class="am-btn am-btn-primary" value="保存"/>
            </div>
        </div>

    </form>
</div>
<div class="am-popup" id="userBriefModal" style="height: 550px">
    <div class="am-popup-inner">
        <div class="am-popup-hd">
            <h4 class="am-popup-title">选择作品</h4>
            <span data-am-modal-close class="am-close">&times;</span>
        </div>
        <div class="am-popup-bd" style="height: 10px">
            <input type="text" name="selectArtwork" style="float: left" placeholder="编号或名称" value=""/>
            <a style="width: 10%;float: left;margin-left: 10px;"
               class="am-btn am-btn-default am-btn-xs am-text-danger am-hide-sm-only"
               href="javascript:void(0);" onclick="selectArtwork()">查找
            </a>
        </div>
        <div class="am-popup-bd" style="height: 420px">
            <table class="am-table am-table-bd am-table-bdrs am-table-striped am-table-hover">
                <tr>
                    <td class="am-text-center" width="4%">操作</td>
                    <th class="am-text-center" width="8%">用户名</th>
                    <th class="am-text-center" width="17%">签名</th>
                </tr>
            </table>
            <div style="height: 350px; overflow-y: auto;">
                <table class="am-table am-table-bd am-table-bdrs am-table-striped am-table-hover"
                       id="artworkTable">
                    <tbody>
                    <c:forEach var="userBrief" items="${virtualUserBriefList}">
                        <tr name="${userBrief.user.name}" description="${userBrief.user.name}">
                            <td align="center" width="13%">
                                <input type="checkbox" name="pModel" value="${userBrief.user.name}"
                                <c:if test="${fn:contains(selectedVirtualUserBriefList, userBrief.id)}">
                                       checked="checked"
                                </c:if>
                                       title="${userBrief.user.name}">
                            </td>
                            <td class="am-text-center" width="33%">${userBrief.user.name}</td>
                                <%--<td class="am-text-center" width="53%">${artwork.description}</td>--%>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div style="height: 10px; margin-top: 10px" align="center">
                <input type="button" name="cancel" value="取消" onclick="btnCancel()"/>
                <input type="button" name="confirm" value="确定" onclick="btnConfirm()"/>
            </div>
        </div>
    </div>
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
