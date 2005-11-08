<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render individual group members
--%><%--
  @author Gary Brown.
  @version $Id: memberLinkView.jsp,v 1.2 2005-11-08 16:12:57 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="memberLink">
<c:set target="${pagerParams}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${pagerParams}" property="chainGroupIds" value="${viewObject.chainGroupIds}"/>
<c:set target="${pagerParams}" property="groupId" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>

 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
				<fmt:param value="${viewObject.desc}"/>
				<fmt:param value="${viewObject.memberOfGroup.desc}"/>
</fmt:message></c:set>


<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
  <span class="groupMemberLink">
		<html:link page="/populateGroupMember.do" name="pagerParams" title="${linkTitle}">
 		<fmt:message bundle="${nav}" key="groups.membership.view-privileges"/></html:link>
		</span>
	
   / <span class="subjectSummaryLink">
   <c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.desc}"/>
		</fmt:message></c:set>
<html:link page="/populateSubjectSummary.do" name="pagerParams" title="${linkTitle}">
				<fmt:message bundle="${nav}" key="groups.membership.view-subject-attributes"/>
			</html:link>
			</span>
			<span class="forGroup">
  				<fmt:message bundle="${nav}" key="groups.membership.for"/>
			  <tiles:insert definition="dynamicTileDef" flush="false">
				  <tiles:put name="viewObject" beanName="viewObject"/>
				  <tiles:put name="view" value="groupMember"/>
			  </tiles:insert>
			  </span>
 
</div>