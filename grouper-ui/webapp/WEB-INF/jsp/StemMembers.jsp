<%-- @annotation@ Displays (filtered and paged if necessary) list of current group 
members with links to edit individual members (should we have 
bulk update capability?). Also link to find new members --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div>
<c:forEach var="stem" items="${groupPath}">
  <c:out value="${stem.displayExtension}"/>:
</c:forEach>
<c:out value="${group.displayExtension}"/>
</div>
<grouper:subtitle key="stems.heading.list-members" />

<ul>
<c:forEach var="subject" items="${groupMembers}">
<c:set target="${groupMembership}" property="subjectId" value="${subject.id}"/>
<c:set target="${groupMembership}" property="subjectType" value="${subject.subjectType}"/>
<c:set target="${groupMembership}" property="sourceId" value="${subject.sourceId}"/>
<c:set var="subject" value="${subject}" scope="request"/>
<%-- @annotation@ User clicks link to edit individual member --%>
				  
				  <li><tiles:insert definition="dynamicTileDef">
					  <tiles:put name="viewObject" beanName="subject"/>
					  <tiles:put name="view" value="memberLink"/>
				  </tiles:insert></li>


</c:forEach>
</ul>
<!--<c:if test="${!empty searchObj && !searchObj.trueSearch}">
<html:link page="/populateFindNewMembers.do" ><grouper:message key="find.return-find"/></html:link><p/>
</c:if>-->
<c:if test="${!empty searchObj && searchObj.trueSearch}">
<html:link page="/searchNewMembers.do" name="searchObj"><grouper:message key="find.return-results"/></html:link><p/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
</c:if>
<br/><html:link page="/populateFindNewMembers.do" name="groupMembership"><grouper:message key="find.add-new-members"/></html:link>
<html:link page="/populateGroupSummary.do" name="groupMembership"><grouper:message key="done"/></html:link>

