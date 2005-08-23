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

<h2 class="actionheader">
<fmt:message bundle="${nav}" key="stems.heading.list-members"/>
</h2>

<ul>
<c:forEach var="subject" items="${groupMembers}">
<c:set target="${groupMembership}" property="subjectId" value="${subject.id}"/>
<c:set target="${groupMembership}" property="subjectType" value="${subject.subjectType}"/>
<c:set var="subject" value="${subject}" scope="request"/>
<%-- @annotation@ User clicks link to edit individual member --%>
				  
				  <li><tiles:insert definition="dynamicTileDef">
					  <tiles:put name="viewObject" beanName="subject"/>
					  <tiles:put name="view" value="memberLink"/>
				  </tiles:insert></li>


</c:forEach>
</ul>
<!--<c:if test="${!empty searchObj && !searchObj.trueSearch}">
<html:link page="/populateFindNewMembers.do" ><fmt:message bundle="${nav}" key="find.return-find"/></html:link><p/>
</c:if>-->
<c:if test="${!empty searchObj && searchObj.trueSearch}">
<html:link page="/searchNewMembers.do" name="searchObj"><fmt:message bundle="${nav}" key="find.return-results"/></html:link><p/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
</c:if>
<br/><html:link page="/populateFindNewMembers.do" name="groupMembership"><fmt:message bundle="${nav}" key="find.add-new-members"/></html:link>
<html:link page="/populateGroupSummary.do" name="groupMembership"><fmt:message bundle="${nav}" key="done"/></html:link>

