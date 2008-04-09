<%-- @annotation@ 
		  New error page 
--%><%--
  @author Gary Brown.
  @version $Id: ErrorPage.jsp,v 1.1 2008-04-09 14:59:02 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
  <grouper:subtitle key="error.heading" />
  <div class="seriousError"><c:out value="${seriousError}"/></div>
  
  <c:if test="${mediaMap['error.ticket']=='true'}">
	<div class="errorTicket">
		<grouper:message bundle="${nav}" key="error.ticket">
			<grouper:param value="${uiRequestId}"/>
		</grouper:message> 
	</div>
  </c:if>
  
  
  <div class="errorGeneral">
		<grouper:message bundle="${nav}" key="error.repeat"/> 
	</div>
