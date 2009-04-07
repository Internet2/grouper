<%-- @annotation@
		  Dynamic tile used to render a link to GroupMembers
--%><%--
  @author Gary Brown.
  @version $Id: groupLinkMembersView.jsp,v 1.3 2008-03-25 16:30:18 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkKey}">
<c:set var="linkKey">groups.action.edit-members</c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateGroupMembers.do" name="viewObject" title="${linkTitle}">
	<fmt:message bundle="${nav}" key="${linkKey}"/></html:link>
</span>
			
