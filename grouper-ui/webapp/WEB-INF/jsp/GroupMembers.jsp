<%-- @annotation@ 
			Displays (filtered and paged if necessary) list of current group 
			members with links to edit individual members  
--%><%--
  @author Gary Brown.
  @version $Id: GroupMembers.jsp,v 1.7 2006-07-17 10:04:09 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>


<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.list-members"/>
</h2>
<tiles:insert definition="changeMembershipScopeDef"/>
<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="memberLinks"/>
	<tiles:put name="headerView" value="memberLinksHeader"/>
	<tiles:put name="itemView" value="membershipInfo"/>
	<tiles:put name="footerView" value="memberLinksFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="noResultsMsg" value="${navMap['groups.list-members.none']}"/>
	<tiles:put name="listInstruction" value="list.instructions.member-links"/> 
	<tiles:put name="linkSeparator">  
		<tiles:insert definition="linkSeparatorDef" flush="false"/>
	</tiles:put>
</tiles:insert>


<br/>
<div class="linkButton">
<c:choose>
	<c:when test="${empty GroupFormBean.map.contextSubject}">
		<c:if test="${groupPrivs.ADMIN || groupPrivs.UPDATE}">
			<c:choose>
				<c:when test="${isCompositeGroup}">
					<html:link page="/removeComposite.do" name="groupMembership">
						<fmt:message bundle="${nav}" key="groups.composite.remove"/>
					</html:link>
					
				</c:when>
				<c:otherwise>
				<c:if test="${empty $param.callerPageId}">
					<html:link page="/populateFindNewMembers.do" name="groupMembership">
						<fmt:message bundle="${nav}" key="find.groups.add-new-members"/>
					</html:link>
				</c:if>
				</c:otherwise>
			</c:choose>
		</c:if>
		<c:if test="${empty $param.callerPageId && (empty listField || listField=='members') && (groupPrivs.ADMIN || groupPrivs.UPDATE)}">
		<html:link page="/populateAddComposite.do" name="groupMembership">
			<c:choose>
				<c:when test="${isCompositeGroup}">
						<fmt:message bundle="${nav}" key="groups.composite.replace"/>
				</c:when>
				<c:otherwise>
					<fmt:message bundle="${nav}" key="groups.composite.members-replace"/>
				</c:otherwise>
			</c:choose>
					</html:link>
		</c:if>
		<c:if test="empty $param.callerPageId">
		<c:set target="${groupMembership}" property="callerPageId"></c:set>
		<html:link page="/populateGroupSummary.do" name="groupMembership">
			<fmt:message bundle="${nav}" key="find.groups.done"/>
		</html:link>
		</c:if>
		<tiles:insert definition="callerPageButtonDef"/>
	</c:when>
	<c:otherwise>
		<html:link page="/populateSubjectSummary.do">
			<fmt:message bundle="${nav}" key="groups.action.summary.return-to-subject-summary"/>
		</html:link>
	</c:otherwise>
</c:choose>
</div>
<br/>

