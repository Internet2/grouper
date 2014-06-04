<%-- @annotation@ 
			Displays list of Subjects with selected privilege, with links 
			to edit privileges for individual Subjects
--%><%--
  @author Gary Brown.
  @version $Id: StemPriviligees.jsp,v 1.10 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
<div class="sectionBody">
<tiles:insert definition="showStemsLocationDef"/>
</div>
</div>

<div class="section">
<grouper:subtitle key="stems.heading.list-members" />
<div class="sectionBody">
<br />
<tiles:insert definition="selectStemPrivilegeDef"/>
<br />

<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="privilegeLinks"/>
	<tiles:put name="headerView" value="privilegeLinksHeader"/>
	<tiles:put name="itemView" value="privilegeLink"/>
	<tiles:put name="footerView" value="privilegeLinksFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="noResultsMsg" value="${navMap['stems.list-privilegees.none']}"/>
	<tiles:put name="listInstruction" value="list.instructions.privilege-links"/> 
	<tiles:put name="linkSeparator">  
		<tiles:insert definition="linkSeparatorDef" flush="false"/>
	</tiles:put>
</tiles:insert>
<div class="linkButton">
<%--<c:if test="${!empty searchObj && searchObj.trueSearch}">
	<c:set target="${searchObj}" property="stems" value="true"/>
	<html:link page="/searchNewMembers.do" name="searchObj">
		<grouper:message key="find.stems.membersreturn-results"/>
	</html:link>
</c:if>--%>

<html:link page="/populateFindNewMembersForStems.do" name="stemMembership">
	<grouper:message key="find.stems.add-new-privilegees"/>
</html:link>
<html:link page="/populate${linkBrowseMode}Groups.do" >
	<grouper:message key="priv.stems.list.cancel"/>
</html:link>
<c:if test="${isNewStem && !empty findForNode}">
<html:link page="/populate${browseMode}Groups.do" paramId="currentNode" paramName="findForNode">
	<grouper:message key="priv.stems.list.cancel-and-work-in-new"/>
</html:link>
</c:if>
</div>
</div>
</div>
