<%-- @annotation@
		  	Tile which displays a standard set of links for a subject summary
--%><%--
  @author Gary Brown.
  @version $Id: subjectLinks.jsp,v 1.2 2005-12-08 15:33:28 isgwb Exp $
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
	<jsp:useBean id="groupMap" class="java.util.HashMap"/>
	
	<c:set target="${groupMap}" property="changeMode" value="true"/>
	<c:set target="${groupMap}" property="groupId" value="${subject.id}"/>		
			<html:link page="/populateGroupSummary.do" name="groupMap">
				<fmt:message bundle="${nav}" key="subject.summary.browse-this-group"/>
			</html:link>
</c:if>

</div>
</div>		
	
<a name="endSubjectLinks" id="endSubjectLinks"></a>
</grouper:recordTile>