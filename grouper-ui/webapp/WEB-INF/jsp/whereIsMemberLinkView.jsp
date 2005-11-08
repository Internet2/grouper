<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group memberships from subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: whereIsMemberLinkView.jsp,v 1.1 2005-11-08 15:38:12 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="memberLink">
<c:set target="${viewObject}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${pagerParams}" property="chainGroupIds" value="${viewObject.chainGroupIds}"/>
<c:set target="${pagerParams}" property="groupId" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>

 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
				<fmt:param value="${viewObject.desc}"/>
				<fmt:param value="${viewObject.memberOfGroup.desc}"/>
</fmt:message></c:set>

<c:set var="group" value="${viewObject.memberOfGroup}"/>
<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
  <span class="groupMemberLink">
		<html:link page="/populateGroupMember.do" name="viewObject" title="${linkTitle}">
 		<fmt:message bundle="${nav}" key="groups.membership.view-privileges"/></html:link>
		</span>
	
   / <span class="subjectSummaryLink">
   <c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.memberOfGroup.displayExtension}"/>
		</fmt:message></c:set>
<html:link page="/populateSubjectSummary.do" name="group" title="${linkTitle}">
				<fmt:message bundle="${nav}" key="groups.membership.view-subject-attributes"/>
			</html:link>
			</span>
			<span class="forGroup">
  				<fmt:message bundle="${nav}" key="groups.membership.for"/>
			  <tiles:insert definition="dynamicTileDef" flush="false">
				  <tiles:put name="viewObject" beanName="group"/>
				  <tiles:put name="view" value="groupMember"/>
			  </tiles:insert>
			  </span>
 
</div>