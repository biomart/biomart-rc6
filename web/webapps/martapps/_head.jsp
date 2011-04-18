<%@ page language="java"%>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<meta name="robots" content="noindex, nofollow">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico" />
<c:set var="basepath" value="${param.path}"/>
<%-- Timestamp for appending to resoures to force refresh from server --%>
<jsp:useBean id="dateValue" class="java.util.Date" />
<c:set scope="request" var="timestamp"><fmt:formatDate value="${dateValue}" pattern="yyyyMMddHH"/></c:set>
<%
  if (request.isSecure()) {
    request.setAttribute("siteUrl", System.getProperty("https.url"));
  } else {
    request.setAttribute("siteUrl", System.getProperty("http.url"));
  }

	String method = request.getMethod();
	request.setAttribute("METHOD", method);
 	String isDebug = System.getProperty("biomart.debug", "true");
	application.setAttribute("debug", Boolean.parseBoolean(isDebug));
%>
<link type="text/css" href="${basepath}css/reset.css" rel="stylesheet" />
<link type="text/css" href="${basepath}css/ui/aristo/jquery-ui-1.8.custom.css" rel="stylesheet" />
<c:choose>
	<c:when test="${not debug}">
<link type="text/css" href="${basepath}css/common.css" rel="stylesheet" />
	</c:when>
	<c:otherwise>
<link type="text/css" href="${basepath}css/jquery.autocomplete.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/jquery.jgrowl.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/buttons.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/tipsy.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/openid.20101209.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/superfish.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/superfish-vertical.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/base.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/layout.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/data.css?v=${timestamp}" rel="stylesheet" />
<link type="text/css" href="${basepath}css/controls.css?v=${timestamp}" rel="stylesheet" />
	</c:otherwise>
</c:choose>
<script src="${basepath}js/lib/modernizr-1.5.min.js"></script>

<!--[if IE]>
<link type="text/css" href="${basepath}css/ie.css?v=${timestamp}" rel="stylesheet" />
<![endif]-->

<c:if test="${param.includemaincss != 'false'}">
  <link type="text/css" href="css/main.css?v=${timestamp}" rel="stylesheet" />
</c:if>

<script type="text/javascript" src="${basepath}js/lib/jquery-1.4.4.min.js"></script>

<script type="text/javascript">
// Dummy console.log
if (typeof window.console == 'undefined') { window.console = { log: function() {} } }
// iPhone and iPad detection
var iOS = /iPhone|iPad/.test(navigator.platform)
if (iOS) document.documentElement.className += ' ios'
</script>


