<%-- @annotation@
		Tile which displays a form which allows a user to filter groups according to whether
		the subject is a direct or effective member. Also can view groups where subject
		has selected Access privilege, and stems where subject has selected Naming privilege
--%><%--
  @author Gary Brown.
  @version $Id: changeSubjectSummaryScope.jsp,v 1.7 2008-04-11 14:49:36 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="changeScope">
<html:form method="post" action="/populateSubjectSummary">
<html:hidden property="subjectId"/>
<html:hidden property="subjectType"/>
<html:hidden property="sourceId"/>
<html:hidden property="returnTo"/>
<html:hidden property="returnToLinkKey"/>
<fieldset>
<c:if test="${!empty memberOfListFields}">
		<span class="membershipListScope">
		
			<html:select property="listField">
				<option value=""><grouper:message bundle="${nav}" key="groups.list-members.scope.ordinary-membership"/></option>
				<html:options name="memberOfListFields"/>
			</html:select> <grouper:message bundle="${nav}" key="groups.list-members.scope.select-list"/>
		</span>
	</c:if>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="imm"/> <grouper:message bundle="${nav}" key="subject.list-membership.scope.imm"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="eff"/> <grouper:message bundle="${nav}" key="subject.list-membership.scope.eff"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="all"/> <grouper:message bundle="${nav}" key="subject.list-membership.scope.all"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="access"/> <grouper:message bundle="${nav}" key="subject.list-access.scope.priv"/>
		<html:select property="accessPriv">
			<html:options name="allAccessPrivs"/>
		</html:select>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="any-access"/> <grouper:message bundle="${nav}" key="subject.list-all-access.scope.priv"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="naming"/> <grouper:message bundle="${nav}" key="subject.list-naming.scope.priv"/>
		<html:select property="namingPriv">
			<html:options name="allNamingPrivs"/>
		</html:select>
	</span>
  <br />
	<span class="membershipListScope">
		<input type="submit" class="blueButton" value="<grouper:message bundle="${nav}" key="groups.list-members.scope.submit"/>"/>
	</span>
	</fieldset>
</html:form>
</div>
</grouper:recordTile>