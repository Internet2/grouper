<%-- @annotation@
		Tile which displays a form which allows a user to select a group privilege and
		see a list of Subjects with that privilege for the active group
--%><%--
  @author Gary Brown.
  @version $Id: selectGroupPrivilege.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="selectGroupPrivilege">
<html:form action="populateGroupPriviligees" method="post">
<fieldset>
	<input type="hidden" name="groupId" value="<c:out value="${group.groupId}"/>"/>
	<input type="submit" class="blueButton" value="<grouper:message key="priv.show-subjects-with"/>"/>
	<label class="noCSSOnly" for="privilege"><grouper:message key="priv.show-subjects-with"/></label> 
	<html:select property="privilege" styleId="privilege">
		<html:optionsCollection name="allGroupPrivs" />
	</html:select> <grouper:message key="priv.privilege"/>
	</fieldset>
</html:form>
</div>
</grouper:recordTile>
