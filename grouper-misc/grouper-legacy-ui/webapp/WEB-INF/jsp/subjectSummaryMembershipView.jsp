<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render memberships from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: subjectSummaryMembershipView.jsp,v 1.2 2008-04-03 13:30:21 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="membershipLink">
<c:set target="${linkParams}" property="groupId" value="${viewObject.group.id}"/>
<c:set target="${linkParams}" property="asMemberOf" value="${viewObject.group.id}"/>
<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>
<c:set target="${linkParams}" property="sourceId" value="${viewObject.subject.sourceId}"/>

 <c:set target="${linkParams}" property="contextSubject" value="true"/>
<c:set target="${linkParams}" property="contextSubjectId" value="${viewObject.subject.id}"/>
<c:set target="${linkParams}" property="contextSubjectType" value="${viewObject.subject.subjectType}"/> 
<c:set target="${linkParams}" property="contextSourceId" value="${viewObject.subject.sourceId}"/> 

<c:set var="linkText"><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject" beanProperty="group"/>
	  <tiles:put name="view" value="groupMember"/>
  </tiles:insert></c:set>
	
	<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="viewObject"/>
		  <tiles:put name="view" value="subjectSummaryMemberLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
	</tiles:insert>
	<c:out value="${linkSeparator}" escapeXml="false"/>
 <c:set var="group" value="${viewObject.group}"/>
<c:set target="${group}" property="contextSubject" value="true"/>
<c:set target="${group}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${group}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
<c:set target="${group}" property="contextSourceId" value="${currentSubject.SourceId}"/>
 <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="subjectSummaryGroupLink"/>
  </tiles:insert> 
  
  </div>
