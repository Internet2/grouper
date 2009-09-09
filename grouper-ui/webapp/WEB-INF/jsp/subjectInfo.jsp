<%-- @annotation@ 
			Displays subject attributes
--%><%--
  @author Gary Brown.
  @version $Id: subjectInfo.jsp,v 1.11 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="subject" value="${viewObject}"/>
<table class="formTable formTableSpaced SubjectInfo">

<%-- get subject type safe --%>
<c:set var="subjectType" value="unknown"  />
<c:forEach var="attrName" items="${subjectAttributeNames}">
  <c:if test="${attrName == 'subjectType'}">
    <c:set var="subjectType" value="${subject[attrName]}"  />
  </c:if>
</c:forEach>

<c:forEach var="attrName" items="${subjectAttributeNames}">
<c:set target="${subject}" property="useMulti" value="${navMap['subject.attribute.multi.separator']}"/>
<tr class="formTableRow">
	<td class="formTableLeft">
    <%-- this could be misleading, but put in some common labels from nav.properties and tooltips
    
subject.summary.displayName=Path
subject.summary.extension=ID
subject.summary.createTime=Created
subject.summary.createSubjectId=Creator ID (entity ID)
subject.summary.createSubjectType=Creator entity type
subject.summary.modifyTime=Last edited
subject.summary.modifySubjectId=Last editor ID (entity ID)
subject.summary.modifySubjectType=Last editor entity type
subject.summary.subjectType=Entity type
    
    --%>
    <c:set var="subjectSummaryNavKey" value="subject.summary.${attrName}"  />
    <%-- change some common ones so they dont overlap --%>
    <c:if test="${subjectType == 'group'}">
      <c:set var="subjectSummaryNavKey" value="subject.summary.group.${attrName}"  />
    </c:if>
    <c:choose>
      <c:when test="${! empty navNullMap[subjectSummaryNavKey]}">
        <grouper:message key="${subjectSummaryNavKey}" />
      </c:when>
      <c:otherwise>
        <c:out value="${attrName}"/>
      </c:otherwise>    
    </c:choose>
	</td>
	<td class="formTableRight">
		<c:out value="${subject[attrName]}"/>
	</td>
</tr>
 
</c:forEach>


	<c:set target="${listFieldParams}" property="groupId" value="${listFieldParams.subjectId}"/>
	<c:set target="${listFieldParams}" property="asMemberOf" value="${listFieldParams.subjectId}"/>
	<c:set target="${listFieldParams}" property="contextSubject" value="true"/>
	<c:set target="${listFieldParams}" property="contextSubjectId" value="${listFieldParams.subjectId}"/>
	<c:set target="${listFieldParams}" property="contextSubjectType" value="${listFieldParams.subjectType}"/>
		<c:set target="${listFieldParams}" property="contextSourceId" value="${listFieldParams.sourceId}"/>
<c:set target="${listFieldParams}" property="callerPageId" value="${thisPageId}"/>
<c:set target="${listFieldParams}" property="contextSubject" value=""/>

<c:forEach var="groupListField" items="${listFields}">
	<c:set target="${listFieldParams}" property="listField" value="${groupListField}"/>

<tr class="formTableRow">
	<td class="formTableLeft">
		<c:out value="${groupListField}"/>
	</td>
	<td class="formTableRight">
		<html:link page="/populateGroupMembers.do" name="listFieldParams" >
	<grouper:message key="subject.summary.view-list-field-members"><grouper:param value="${groupListField}"/></grouper:message></html:link>
	</td>
</tr>
 
</c:forEach>
</table><!--/SubjectInfo-->

