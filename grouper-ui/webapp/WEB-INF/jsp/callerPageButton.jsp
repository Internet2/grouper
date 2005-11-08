<%-- @annotation@
		  Shows 'back' button - can pass in text and title
--%><%--
  @author Gary Brown.
  @version $Id: callerPageButton.jsp,v 1.1 2005-11-08 15:08:29 isgwb Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>

<c:if test="${empty form}">
	<c:set var="form" value="${grouperForm}"/>
</c:if>

<c:if test="${empty buttonTitle}">
	<c:set var="buttonTitle"><fmt:message bundle="${nav}" key="cancel.to.caller-page-title"/></c:set>
</c:if>
<c:if test="${empty buttonText}">
	<c:set var="buttonText"><fmt:message bundle="${nav}" key="cancel.to.caller-page"/></c:set>
</c:if>


<c:if test="${!empty form.map.callerPageId}">
	<html:link page="/gotoCallerPage" paramId="pageId" paramName="form" paramProperty="callerPageId" title="${buttonTitle}">
				<c:out value="${buttonText}"/>
	</html:link>	
</c:if>
</grouper:recordTile>