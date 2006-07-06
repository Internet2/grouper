<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members
--%><%--
  @author Gary Brown.
  @version $Id: memberLinkView.jsp,v 1.7 2006-07-06 14:53:33 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
				<fmt:param value="${viewObject.subject.desc}"/>
				<fmt:param value="${viewObject.group.desc}"/>
</fmt:message></c:set>
  <span class="memberLink">

   <c:choose>
		<c:when test="${viewObject.noWays gt 1}">
		<html:link page="/populateChains.do" name="linkParams" title="${linkTitle}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.multiple">
		 	<fmt:param value="${viewObject.noWays}"/>
		 </fmt:message></html:link> 
		
	</c:when>
  	<c:when test="${!empty viewObject.viaGroup || isCompositeGroup}">
		<html:link page="/populateChains.do" name="linkParams" title="${linkTitle}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.indirect-member"/></html:link> 
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="linkParams" title="${linkTitle}">
 		<fmt:message bundle="${nav}" key="groups.membership.chain.member"/></html:link> 
	</c:otherwise>
  </c:choose>	

</span>
