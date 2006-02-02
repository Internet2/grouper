<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render memberships from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: subjectSummaryMemberLinkView.jsp,v 1.5 2006-02-02 16:38:08 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
  <c:choose>
	<c:when test="${viewObject.noWays gt 1}">
		<html:link page="/populateChains.do" name="linkParams" title="${navMap['groups.membership.chain.title']} ${viewObject.subject.desc}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.multiple-of">
		 	<fmt:param value="${viewObject.noWays}"/>
		 </fmt:message></html:link> <c:out value="${linkSeparator}" escapeXml="false"/>
		
	</c:when>
  	<c:when test="${!empty viewObject.group.viaGroup}">
		<html:link page="/populateChains.do" name="linkParams" title="${navMap['groups.membership.chain.title']} ${viewObject.subject.desc}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.indirect-member-of"/></html:link> <c:out value="${linkSeparator}" escapeXml="false"/>
		
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="linkParams" title="${navMap['browse.assign']} ${viewObject.subject.desc}">
 		<fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></html:link> <c:out value="${linkSeparator}" escapeXml="false"/>
	</c:otherwise>
  </c:choose>