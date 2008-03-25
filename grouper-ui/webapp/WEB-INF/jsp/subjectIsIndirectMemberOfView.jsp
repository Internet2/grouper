<%-- @annotation@
		  Dynamic tile used to render a link to a chain
--%><%--
  @author Gary Brown.
  @version $Id: subjectIsIndirectMemberOfView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkText}">
<c:set var="linkText"><grouper:message bundle="${nav}" key="groups.membership.chain.indirect-member-of"/></c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateChains.do" name="params" title="${linkTitle}">
	<c:out value="${linkText}" escapeXml="false"/></html:link>
</span>
			
