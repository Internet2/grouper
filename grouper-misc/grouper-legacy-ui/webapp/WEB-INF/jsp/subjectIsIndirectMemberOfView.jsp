<%-- @annotation@
		  Dynamic tile used to render a link to a chain
--%><%--
  @author Gary Brown.
  @version $Id: subjectIsIndirectMemberOfView.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkText}">
<c:set var="linkText"><grouper:message key="groups.membership.chain.indirect-member-of"/></c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateChains.do" name="params" title="${linkTitle}">
	<c:out value="${linkText}" escapeXml="false"/></html:link>
</span>
			
