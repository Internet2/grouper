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

<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="chainPath"/>
	<tiles:put name="view" value="chain"/>
	<tiles:put name="currentSubject" beanName="subject"/>
	<tiles:put name="currentGroup" beanName="browseParent"/>
</tiles:insert>


<br/>
<div class="linkButton">
<html:link page="/populateGroupMembers.do" name="requestParams">
	<fmt:message bundle="${nav}" key="groups.membership.chain.cancel"/>
</html:link>

</div>
<br/>

