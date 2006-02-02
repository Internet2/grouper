<%-- @annotation@ 
			Displays (filtered and paged if necessary) list of current group 
			members with links to edit individual members  
--%><%--
  @author Gary Brown.
  @version $Id: GroupMembers.jsp,v 1.2 2006-02-02 16:38:08 isgwb Exp $
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
<c:if test="${groupPrivs.ADMIN || groupPrivs.UPDATE}">
<html:link page="/populateFindNewMembers.do" name="groupMembership">
	<fmt:message bundle="${nav}" key="find.groups.add-new-members"/>
</html:link>
</c:if>
<html:link page="/populateGroupSummary.do" name="groupMembership">
	<fmt:message bundle="${nav}" key="find.groups.done"/>
</html:link>
</div>
<br/>

