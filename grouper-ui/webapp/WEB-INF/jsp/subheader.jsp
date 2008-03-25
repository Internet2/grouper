<%-- @annotation@
		 Stanard tile which displays a menu bar below the header
--%><%--
  @author Gary Brown.
  @version $Id: subheader.jsp,v 1.6 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div id="LeftInNav" ><span/></div>
<div id="RightInNav" >
	<c:if test="${!empty AuthSubject}">
		<grouper:message bundle="${nav}" key="auth.message.login-welcome"/> <tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="AuthSubject"/>
	  			<tiles:put name="view" value="logout"/>
  			</tiles:insert> <c:if test="${mediaMap['logout.link.show']=='true'}">|
		<html:link action="/logout">
			<grouper:message bundle="${nav}" key="logout"/> 
		</html:link></c:if>
		<c:if test="${authUser!='GrouperSystem' && isWheelGroupMember}">
		<form method="post" style="display:inline" action="<c:out value="${pageUrl}"/>">
		<select class="inNav" name="wheelGroupAction">
			<option value="toMortal"><grouper:message bundle="${nav}" key="wheelgroup.action.to-mortal"/></option>
			<option value="toAdmin" <c:if test="${activeWheelGroupMember}">selected="selected"</c:if>><grouper:message bundle="${nav}" key="wheelgroup.action.to-admin"/></option>
		</select><input class="inNav" type="submit" value="<grouper:message bundle="${nav}" key="wheelgroup.action.submit"/>"/>
		</form>
		</c:if>
	</c:if>
</div>
</grouper:recordTile>

