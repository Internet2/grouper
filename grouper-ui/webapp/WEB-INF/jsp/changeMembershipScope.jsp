<%-- @annotation@
		Tile which displays a form which allows a user to change whether only immediate, or only effective 
		or all members of the active group should be displayed
--%><%--
  @author Gary Brown.
  @version $Id: changeMembershipScope.jsp,v 1.2 2006-02-21 16:17:56 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="changeScope">
<html:form method="get" action="/populateGroupMembers">
<html:hidden property="groupId"/>
<html:hidden property="contextSubject"/>
<html:hidden property="contextSubjectId"/>
<html:hidden property="contextSubjectType"/>
<fieldset>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="imm"/> <fmt:message bundle="${nav}" key="groups.list-members.scope.imm"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="eff"/> <fmt:message bundle="${nav}" key="groups.list-members.scope.eff"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="all"/> <fmt:message bundle="${nav}" key="groups.list-members.scope.all"/>
	</span>
	<c:if test="${listFieldsSize gt 0}">
		<span class="membershipListScope">
		
			<html:select property="listField">
				<option value=""><fmt:message bundle="${nav}" key="groups.list-members.scope.ordinary-membership"/></option>
				<html:options name="listFields"/>
			</html:select> <fmt:message bundle="${nav}" key="groups.list-members.scope.select-list"/>
		</span>
	</c:if>
	<span class="membershipListScope">
		<input type="submit" value="<fmt:message bundle="${nav}" key="groups.list-members.scope.submit"/>"/>
	</span>
	</fieldset>
</html:form>
</div>
</grouper:recordTile>