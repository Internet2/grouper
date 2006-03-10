<%-- @annotation@
		 Stanard tile which displays a menu bar below the header
--%><%--
  @author Gary Brown.
  @version $Id: subheader.jsp,v 1.2.2.1 2006-03-10 10:04:40 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div id="LeftInNav" ><span/></div>
<div id="RightInNav" >
	<c:if test="${!empty AuthSubject}">
		<c:if test="${empty AuthSubject[mediaMap['subject.default.attribute']]}">
			<c:set target="${AuthSubject}" property="${mediaMap['subject.default.attribute']}" value="${AuthSubject.id}"/>
		</c:if>
		<fmt:message bundle="${nav}" key="auth.message.login-welcome"/> <tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="AuthSubject"/>
	  			<tiles:put name="view" value="logout"/>
  			</tiles:insert><c:if test="${mediaMap['logout.link.show']=='true'}">|
		<html:link action="/logout">
			<fmt:message bundle="${nav}" key="logout"/> 
		</html:link></c:if>
	</c:if>
</div>
</grouper:recordTile>

