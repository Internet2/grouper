<%-- @annotation@
		  Dynamic tile used to render a link to GroupMembers
--%><%--
  @author Gary Brown.
  @version $Id: groupLinkMembersView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkKey}">
<c:set var="linkKey">groups.action.edit-members</c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateGroupMembers.do" name="viewObject" title="${linkTitle}">
	<grouper:message bundle="${nav}" key="${linkKey}"/></html:link>
</span>
			
