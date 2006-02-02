<%-- @annotation@
		  Dynamic tile used to render is member of links
--%><%--
  @author Gary Brown.
  @version $Id: subjectIsMemberOfView.jsp,v 1.1 2006-02-02 16:40:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<span class="isMemberof">
	<html:link page="/populateGroupMember.do" name="params" title="${linkTitle}">
	<fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></html:link>
</span>
			
