<%-- @annotation@
		  Shows 'back' button - can pass in text and title
--%><%--
  @author Gary Brown.
  @version $Id: callerPageButton.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>

<c:if test="${empty form}">
	<c:set var="form" value="${grouperForm}"/>
</c:if>

<c:if test="${empty buttonTitle}">
	<c:set var="buttonTitle"><grouper:message key="cancel.to.caller-page-title"/></c:set>
</c:if>
<c:if test="${empty buttonText}">
	<c:set var="buttonText"><grouper:message key="cancel.to.caller-page"/></c:set>
</c:if>

<c:choose>
	<c:when test="${!empty forceCallerPageId}">
		<jsp:useBean id="linkParams" class="java.util.HashMap" scope="page"/>
		<c:set target="${linkParams}" property="forceCallerPageId" value="${form.map.origCallerPageId}"/>
		<html:link page="/gotoCallerPage" paramId="pageId" paramName="linkParams" paramProperty="forceCallerPageId" title="${buttonTitle}">
				<c:out value="${buttonText}"/>
	</html:link>
	</c:when>
	<c:when test="${!empty form.map.callerPageId}">
	<html:link page="/gotoCallerPage" paramId="pageId" paramName="form" paramProperty="callerPageId" title="${buttonTitle}">
				<c:out value="${buttonText}"/>
	</html:link>	
</c:when>
</c:choose>
</grouper:recordTile>