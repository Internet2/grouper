<%-- @annotation@ 
			Displays list of Subjects with selected privilege, with links 
			to edit privileges for individual Subjects
--%><%--
  @author Gary Brown.
  @version $Id: GroupPriviligees.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
<div class="sectionBody">
<tiles:insert definition="showStemsLocationDef"/>
</div>
</div>

<div class="section">
<grouper:subtitle key="groups.heading.list-privilegees" />
<div class="sectionBody">
<br />
<tiles:insert definition="selectGroupPrivilegeDef"/>
<br />

<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="privilegeLinks"/>
	<tiles:put name="headerView" value="privilegeLinksHeader"/>
	<tiles:put name="itemView" value="privilegeLink"/>
	<tiles:put name="footerView" value="privilegeLinksFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="noResultsMsg" value="${navMap['groups.list-privilegees.none']}"/>
	<tiles:put name="listInstruction" value="list.instructions.privilege-links"/> 
	<tiles:put name="linkSeparator">  
		<tiles:insert definition="linkSeparatorDef" flush="false"/>
	</tiles:put>
</tiles:insert>

<div class="linkButton">
<c:if test="${!empty searchObj && searchObj.trueSearch}">
	<html:link page="/searchNewMembers.do" name="searchObj">
		<grouper:message key="find.return-results"/>
	</html:link>
</c:if>

<c:if test="${groupPrivResolver.canManagePrivileges}">
<html:link page="/populateFindNewMembers.do" name="groupMembership">
	<grouper:message key="find.groups.add-new-privilegees"/>
</html:link>
</c:if>
<html:link page="/populateGroupSummary.do" name="groupMembership">
	<grouper:message key="find.groups.done"/>
</html:link>
</div>
<br/>

</div>
</div>

