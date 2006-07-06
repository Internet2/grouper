<%-- @annotation@ Top level JSP which shows how a Subject is an effective
member of a group, possibly bt a composite. Also offers link to directly
assign privileges for the Subject --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>

<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.show-chain">
		<fmt:param value="${subject.desc}"/>
		<fmt:param value="${browseParent.desc}"/>
	</fmt:message>
</h2>
<c:choose>
	<c:when test="${!empty composite}">
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="composite"/>
			<tiles:put name="view" value="chain"/>
			<tiles:put name="linkSeparator">  
				<tiles:insert definition="linkSeparatorDef" flush="false"/>
			</tiles:put>
		</tiles:insert>
		<br/>
	</c:when>
	<c:otherwise>
		<c:forEach var="chainSubject" items="${chainPaths}">
			<c:set var="chainPath" value="${chainSubject.chainPath}"/>
			<tiles:insert definition="dynamicTileDef">
				<tiles:put name="viewObject" beanName="chainPath"/>
				<tiles:put name="view" value="chain"/>
				<tiles:put name="chainSize" value="${chainSubject.chainPathSize}"/>
				<tiles:put name="currentSubject" beanName="subject"/>
				<tiles:put name="currentGroup" beanName="browseParent"/>
				<tiles:put name="linkSeparator">  
					<tiles:insert definition="linkSeparatorDef" flush="false"/>
				</tiles:put>
			</tiles:insert>
		</c:forEach>
	</c:otherwise>
</c:choose>
<div class="currentPrivs">
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="subject"/>
	  <tiles:put name="view" value="currentPrivs"/>
</tiles:insert>
<c:choose>
	<c:when test="${privsSize>0}">
		<fmt:message bundle="${nav}" key="subject.privileges.chain"/>
		<c:forEach var="priv" items="${privs}">
			<c:out value="${priv}"/><tiles:insert definition="linkSeparatorDef" flush="false"/>
		</c:forEach>
		<c:set var="linkText"><fmt:message bundle="${nav}" key="subject.privileges.chain.change"/></c:set>
	</c:when>
	<c:otherwise><fmt:message bundle="${nav}" key="subject.privileges.chain.none"/> 
		<c:set var="linkText"><fmt:message bundle="${nav}" key="subject.privileges.chain.assign"/></c:set>
	</c:otherwise>
</c:choose>
<html:link page="/populateGroupMember.do" name="groupMemberParams"><c:out value="${linkText}"/></html:link>
</div>
<br/>
<div class="linkButton">
<c:choose>
	<c:when test="${GroupFormBean.map.contextSubject=='true'}">
		<html:link page="/populateSubjectSummary.do">
					<fmt:message bundle="${nav}" key="groups.action.summary.return-to-subject-summary"/>
		</html:link>
	</c:when>
	<c:when test="${!empty composite}">
		<tiles:insert definition="callerPageButtonDef"/>
	</c:when>
	<c:otherwise>
		<html:link page="/populateGroupMembers.do" name="requestParams">
			<fmt:message bundle="${nav}" key="groups.membership.chain.cancel"/>
		</html:link>
	</c:otherwise>
</c:choose>
</div>
<br/>

