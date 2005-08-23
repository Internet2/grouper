<%-- @annotation@
		Tile which displays a form which allows a user to select a stem privilege and
		see a list of Subjects with that privilege for the active stem
--%><%--
  @author Gary Brown.
  @version $Id: selectStemPrivilege.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<html:form action="populateStemPriviligees">
<fieldset>
	<html:hidden property="stemId"/>
	<input type="hidden" name="stems" value="true"/>
	<input type="submit" value="<fmt:message bundle="${nav}" key="priv.show-subjects-with"/>"/>
	<label class="noCSSOnly" for="privilege"><fmt:message bundle="${nav}" key="priv.show-subjects-with"/></label> 
	<html:select property="privilege" styleId="privilege">
		<html:options name="allStemPrivs" />
	</html:select> <fmt:message bundle="${nav}" key="priv.privilege"/>
</fieldset>
</html:form>
</grouper:recordTile>