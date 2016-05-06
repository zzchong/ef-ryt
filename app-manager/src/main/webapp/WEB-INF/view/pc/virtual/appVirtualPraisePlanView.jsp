<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2015/12/10
  Time: 17:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ming800" uri="http://java.ming800.com/taglib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>虚拟融资计划信息</title>
    <script src="<c:url value='/resources/plugins/My97DatePicker/WdatePicker.js'/>"></script>
</head>
<body>
<div class="am-cf am-padding">
    <div class="am-fl am-cf">
        <strong class="am-text-primary am-text-lg">虚拟融资计划</strong>
    </div>
</div>
<hr/>
<div class="am-g">
    <form id="orderPlanForm" onsubmit="return isSubmitForm()"
          action="<c:url value='/virtualPlan/saveVirtualPraisePlan.do'/>"
          method="post" enctype="multipart/form-data" class="am-form am-form-horizontal">
        <input type="hidden" name="id" value="${object.id}">
        <input type="hidden" name="status" value="${object.status}"/>
        <input type="hidden" name="implementClass" value="${object.implementClass}"/>
        <input type="hidden" name="createDatetime" value="${object.createDatetime}"/>

        <div class="am-form-group">
            <label for="serial" class="am-u-sm-3 am-form-label">虚拟计划编号
                <small>*</small>
            </label>

            <div class="am-u-sm-9">
                <input type="text" name="serial" id="serial" title="虚拟计划编号"
                       value="${object.serial}" required="true" readonly>
            </div>
        </div>

        <div class="am-form-group">
            <label for="description" class="am-u-sm-3 am-form-label">虚拟数据批次名
                <small>*</small>
            </label>

            <div class="am-u-sm-9">
                <input type="text" name="description" id="description"
                       title="虚拟数据批次名" placeholder="虚拟数据批次名"
                       value="${object.description}" required="true" readonly>
            </div>
        </div>

        <div class="am-form-group">
            <label for="planType" class="am-u-sm-3 am-form-label">虚拟数据对象
                <small>*</small>
            </label>

            <div class="am-u-sm-9" style="margin-top: 8px">
                <input type="text"
                       value="<ming800:status name="planTypeSelect" dataType="appVirtualPlan.planType" checkedValue="${object.planType}" type="normal"/>"
                       readonly>
                <input type="hidden" name="planType" id="planType" title="虚拟数据对象" required="true"
                       value="${object.planType}" readonly>
            </div>
        </div>

        <%--<div class="am-form-group">--%>
            <%--<label class="am-u-sm-3 am-form-label">任务截止日期--%>
                <%--<small>*</small>--%>
            <%--</label>--%>

            <%--<div class="am-u-sm-9">--%>

                <%--<input type="text" style="width: auto; float: left; height: 35px"--%>
                       <%--name="startDate" id="startDate" title="开始日期"--%>
                       <%--value="${object.startDate}" required="true" readonly>--%>

                <%--<span style="width: auto; float: left; font-family:'应用字体 Regular', '应用字体'; margin-left: 10px; margin-top: 4px">至</span>--%>

                <%--<input type="text" style="width: auto; float: left; margin-left: 10px; height: 35px"--%>
                       <%--name="endDate" id="endDate" title="结束日期"--%>
                       <%--value="${object.endDate}" required="true" readonly>--%>
            <%--</div>--%>
        <%--</div>--%>

        <%--<div class="am-form-group">--%>
            <%--<label class="am-u-sm-3 am-form-label">任务运行时间--%>
                <%--<small>*</small>--%>
            <%--</label>--%>

            <%--<div class="am-u-sm-9">--%>
                <%--<input type="text" style="width: auto; float: left; height: 35px"--%>
                       <%--name="startTime" id="startTime" title="开始时间"--%>
                       <%--value="${object.startTime}" required="true" readonly>--%>

                <%--<span style="width: auto; float: left; font-family:'应用字体 Regular', '应用字体'; margin-left: 10px; margin-top: 4px">至</span>--%>

                <%--<input type="text" style="width: auto; float: left; margin-left: 10px; height: 35px"--%>
                       <%--name="endTime" id="endTime" title="结束时间"--%>
                       <%--value="${object.endTime}" required="true" readonly>--%>
            <%--</div>--%>
        <%--</div>--%>
        <hr/>
        <div class="am-form-group">
            <label for="serverUrl" class="am-u-sm-3 am-form-label">融资请求地址
                <small>*</small>
            </label>

            <div class="am-u-sm-9">
                <input type="text" name="serverUrl" id="serverUrl"
                       title="融资请求地址http://" placeholder="融资请求地址http://"
                       value="${object.url}" required="true" >
            </div>
        </div>
        <%--<div class="am-form-group">--%>
            <%--<label class="am-u-sm-3 am-form-label">选择融资用户组--%>
                <%--<small>*</small>--%>
            <%--</label>--%>

            <%--<div class="am-u-sm-9">--%>
                <%--<input type="hidden" name="virtualInvestorPlanId" id="virtualInvestorPlanId"--%>
                       <%--placeholder="融资用户组" value="${object.virtualInvestorPlan.id}" required="true">--%>
                <%--<input type="text" name="virtualInvestorPlan.description" id="virtualInvestorPlanName"--%>
                       <%--placeholder="融资用户组" data-am-modal="{target: '#virtualInvestorPlanModal'}"--%>
                       <%--value="${object.virtualInvestorPlan.group}">--%>
            <%--</div>--%>
        <%--</div>--%>
        <div class="am-form-group">
            <label class="am-u-sm-3 am-form-label">选择作品
                <small>*</small>
            </label>

            <div class="am-u-sm-9">
                <input type="hidden" name="artworkId" id="artworkId"
                       placeholder="作品" value="${pmIdList}" required="true">
                <input type="text" name="artworkNameList" id="artworkNameList"
                       placeholder="作品" data-am-modal="{target: '#artworkModal'}" value="${artworkNameList}">
            </div>
        </div>
        <c:if test="${object.status == '1' || object.status == '2'}">
            <div class="am-form-group">
                <div class="am-u-sm-9 am-u-sm-push-3">
                    <input type="submit" class="am-btn am-btn-primary" value="保存"/>
                </div>
            </div>
        </c:if>

    </form>
</div>

<div class="am-popup" id="virtualInvestorPlanModal" style="height: 500px">
    <div class="am-popup-inner">
        <div class="am-popup-hd">
            <h4 class="am-popup-title">融资者用户组</h4>
            <span data-am-modal-close class="am-close">&times;</span>
        </div>
        <div class="am-popup-bd" style="height: 10px">
            <input type="text" name="selectInvestorPlan" style="float: left" placeholder="编号或名称" value=""/>
            <%--<a style="width: 10%;float: left;margin-left: 10px;"--%>
               <%--class="am-btn am-btn-default am-btn-xs am-text-danger am-hide-sm-only"--%>
               <%--href="javascript:void(0);" onclick="selectInvestorPlan()">查找--%>
            <%--</a>--%>
        </div>
        <div class="am-popup-bd" style="height: 420px">
            <table class="am-table am-table-bd am-table-bdrs am-table-striped am-table-hover">
                <tr>
                    <td class="am-text-center" width="14%">勾选</td>
                    <td class="am-text-center" width="17%">融资者组别</td>
                    <td class="am-text-center" width="17%">融资者总数</td>
                </tr>
            </table>
            <div style="height: 350px; overflow-y: auto; margin-top: 0px">
                <table class="am-table am-table-bd am-table-bdrs am-table-striped am-table-hover"
                       id="investorPlanTable">
                    <tbody>
                    <c:forEach var="investor" items="${virtualInvestorPlanList}">
                        <tr name="${investor.group}" count="${investor.count}">
                            <td align="center" width="13%">
                                <input type="radio" name="pInvestor" value="${investor.id}" title="${investor.group}">
                            </td>
                            <td class="am-text-center" width="33%">${investor.group}</td>
                            <td class="am-text-center" width="33%">${investor.count}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div style="height: 10px; margin-top: 10px" align="center">
                <input type="button" name="cancel" value="取消" onclick="btnCancel2()"/>
                <input type="button" name="confirm" value="确定" onclick="btnConfirm2()"/>
            </div>
        </div>
    </div>
</div>

<div class="am-popup" id="artworkModal" style="height: 550px">
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
                    <th class="am-text-center" width="8%">标题</th>
                    <th class="am-text-center" width="17%">描述</th>
                </tr>
            </table>
            <div style="height: 350px; overflow-y: auto;">
                <table class="am-table am-table-bd am-table-bdrs am-table-striped am-table-hover"
                       id="artworkTable">
                    <tbody>
                    <c:forEach var="artwork" items="${artworkList}">
                        <tr name="${artwork.title}" description="${artwork.description}">
                            <td align="center" width="13%">
                                <input type="radio" name="pModel" value="${artwork.id}" title="${artwork.title}">
                            </td>
                            <td class="am-text-center" width="33%">${artwork.title}</td>
                            <td class="am-text-center" width="53%">${artwork.description}</td>
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
    function isSubmitForm() {
        if (afterSubmitForm()
//                && amountCheck()
        ) {
            return true;
        }
        return false;
    }
    function afterSubmitForm() {
        var form = document.getElementById("orderPlanForm");
        var a = form.elements.length;//所有的控件个数
        for (var j = 0; j < a; j++) {
            if (form.elements[j].required) {
                if (form.elements[j].value == "" || form.elements[j].value == null) {
                    alert(form.elements[j].placeholder + "不能为空");
                    form.elements[j].focus();
                    return false;
                }
            }
        }
        return true;
    }
    function amountCheck() {
        var oaf = $("#orderAmountFloor").val();
        var oac = $("#orderAmountCeil").val();
        if (oac - oaf <= 0) {
            alert("订单数量下限应小于数量上限");
            return false;
        }
        return true;
    }

    function selectInvestorPlan() {
        var v = $("input[name='selectInvestorPlan']").val();
        if (v == "") {
            $("#investorPlanTable tr:gt(0)").each(function () {
                $(this).show();
            });
        } else {
            $("#investorPlanTable tr:gt(0)").each(function () {
                if ($(this).attr("name").indexOf(v) != -1 || $(this).attr("serial").indexOf(v) != -1) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        }
    }
//    function selectInvestorPlan(id, name) {
//        $("#virtualInvestorPlanId").val(id);
//        $("#virtualInvestorPlanName").val(name);
//        $("#virtualInvestorPlanModal").modal('close');
//    }

    function selectArtwork() {
        var v = $("input[name='selectArtwork']").val();
        if (v == "") {
            $("#artworkTable tr:gt(0)").each(function () {
                $(this).show();
            });
        } else {
            $("#artworkTable tr:gt(0)").each(function () {
                if ($(this).attr("name").indexOf(v) != -1 || $(this).attr("serial").indexOf(v) != -1) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        }
    }
    function btnCancel() {
        $("#artworkModal").modal('close');
    }
    function btnConfirm() {
        var idList = "";
        var nameList = "";
        $("input[name='pModel']:checked").each(function () {
//            alert($(this).attr("title"));
            if (idList != "") {
                idList = idList + "," + $(this).val();
            } else {
                idList = $(this).val();
            }
            if (nameList != "") {
                nameList = nameList + "," + $(this).attr("title");
            } else {
                nameList = $(this).attr("title");
            }
        });

        $("#artworkModal").val(idList);
        $("#artworkNameList").val(nameList);
        $("#artworkId").val(idList);
        $("#artworkModal").modal('close');
    }

    function btnCancel2() {
        $("#virtualInvestorPlanModal").modal('close');
    }
    function btnConfirm2() {
        var idList = "";
        var nameList = "";
        $("input[name='pInvestor']:checked").each(function () {
//            alert($(this).attr("title"));
            if (idList != "") {
                idList = idList + "," + $(this).val();
            } else {
                idList = $(this).val();
            }
            if (nameList != "") {
                nameList = nameList + "," + $(this).attr("title");
            } else {
                nameList = $(this).attr("title");
            }
        });

        $("#virtualInvestorPlanModal").val(idList);
        $("#virtualInvestorPlanName").val(nameList);
        $("#virtualInvestorPlanId").val(idList);
        $("#virtualInvestorPlanModal").modal('close');
    }
</script>
</body>
</html>
