<%-- @annotation@
		Tile which displays a form which allows a user to filter groups according to whether
		the subject is a direct or effective member. Also can view groups where subject
		has selected Access privilege, and stems where subject has selected Naming privilege
--%><%--
  @author Gary Brown.
  @version $Id: changeSubjectSummaryScope.jsp,v 1.10 2009-09-09 15:10:03 mchyzer Exp $
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
				<option value=""><grouper:message key="groups.list-members.scope.ordinary-membership"/></option>
				<html:options name="memberOfListFields"/>
			</html:select> <grouper:message key="groups.list-members.scope.select-list"/>
		</span>
	</c:if>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="imm"/> <grouper:message key="subject.list-membership.scope.imm"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="eff"/> <grouper:message key="subject.list-membership.scope.eff"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="all"/> <grouper:message key="subject.list-membership.scope.all"/>
	</span>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="access"/> <grouper:message key="subject.list-access.scope.priv"/>
		<html:select property="accessPriv">
			<html:optionsCollection name="allAccessPrivs"/>
		</html:select>
    <grouper:infodot hideShowHtmlId="accessPrivInfodot" />
	</span>
  <div <grouper:hideShowTarget hideShowHtmlId="accessPrivInfodot"  /> class="helpText" >
   <grouper:message key="access.priv.infodot" useNewTermContext="true" />
  </div>
	<span class="membershipListScope">
		<html:radio property="membershipListScope" value="naming"/> <grouper:message key="subject.list-naming.scope.priv"/>
		<html:select property="namingPriv">
			<html:optionsCollection name="allNamingPrivs"/>
		</html:select>
    <grouper:infodot hideShowHtmlId="namingPrivInfodot" />
	</span>
  <div <grouper:hideShowTarget hideShowHtmlId="namingPrivInfodot"  /> class="helpText" >
   <grouper:message key="naming.priv.infodot" useNewTermContext="true" />
  </div>
  <span class="membershipListScope">
    <html:radio property="membershipListScope" value="any-access"/> <grouper:message key="subject.list-all-access.scope.priv"/>
  </span>
  <br />
	<span class="membershipListScope">
		<input type="submit" class="blueButton" value="<grouper:message key="groups.list-members.scope.submit"/>"/>
	</span>
	</fieldset>
</html:form>
</div>
</grouper:recordTile>
