<%-- @annotation@
		  Dynamic tile used to render a group on the SavedGroups page
--%><%--
  @author Gary Brown.
  @version $Id: savedGroupView.jsp,v 1.4 2009-03-15 08:14:12 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.assign.title" tooltipDisable="true">
				<grouper:param value="${viewObject.subject.desc}"/>
				<grouper:param value="${viewObject.group.desc}"/>
</grouper:message></c:set>
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
