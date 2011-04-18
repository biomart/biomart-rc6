<!doctype html>
<%@ page language="java" %>
<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib prefix="biomart" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="/WEB-INF/bmtaglib.tld" prefix="bm" %>
<html lang="<bm:locale/>">
<head>
	<jsp:include page="/conf/config.jsp"/>
  <title><bm:message code="document_title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<jsp:include page="/_head.jsp">
		<jsp:param name="path" value="../"/>
	</jsp:include>
</head>
<!--[if lt IE 7 ]> <body id="martview" class="biomart layout1 ie6 "> <![endif]--> 
<!--[if IE 7 ]>    <body id="martview" class="biomart layout1 ie7 "> <![endif]--> 
<!--[if IE 8 ]>    <body id="martview" class="biomart layout1 "> <![endif]--> 
<!--[if !IE]><!--> <body id="martview" class="biomart layout1 "> <!--<![endif]--> 
<div id="biomart-top-wrapper" class="ui-corner-all clearfix">
  <div id="biomart-header">
    <div class="content">
      <jsp:include page="/_header.jsp"/>
    </div>
  </div>
  <jsp:include page="/_context.jsp">
    <jsp:param name="path" value="../"/>
  </jsp:include>

  <div id="biomart-wrapper">
    <h2 class="ui-widget-header ui-state-default ui-corner-top">
      Mart View
    </h2>

    <div id="biomart-content" class="ui-widget-content clearfix">
      <div id="biomart-results">
        <span class="loading"></span>
      </div>
    </div>

    <div id="biomart-content-footer" class="ui-widget-content ui-corner-bottom gradient-grey clearfix">
      <jsp:include page="/_content_footer.jsp"/>
    </div>
     
    <jsp:include page="/conf/error.jsp"/>
  </div>
  <div id="biomart-footer">
    <jsp:include page="/_footer.jsp"/>
  </div>
</div>

<jsp:include page="/_js_includes.jsp">
	<jsp:param name="path" value="../"/>
</jsp:include>

<%-- MartForm specific JS --%>
<script type="text/javascript" src="js/main.js?v=${timestamp}"></script>

<script type="text/javascript">
	$(document).ready(function() {
		$.publish('biomart.login');	
	});
</script>

</body>
</html>
