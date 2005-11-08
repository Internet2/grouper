<%-- @annotation@
		 Standard tile used in baseDef which appears above the content space
		 and renders any Message object assigned to the request attribute key
		 'message'
--%><%--
  @author Gary Brown.
  @version $Id: message.jsp,v 1.2 2005-11-08 16:13:47 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div id="<c:out value="${message.containerId}"/>">
<!--message-->
<fmt:message bundle="${nav}" key="${message.text}">
<c:forEach var="arg" items="${message.args}">
   <fmt:param value="${arg}"/>
</c:forEach>
</fmt:message>
<!--/message-->
</div>
<p>&nbsp;</p>
</grouper:recordTile>