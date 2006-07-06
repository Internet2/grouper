<%-- @annotation@
		  Dynamic tile used to render a link to a chain
--%><%--
  @author Gary Brown.
  @version $Id: subjectIsIndirectMemberOfView.jsp,v 1.1 2006-07-06 15:31:14 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkText}">
<c:set var="linkText"><fmt:message bundle="${nav}" key="groups.membership.chain.indirect-member-of"/></c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateChains.do" name="params" title="${linkTitle}">
	<c:out value="${linkText}" escapeXml="false"/></html:link>
</span>
			
