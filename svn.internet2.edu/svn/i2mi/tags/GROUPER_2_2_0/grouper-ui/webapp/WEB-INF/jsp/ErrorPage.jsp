<%-- @annotation@ 
		  New error page 
--%><%--
  @author Gary Brown.
  @version $Id: ErrorPage.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

  <div class="grouperMessage ErrorMessage"  >

<%-- print out prefix --%>
<grouper:message key="message.ErrorMessage" />
<ul>
<li><c:out value="${seriousError}"/></li>
  
  <c:if test="${mediaMap['error.ticket']=='true'}">

		<li><grouper:message key="error.ticket">
			<grouper:param value="${uiRequestId}"/>
		</grouper:message> </li>

  </c:if>
  
  

	<li>	<grouper:message key="error.repeat"/></li> 
</ul>
</div>
  
