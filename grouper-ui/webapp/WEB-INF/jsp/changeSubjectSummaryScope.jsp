<%-- @annotation@
		Tile which displays a form which allows a user to filter groups according to whether
		the subject is a direct or effective member. Also can view groups where subject
		has selected Access privilege, and stems where subject has selected Naming privilege
--%><%--
  @author Gary Brown.
  @version $Id: changeSubjectSummaryScope.jsp,v 1.2 2006-02-21 16:18:08 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="changeScope">
<html:form method="post" action="/populateSubjectSummary">
<html:hidden property="subjectId"/>
<html:hidden property="subjectType"/>
<html:hidden property="returnTo"/>
<html:hidden property="returnToLinkKey"/>
<fieldset>
<c:if test="${!empty memberOfListFields}">
		<span class="membershipListScope">
		
			<html:select property="listField">
				<option value=""><fmt:message bundle="${nav}" key="groups.list-members.scope.ordinary-membership"/></option>
				<html:options name="memberOfListFields"/>
			</html:select> <fmt:message bundle="${nav}" key="groups.list-members.scope.select-list"/>
		</span>
	</c:if>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="imm"/> <fmt:message bundle="${nav}" key="subject.list-membership.scope.imm"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="eff"/> <fmt:message bundle="${nav}" key="subject.list-membership.scope.eff"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="all"/> <fmt:message bundle="${nav}" key="subject.list-membership.scope.all"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="access"/> <fmt:message bundle="${nav}" key="subject.list-access.scope.priv"/>
		<html:select property="accessPriv">
			<html:options name="allAccessPrivs"/>
		</html:select>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="naming"/> <fmt:message bundle="${nav}" key="subject.list-naming.scope.priv"/>
		<html:select property="namingPriv">
			<html:options name="allNamingPrivs"/>
		</html:select>
	</span>
	<span class="membershipListScope">
		<input type="submit" value="<fmt:message bundle="${nav}" key="groups.list-members.scope.submit"/>"/>
	</span>
	</fieldset>
</html:form>
</div>
</grouper:recordTile>