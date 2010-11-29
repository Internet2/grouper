<%-- @annotation@
		  	Tile which displays a standard set of group management links based
		 	on the privileges of the current user for the current group
--%><%--
  @author Gary Brown.
  @version $Id: groupLinks.jsp,v 1.17 2009-10-20 15:05:59 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endGroupLinks" class="noCSSOnly"><grouper:message key="page.skip.group-links"/></a>
<div class="groupLinks">
<div class="linkButton">

<c:if test="${groupPrivs.ADMIN}">
		<tiles:insert definition="selectGroupPrivilegeDef"/>
	</c:if>
	   <c:if test="${groupPrivResolver.canEditGroup}">
    
      <html:link page="/deleteGroup.do" styleClass="redLink" name="group" onclick="return confirm('${navMap['groups.delete.warn']}')">
        <grouper:message key="groups.action.delete"/>
      </html:link>

  </c:if>
  
  <c:out value="${saveButton}" escapeXml="false"/>

	<c:if test="${groupPrivResolver.canEditGroup}">
			<html:link page="/populateEditGroup.do" name="group">
				<grouper:message key="groups.action.edit"/>
			</html:link>
		
	</c:if>
	
	<c:if test="${userCanEditACustomAttribute}">
		
			<html:link page="/populateEditGroupAttributes.do" name="group">
				<grouper:message key="groups.action.edit-attr"/>
			</html:link>
		
	</c:if>
	
	
	
	<c:if test="${groupPrivs.ADMIN  || groupPrivs.READ}">
		
			<html:link page="/populateGroupMembers.do"  name="group">
				<grouper:message key="groups.action.edit-members"/>
			</html:link>
		</c:if>

		<c:if test="${!isCompositeGroup && groupPrivResolver.canManageMembers}">
		
			<html:link page="/populateFindNewMembers.do"  name="group">
				<grouper:message key="find.groups.add-new-members"/>
			</html:link>
		
		</c:if>
		
		<c:if test="${!isCompositeGroup && groupPrivResolver.canManageMembers && mediaMap['ui-lite.link-from-admin-ui'] == 'true'}">
		
			<html:link page="${mediaMap['ui.lite.group-link']}${group.id}" >
				<grouper:message key="ui-lite.group-link"/>
			</html:link>
		
		</c:if>
		
    <c:if test="${!isCompositeGroup && groupPrivResolver.canInviteExternalPeople && mediaMap['inviteExternalPeople.link-from-admin-ui'] == 'true'}">
    
      <html:link page="/grouperUi/appHtml/grouper.html?operation=InviteExternalSubjects.inviteExternalSubject&groupId=${group.id}" >
        <grouper:message key="ui-lite.invite-link"/>
      </html:link>
    
    </c:if>
    
		<c:if test="${isFactor}">
			<html:link page="/populateGroupAsFactor.do"  name="factorParams">
				<grouper:message key="groups.action.as-factor"/>
			</html:link>
		</c:if>
	<c:if test="${groupPrivs.OPTIN && !groupPrivs.MEMBER}">
		
			<html:link page="/joinGroup.do"  name="group">
				<grouper:message key="groups.action.join"/>
			</html:link>
		
	</c:if>
	<c:if test="${groupPrivs.OPTOUT && groupPrivs.MEMBER}">
		
			<html:link page="/leaveGroup.do"  name="group">
				<grouper:message key="groups.action.leave"/>
			</html:link>
		
	</c:if>
	<c:if test="${groupPrivs.ADMIN}">
		<html:link page="/populateMoveGroup.do"  name="group">
			<grouper:message key="groups.action.move"/>
		</html:link>
	</c:if>
	<c:if test="${groupPrivs.ADMIN || groupPrivs.READ}">
		<html:link page="/populateCopyGroup.do"  name="group">
			<grouper:message key="groups.action.copy"/>
		</html:link>
	</c:if>
	<c:if test="${groupPrivs.ADMIN}">
		<jsp:useBean id="auditParams" class="java.util.HashMap" scope="page"></jsp:useBean>
		<c:set target="${auditParams}" property="origCallerPageId" value="${thisPageId}"/>
		<c:set target="${auditParams}" property="groupId" value="${group.id}"/>
		<html:link page="/userAudit.do"  name="auditParams">
			<grouper:message key="groups.action.audit"/>
		</html:link>
	</c:if>
	<jsp:useBean id="subjSum" class="java.util.HashMap"/>
	<c:set target="${subjSum}" property="subjectId" value="${group.id}"/>
	<c:set target="${subjSum}" property="subjectType" value="group"/>
	<c:set target="${subjSum}" property="sourceId" value="g:gsa"/>
	<c:set target="${subjSum}" property="changeMode" value="true"/>
			<html:link page="/populateSubjectSummary.do" name="subjSum">
				<grouper:message key="groups.action.summary.goto-this-subject"/>
			</html:link>
</div>
<tiles:insert definition="selectListFieldsDef"/>
</div>
<a name="endGroupLinks" id="endGroupLinks"></a>
</grouper:recordTile>
