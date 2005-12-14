<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render memberships from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: subjectSummaryMemberLinkView.jsp,v 1.2 2005-12-14 15:15:47 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set target="${pagerParams}" property="chainGroupIds" value="${viewObject.chainGroupIds}"/>
<c:set target="${pagerParams}" property="groupId" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="asMemberOf" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>

 <c:set target="${pagerParams}" property="contextSubject" value="true"/>
<c:set target="${pagerParams}" property="contextSubjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="contextSubjectType" value="${viewObject.subjectType}"/> 

<c:set var="linkText"><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="memberOfGroup"/>
	  <tiles:put name="view" value="groupMember"/>
  </tiles:insert></c:set>
  <c:choose>

  	<c:when test="${!empty viewObject.via}">
		<html:link page="/populateChains.do" name="pagerParams" title="${navMap['groups.membership.chain.title']} ${viewObject.desc}">
		 <fmt:message bundle="${nav}" key="groups.membership.chain.indirect-member-of"/></html:link> :
		
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMember.do" name="pagerParams" title="${navMap['browse.assign']} ${viewObject.desc}">
 		<fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></html:link> :
	</c:otherwise>
  </c:choose>
 <c:set var="group" value="${viewObject.memberOfGroup}"/>
<c:set target="${group}" property="contextSubject" value="true"/>
<c:set target="${group}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${group}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
 <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="subjectSummaryGroupLink"/>
  </tiles:insert> 
