<%@ page language="java" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${METHOD=='GET'}">
</c:if>

<c:set var="v" value="314159"/>
<c:set var="basepath" value="${param.path}"/>
<script type="text/javascript" src="${basepath}js/lib/jquery-ui-1.8.custom.min.js"></script>
<script type="text/javascript" src="${basepath}conf/config.js.jsp?v=${timestamp}"></script>
<!--[if IE]><script language="javascript" type="text/javascript" src="${basepath}js/lib/excanvas.min.js"></script><![endif]-->
<c:choose>
	<c:when test="${not debug}">
<script type="text/javascript" src="${basepath}js/common-lib.min.${v}.js"></script>
<script type="text/javascript" src="${basepath}js/biomart-all.min.${v}.js"></script>
	</c:when>
	<c:otherwise>
<script type="text/javascript" src="${basepath}js/lib/jquery.ba-bbq.js"></script>
<script type="text/javascript" src="${basepath}js/lib/jquery.openid.20101209.js"></script>
<script type="text/javascript" src="${basepath}js/lib/jquery.blockUI.js"></script>
<script type="text/javascript" src="${basepath}js/lib/hoverIntent.js"></script>
<script type="text/javascript" src="${basepath}js/lib/jquery.cookies.js"></script>
<script type="text/javascript" src="${basepath}js/lib/jquery.jgrowl_minimized.js"></script>
<script type="text/javascript" src="${basepath}js/lib/ajaxupload.js"></script>
<script type="text/javascript" src="${basepath}js/lib/jquery.tipsy.js"></script>
<script type="text/javascript" src="${basepath}js/lib/flot/jquery.flot.js"></script>
<script type="text/javascript" src="${basepath}js/lib/flot/jquery.flot.stack.js"></script>
<script type="text/javascript" src="${basepath}js/lib/superfish.js"></script>

<script type="text/javascript" src="${basepath}js/src/_main.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/auth.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/data.widgets.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/renderer/_main.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/renderer/charts.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/renderer/heatmap.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/renderer/list.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/renderer/table.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/query.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/signals.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/streaming.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/resources.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/ui.common.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/ui.queryresults.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/url.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/utils.js?v=${timestamp}"></script>
<script type="text/javascript" src="${basepath}js/src/validator.js?v=${timestamp}"></script>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
biomart.METHOD = '${METHOD}';
<c:if test="${METHOD=='POST'}">
biomart.POST = {
	<c:forEach var='parameter' items='${param}' varStatus="status"> 
		<c:if test="${parameter.key!='path'}">
			'${parameter.key}': '${parameter.value}'<c:if test="${!status.last}">,</c:if>
		</c:if>
	</c:forEach>
}
</c:if>
</script>
