<%-- @annotation@
		Tile which displays a form which allows a user to change whether only immediate, or only effective 
		or all members of the active group should be displayed
--%><%--
  @author Gary Brown.
  @version $Id: changeMembershipScope.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="changeScope">
<html:form method="post" action="/populateGroupMembers">
<html:hidden property="groupId"/>
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
	<span class="membershipListScope">
		<input type="submit" value="<fmt:message bundle="${nav}" key="groups.list-members.scope.submit"/>"/>
	</span>
	</fieldset>
</html:form>
</div>
</grouper:recordTile>