<%@page import="edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer"%>
<%@ include file="WEB-INF/grouperUi2/assetsJsp/commonTaglib.jsp"%>
<html>
<%
  GrouperRequestContainer.retrieveFromRequestOrCreate();
  String location="grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain";
%>

<c:if test="${grouperRequestContainer.commonRequestContainer.rootUiNewUi}==false">
<%
  location="grouperUi/appHtml/grouper.html?operation=Misc.index";
%>
</c:if>

<head><meta http-equiv="refresh" content="0;<%=location%>"/></head>

<body onload="document.location.href='<%=location%>'">

</body>
</html>
