<%-- @annotation@ 
			Displays summary of a group and provides links for 
			the maintenance of the group
--%><%--
  @author Gary Brown.
  @version $Id: GroupSummary.jsp,v 1.3 2005-11-22 10:42:54 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef" controllerUrl="/prepareBrowsePath.do"/>
<tiles:insert definition="groupInfoDef"/>
<c:if test="${(empty GroupFormBean.map.contextGroup) && (empty GroupFormBean.map.contextSubject) && (empty GroupFormBean.map.callerPageId)}">
	<tiles:insert definition="groupLinksDef"/>
</c:if>
<tiles:insert definition="groupStuffDef"/>
<jsp:useBean id="contextParams" class="java.util.HashMap"/>

<c:choose>
	<c:when test="${!empty GroupFormBean.map.contextGroup}">
		
		<c:set target="${contextParams}" property="subjectId" value="${GroupFormBean.map.contextSubjectId}"/>
		<c:set target="${contextParams}" property="subjectType" value="${GroupFormBean.map.contextSubjectType}"/>
		<c:set target="${contextParams}" property="contextSubject" value="${GroupFormBean.map.contextSubject}"/>
		<c:set target="${contextParams}" property="groupId" value="${GroupFormBean.map.contextGroup}"/>
		<div class="linkButton">
		<tiles:insert definition="callerPageButtonDef"/>
		<html:link page="/populateChains.do" name="contextParams">
					<fmt:message bundle="${nav}" key="privs.group.member.return-to-chains"/>
		</html:link>
		<html:link page="/populateGroupSummary.do" name="browseParent">
					<fmt:message bundle="${nav}" key="groups.action.summary.start-again-here"/>
		</html:link>
		</div>
	</c:when>
	<c:when test="${!empty GroupFormBean.map.contextSubject}">


		<div class="linkButton">
		<tiles:insert definition="callerPageButtonDef"/>
		<html:link page="/populateSubjectSummary.do">
					<fmt:message bundle="${nav}" key="groups.action.summary.return-to-subject-summary"/>
		</html:link>
		<html:link page="/populateGroupSummary.do" name="browseParent">
					<fmt:message bundle="${nav}" key="groups.action.summary.start-again-here"/>
		</html:link>
		</div>
	</c:when>
	<c:when test="${!empty GroupFormBean.map.callerPageId}">


		<div class="linkButton">
		<tiles:insert definition="callerPageButtonDef"/>
		<!--html:link page="/populateSubjectSummary.do">
					<fmt:message bundle="${nav}" key="groups.action.summary.return-to-subject-summary"/>
		</html:link-->
		<html:link page="/populateGroupSummary.do" name="browseParent">
					<fmt:message bundle="${nav}" key="groups.action.summary.start-again-here"/>
		</html:link>
		</div>
	</c:when>
	<c:otherwise>
		<c:if test="${isFlat}">
			<div class="linkButton">
			<html:link page="/populate${functionalArea}.do">
				<fmt:message bundle="${nav}" key="groups.summary.cancel"/>
			</html:link>
			</div>
			</c:if>
			<c:if test="${!isFlat}">
			<h2 class="actionheader">
				<fmt:message bundle="${nav}" key="groups.heading.select-other"/>
			</h2>
			<tiles:insert definition="browseStemsDef"/>
		</c:if>
	</c:otherwise>
</c:choose>


