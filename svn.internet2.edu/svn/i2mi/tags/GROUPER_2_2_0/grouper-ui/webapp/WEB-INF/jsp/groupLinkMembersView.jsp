<%-- @annotation@
		  Dynamic tile used to render a link to GroupMembers
--%><%--
  @author Gary Brown.
  @version $Id: groupLinkMembersView.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty linkKey}">
<c:set var="linkKey">groups.action.edit-members</c:set>
</c:if>
<span class="isMemberof">
	<html:link page="/populateGroupMembers.do" name="viewObject" title="${linkTitle}">
	<grouper:message key="${linkKey}"/></html:link>
</span>
			
