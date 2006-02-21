<%-- @annotation@ 
			Displays subject attributes
--%><%--
  @author Gary Brown.
  @version $Id: subjectInfo.jsp,v 1.3 2006-02-21 16:34:21 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="SubjectInfo">

<c:forEach var="attrName" items="${subjectAttributeNames}">

<div class="formRow">
	<div class="formLeft">
		<c:out value="${attrName}"/>
	</div>
	<div class="formRight">
		<c:out value="${subject[attrName]}"/>
	</div>
</div>
 
</c:forEach>
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="subject.summary.subject-type"/>
	</div>
	<div class="formRight">
		<c:out value="${subject.subjectType}"/>
	</div>
</div>
<div class="formRow">
	<div class="formLeft">
		<c:out value="ID"/>
	</div>
	<div class="formRight">
		<c:out value="${subject['id']}"/>
	</div>
</div>

	<c:set target="${listFieldParams}" property="groupId" value="${listFieldParams.subjectId}"/>
	<c:set target="${listFieldParams}" property="asMemberOf" value="${listFieldParams.subjectId}"/>
	<c:set target="${listFieldParams}" property="contextSubject" value="true"/>
	<c:set target="${listFieldParams}" property="contextSubjectId" value="${listFieldParams.subjectId}"/>
	<c:set target="${listFieldParams}" property="contextSubjectType" value="${listFieldParams.subjectType}"/>
<c:forEach var="groupListField" items="${listFields}">
	<c:set target="${listFieldParams}" property="listField" value="${groupListField}"/>

<div class="formRow">
	<div class="formLeft">
		<c:out value="${groupListField}"/>
	</div>
	<div class="formRight">
		<html:link page="/populateGroupMembers.do" name="listFieldParams" >
	<fmt:message bundle="${nav}" key="subject.summary.view-list-field-members"><fmt:param value="${groupListField}"/></fmt:message></html:link>
	</div>
</div>
 
</c:forEach>
<div style="clear:left;"></div>
</div><!--/SubjectInfo-->
</grouper:recordTile>
