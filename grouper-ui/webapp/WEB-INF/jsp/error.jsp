<%-- @annotation@
			Page displayed if uncaught exception thrown
--%><%--
  @author Gary Brown.
  @version $Id: error.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<%@page import="java.io.PrintWriter"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="exception">
<c:choose>
	<c:when test="${!empty exception}">
		
		<div class="exceptionBody">
		<%	Exception exception = (Exception) request.getAttribute("exception");
			exception.printStackTrace(new PrintWriter(out));
		%>	
		</div>
	</c:when>
	<c:otherwise>
	<div class="exceptionUndefined">
		<grouper:message bundle="${nav}" key="error.undefined"/>
	</div>
	</c:otherwise>
</c:choose>
</div>
</grouper:recordTile>