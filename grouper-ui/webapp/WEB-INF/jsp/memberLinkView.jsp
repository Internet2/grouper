<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members
--%><%--
  @author Gary Brown.
  @version $Id: memberLinkView.jsp,v 1.4 2005-12-20 11:49:28 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="memberLink">
<c:set target="${pagerParams}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${pagerParams}" property="chainGroupIds" value="${viewObject.chainGroupIds}"/>
<c:set target="${pagerParams}" property="groupId" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>

<span class="subjectSummaryLink">
   <c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.desc}"/>
		</fmt:message></c:set>
<html:link page="/populateSubjectSummary.do" name="pagerParams" title="${linkTitle}">
			<tiles:insert definition="dynamicTileDef" flush="false">
				  <tiles:put name="viewObject" beanName="viewObject"/>
				  <tiles:put name="view" value="groupMember"/>
			  </tiles:insert>	
			</html:link>
			</span>:



 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
				<fmt:param value="${viewObject.desc}"/>
				<fmt:param value="${browseParent.desc}"/>
</fmt:message></c:set>


<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
  <span class="groupMemberLink">
   <c:choose>
	<c:when test="${memberLinkMode=='privilege'}">
		<c:set var="linkText" value="groups.privilege.direct"/>
		<c:if test="${!viewObject.isDirect}"><c:set var="linkText" value="groups.privilege.indirect"/></c:if>
		<html:link page="/populateGroupMember.do" name="pagerParams" title="${linkTitle}">
 		<fmt:message bundle="${nav}" key="${linkText}"/></html:link>  
	</c:when>
	<c:when test="${viewObject.noWays gt 1}">
		<html:link page="/populateChains.do" name="pagerParams" title="${linkTitle}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.multiple">
		 	<fmt:param value="${viewObject.noWays}"/>
		 </fmt:message></html:link> 
		
	</c:when>
  	<c:when test="${!empty viewObject.via}">
		<html:link page="/populateChains.do" name="pagerParams" title="${linkTitle}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.indirect-member"/></html:link> 
		
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="pagerParams" title="${linkTitle}">
 		<fmt:message bundle="${nav}" key="groups.membership.chain.member"/></html:link> 
	</c:otherwise>
  </c:choose>	

		</span>
</div>