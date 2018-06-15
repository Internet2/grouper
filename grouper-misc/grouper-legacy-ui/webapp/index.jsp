<%@page import="edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer"%>
<%@ include file="WEB-INF/grouperUi2/assetsJsp/commonTaglib.jsp"%>
<html>
<%
GrouperRequestContainer.retrieveFromRequestOrCreate();
String location=null;
if(request.getRemoteUser()==null || "y".equals(request.getParameter("badRole"))) {
	location="populateIndex.do";
}else{
	location="home.do";
}%>

<c:if test="${grouperRequestContainer.commonRequestContainer.rootUiNewUi}">
<%
location="grouperUi";
%>
</c:if>


<head><meta http-equiv="refresh" content="0;<%=location%>"/></head>

<body onload="document.location.href='<%=location%>'">

</body>
</html>
