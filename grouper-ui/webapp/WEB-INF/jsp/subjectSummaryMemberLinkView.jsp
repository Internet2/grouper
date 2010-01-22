<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render memberships from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: subjectSummaryMemberLinkView.jsp,v 1.11 2009-11-07 15:50:38 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
  <c:choose>
	<c:when test="${viewObject.noWays gt 1}">
		<html:link page="/populateChains.do" name="linkParams" title="${navMap['groups.membership.chain.title']} ${viewObject.subject.desc}">
		 <grouper:message key="groups.membership.chain.multiple-of">
		 	<grouper:param value="${viewObject.noWays}"/>
		 </grouper:message></html:link> <c:out value="${linkSeparator}" escapeXml="false"/>
		
	</c:when>
  	<c:when test="${!empty viewObject.viaGroup || viewObject.group.hasComposite}">
		<html:link page="/populateChains.do" name="linkParams" title="${navMap['groups.membership.chain.title']} ${viewObject.subject.desc}">
		 <grouper:message key="groups.membership.chain.indirect-member-of"/></html:link> <c:out value="${linkSeparator}" escapeXml="false"/>
		
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="linkParams" title="${navMap['browse.assign']} ${viewObject.subject.desc}">
 		<grouper:message key="groups.membership.chain.member-of"/></html:link> <c:out value="${linkSeparator}" escapeXml="false"/>
	</c:otherwise>
  </c:choose>