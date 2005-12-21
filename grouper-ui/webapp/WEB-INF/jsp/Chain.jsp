<%-- @annotation@ Displays (filtered and paged if necessary) list of current group 
members with links to edit individual members (should we have 
bulk update capability?). Also link to find new members --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>

<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.show-chain">
		<fmt:param value="${subject.desc}"/>
		<fmt:param value="${browseParent.desc}"/>
	</fmt:message>
</h2>

<c:forEach var="chainSubject" items="${chainPaths}">
	<c:set var="chainPath" value="${chainSubject.chainPath}"/>
	<tiles:insert definition="dynamicTileDef">
		<tiles:put name="viewObject" beanName="chainPath"/>
		<tiles:put name="view" value="chain"/>
		<tiles:put name="chainSize" value="${chainSubject.chainPathSize}"/>
		<tiles:put name="currentSubject" beanName="subject"/>
		<tiles:put name="currentGroup" beanName="browseParent"/>
	</tiles:insert>
</c:forEach>
<br/>
<div class="linkButton">
<c:choose>
	<c:when test="${GroupFormBean.map.contextSubject=='true'}">
		<html:link page="/populateSubjectSummary.do">
					<fmt:message bundle="${nav}" key="groups.action.summary.return-to-subject-summary"/>
		</html:link>
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMembers.do" name="requestParams">
			<fmt:message bundle="${nav}" key="groups.membership.chain.cancel"/>
		</html:link>
	</c:otherwise>
</c:choose>
</div>
<br/>

