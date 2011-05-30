<!doctype html>
<%@ page language="java"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="biomart" tagdir="/WEB-INF/tags" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="currentPage" scope="request">
  Test Page
</c:set>
<html>
<head>
  <c:import url="/conf/config.jsp" context="/"/>
  <title>${requestScope.currentPage}</title>

	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

  <c:import url="/_head.jsp?path=../" context="/"/>
</head>
<!--[if lt IE 7 ]> <body class="biomart layout1 main ie6 "> <![endif]--> 
<!--[if IE 7 ]>    <body class="biomart layout1 main ie7 "> <![endif]--> 
<!--[if IE 8 ]>    <body class="biomart layout1 main ie8 "> <![endif]--> 
<!--[if !IE]><!--> <body class="biomart layout1 main"> <!--<![endif]--> 

<div id="biomart-top-wrapper">
  <div id="biomart-header">
    <c:import url="/_header.jsp?path=../" context="/"/>
  </div>

  <c:import url="/_context.jsp?path=../" context="/"/>

  <div id="biomart-wrapper">
    <div id="biomart-content" class="clearfix">
      <h2>Test Page</h2>
      <p>It Works!</p>
    </div>

    <div id="biomart-content-footer" class="clearfix">
      <p class="version">Powered by <a href="http://www.biomart.org/" title="Visit biomart.org">BioMart</a></p>
    </div>
  </div>

  <div id="biomart-footer">
    <c:import url="/_footer.jsp?path=../" context="/"/>
  </div>
</div>

<c:import url="/_js_includes.jsp?path=../" context="/"/>

<script type="text/javascript">
  // Do not touch this
	$(document).ready(function() {
		$.publish('biomart.login');	
    $.subscribe('biomart.restart', {refresh:function(){location=location.href}}, 'refresh');
	});
</script>

</body>
</html>
