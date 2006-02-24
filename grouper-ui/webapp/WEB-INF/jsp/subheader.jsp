<%-- @annotation@
		 Stanard tile which displays a menu bar below the header
--%><%--
  @author Gary Brown.
  @version $Id: subheader.jsp,v 1.3 2006-02-24 13:42:51 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div id="LeftInNav" ><span/></div>
<div id="RightInNav" >
	<c:if test="${!empty AuthSubject}">
		<fmt:message bundle="${nav}" key="auth.message.login-welcome"/> <tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="AuthSubject"/>
	  			<tiles:put name="view" value="logout"/>
  			</tiles:insert> <c:if test="${mediaMap['logout.link.show']=='true'}">|
		<html:link action="/logout">
			<fmt:message bundle="${nav}" key="logout"/> 
		</html:link></c:if>
		<c:if test="${isWheelGroupMember}">
		<form method="post" style="display:inline">
		<select class="inNav" name="wheelGroupAction">
			<option value="toMortal"><fmt:message bundle="${nav}" key="wheelgroup.action.to-mortal"/></option>
			<option value="toAdmin" <c:if test="${activeWheelGroupMember}">selected="selected"</c:if>><fmt:message bundle="${nav}" key="wheelgroup.action.to-admin"/></option>
		</select><input class="inNav" type="submit" value="<fmt:message bundle="${nav}" key="wheelgroup.action.submit"/>"/>
		</form>
		</c:if>
	</c:if>
</div>
</grouper:recordTile>

