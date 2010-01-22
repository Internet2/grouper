<%-- @annotation@ 
			Displays (filtered and paged if necessary) list of current group 
			members with links to edit individual members  
--%><%--
  @author Gary Brown.
  @version $Id: GroupMembers.jsp,v 1.16 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
<div class="sectionBody">
<tiles:insert definition="showStemsLocationDef"/>
</div>
</div>

<div class="changeScope section">
<grouper:subtitle key="groups.heading.list-members" />
<div class="sectionBody">

<tiles:insert definition="changeMembershipScopeDef"/>

<c:choose>
	<c:when test="${!removableMembers}">
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
			<tiles:put name="view" value="memberLinks"/>
			<tiles:put name="headerView" value="memberLinksHeader"/>
			<tiles:put name="itemView" value="membershipInfo"/>
			<tiles:put name="footerView" value="memberLinksFooter"/>
			<tiles:put name="pager" beanName="pager"/>
			<tiles:put name="noResultsMsg" value="${navMap[noResultsKey]}"/>
			<tiles:put name="listInstruction" value="list.instructions.member-links"/> 
			<tiles:put name="linkSeparator">  
				<tiles:insert definition="linkSeparatorDef" flush="false"/>
			</tiles:put>
		</tiles:insert>
	</c:when>
	<c:otherwise>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
			<tiles:put name="view" value="memberLinks"/>
			<tiles:put name="headerView" value="removableMemberLinksHeader"/>
			<tiles:put name="itemView" value="removableMembershipInfo"/>
			<tiles:put name="footerView" value="removableMemberLinksFooter"/>
			<tiles:put name="pager" beanName="pager"/>
			<tiles:put name="noResultsMsg" value="${navMap[noResultsKey]}"/>
			<tiles:put name="listInstruction" value="list.instructions.member-links"/> 
			<tiles:put name="linkSeparator">  
				<tiles:insert definition="linkSeparatorDef" flush="false"/>
			</tiles:put>
		</tiles:insert>

	</c:otherwise>
</c:choose>


<br/>
<div class="linkButton">
<c:choose>
	<c:when test="${empty GroupFormBean.map.contextSubject}">
		<c:if test="${canWriteField}">
			<c:choose>
				<c:when test="${isCompositeGroup}">
					<html:link page="/removeComposite.do" name="groupMembership" onclick="return confirm('${navMap['groups.remove.all.warn']}')">
						<grouper:message key="groups.composite.remove"/>
					</html:link>
					
				</c:when>
				<c:otherwise>
				<c:if test="${empty $param.callerPageId}">
					<html:link page="/populateFindNewMembers.do" name="groupMembership">
						<grouper:message key="find.groups.add-new-members"/>
					</html:link>
				</c:if>
				</c:otherwise>
			</c:choose>
		</c:if>
		<c:if test="${empty $param.callerPageId && (empty listField || listField=='members') && canWriteField}">
		<html:link page="/populateAddComposite.do" name="groupMembership" onclick="return confirm('${navMap['groups.remove.all.warn']}')">
			<c:choose>
				<c:when test="${isCompositeGroup}">
						<grouper:message key="groups.composite.replace"/>
				</c:when>
				<c:otherwise>
					<grouper:message key="groups.composite.members-replace"/>
				</c:otherwise>
			</c:choose>
					</html:link>
		</c:if>
		
		<c:set target="${groupMembership}" property="callerPageId"></c:set>
		<html:link page="/populateGroupSummary.do" name="groupMembership">
			<grouper:message key="find.groups.done"/>
		</html:link>
		
		<tiles:insert definition="callerPageButtonDef"/>
	</c:when>
	<c:otherwise>
		<html:link page="/populateSubjectSummary.do">
			<grouper:message key="groups.action.summary.return-to-subject-summary"/>
		</html:link>
	</c:otherwise>
</c:choose>
</div>
<br/>
</div>
</div>
