<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render naming privileges from subject perspecive
--%><%--
  @author Gary Brown.
  @version $Id: subjectNamingPrivView.jsp,v 1.2 2005-12-22 13:54:43 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="subjectNamingPriv">
<%-- Set up parameters for link to maintain context --%>
<c:set target="${pagerParams}" property="groupId" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="contextGroup" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="asMemberOf" value="${viewObject.asMemberOf}"/>
<c:set target="${pagerParams}" property="subjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="subjectType" value="${viewObject.subjectType}"/>
<c:set target="${pagerParams}" property="contextSubject" value="true"/>
<c:set target="${pagerParams}" property="contextSubjectId" value="${viewObject.id}"/>
<c:set target="${pagerParams}" property="contextSubjectType" value="${viewObject.subjectType}"/> 

 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="stems.access.chain.title">
		 		<fmt:param value="${SubjectFormBean.map.namingPriv}"/>
				<fmt:param value="${viewObject.desc}"/>
				<fmt:param value="${viewObject.memberOfGroup.desc}"/>
</fmt:message></c:set>
  <span class="stemMemberLink">
		<html:link page="/populateStemMember.do" name="pagerParams" title="${linkTitle} 
			${viewObject.desc}">
		 <fmt:message bundle="${nav}" key="groups.privilege.has-for">
		 	<fmt:param value="${SubjectFormBean.map.namingPriv}"/>
			</fmt:message></html:link></span> <c:out value="${linkSeparator}" escapeXml="false"/>
		
	
 <c:set var="group" value="${viewObject.memberOfGroup}"/>
<c:set target="${group}" property="contextSubject" value="true"/>
<c:set target="${group}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${group}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
<span class="stemSummaryLink">
 <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="subjectSummaryGroupLink"/>
  </tiles:insert>
  </span>
</div>