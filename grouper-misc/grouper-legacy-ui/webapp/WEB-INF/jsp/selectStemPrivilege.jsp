<%-- @annotation@
		Tile which displays a form which allows a user to select a stem privilege and
		see a list of Subjects with that privilege for the active stem
--%><%--
  @author Gary Brown.
  @version $Id: selectStemPrivilege.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<html:form action="populateStemPriviligees" method="post">
<fieldset>
	<html:hidden property="stemId"/>
	<input type="hidden" name="stems" value="true"/>
	<input type="submit" class="blueButton" value="<grouper:message key="priv.show-subjects-with"/>"/>
	<label class="noCSSOnly" for="privilege"><grouper:message key="priv.show-subjects-with"/></label> 
	<html:select property="privilege" styleId="privilege">
		<html:optionsCollection name="allStemPrivs" />
	</html:select> <grouper:message key="priv.privilege"/>
</fieldset>
</html:form>
</grouper:recordTile>