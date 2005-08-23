<%-- @annotation@
			Page displayed if uncaught exception thrown
--%><%--
  @author Gary Brown.
  @version $Id: error.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
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
		<fmt:message bundle="${nav}" key="error.undefined"/>
	</div>
	</c:otherwise>
</c:choose>
</div>
</grouper:recordTile>