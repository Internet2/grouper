<%-- @annotation@
		  Dynamic tile used to render is member of links
--%><%--
  @author Gary Brown.
  @version $Id: subjectIsMemberOfView.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkText}">
<c:set var="linkText"><grouper:message key="groups.membership.chain.member-of"/></c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateGroupMember.do" name="params" title="${linkTitle}">
	<c:out value="${linkText}" escapeXml="false"/></html:link>
</span>
			
