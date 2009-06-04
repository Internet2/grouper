<%-- @annotation@ 
		  New error page 
--%><%--
  @author Gary Brown.
  @version $Id: ErrorPage.jsp,v 1.2 2008-04-15 07:41:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

  <div class="grouperMessage ErrorMessage"  >

<%-- print out prefix --%>
<grouper:message key="message.ErrorMessage" />
<ul>
<li><c:out value="${seriousError}"/></li>
  
  <c:if test="${mediaMap['error.ticket']=='true'}">

		<li><grouper:message bundle="${nav}" key="error.ticket">
			<grouper:param value="${uiRequestId}"/>
		</grouper:message> </li>

  </c:if>
  
  

	<li>	<grouper:message bundle="${nav}" key="error.repeat"/></li> 
</ul>
</div>
  
