<%-- @annotation@
		  Dynamic tile used by genericList mechanism to 
		  render privileges from Subject perspective
--%><%--
  @author Gary Brown.
  @version $Id: subjectSummaryPrivilegeView.jsp,v 1.1 2006-02-02 16:40:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="privilegeLink">

	<c:set target="${linkParams}" property="groupId" value="${viewObject.groupOrStem.id}"/>
	<c:set target="${linkParams}" property="asMemberOf" value="${viewObject.groupOrStem.id}"/>
	<c:set target="${linkParams}" property="subjectId" value="${viewObject.subject.id}"/>
	<c:set target="${linkParams}" property="subjectType" value="${viewObject.subject.subjectType}"/>
	<c:set target="${linkParams}" property="contextSubject" value="true"/>
	<c:set target="${linkParams}" property="contextSubjectId" value="${currentSubject.id}"/>
	<c:set target="${linkParams}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
	<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="viewObject"/>
		  <tiles:put name="view" value="privilegesLink"/>
		  <tiles:put name="params" beanName="linkParams"/>
	</tiles:insert>
	
	<c:out value="${linkSeparator}" escapeXml="false"/>
	

<c:set var="groupOrStem" value="${viewObject.groupOrStem}"/>
<c:set target="${groupOrStem}" property="contextSubject" value="true"/>
<c:set target="${groupOrStem}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${groupOrStem}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
 <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="groupOrStem"/>
	  <tiles:put name="view" value="subjectSummaryGroupLink"/>
  </tiles:insert> 
	
</div>