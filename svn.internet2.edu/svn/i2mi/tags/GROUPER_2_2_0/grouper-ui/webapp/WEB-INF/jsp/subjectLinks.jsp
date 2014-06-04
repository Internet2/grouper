<%-- @annotation@
		  	Tile which displays a standard set of links for a subject summary
--%><%--
  @author Gary Brown.
  @version $Id: subjectLinks.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSubjectLinks" class="noCSSOnly"><grouper:message key="page.skip.subject-links"/></a>
<div class="subjectLinks" style="clear:none;">
<div class="linkButton">
			<c:if test="${!empty subject}">
				<html:link page="/addSavedSubject.do" name="saveParams">
					<grouper:message key="saved-subjects.add.subject"/>
				</html:link>
			<c:if test="${activeWheelGroupMember || AuthSubject.id == 'GrouperSystem'}">
				<jsp:useBean id="auditParams" class="java.util.HashMap" scope="page"></jsp:useBean>
		<c:set target="${auditParams}" property="origCallerPageId" value="${thisPageId}"/>
		<c:set target="${auditParams}" property="subjectId" value="${subject.id}"/>
		<c:set target="${auditParams}" property="subjectType" value="${subject.subjectType}"/>
		<c:set target="${auditParams}" property="sourceId" value="${subject.source.id}"/>
		<c:set target="${auditParams}" property="filterType" value="actions"/>
		<html:link page="/userAudit.do"  name="auditParams">
			<grouper:message key="subject.action.audit.actions"/>
		</html:link>
		
		<c:set target="${auditParams}" property="filterType" value="memberships"/>
		<html:link page="/userAudit.do"  name="auditParams">
			<grouper:message key="subject.action.audit.memberships"/>
		</html:link>
		<c:set target="${auditParams}" property="filterType" value="privileges"/>
		<html:link page="/userAudit.do"  name="auditParams">
			<grouper:message key="subject.action.audit.privileges"/>
		</html:link>
		</c:if>
			</c:if>
		
			<html:link page="/populateSearchSubjects.do" >
				<grouper:message key="subject.action.new-search"/>
			</html:link>
			
		
			
<c:if test="${!empty pager.params.returnTo}">		
			<html:link page="${pager.params.returnTo}" >
				<grouper:message key="${pager.params.returnToLinkKey}"/>
			</html:link>
</c:if>
<c:if test="${subject.subjectType=='group'}">
	<jsp:useBean id="groupMap" class="java.util.HashMap"/>
	
	<c:set target="${groupMap}" property="changeMode" value="true"/>
	<c:set target="${groupMap}" property="groupId" value="${subject.id}"/>		
			<html:link page="/populateGroupSummary.do" name="groupMap">
				<grouper:message key="subject.summary.browse-this-group"/>
			</html:link>
</c:if>

</div>
</div>		
	
<a name="endSubjectLinks" id="endSubjectLinks"></a>
</grouper:recordTile>