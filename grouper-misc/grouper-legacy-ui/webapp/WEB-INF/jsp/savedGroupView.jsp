<%-- @annotation@
		  Dynamic tile used to render a group on the SavedGroups page
--%><%--
  @author Gary Brown.
  @version $Id: savedGroupView.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

<c:set var="linkTitle"><grouper:message key="browse.assign.title" tooltipDisable="true">
				<grouper:param value="${viewObject.subject.desc}"/>
				<grouper:param value="${viewObject.group.desc}"/>
</grouper:message></c:set>
  <span class="memberLink">
   <c:choose>
		<c:when test="${viewObject.noWays gt 1}">
		<html:link page="/populateChains.do" name="linkParams" title="${linkTitle}">
		 <grouper:message key="groups.membership.chain.multiple">
		 	<grouper:param value="${viewObject.noWays}"/>
		 </grouper:message></html:link> 
		
	</c:when>
  	<c:when test="${!empty viewObject.viaGroup || isCompositeGroup}">
		<html:link page="/populateChains.do" name="linkParams" title="${linkTitle}">
		 <grouper:message key="groups.membership.chain.indirect-member"/></html:link> 
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="linkParams" title="${linkTitle}">
 		<grouper:message key="groups.membership.chain.member"/></html:link> 
	</c:otherwise>
  </c:choose>	

</span>
