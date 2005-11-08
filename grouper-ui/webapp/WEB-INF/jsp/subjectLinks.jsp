<%-- @annotation@
		  	Tile which displays a standard set of links for a subject summary
--%><%--
  @author Gary Brown.
  @version $Id: subjectLinks.jsp,v 1.1 2005-11-08 15:31:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSubjectLinks" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.subject-links"/></a>
<div class="subjectLinks" style="clear:none;">
<div class="linkButton">		
			<html:link page="/populateSearchSubjects.do" >
				<fmt:message bundle="${nav}" key="subject.action.new-search"/>
			</html:link>
<c:if test="${!empty pager.params.returnTo}">		
			<html:link page="${pager.params.returnTo}" >
				<fmt:message bundle="${nav}" key="${pager.params.returnToLinkKey}"/>
			</html:link>
</c:if>
<c:if test="${subject.subjectType=='group'}">		
			<html:link page="/populateGroupSummary.do" paramId="groupId" paramName="subject" paramProperty="id">
				<fmt:message bundle="${nav}" key="subject.summary.browse-this-group"/>
			</html:link>
</c:if>

</div>
</div>		
	
<a name="endSubjectLinks" id="endSubjectLinks"></a>
</grouper:recordTile>